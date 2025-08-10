package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.NetworkClient
import com.example.playlistmaker.data.dto.Response
import com.example.playlistmaker.data.dto.TrackSearchRequest
import retrofit2.Retrofit

class RetrofitNetworkClient(private val retrofit: Retrofit) : NetworkClient {

    private val itunesService = retrofit.create(ItunesApi::class.java)

    override fun doRequset(dto: Any): Response {
        if (dto is TrackSearchRequest) {
            val resp = itunesService.search(dto.expression).execute()
            val body = resp.body() ?: Response()
            return body.apply { resultCode = resp.code() }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }
}