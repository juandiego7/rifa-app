package com.afelix.rifaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@Composable
fun ShareableGrid(
    raffle: Raffle,
    tickets: List<Ticket>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header for Ads
            Text(
                text = raffle.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1C1E),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VALOR BOLETA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        CurrencyFormatter.format(raffle.ticketValue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FECHA SORTEO", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        DateFormatter.format(raffle.drawDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // The actual grid (always 5 columns for ads)
            Column(
                modifier = Modifier.heightIn(max = 2000.dp) // Support many tickets
            ) {
                val columns = 5
                val rows = (tickets.size + columns - 1) / columns
                
                repeat(rows) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(columns) { colIndex ->
                            val ticketIndex = rowIndex * columns + colIndex
                            if (ticketIndex < tickets.size) {
                                val ticket = tickets[ticketIndex]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val backgroundColor = when (ticket.status) {
                                        TicketStatus.AVAILABLE -> Color(0xFFF5F5F5)
                                        TicketStatus.RESERVED -> Color(0xFFFFD54F)
                                        TicketStatus.PAID -> Color(0xFF81C784)
                                    }
                                    val contentColor = if (ticket.status == TicketStatus.AVAILABLE) Color.Gray else Color.Black
                                    
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = backgroundColor
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = ticket.number.toString().padStart(raffle.digits, '0'),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = contentColor
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "¡Separa tu número ahora!",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
