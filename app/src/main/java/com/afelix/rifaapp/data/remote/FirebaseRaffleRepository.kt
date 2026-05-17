package com.afelix.rifaapp.data.remote

import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Collaborator(val uid: String, val email: String)

class FirebaseRaffleRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun syncRaffle(raffle: Raffle, tickets: List<Ticket>): String {
        val user = auth.currentUser ?: return ""
        
        val docRef = if (!raffle.cloudId.isNullOrBlank()) {
            firestore.collection("raffles").document(raffle.cloudId)
        } else {
            firestore.collection("raffles").document()
        }

        val raffleData = mutableMapOf<String, Any?>(
            "title" to raffle.title,
            "description" to raffle.description,
            "digits" to raffle.digits,
            "maxNumber" to raffle.maxNumber,
            "ticketValue" to raffle.ticketValue,
            "prizeValue" to raffle.prizeValue,
            "drawDate" to raffle.drawDate,
            "status" to raffle.status.name,
            "winningNumber" to raffle.winningNumber,
            "createdAt" to raffle.createdAt,
            "lastUpdated" to FieldValue.serverTimestamp()
        )
        
        if (raffle.cloudId.isNullOrBlank()) {
            raffleData["ownerId"] = user.uid
            raffleData["ownerEmail"] = user.email
            raffleData["collaborators"] = listOf<String>()
            docRef.set(raffleData).await()
        } else {
            docRef.update(raffleData).await()
        }
        
        val ticketsCollection = docRef.collection("tickets")
        val assignedTickets = tickets.filter { it.status != com.afelix.rifaapp.domain.model.TicketStatus.AVAILABLE }
        
        for (ticket in assignedTickets) {
            val ticketData = mapOf(
                "number" to ticket.number,
                "status" to ticket.status.name,
                "customerName" to ticket.customerName,
                "customerPhone" to ticket.customerPhone,
                "sellerId" to user.uid
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
        
        // Add to collaborators list (using a map to store email too)
        val collaboratorInfo = mapOf("uid" to user.uid, "email" to user.email)
        docRef.update("collaboratorDetails", FieldValue.arrayUnion(collaboratorInfo)).await()
        docRef.update("collaborators", FieldValue.arrayUnion(user.uid)).await()
        
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
            userId = user.uid,
            ownerId = snapshot.getString("ownerId"),
            cloudId = cloudId,
            ownerEmail = snapshot.getString("ownerEmail"),
            createdAt = snapshot.getLong("createdAt") ?: 0L
        )
        
        val ticketsSnapshot = docRef.collection("tickets").get().await()
        val tickets = ticketsSnapshot.documents.map { tDoc ->
            Ticket(
                raffleId = 0,
                number = tDoc.getLong("number")?.toInt() ?: 0,
                status = com.afelix.rifaapp.domain.model.TicketStatus.valueOf(tDoc.getString("status") ?: "AVAILABLE"),
                customerName = tDoc.getString("customerName"),
                customerPhone = tDoc.getString("customerPhone")
            )
        }
        
        return raffle to tickets
    }

    suspend fun fetchCollaborators(cloudId: String): List<Collaborator> {
        val docRef = firestore.collection("raffles").document(cloudId)
        val snapshot = docRef.get().await()
        val details = snapshot.get("collaboratorDetails") as? List<Map<String, String>> ?: emptyList()
        return details.map { Collaborator(it["uid"] ?: "", it["email"] ?: "") }
    }

    suspend fun removeCollaborator(cloudId: String, collaboratorUid: String, collaboratorEmail: String) {
        val docRef = firestore.collection("raffles").document(cloudId)
        val collaboratorInfo = mapOf("uid" to collaboratorUid, "email" to collaboratorEmail)
        docRef.update("collaboratorDetails", FieldValue.arrayRemove(collaboratorInfo)).await()
        docRef.update("collaborators", FieldValue.arrayRemove(collaboratorUid)).await()
    }

