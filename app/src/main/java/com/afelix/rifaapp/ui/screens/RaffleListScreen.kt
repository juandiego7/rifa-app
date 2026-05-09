package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.RaffleStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleListScreen(
    raffles: List<Raffle>,
    onRaffleClick: (Raffle) -> Unit,
    onCreateRaffleClick: () -> Unit,
    onDeleteRaffle: (Raffle) -> Unit
) {
    var raffleToDelete by remember { mutableStateOf<Raffle?>(null) }

    if ( raffleToDelete != null ) {
        AlertDialog(
            onDismissRequest = { raffleToDelete = null },
            title = { Text("Eliminar Rifa") },
            text = { Text("¿Estás seguro de que deseas eliminar esta rifa? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        raffleToDelete?.let { onDeleteRaffle(it) }
                        raffleToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { raffleToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Rifas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRaffleClick) {
                Icon(Icons.Default.Add, contentDescription = "Crear Rifa")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(raffles) { raffle ->
                RaffleItem(
                    raffle = raffle,
                    onClick = { onRaffleClick(raffle) },
                    onDelete = { raffleToDelete = raffle }
                )
            }
        }
    }
}


@Composable
fun RaffleItem(raffle: Raffle, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = raffle.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 1. Full Width Prize & Winner Box (Centered)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF3E5F5),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            tint = Color(0xFF7B1FA2), 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PREMIO", 
                            style = MaterialTheme.typography.labelMedium, 
                            color = Color(0xFF7B1FA2), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4A148C),
                        textAlign = TextAlign.Center
                    )

                    // Show winner inside the same box if finished
                    if (raffle.status == RaffleStatus.FINISHED && raffle.winningNumber != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF7B1FA2).copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "GANADOR: ${raffle.winningNumber.toString().padStart(raffle.digits, '0')}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF57F17)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Financial Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Recaudado",
                    value = CurrencyFormatter.format(raffle.stats?.moneyCollected ?: 0.0),
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    isLarge = true
                )
                InfoBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.MonetizationOn,
                    label = "Valor Boleta",
                    value = CurrencyFormatter.format(raffle.ticketValue),
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    isLarge = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 3. Progress Section
            raffle.stats?.let { stats ->
                val occupied = stats.soldTickets + stats.reservedTickets
                val progress = if (stats.totalTickets > 0) occupied.toFloat() / stats.totalTickets else 0f
                val percentage = (progress * 100).toInt()
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$occupied de ${stats.totalTickets} boletas",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFF1F1F1),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom Row: Date + Status + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sorteo: ${DateFormatter.format(raffle.drawDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (raffle.status == RaffleStatus.ACTIVE) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (raffle.status == RaffleStatus.ACTIVE) "ACTIVA" else "CERRADA",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (raffle.status == RaffleStatus.ACTIVE) Color(0xFF1976D2) else Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBox(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    isLarge: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = if (isLarge) 10.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = contentColor, 
                modifier = Modifier.size(if (isLarge) 20.dp else 16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.labelSmall, 
                    fontSize = if (isLarge) 10.sp else 8.sp,
                    color = contentColor.copy(alpha = 0.7f)
                )
                Text(
                    text = value, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold, 
                    color = contentColor,
                    maxLines = 1,
                    fontSize = if (isLarge) 13.sp else 11.sp
                )
            }
        }
    }
}
