package com.scudderapps.moviesup.repository.peoplelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.scudderapps.moviesup.api.POST_PER_PAGE
import com.scudderapps.moviesup.api.TheTMDBApiInterface
import com.scudderapps.moviesup.models.People
import com.scudderapps.moviesup.repository.NetworkState
import io.reactivex.disposables.CompositeDisposable

class PeoplePagedListRepository(private val apiService: TheTMDBApiInterface) {

    lateinit var peoplePageList: LiveData<PagedList<People>>
    lateinit var peopleDataSourceFactory: PeopleDataSourceFactory

    fun fetchingPeopleList(
        compositeDisposable: CompositeDisposable
    ): LiveData<PagedList<People>> {

        peopleDataSourceFactory = PeopleDataSourceFactory(apiService, compositeDisposable)
        val config = PagedList.Config
            .Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        peoplePageList = LivePagedListBuilder(peopleDataSourceFactory, config).build()
        return peoplePageList
    }

    fun getNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<PeopleDataSource, NetworkState>(
            peopleDataSourceFactory.peopleLiveDataSource, PeopleDataSource::networkState
        )
    }
}