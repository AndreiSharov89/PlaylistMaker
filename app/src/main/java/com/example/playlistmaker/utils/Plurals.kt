package com.example.playlistmaker.utils

fun formatTrackCount(count: Int): String {
    val lastDigit = count % 10
    val lastTwoDigits = count % 100

    return when {
        lastDigit == 1 && lastTwoDigits != 11 -> "$count трек"
        lastDigit in 2..4 && lastTwoDigits !in 12..14 -> "$count трека"
        else -> "$count треков"
    }
}
