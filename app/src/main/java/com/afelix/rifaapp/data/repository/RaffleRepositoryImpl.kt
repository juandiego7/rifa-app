package com.afelix.rifaapp.data.repository

import com.afelix.rifaapp.data.local.RaffleDao
import com.afelix.rifaapp.data.local.toDomain
import com.afelix.rifaapp.data.local.toEntity
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.repository.RaffleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RaffleRepositoryImpl(private val dao: RaffleDao) : RaffleRepository {
    override fun getRaffles(userId: String): Flow<List<Raffle>> =
        dao.getRafflesByUser(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getGuestRaffles(): Flow<List<Raffle>> =
        dao.getGuestRaffles().map { entities -> entities.map { it.toDomain() } }

    override fun getAllRaffles(): Flow<List<Raffle>> =
        dao.getAllRaffles().map { entities -> entities.map { it.toDomain() } }

    override fun getRaffleByIdFlow(id: Long): Flow<Raffle?> =
        dao.getRaffleByIdFlow(id).map { it?.toDomain() }

    override suspend fun getRaffleById(id: Long): Raffle? =
        dao.getRaffleById(id)?.toDomain()

    override suspend fun getRaffleByCloudId(cloudId: String): Raffle? =
        dao.getRaffleByCloudId(cloudId)?.toDomain()

    override suspend fun insertRaffle(raffle: Raffle): Long =
        dao.insertRaffle(raffle.toEntity())

    override suspend fun updateRaffle(raffle: Raffle) =
        dao.updateRaffle(raffle.toEntity())

    override suspend fun deleteRaffle(raffle: Raffle) =
        dao.deleteRaffle(raffle.toEntity())

    override fun getTicketsByRaffleId(raffleId: Long): Flow<List<Ticket>> =
        dao.getTicketsByRaffleId(raffleId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun updateTicket(ticket: Ticket) =
        dao.updateTicket(ticket.toEntity())

    override suspend fun updateTickets(tickets: List<Ticket>) =
        dao.updateTickets(tickets.map { it.toEntity() })

    override suspend fun insertTickets(tickets: List<Ticket>) =
        dao.insertTickets(tickets.map { it.toEntity() })

    override suspend fun deleteTicketsByRaffleId(raffleId: Long) =
        dao.deleteTicketsByRaffleId(raffleId)
}
