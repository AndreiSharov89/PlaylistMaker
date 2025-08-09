package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.Track

interface TrackSearchInteractor {
    fun searchTrack(expression: String, consumer: Consumer<List<Track>>)
    interface Consumer<T>{
        fun consume(data: ConsumerData<T>)
        sealed interface ConsumerData<T>{
            data class Data<T>(val value: T) : ConsumerData<T>
            data class Error<T>(val message: Int) : ConsumerData<T>
        }
    }
    sealed interface Resource<T> {
        data class Success<T>(val data: T): Resource<T>
        data class Error<T>(val message: Int): Resource<T>
    }
}