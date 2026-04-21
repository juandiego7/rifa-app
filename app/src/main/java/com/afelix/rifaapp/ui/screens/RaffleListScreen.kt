package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.domain.model.Raffle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleListScreen(
    raffles: List<Raffle>,
    onRaffleClick: (Raffle) -> Unit,
    onCreateRaffleClick: () -> Unit,
    onDeleteRaffle: (Raffle) -> Unit
) {
    var raffleToDelete by remember { mutableStateOf<Raffle?>(null) }

    if (raffleToDelete != null) {
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = raffle.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = if (raffle.status.name == "ACTIVE") Color(0xFF81C784) else Color.Gray,
                        contentColor = Color.Black
                    ) {
                        Text(text = raffle.status.name)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Si el premio es dinero, mostramos el formato de moneda. Si es texto, mostramos la descripción.
            val prizeText = if (raffle.prizeValue > 0) {
                "Premio: ${CurrencyFormatter.format(raffle.prizeValue)}"
            } else {
                raffle.description
            }
            
            Text(text = prizeText, style = MaterialTheme.typography.bodyMedium, color = if(raffle.prizeValue > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)

            Text(
                text = "Juega el: ${DateFormatter.format(raffle.drawDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            raffle.stats?.let { stats ->
                val occupiedTickets = stats.soldTickets + stats.reservedTickets
                val progress = if (stats.totalTickets > 0) occupiedTickets.toFloat() / stats.totalTickets else 0f
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Progreso (Ventas + Reservas)", style = MaterialTheme.typography.labelMedium)
                        Text(text = "$occupiedTickets / ${stats.totalTickets}", style = MaterialTheme.typography.labelMedium)
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFC8E6C9),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "Valor: ${CurrencyFormatter.format(raffle.ticketValue)}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
