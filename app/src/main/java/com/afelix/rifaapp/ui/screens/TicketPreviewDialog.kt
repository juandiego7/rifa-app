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
import com.afelix.rifaapp.core.util.ImageSharing
import com.afelix.rifaapp.core.util.ViewCaptureWrapper
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
    var captureView by remember { mutableStateOf<View?>(null) }
    
    val shareImage: () -> Unit = {
        captureView?.let {
            val bitmap = ImageSharing.captureView(it)
            ImageSharing.shareBitmap(context, bitmap, "ticket_rifa_${raffle.id}")
        }
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
                        onClick = { shareImage() },
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
