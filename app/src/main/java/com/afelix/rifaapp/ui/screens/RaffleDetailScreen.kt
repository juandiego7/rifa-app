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
            DashboardSection(stats)
            
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
                        onClick = { onTicketClick(ticket) }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardSection(stats: RaffleDashboardStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Resumen", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = "Vendidas", value = stats.soldTickets.toString())
                StatItem(label = "Reservadas", value = stats.reservedTickets.toString())
                StatItem(label = "Faltan", value = stats.availableTickets.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = "Recaudado", value = "$${stats.moneyCollected}")
                StatItem(label = "En reserva", value = "$${stats.moneyReserved}")
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
fun TicketCircle(ticket: Ticket, digits: Int, onClick: () -> Unit) {
    val backgroundColor = when (ticket.status) {
        TicketStatus.AVAILABLE -> Color.LightGray
        TicketStatus.RESERVED -> Color.Yellow
        TicketStatus.PAID -> Color.Green
    }
    
    val formattedNumber = ticket.number.toString().padStart(digits, '0')
    
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formattedNumber,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
