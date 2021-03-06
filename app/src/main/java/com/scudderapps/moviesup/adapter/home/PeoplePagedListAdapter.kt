package com.scudderapps.moviesup.adapter.home


import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scudderapps.moviesup.CastAndCrewDetailActivity
import com.scudderapps.moviesup.R
import com.scudderapps.moviesup.api.IMAGE_BASE_URL
import com.scudderapps.moviesup.models.main.People
import com.scudderapps.moviesup.repository.NetworkState
import kotlinx.android.synthetic.main.network_state_item.view.*
import kotlinx.android.synthetic.main.people_list_item.view.*
import android.util.Pair as UtilPair

class PeoplePagedListAdapter
    (private val context: Context) :
    PagedListAdapter<People, RecyclerView.ViewHolder>(MovieDiffCallback()) {

    val POPULAR_PEOPLE_VIEW_TYPE = 1
    private val NETWORK_VIEW_TYPE = 2
    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        lateinit var view: View
        return if (viewType == POPULAR_PEOPLE_VIEW_TYPE) {
            view = layoutInflater.inflate(R.layout.people_list_item, parent, false)
            MovieItemVieHolder(
                view
            )
        } else {
            view = layoutInflater.inflate(R.layout.network_state_item, parent, false)
            NetworkStateItemViewHolder(
                view
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == POPULAR_PEOPLE_VIEW_TYPE) {
            (holder as MovieItemVieHolder).bindPeopleData(getItem(position), context)
        } else if (getItemViewType(position) == NETWORK_VIEW_TYPE) {
            (holder as NetworkStateItemViewHolder).bind(networkState)
        }
    }

    private fun hasAnyExtraRow(): Boolean {
        return networkState != null && networkState != NetworkState.LOADED
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasAnyExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasAnyExtraRow() && position == itemCount - 1) {
            NETWORK_VIEW_TYPE
        } else {
            POPULAR_PEOPLE_VIEW_TYPE
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<People>() {
        override fun areItemsTheSame(oldItem: People, newItem: People): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: People, newItem: People): Boolean {
            return oldItem == newItem
        }
    }

    class MovieItemVieHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindPeopleData(people: People?, context: Context) {
            itemView.people_name.text = people?.name

            if (!people?.profilePath.isNullOrEmpty()) {
                val posterUrl = IMAGE_BASE_URL + people?.profilePath
                Glide.with(itemView.context)
                    .load(posterUrl)
                    .into(itemView.people_image)
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.default_avatar)
                    .centerInside()
                    .into(itemView.people_image)
            }

            itemView.setOnClickListener {

                val intent = Intent(context, CastAndCrewDetailActivity::class.java)
                intent.putExtra("id", people?.id)
//                val options = ActivityOptions.makeSceneTransitionAnimation(
//                    context,
//                    UtilPair<View, String>(itemView.people_image, "imageTransition")
//                )
//                context.startActivity(intent, options.toBundle())
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

            }
        }
    }

    class NetworkStateItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(networkState: NetworkState?) {
            if (networkState != null && networkState == NetworkState.LOADING) {
                itemView.networkStateBar.visibility = View.VISIBLE
            } else {
                itemView.networkStateBar.visibility = View.GONE
            }
            if (networkState != null && networkState == NetworkState.ERROR) {
                itemView.networkTextView.visibility = View.VISIBLE
                itemView.networkTextView.text = networkState.msg
            } else if (networkState != null && networkState == NetworkState.ENDOFLIST) {
                itemView.networkTextView.visibility = View.VISIBLE
                itemView.networkTextView.text = "No more items"
            } else {
                itemView.networkTextView.visibility = View.GONE
            }
        }
    }

    fun setNetworkState(newNetworkState: NetworkState) {
        val previousState: NetworkState? = this.networkState
        val hadExtraRow: Boolean = hasAnyExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow: Boolean = hasAnyExtraRow()

        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(super.getItemCount() - 1)
        }
    }
}