    suspend fun fetchAllUserRaffles(): List<Pair<Raffle, List<Ticket>>> {
        val user = auth.currentUser ?: return emptyList()
        
        val ownedQuery = firestore.collection("raffles").whereEqualTo("ownerId", user.uid).get()
        val collabQuery = firestore.collection("raffles").whereArrayContains("collaborators", user.uid).get()
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
                    userId = user.uid,
                    ownerId = doc.getString("ownerId"),
                    cloudId = cloudId,
                    ownerEmail = doc.getString("ownerEmail"),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
                
                val ticketsSnapshot = doc.reference.collection("tickets").get().await()
                val tickets = ticketsSnapshot.documents.map { tDoc ->
                    Ticket(
                        raffleId = 0,
                        number = tDoc.getLong("number")?.toInt() ?: 0,
                        status = com.afelix.rifaapp.domain.model.TicketStatus.valueOf(tDoc.getString("status") ?: "AVAILABLE"),
                        customerName = tDoc.getString("customerName"),
                        customerPhone = tDoc.getString("customerPhone")
                    )
                }
                result.add(raffle to tickets)
                
                if (snapshot == snapshots[2]) {
                    syncRaffle(raffle, tickets)
                    doc.reference.delete().await()
                }
            }
        }
        return result
    }

    suspend fun deleteRaffle(cloudId: String) {
        val user = auth.currentUser ?: return
        val docRef = firestore.collection("raffles").document(cloudId)
        val snapshot = docRef.get().await()
        
        if (snapshot.exists()) {
            val ownerId = snapshot.getString("ownerId")
            if (ownerId == user.uid) {
                val ticketsSnapshot = docRef.collection("tickets").get().await()
                for (ticketDoc in ticketsSnapshot.documents) {
                    ticketDoc.reference.delete().await()
                }
                docRef.delete().await()
            } else {
                // If collaborator, remove self from lists
                val collaboratorInfo = mapOf("uid" to user.uid, "email" to user.email)
                docRef.update("collaboratorDetails", FieldValue.arrayRemove(collaboratorInfo)).await()
                docRef.update("collaborators", FieldValue.arrayRemove(user.uid)).await()
            }
        }
        
        val oldDocRef = firestore.collection("users").document(user.uid).collection("raffles").document(cloudId)
        val oldTicketsSnapshot = oldDocRef.collection("tickets").get().await()
        for (ticketDoc in oldTicketsSnapshot.documents) {
            ticketDoc.reference.delete().await()
        }
        oldDocRef.delete().await()
    }

    suspend fun sendInvitation(raffleId: String, raffleTitle: String, targetEmail: String) {
        val user = auth.currentUser ?: return
        val invitation = mapOf(
            "raffleId" to raffleId,
            "raffleTitle" to raffleTitle,
            "ownerEmail" to user.email,
            "targetEmail" to targetEmail,
            "status" to "PENDING",
            "timestamp" to FieldValue.serverTimestamp()
        )
        val invId = "${raffleId}_${targetEmail.replace(".", "_")}"
        firestore.collection("invitations").document(invId).set(invitation).await()
    }

    suspend fun fetchPendingInvitations(): List<Map<String, Any>> {
        val user = auth.currentUser ?: return emptyList()
        val email = user.email ?: return emptyList()
        
        val snapshot = firestore.collection("invitations")
            .whereEqualTo("targetEmail", email)
            .whereEqualTo("status", "PENDING")
            .get().await()
            
        return snapshot.documents.map { doc ->
            doc.data?.toMutableMap()?.apply { this["id"] = doc.id } ?: emptyMap()
        }
    }

    suspend fun respondToInvitation(invitationId: String, accept: Boolean) {
        val docRef = firestore.collection("invitations").document(invitationId)
        if (accept) {
            val snapshot = docRef.get().await()
            val raffleId = snapshot.getString("raffleId") ?: return
            joinRaffle(raffleId)
            docRef.update("status", "ACCEPTED").await()
        } else {
            docRef.update("status", "DECLINED").await()
        }
    }
}
