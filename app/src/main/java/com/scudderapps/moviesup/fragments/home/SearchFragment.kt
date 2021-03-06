package com.scudderapps.moviesup.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import com.scudderapps.moviesup.R
import com.scudderapps.moviesup.adapter.home.SearchListAdapter
import com.scudderapps.moviesup.api.TheTMDBClient
import com.scudderapps.moviesup.api.TmdbApiInterface
import com.scudderapps.moviesup.models.common.MovieResponse
import com.scudderapps.moviesup.models.main.Movie
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.SingleSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    @BindView(R.id.search_recycler_view)
    lateinit var searchView: RecyclerView

    @BindView(R.id.search_edit_box)
    lateinit var searchEditTextView: EditText

    lateinit var rootView: View

    private lateinit var searchAdapter: SearchListAdapter
    private val disposable = CompositeDisposable()
    private val publishSubject = PublishSubject.create<String>()
    var searchMovieList = ArrayList<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchAdapter =
            SearchListAdapter(
                searchMovieList,
                context!!
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.search_fragment, container, false)
        ButterKnife.bind(this, rootView)
        val linearLayoutManager = GridLayoutManager(activity, 3)
        linearLayoutManager.reverseLayout = false
        searchView.layoutManager = linearLayoutManager
        searchView.setHasFixedSize(true)
        searchView.adapter = searchAdapter
        settingUpSearchData()
        return rootView
    }

    private fun settingUpSearchData() {
        val apiService: TmdbApiInterface = TheTMDBClient.getClient()

        val observer: DisposableObserver<MovieResponse> = getSearchObserver()

        disposable.add(
            publishSubject.debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMapSingle(object : Single<MovieResponse>(),
                    Function<String, SingleSource<MovieResponse>> {
                    @Throws(Exception::class)
                    override fun apply(s: String): Single<MovieResponse> {
                        return apiService.getSearchResults(s)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                    }

                    override fun subscribeActual(observer: SingleObserver<in MovieResponse>) {
                        TODO("Not yet implemented")
                    }
                })
                .subscribeWith(observer)
        )

        disposable.add(observer)

        if (searchEditTextView.text.isNullOrEmpty()) {
            searchMovieList.clear()
            searchAdapter.notifyDataSetChanged()
            RxTextView.textChangeEvents(searchEditTextView)
                .skipInitialValue()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchContactsTextWatcher())?.let {
                    disposable.add(
                        it
                    )
                }
        } else {
            publishSubject.onNext("")
        }
    }

    private fun getSearchObserver(): DisposableObserver<MovieResponse> {
        return object : DisposableObserver<MovieResponse>() {
            override fun onNext(movies: MovieResponse) {
                searchMovieList.clear()
                searchMovieList.addAll(movies.movieList)
                searchAdapter.notifyDataSetChanged()
            }

            override fun onError(e: Throwable) {
                Log.e("FragmentActivity", "onError: " + e.message)
            }

            override fun onComplete() {
                publishSubject.onNext("")
            }
        }
    }

    private fun searchContactsTextWatcher(): DisposableObserver<TextViewTextChangeEvent?>? {
        return object : DisposableObserver<TextViewTextChangeEvent?>() {
            override fun onNext(textViewTextChangeEvent: TextViewTextChangeEvent) {
                Log.d("SearchActivity", "Search query: " + textViewTextChangeEvent.text())
                if (textViewTextChangeEvent.text().isNullOrBlank() || textViewTextChangeEvent.text()
                        .isNullOrEmpty()
                ) {
                    searchMovieList.clear()
                    searchAdapter.notifyDataSetChanged()
                } else {
                    publishSubject.onNext(textViewTextChangeEvent.text().toString())
                }
            }

            override fun onError(e: Throwable) {
                Log.e("SearchActivity", "onError: " + e.message)
            }

            override fun onComplete() {}
        }
    }

    override fun onPause() {
        disposable.clear()
        searchMovieList.clear()
        searchEditTextView.setText("")
        super.onPause()
    }
}