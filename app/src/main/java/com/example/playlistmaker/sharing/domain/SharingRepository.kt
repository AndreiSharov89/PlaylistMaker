package com.example.playlistmaker.sharing.domain

interface SharingRepository {
    fun getShareAppLink(): String
    fun getSupportEmailData(): EmailData
    fun getTermsLink(): String
}