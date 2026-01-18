package com.example.playlistmaker.di

import com.example.playlistmaker.createplaylist.ui.CreatePlaylistViewModel
import com.example.playlistmaker.library.ui.FavoritesViewModel
import com.example.playlistmaker.library.ui.PlaylistsViewModel
import com.example.playlistmaker.player.ui.PlayerViewModel
import com.example.playlistmaker.playlist.ui.PlaylistViewModel
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.ui.SearchViewModel
import com.example.playlistmaker.settings.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SettingsViewModel(get(), get())
    }

    viewModel { (track: Track) ->
        PlayerViewModel(
            track = track,
            get(),
            get(),
            get()
        )
    }

    viewModel {
        SearchViewModel(get(), get())
    }
    viewModel {
        FavoritesViewModel(get())
    }
    viewModel {
        CreatePlaylistViewModel(get())
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel { (playlistId: Long) ->
        PlaylistViewModel(
            playlistId = playlistId,
            createPlaylistInteractor = get()
        )
    }

}
