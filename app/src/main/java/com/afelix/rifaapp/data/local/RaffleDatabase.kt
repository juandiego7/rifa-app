package com.afelix.rifaapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RaffleEntity::class, TicketEntity::class], version = 2)
abstract class RaffleDatabase : RoomDatabase() {
    abstract val raffleDao: RaffleDao
}
