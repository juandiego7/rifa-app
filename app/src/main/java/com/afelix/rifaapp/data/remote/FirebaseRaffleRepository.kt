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

    suspend fun fetchAllRaffles(): List<Pair<Raffle, List<Ticket>>> {
        val collection = getRafflesCollection() ?: return emptyList()
        val snapshot = collection.get().await()
        
        val result = mutableListOf<Pair<Raffle, List<Ticket>>>()
        
        for (doc in snapshot.documents) {
            val raffle = Raffle(
                id = doc.id.toLong(),
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                digits = doc.getLong("digits")?.toInt() ?: 2,
                maxNumber = doc.getLong("maxNumber")?.toInt() ?: 100,
                ticketValue = doc.getDouble("ticketValue") ?: 0.0,
                prizeValue = doc.getDouble("prizeValue") ?: 0.0,
                drawDate = doc.getLong("drawDate") ?: 0L,
                status = com.afelix.rifaapp.domain.model.RaffleStatus.valueOf(doc.getString("status") ?: "ACTIVE"),
                winningNumber = doc.getLong("winningNumber")?.toInt(),
                userId = auth.currentUser?.uid
            )
            
            val ticketsSnapshot = doc.reference.collection("tickets").get().await()
            val tickets = ticketsSnapshot.documents.map { tDoc ->
                Ticket(
                    raffleId = raffle.id,
                    number = tDoc.getLong("number")?.toInt() ?: 0,
                    status = com.afelix.rifaapp.domain.model.TicketStatus.valueOf(tDoc.getString("status") ?: "AVAILABLE"),
                    customerName = tDoc.getString("customerName"),
                    customerPhone = tDoc.getString("customerPhone")
                )
            }
            result.add(raffle to tickets)
        }
        return result
    }
}
