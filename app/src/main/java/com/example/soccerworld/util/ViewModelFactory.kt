package com.example.soccerworld.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.ui.fixture.FixtureViewModel
import com.example.soccerworld.ui.fixture.detail.h2h.H2HViewModel
import com.example.soccerworld.ui.fixture.detail.statistic.StatisticViewModel
import com.example.soccerworld.ui.home.leaguetable.LeagueTableViewModel
import com.example.soccerworld.ui.home.topscorer.TopScorerViewModel
import com.example.soccerworld.ui.team.TeamViewModel
import com.example.soccerworld.ui.team.team_detail.player.PlayerViewModel
import com.example.soccerworld.ui.team.team_detail.transfer.TransferViewModel


class ViewModelFactory(private val repository: FootballRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 1. Nếu hệ thống đòi LeagueTableViewModel, nhét repository vào và trả về
        if (modelClass.isAssignableFrom(LeagueTableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeagueTableViewModel(repository) as T
        }

        // 2. Nếu hệ thống đòi TopScorerViewModel, nhét repository vào và trả về
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamViewModel(repository) as T
        }

        if (modelClass.isAssignableFrom(TransferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransferViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TopScorerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopScorerViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(FixtureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FixtureViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(StatisticViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(H2HViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return H2HViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}