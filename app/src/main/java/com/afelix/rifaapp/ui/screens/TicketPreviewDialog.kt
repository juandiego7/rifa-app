package com.afelix.rifaapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.ui.components.DigitalTicket

@Composable
fun TicketPreviewDialog(
    raffle: Raffle,
    tickets: List<Ticket>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val shareTicket = {
        val firstTicket = tickets.firstOrNull()
        val customerName = firstTicket?.customerName ?: "N/A"
        val numbers = tickets.joinToString(", ") { it.number.toString().padStart(raffle.digits, '0') }
        val prize = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description
        val total = CurrencyFormatter.format(raffle.ticketValue * tickets.size)
        val date = DateFormatter.format(raffle.drawDate)

        val message = """
            🎟️ *TICKET DE RIFA* 🎟️
            
            *Premio:* $prize
            *Cliente:* $customerName
            *Números:* $numbers
            *Fecha Sorteo:* $date
            *Total a pagar:* $total
            
            ¡Gracias por participar y mucha suerte! 🍀
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Ticket"))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DigitalTicket(raffle = raffle, tickets = tickets)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar")
                    }
                    
                    Button(
                        onClick = shareTicket,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir")
                    }
                }
            }
        }
    }
}
