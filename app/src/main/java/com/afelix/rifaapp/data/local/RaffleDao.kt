package com.afelix.rifaapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RaffleDao {
    @Query("SELECT * FROM raffles")
    fun getRaffles(): Flow<List<RaffleEntity>>

    @Query("SELECT * FROM raffles WHERE id = :id")
    suspend fun getRaffleById(id: Long): RaffleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaffle(raffle: RaffleEntity): Long

    @Update
    suspend fun updateRaffle(raffle: RaffleEntity)

    @Delete
    suspend fun deleteRaffle(raffle: RaffleEntity)

    @Query("SELECT * FROM tickets WHERE raffleId = :raffleId ORDER BY number ASC")
    fun getTicketsByRaffleId(raffleId: Long): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)
}
