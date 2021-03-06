package com.scudderapps.moviesup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.scudderapps.moviesup.models.common.CastResponse
import com.scudderapps.moviesup.models.common.MediaResponse
import com.scudderapps.moviesup.models.common.VideoResponse
import com.scudderapps.moviesup.models.movie.*
import com.scudderapps.moviesup.repository.NetworkState
import com.scudderapps.moviesup.repository.movie.moviedetails.MovieDetailRepository
import io.reactivex.disposables.CompositeDisposable

class MovieDetailViewModel(private val movieDetailRepository: MovieDetailRepository, movieId: Int) :
    ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val movieDetails: LiveData<MovieDetail> by lazy {
        movieDetailRepository.fetchingMoviesDetails(compositeDisposable, movieId)
    }

    val videoDetails: LiveData<VideoResponse> by lazy {
        movieDetailRepository.fetchingMoviesVideos(compositeDisposable, movieId)
    }

    val castDetails: LiveData<CastResponse> by lazy {
        movieDetailRepository.fetchingCastDetails(compositeDisposable, movieId)
    }

    val movieMedia: LiveData<MediaResponse> by lazy {
        movieDetailRepository.fetchingMovieMedia(compositeDisposable, movieId)
    }

    val collection: LiveData<CollectionResponse> by lazy {
        movieDetailRepository.fetchingCollection(compositeDisposable, movieId)
    }

    val networkState: LiveData<NetworkState> by lazy {
        movieDetailRepository.getMovieDetailNetworkState()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}