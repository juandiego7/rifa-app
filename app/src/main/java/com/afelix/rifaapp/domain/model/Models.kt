package com.afelix.rifaapp.domain.model

data class Raffle(
    val id: Long = 0,
    val title: String,
    val description: String,
    val digits: Int,
    val maxNumber: Int,
    val ticketValue: Double,
    val prizeValue: Double,
    val drawDate: Long,
    val status: RaffleStatus = RaffleStatus.ACTIVE,
    val winningNumber: Int? = null,
    val stats: RaffleDashboardStats? = null,
    val userId: String? = null, // Owner ID (local backup ref)
    val cloudId: String? = null, // Unique Firestore ID for sharing
    val ownerEmail: String? = null, // For display purposes
    val createdAt: Long = System.currentTimeMillis() // Unique creation timestamp
)

enum class RaffleStatus {
    ACTIVE, FINISHED
}

data class Ticket(
    val id: Long = 0,
    val raffleId: Long,
    val number: Int,
    val status: TicketStatus = TicketStatus.AVAILABLE,
    val customerName: String? = null,
    val customerPhone: String? = null
)

enum class TicketStatus {
    AVAILABLE, RESERVED, PAID
}

data class RaffleDashboardStats(
    val totalTickets: Int,
    val soldTickets: Int,
    val reservedTickets: Int,
    val availableTickets: Int,
    val moneyCollected: Double,
    val moneyReserved: Double
)
