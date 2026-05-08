package com.afelix.rifaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
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

@Composable
fun WinnerCard(
    raffle: Raffle,
    winnerTicket: Ticket?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFF57F17)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "¡TENEMOS UN GANADOR!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF57F17),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            /* Title removed as per user request */
            
            val prizeDisplay = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description
            Text(
                text = "Premio: $prizeDisplay",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                color = Color(0xFFF3E5F5),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NÚMERO GANADOR",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (raffle.winningNumber ?: 0).toString().padStart(raffle.digits, '0'),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4A148C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (winnerTicket != null && !winnerTicket.customerName.isNullOrBlank()) {
                Text(
                    text = "Ganador(a):",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    text = winnerTicket.customerName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1C1E),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "¡El número no estaba asignado!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Sorteo realizado el: ${DateFormatter.format(raffle.drawDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
