package com.example.playlistmaker.search.data

data class TrackSearchResponse(val searchType: String,
                               val expression: String,
                               val results: List<TrackDto>) : Response()