package com.afelix.rifaapp.domain.repository

import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface RaffleRepository {
    fun getRaffles(): Flow<List<Raffle>>
    suspend fun getRaffleById(id: Long): Raffle?
    suspend fun insertRaffle(raffle: Raffle): Long
    suspend fun updateRaffle(raffle: Raffle)
    suspend fun deleteRaffle(raffle: Raffle)
    
    fun getTicketsByRaffleId(raffleId: Long): Flow<List<Ticket>>
    suspend fun updateTicket(ticket: Ticket)
    suspend fun updateTickets(tickets: List<Ticket>)
    suspend fun insertTickets(tickets: List<Ticket>)
}
