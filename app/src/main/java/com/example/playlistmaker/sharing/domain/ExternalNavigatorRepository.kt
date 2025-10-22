package com.example.playlistmaker.sharing.domain

interface ExternalNavigatorRepository {
    fun shareLink(text: String)
    fun openLink(link: String)
    fun openEmail(email: EmailData)
}