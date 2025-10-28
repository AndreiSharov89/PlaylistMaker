package com.example.playlistmaker.search.data

interface NetworkClient {
    fun doRequset(dto: Any): Response
}