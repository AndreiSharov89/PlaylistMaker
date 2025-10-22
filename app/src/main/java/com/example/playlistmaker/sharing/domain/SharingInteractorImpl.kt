package com.example.playlistmaker.sharing.domain

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigatorRepository,
    private val sharingRepository: SharingRepository
) : SharingInteractor {
    override fun shareApp() {
        externalNavigator.shareLink(sharingRepository.getShareAppLink())
    }

    override fun openTerms() {
        externalNavigator.openLink(sharingRepository.getTermsLink())
    }

    override fun openSupport() {
        externalNavigator.openEmail(sharingRepository.getSupportEmailData())
    }
}
