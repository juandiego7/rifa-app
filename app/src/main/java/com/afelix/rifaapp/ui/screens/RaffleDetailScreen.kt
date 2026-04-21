package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onTicketsAssign: (List<Ticket>) -> Unit
) {
    if (raffle == null) return

    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    var isGridView by remember { mutableStateOf(true) }
    val isSelectionMode = selectedIds.isNotEmpty()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} seleccionadas") },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                val selectedTickets = tickets.filter { it.id in selectedIds }
                                onTicketsAssign(selectedTickets)
                                selectedIds = emptySet()
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Asignar")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(raffle.title, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                contentDescription = if (isGridView) "Vista de Lista" else "Vista de Cuadrícula"
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DashboardSection(raffle, stats)
            
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 50.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tickets) { ticket ->
                        val isSelected = ticket.id in selectedIds
                        TicketCircle(
                            ticket = ticket,
                            digits = raffle.digits,
                            isSelected = isSelected,
                            showBadge = true,
                            onClick = {
                                if (isSelectionMode) {
                                    selectedIds = if (isSelected) selectedIds - ticket.id else selectedIds + ticket.id
                                } else {
                                    onTicketsAssign(listOf(ticket))
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedIds = setOf(ticket.id)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tickets) { ticket ->
                        val isSelected = ticket.id in selectedIds
                        TicketListItem(
                            ticket = ticket,
                            digits = raffle.digits,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelectionMode) {
                                    selectedIds = if (isSelected) selectedIds - ticket.id else selectedIds + ticket.id
                                } else {
                                    onTicketsAssign(listOf(ticket))
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedIds = setOf(ticket.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSection(raffle: Raffle, stats: RaffleDashboardStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sorteo: ${DateFormatter.format(raffle.drawDate)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Premio: ${if(raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactStat(label = "Vend.", value = stats.soldTickets.toString())
                CompactStat(label = "Res.", value = stats.reservedTickets.toString())
                CompactStat(label = "Faltan", value = stats.availableTickets.toString())
                CompactStat(label = "Recaudado", value = CurrencyFormatter.format(stats.moneyCollected))
            }
        }
    }
}

@Composable
fun CompactStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TicketListItem(
    ticket: Ticket,
    digits: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TicketCircle(
                ticket = ticket,
                digits = digits,
                isSelected = isSelected,
                showBadge = false, // In list mode, we don't show the badge over the circle
                onClick = onClick,
                onLongClick = onLongClick,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (ticket.status == TicketStatus.AVAILABLE) "Disponible" else (ticket.customerName ?: "Sin nombre"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (ticket.status == TicketStatus.AVAILABLE) FontWeight.Normal else FontWeight.Bold,
                    color = if (ticket.status == TicketStatus.AVAILABLE) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                if (!ticket.customerPhone.isNullOrBlank()) {
                    Text(
                        text = ticket.customerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TicketCircle(
    ticket: Ticket,
    digits: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    showBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        ticket.status == TicketStatus.AVAILABLE -> MaterialTheme.colorScheme.surfaceVariant
        ticket.status == TicketStatus.RESERVED -> Color(0xFFFFD54F)
        ticket.status == TicketStatus.PAID -> Color(0xFF81C784)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        ticket.status == TicketStatus.AVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.Black
    }
    
    val formattedNumber = ticket.number.toString().padStart(digits, '0')
    
    Box(modifier = modifier.padding(2.dp), contentAlignment = Alignment.TopEnd) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = backgroundColor,
            contentColor = contentColor,
            tonalElevation = if (isSelected) 4.dp else 1.dp,
            shadowElevation = if (isSelected) 6.dp else 2.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedNumber,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (digits > 3) 10.sp else 14.sp
                )
            }
        }
        
        if (isSelected && showBadge) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .offset(x = (2).dp, y = (-2).dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
