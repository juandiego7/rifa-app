package com.afelix.rifaapp.domain.usecase

import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.RaffleDashboardStats
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus
import com.afelix.rifaapp.domain.repository.RaffleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRafflesUseCase(private val repository: RaffleRepository) {
    operator fun invoke(): Flow<List<Raffle>> = repository.getRaffles()
}

class CreateRaffleUseCase(private val repository: RaffleRepository) {
    suspend operator fun invoke(raffle: Raffle) {
        val raffleId = repository.insertRaffle(raffle)
        val tickets = (0 until raffle.maxNumber).map {
            Ticket(raffleId = raffleId, number = it)
        }
        repository.insertTickets(tickets)
    }
}

class GetRaffleDashboardStatsUseCase {
    operator fun invoke(tickets: List<Ticket>, ticketValue: Double): RaffleDashboardStats {
        val sold = tickets.count { it.status == TicketStatus.PAID }
        val reserved = tickets.count { it.status == TicketStatus.RESERVED }
        val available = tickets.count { it.status == TicketStatus.AVAILABLE }
        
        return RaffleDashboardStats(
            totalTickets = tickets.size,
            soldTickets = sold,
            reservedTickets = reserved,
            availableTickets = available,
            moneyCollected = sold * ticketValue,
            moneyReserved = reserved * ticketValue
        )
    }
}
