package com.example.playlistmaker.search.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class RetrofitNetworkClient(private val retrofit: Retrofit) : NetworkClient {

    private val itunesService = retrofit.create(ItunesApi::class.java)

    override suspend fun doRequset(dto: Any): Response {
        if (dto !is TrackSearchRequest) {
            return Response().apply { resultCode = 400 }
        }

        return withContext(Dispatchers.IO) {
            try {
                val resp = itunesService.search(dto.expression)
                val body = resp.body() ?: Response()
                body.apply { resultCode = resp.code() }
            } catch (e: Exception) {
                Response().apply { resultCode = 500 }
            }
        }
    }
}