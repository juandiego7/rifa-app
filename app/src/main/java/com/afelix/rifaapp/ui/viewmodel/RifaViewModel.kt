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
import com.afelix.rifaapp.data.remote.FirebaseRaffleRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RifaViewModel(private val repository: RaffleRepository) : ViewModel() {

    private val firebaseRepository = FirebaseRaffleRepository()
    private val auth = FirebaseAuth.getInstance()

    private val getRafflesUseCase = GetRafflesUseCase(repository)
    private val createRaffleUseCase = CreateRaffleUseCase(repository)
    private val getStatsUseCase = GetRaffleDashboardStatsUseCase()

    private val _userFilter = MutableStateFlow<String?>(auth.currentUser?.uid)

    init {
        // Listen to Auth changes to update filter automatically
        auth.addAuthStateListener { firebaseAuth ->
            _userFilter.value = firebaseAuth.currentUser?.uid
            if (firebaseAuth.currentUser != null) {
                syncAllToCloud()
            }
        }
    }

    val raffles: StateFlow<List<Raffle>> = _userFilter.flatMapLatest { userId ->
        getRafflesUseCase(userId).flatMapLatest { raffleList ->
            if (raffleList.isEmpty()) return@flatMapLatest flowOf(emptyList<Raffle>())
            
            val flows = raffleList.map { raffle ->
                repository.getTicketsByRaffleId(raffle.id).map { tickets ->
                    raffle.copy(stats = getStatsUseCase(tickets, raffle.ticketValue))
                }
            }
            combine(flows) { it.toList() }
        }
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
            val raffleWithUser = raffle.copy(userId = auth.currentUser?.uid)
            val id = createRaffleUseCase(raffleWithUser)
            syncToCloud(id)
        }
    }

    fun updateTicket(ticket: Ticket) {
        viewModelScope.launch {
            repository.updateTicket(ticket)
            syncToCloud(ticket.raffleId)
        }
    }

    fun updateTickets(tickets: List<Ticket>) {
        viewModelScope.launch {
            repository.updateTickets(tickets)
            if (tickets.isNotEmpty()) {
                syncToCloud(tickets.first().raffleId)
            }
        }
    }

    fun deleteRaffle(raffle: Raffle) {
        viewModelScope.launch {
            repository.deleteRaffle(raffle)
            if (auth.currentUser != null && raffle.cloudId != null) {
                firebaseRepository.deleteRaffle(raffle.cloudId)
            }
        }
    }

    fun setWinningNumber(raffle: Raffle, number: Int) {
        viewModelScope.launch {
            val updatedRaffle = raffle.copy(
                winningNumber = number,
                status = com.afelix.rifaapp.domain.model.RaffleStatus.FINISHED
            )
            repository.updateRaffle(updatedRaffle)
            _selectedRaffle.value = updatedRaffle
            syncToCloud(raffle.id)
        }
    }

    fun syncAllToCloud() {
        if (auth.currentUser == null) return
        _userFilter.value = auth.currentUser?.uid
        viewModelScope.launch {
            try {
                // Push local data
                repository.getAllRaffles().first().forEach { raffle ->
                    val tickets = repository.getTicketsByRaffleId(raffle.id).first()
                    val cloudId = firebaseRepository.syncRaffle(raffle, tickets)
                    if (raffle.cloudId == null) {
                        repository.updateRaffle(raffle.copy(cloudId = cloudId))
                    }
                }
                
                // Pull cloud data
                val cloudRaffles = firebaseRepository.fetchAllUserRaffles()
                cloudRaffles.forEach { (cloudRaffle, cloudTickets) ->
                    // 1. DEDUPLICATION: Try to match existing local raffle
                    // Match by cloudId OR by Title + Date (for newly synced raffles)
                    val allLocal = repository.getAllRaffles().first()
                    val existingLocal = allLocal.find { 
                        it.cloudId == cloudRaffle.cloudId || 
                        (it.title == cloudRaffle.title && it.drawDate == cloudRaffle.drawDate)
                    }
                    
                    val raffleToInsert = if (existingLocal != null) {
                        cloudRaffle.copy(id = existingLocal.id)
                    } else {
                        cloudRaffle.copy(id = 0) // New local entry
                    }
                    
                    val localId = repository.insertRaffle(raffleToInsert)
                    
                    // Reconstruct full ticket list
                    val fullTickets = (0 until raffleToInsert.maxNumber).map { number ->
                        cloudTickets.find { it.number == number } ?: Ticket(raffleId = localId, number = number)
                    }
                    repository.insertTickets(fullTickets)
                }
            } catch (e: Exception) {
                // Si falla la nube por permisos, al menos no se cierra la app
                e.printStackTrace()
            }
        }
    }

    fun joinRaffle(cloudId: String) {
        if (auth.currentUser == null) return
        viewModelScope.launch {
            val result = firebaseRepository.joinRaffle(cloudId)
            if (result != null) {
                val (raffle, cloudTickets) = result
                val localId = repository.insertRaffle(raffle)
                val fullTickets = (0 until raffle.maxNumber).map { number ->
                    cloudTickets.find { it.number == number } ?: Ticket(raffleId = localId, number = number)
                }
                repository.insertTickets(fullTickets)
                syncAllToCloud()
            }
        }
    }

    fun onUserLogin() {
        _userFilter.value = auth.currentUser?.uid
        syncAllToCloud()
    }

    fun onUserLogout() {
        _userFilter.value = null
    }

    private fun syncToCloud(raffleId: Long) {
        if (auth.currentUser == null) return
        
        viewModelScope.launch {
            val raffle = repository.getRaffleById(raffleId)
            val tickets = repository.getTicketsByRaffleId(raffleId).first()
            if (raffle != null) {
                val cloudId = firebaseRepository.syncRaffle(raffle, tickets)
                if (raffle.cloudId == null) {
                    repository.updateRaffle(raffle.copy(cloudId = cloudId))
                }
            }
        }
    }
}
