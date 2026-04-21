package com.afelix.rifaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.RaffleDashboardStats
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus
import com.afelix.rifaapp.domain.repository.RaffleRepository
import com.afelix.rifaapp.domain.usecase.CreateRaffleUseCase
import com.afelix.rifaapp.domain.usecase.GetRaffleDashboardStatsUseCase
import com.afelix.rifaapp.domain.usecase.GetRafflesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RifaViewModel(private val repository: RaffleRepository) : ViewModel() {

    private val getRafflesUseCase = GetRafflesUseCase(repository)
    private val createRaffleUseCase = CreateRaffleUseCase(repository)
    private val getStatsUseCase = GetRaffleDashboardStatsUseCase()

    val raffles: StateFlow<List<Raffle>> = repository.getRaffles().flatMapLatest { raffleList ->
        if (raffleList.isEmpty()) return@flatMapLatest flowOf(emptyList<Raffle>())
        
        val flows = raffleList.map { raffle ->
            repository.getTicketsByRaffleId(raffle.id).map { tickets ->
                raffle.copy(stats = getStatsUseCase(tickets, raffle.ticketValue))
            }
        }
        combine(flows) { it.toList() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRaffle = MutableStateFlow<Raffle?>(null)
    val selectedRaffle = _selectedRaffle.asStateFlow()

    val tickets: StateFlow<List<Ticket>> = _selectedRaffle
        .filterNotNull()
        .flatMapLatest { raffle ->
            repository.getTicketsByRaffleId(raffle.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats = combine(tickets, _selectedRaffle) { tickets, raffle ->
        if (raffle != null) {
            getStatsUseCase(tickets, raffle.ticketValue)
        } else {
            RaffleDashboardStats(0, 0, 0, 0, 0.0, 0.0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RaffleDashboardStats(0, 0, 0, 0, 0.0, 0.0))

    fun selectRaffle(raffle: Raffle) {
        _selectedRaffle.value = raffle
    }

    fun createRaffle(raffle: Raffle) {
        viewModelScope.launch {
            createRaffleUseCase(raffle)
        }
    }

    fun updateTicket(ticket: Ticket) {
        viewModelScope.launch {
            repository.updateTicket(ticket)
        }
    }

    fun updateTickets(tickets: List<Ticket>) {
        viewModelScope.launch {
            repository.updateTickets(tickets)
        }
    }

    fun deleteRaffle(raffle: Raffle) {
        viewModelScope.launch {
            repository.deleteRaffle(raffle)
        }
    }
}
