package com.example.playlistmaker.sharing.data

import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.sharing.domain.EmailData
import com.example.playlistmaker.sharing.domain.SharingRepository

class SharingRepositoryImpl(private val context: Context) : SharingRepository {

    override fun getShareAppLink(): String {
        return context.getString(R.string.share_url)
    }

    override fun getTermsLink(): String {
        return context.getString(R.string.eula_url)
    }

    override fun getSupportEmailData(): EmailData {
        return EmailData(
            email = context.getString(R.string.support_email),
            subject = context.getString(R.string.support_subj),
            text = context.getString(R.string.support_text)
        )
    }
}