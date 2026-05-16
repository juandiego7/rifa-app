package com.afelix.rifaapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RaffleDao {
    @Query("SELECT * FROM raffles WHERE userId = :userId")
    fun getRafflesByUser(userId: String): Flow<List<RaffleEntity>>

    @Query("SELECT * FROM raffles WHERE userId IS NULL")
    fun getGuestRaffles(): Flow<List<RaffleEntity>>

    @Query("SELECT * FROM raffles")
    fun getAllRaffles(): Flow<List<RaffleEntity>>

    @Query("SELECT * FROM raffles WHERE id = :id")
    fun getRaffleByIdFlow(id: Long): Flow<RaffleEntity?>

    @Query("SELECT * FROM raffles WHERE id = :id")
    suspend fun getRaffleById(id: Long): RaffleEntity?

    @Query("SELECT * FROM raffles WHERE cloudId = :cloudId")
    suspend fun getRaffleByCloudId(cloudId: String): RaffleEntity?

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

    @Query("DELETE FROM tickets WHERE raffleId = :raffleId")
    suspend fun deleteTicketsByRaffleId(raffleId: Long)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Update
    suspend fun updateTickets(tickets: List<TicketEntity>)
}
