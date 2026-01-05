package com.example.playlistmaker.search.data

interface NetworkClient {
    suspend fun doRequset(dto: Any): Response
}