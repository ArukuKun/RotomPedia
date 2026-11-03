package com.duoc.rotompedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TeamBuilderViewModelFactory(
    private val teams: com.duoc.rotompedia.data.repository.TeamRepository,
    private val sessions: com.duoc.rotompedia.data.repository.SessionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamBuilderViewModel::class.java)) {
            return TeamBuilderViewModel(teams, sessions) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
