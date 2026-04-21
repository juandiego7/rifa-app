package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.RaffleDashboardStats
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleDetailScreen(
    raffle: Raffle?,
    tickets: List<Ticket>,
    stats: RaffleDashboardStats,
    onBack: () -> Unit,
    onTicketClick: (Ticket) -> Unit
) {
    if (raffle == null) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(raffle.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DashboardSection(raffle, stats)
            
            Text(
                text = "Boletas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 60.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(tickets) { ticket ->
                    TicketCircle(
                        ticket = ticket,
                        digits = raffle.digits,
                        onClick = { onTicketClick(ticket) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardSection(raffle: Raffle, stats: RaffleDashboardStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Resumen", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Sorteo: ${DateFormatter.format(raffle.drawDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = "Vendidas", value = stats.soldTickets.toString())
                StatItem(label = "Reservadas", value = stats.reservedTickets.toString())
                StatItem(label = "Faltan", value = stats.availableTickets.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = "Recaudado", value = CurrencyFormatter.format(stats.moneyCollected))
                StatItem(label = "En reserva", value = CurrencyFormatter.format(stats.moneyReserved))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TicketCircle(
    ticket: Ticket,
    digits: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (ticket.status) {
        TicketStatus.AVAILABLE -> MaterialTheme.colorScheme.surfaceVariant
        TicketStatus.RESERVED -> Color(0xFFFFD54F) // Un amarillo más cálido
        TicketStatus.PAID -> Color(0xFF81C784) // Un verde más agradable
    }
    
    val contentColor = when (ticket.status) {
        TicketStatus.AVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.Black
    }
    
    val formattedNumber = ticket.number.toString().padStart(digits, '0')
    
    Surface(
        onClick = onClick,
        modifier = modifier.padding(4.dp),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formattedNumber,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
