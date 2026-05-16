package com.afelix.rifaapp.data.remote

import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRaffleRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getRafflesCollection() = auth.currentUser?.let { 
        firestore.collection("users").document(it.uid).collection("raffles")
    }

    suspend fun syncRaffle(raffle: Raffle, tickets: List<Ticket>) {
        val collection = getRafflesCollection() ?: return
        
        // We use the local ID as the document ID for simplicity in this V1
        // but it's better to use a dedicated field or map them.
        val raffleData = mapOf(
            "title" to raffle.title,
            "description" to raffle.description,
            "digits" to raffle.digits,
            "maxNumber" to raffle.maxNumber,
            "ticketValue" to raffle.ticketValue,
            "prizeValue" to raffle.prizeValue,
            "drawDate" to raffle.drawDate,
            "status" to raffle.status.name,
            "winningNumber" to raffle.winningNumber,
            "lastUpdated" to System.currentTimeMillis()
        )

        collection.document(raffle.id.toString()).set(raffleData).await()
        
        // Sync tickets in a subcollection
        val ticketsCollection = collection.document(raffle.id.toString()).collection("tickets")
        
        // Only sync assigned tickets to save bandwidth/costs
        val assignedTickets = tickets.filter { it.status != com.afelix.rifaapp.domain.model.TicketStatus.AVAILABLE }
        
        for (ticket in assignedTickets) {
            val ticketData = mapOf(
                "number" to ticket.number,
                "status" to ticket.status.name,
                "customerName" to ticket.customerName,
                "customerPhone" to ticket.customerPhone
            )
            ticketsCollection.document(ticket.number.toString()).set(ticketData).await()
        }
    }

    suspend fun deleteRaffle(raffleId: Long) {
        getRafflesCollection()?.document(raffleId.toString())?.delete()?.await()
    }
}
