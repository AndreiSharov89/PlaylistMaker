package com.example.playlistmaker.search.domain

import java.util.concurrent.Executors

class TrackSearchInteractorImpl(private val repository: TrackSearchRepository) :
    TrackSearchInteractor {
    private val executor = Executors.newCachedThreadPool()

    override fun searchTrack(
        expression: String,
        consumer: TrackSearchInteractor.Consumer<List<Track>>
    ) {
        executor.execute {
            try {
                val response = repository.searchTrack(sanitizeText(expression))
                when (response) {
                    is TrackSearchInteractor.Resource.Success -> {
                        consumer.consume(TrackSearchInteractor.Consumer.ConsumerData.Data(response.data))
                    }

                    is TrackSearchInteractor.Resource.Error -> {
                        consumer.consume(TrackSearchInteractor.Consumer.ConsumerData.Error(response.message))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                consumer.consume(
                    TrackSearchInteractor.Consumer.ConsumerData.Error(-1)
                )
            }
        }
    }

    private fun sanitizeText(text: String): String {
        return text.replace(Regex("[^\\p{L}\\p{N}.&\\-'/ ]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}