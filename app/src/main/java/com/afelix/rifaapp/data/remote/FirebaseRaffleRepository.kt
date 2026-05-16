package com.afelix.rifaapp.data.remote

import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRaffleRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun syncRaffle(raffle: Raffle, tickets: List<Ticket>): String {
        val user = auth.currentUser ?: return ""
        
        // If raffle already has a cloudId, use it. Otherwise, create a new doc.
        val docRef = if (!raffle.cloudId.isNullOrBlank()) {
            firestore.collection("raffles").document(raffle.cloudId)
        } else {
            firestore.collection("raffles").document()
        }

        val raffleData = mutableMapOf(
            "title" to raffle.title,
            "description" to raffle.description,
            "digits" to raffle.digits,
            "maxNumber" to raffle.maxNumber,
            "ticketValue" to raffle.ticketValue,
            "prizeValue" to raffle.prizeValue,
            "drawDate" to raffle.drawDate,
            "status" to raffle.status.name,
            "winningNumber" to raffle.winningNumber,
            "ownerId" to (raffle.userId ?: user.uid),
            "ownerEmail" to (raffle.ownerEmail ?: user.email),
            "lastUpdated" to FieldValue.serverTimestamp()
        )
        
        // Only set the collaborators list on creation or if needed
        if (raffle.cloudId.isNullOrBlank()) {
            raffleData["collaborators"] = listOf<String>()
        }

        docRef.set(raffleData).await()
        
        // Sync assigned tickets in a subcollection
        val ticketsCollection = docRef.collection("tickets")
        val assignedTickets = tickets.filter { it.status != com.afelix.rifaapp.domain.model.TicketStatus.AVAILABLE }
        
        for (ticket in assignedTickets) {
            val ticketData = mapOf(
                "number" to ticket.number,
                "status" to ticket.status.name,
                "customerName" to ticket.customerName,
                "customerPhone" to ticket.customerPhone,
                "sellerId" to user.uid // Track who sold the ticket
            )
            ticketsCollection.document(ticket.number.toString()).set(ticketData).await()
        }
        
        return docRef.id
    }

    suspend fun joinRaffle(cloudId: String): Pair<Raffle, List<Ticket>>? {
        val user = auth.currentUser ?: return null
        val docRef = firestore.collection("raffles").document(cloudId)
        val snapshot = docRef.get().await()
        
        if (!snapshot.exists()) return null
        
        // Add current user to collaborators list
        docRef.update("collaborators", FieldValue.arrayUnion(user.uid)).await()
        
        // Fetch the data
        val raffle = Raffle(
            title = snapshot.getString("title") ?: "",
            description = snapshot.getString("description") ?: "",
            digits = snapshot.getLong("digits")?.toInt() ?: 2,
            maxNumber = snapshot.getLong("maxNumber")?.toInt() ?: 100,
            ticketValue = snapshot.getDouble("ticketValue") ?: 0.0,
            prizeValue = snapshot.getDouble("prizeValue") ?: 0.0,
            drawDate = snapshot.getLong("drawDate") ?: 0L,
            status = com.afelix.rifaapp.domain.model.RaffleStatus.valueOf(snapshot.getString("status") ?: "ACTIVE"),
            winningNumber = snapshot.getLong("winningNumber")?.toInt(),
            userId = snapshot.getString("ownerId"),
            cloudId = cloudId,
            ownerEmail = snapshot.getString("ownerEmail")
        )
        
        val ticketsSnapshot = docRef.collection("tickets").get().await()
        val tickets = ticketsSnapshot.documents.map { tDoc ->
            Ticket(
                raffleId = 0, // Will be set by local repo
                number = tDoc.getLong("number")?.toInt() ?: 0,
                status = com.afelix.rifaapp.domain.model.TicketStatus.valueOf(tDoc.getString("status") ?: "AVAILABLE"),
                customerName = tDoc.getString("customerName"),
                customerPhone = tDoc.getString("customerPhone")
            )
        }
        
        return raffle to tickets
    }

    suspend fun fetchAllUserRaffles(): List<Pair<Raffle, List<Ticket>>> {
        val user = auth.currentUser ?: return emptyList()
        
        // 1. Fetch from NEW collaborative collection
        val ownedQuery = firestore.collection("raffles").whereEqualTo("ownerId", user.uid).get()
        val collabQuery = firestore.collection("raffles").whereArrayContains("collaborators", user.uid).get()
        
        // 2. Fetch from OLD user-specific collection (Migration support)
        val oldCollection = firestore.collection("users").document(user.uid).collection("raffles").get()
        
        val snapshots = listOf(ownedQuery.await(), collabQuery.await(), oldCollection.await())
        val result = mutableListOf<Pair<Raffle, List<Ticket>>>()
        
        val processedIds = mutableSetOf<String>()

        for (snapshot in snapshots) {
            for (doc in snapshot.documents) {
                val cloudId = doc.id
                if (processedIds.contains(cloudId)) continue
                processedIds.add(cloudId)
                
                val raffle = Raffle(
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    digits = doc.getLong("digits")?.toInt() ?: 2,
                    maxNumber = doc.getLong("maxNumber")?.toInt() ?: 100,
                    ticketValue = doc.getDouble("ticketValue") ?: 0.0,
                    prizeValue = doc.getDouble("prizeValue") ?: 0.0,
                    drawDate = doc.getLong("drawDate") ?: 0L,
                    status = com.afelix.rifaapp.domain.model.RaffleStatus.valueOf(doc.getString("status") ?: "ACTIVE"),
                    winningNumber = doc.getLong("winningNumber")?.toInt(),
                    userId = doc.getString("ownerId") ?: user.uid,
                    cloudId = cloudId,
                    ownerEmail = doc.getString("ownerEmail") ?: user.email
                )
                
                // Fetch tickets (Works for both old and new structure as they use the same subcollection name)
                val ticketsSnapshot = doc.reference.collection("tickets").get().await()
                val tickets = ticketsSnapshot.documents.map { tDoc ->
                    Ticket(
                        raffleId = 0, // Set locally
                        number = tDoc.getLong("number")?.toInt() ?: 0,
                        status = com.afelix.rifaapp.domain.model.TicketStatus.valueOf(tDoc.getString("status") ?: "AVAILABLE"),
                        customerName = tDoc.getString("customerName"),
                        customerPhone = tDoc.getString("customerPhone")
                    )
                }
                result.add(raffle to tickets)
                
                // If it was in the OLD collection, sync it to the NEW one automatically
                if (snapshot == snapshots[2]) {
                    syncRaffle(raffle, tickets)
                }
            }
        }
        return result
    }

    suspend fun deleteRaffle(cloudId: String) {
        // Only allow if owner - security rules will handle this
        firestore.collection("raffles").document(cloudId).delete().await()
    }
}
