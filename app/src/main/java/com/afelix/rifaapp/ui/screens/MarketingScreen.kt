package com.afelix.rifaapp.ui.screens

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.core.util.ImageSharing
import com.afelix.rifaapp.core.util.ViewCaptureWrapper
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MarketingScreen(
    raffle: Raffle?,
    tickets: List<Ticket>,
    onBack: () -> Unit
) {
    if (raffle == null) return

    val context = LocalContext.current
    var showTitle by remember { mutableStateOf(true) }
    var showDescription by remember { mutableStateOf(true) }
    var showOnlyAvailable by remember { mutableStateOf(false) }
    var showPrice by remember { mutableStateOf(true) }
    var showDate by remember { mutableStateOf(true) }
    var marketingMessage by remember { mutableStateOf("¡Separa tu número ahora!") }
    
    var captureView by remember { mutableStateOf<View?>(null) }
    val filteredTickets = if (showOnlyAvailable) tickets.filter { it.status == TicketStatus.AVAILABLE } else tickets

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizar Publicidad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    captureView?.let {
                        val bitmap = ImageSharing.captureView(it)
                        if (bitmap != null) {
                            ImageSharing.shareBitmap(context, bitmap, "publicidad_${raffle.id}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(Modifier.width(8.dp))
                Text("Compartir Imagen")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Configuration Controls
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Configuración de la imagen", style = MaterialTheme.typography.titleSmall)
                    
                    ControlRow("Mostrar título", showTitle) { showTitle = it }
                    ControlRow("Mostrar premio", showDescription) { showDescription = it }
                    ControlRow("Solo disponibles", showOnlyAvailable) { showOnlyAvailable = it }
                    ControlRow("Mostrar precio", showPrice) { showPrice = it }
                    ControlRow("Mostrar sorteo", showDate) { showDate = it }
                    
                    OutlinedTextField(
                        value = marketingMessage,
                        onValueChange = { marketingMessage = it },
                        label = { Text("Mensaje publicitario") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                "Vista Previa:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Preview and Capture Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            ) {
                // We keep it inside a box with width but allow it to grow for capture
                ViewCaptureWrapper<View>(onViewReady = { captureView = it }) {
                    // This is exactly what will be shared
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 2.dp, vertical = 12.dp), // Near-zero horizontal padding
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showTitle) {
                            Text(
                                text = raffle.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        if (showDescription) {
                            val prizeDisplay = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description
                            Text(
                                text = "Premio: $prizeDisplay",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (showPrice || showDate) {
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                if (showPrice) {
                                    MarketingStat("VALOR BOLETA", CurrencyFormatter.format(raffle.ticketValue), Color(0xFF2E7D32))
                                }
                                if (showDate) {
                                    MarketingStat("FECHA SORTEO", DateFormatter.format(raffle.drawDate), Color(0xFF1976D2))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Maximum density grid using full width (12 columns for better balance with larger circles)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            maxItemsInEachRow = 12
                        ) {
                            filteredTickets.forEach { ticket ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp) // Larger circles as requested
                                        .padding(1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bgColor = when (ticket.status) {
                                        TicketStatus.AVAILABLE -> Color(0xFFF5F5F5)
                                        TicketStatus.RESERVED -> Color(0xFFFFD54F)
                                        TicketStatus.PAID -> Color(0xFF81C784)
                                    }
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = bgColor
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = ticket.number.toString().padStart(raffle.digits, '0'),
                                                fontSize = if (raffle.digits > 3) 8.sp else 11.sp, // Larger font size
                                                fontWeight = FontWeight.Bold,
                                                color = if (ticket.status == TicketStatus.AVAILABLE) Color.Gray else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (marketingMessage.isNotBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = marketingMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.scale(0.7f))
    }
}

@Composable
fun MarketingStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 12.sp, color = color, fontWeight = FontWeight.Black)
    }
}
