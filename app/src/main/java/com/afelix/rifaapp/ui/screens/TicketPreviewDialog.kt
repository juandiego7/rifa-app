package com.afelix.rifaapp.ui.screens

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.core.util.ImageSharing
import com.afelix.rifaapp.core.util.ViewCaptureWrapper
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.ui.components.DigitalTicket
import java.net.URLEncoder
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color

@Composable
fun TicketPreviewDialog(
    raffle: Raffle,
    tickets: List<Ticket>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var captureView by remember { mutableStateOf<View?>(null) }
    val firstTicket = tickets.firstOrNull()
    val customerPhone = firstTicket?.customerPhone ?: ""
    
    val createMessage = {
        val customerName = firstTicket?.customerName ?: "N/A"
        val numbers = tickets.joinToString(", ") { it.number.toString().padStart(raffle.digits, '0') }
        val prize = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description
        val total = CurrencyFormatter.format(raffle.ticketValue * tickets.size)
        val date = DateFormatter.format(raffle.drawDate)

        """
            🎟️ *TICKET DE RIFA* 🎟️
            
            *Premio:* $prize
            *Cliente:* $customerName
            *Números:* $numbers
            *Fecha Sorteo:* $date
            *Total a pagar:* $total
            
            ¡Gracias por participar y mucha suerte! 🍀
        """.trimIndent()
    }

    val shareUniversal: () -> Unit = {
        captureView?.let {
            val bitmap = ImageSharing.captureView(it)
            if (bitmap != null) {
                ImageSharing.shareBitmap(context, bitmap, "ticket_rifa_${raffle.id}")
            }
        }
    }

    val shareWhatsApp: () -> Unit = {
        val message = createMessage()
        val cleanPhone = customerPhone.filter { it.isDigit() }
        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(message, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
        context.startActivity(intent)
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
                ViewCaptureWrapper<View>(
                    onViewReady = { captureView = it }
                ) {
                    DigitalTicket(raffle = raffle, tickets = tickets)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (customerPhone.isNotBlank()) {
                            Button(
                                onClick = { shareWhatsApp() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Text("WhatsApp")
                            }
                        }
                        
                        Button(
                            onClick = { shareUniversal() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Imagen")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}
