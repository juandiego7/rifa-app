package com.afelix.rifaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.afelix.rifaapp.domain.repository.RaffleRepository

class RifaViewModelFactory(private val repository: RaffleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RifaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RifaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
