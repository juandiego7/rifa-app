package com.afelix.rifaapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.RaffleStatus
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@Entity(tableName = "raffles")
data class RaffleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val digits: Int,
    val maxNumber: Int,
    val ticketValue: Double,
    val prizeValue: Double,
    val drawDate: Long,
    val status: String
)

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = RaffleEntity::class,
            parentColumns = ["id"],
            childColumns = ["raffleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raffleId: Long,
    val number: Int,
    val status: String,
    val customerName: String?,
    val customerPhone: String?
)

fun RaffleEntity.toDomain() = Raffle(
    id = id,
    title = title,
    description = description,
    digits = digits,
    maxNumber = maxNumber,
    ticketValue = ticketValue,
    prizeValue = prizeValue,
    drawDate = drawDate,
    status = RaffleStatus.valueOf(status)
)

fun Raffle.toEntity() = RaffleEntity(
    id = id,
    title = title,
    description = description,
    digits = digits,
    maxNumber = maxNumber,
    ticketValue = ticketValue,
    prizeValue = prizeValue,
    drawDate = drawDate,
    status = status.name
)

fun TicketEntity.toDomain() = Ticket(
    id = id,
    raffleId = raffleId,
    number = number,
    status = TicketStatus.valueOf(status),
    customerName = customerName,
    customerPhone = customerPhone
)

fun Ticket.toEntity() = TicketEntity(
    id = id,
    raffleId = raffleId,
    number = number,
    status = status.name,
    customerName = customerName,
    customerPhone = customerPhone
)
