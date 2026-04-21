package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TicketAssignmentDialog(
    ticket: Ticket,
    digits: Int,
    onDismiss: () -> Unit,
    onConfirm: (Ticket) -> Unit
) {
    var name by remember { mutableStateOf(ticket.customerName ?: "") }
    var phone by remember { mutableStateOf(ticket.customerPhone ?: "") }
    var status by remember { mutableStateOf(ticket.status) }

    val formattedNumber = ticket.number.toString().padStart(digits, '0')

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Boleta #$formattedNumber",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TicketCircle(
                    ticket = ticket.copy(status = status),
                    digits = digits,
                    onClick = {},
                    modifier = Modifier.size(80.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del cliente") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Estado:")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = status == TicketStatus.RESERVED,
                            onClick = { status = TicketStatus.RESERVED },
                            label = { Text("Reservado") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD54F),
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        FilterChip(
                            selected = status == TicketStatus.PAID,
                            onClick = { status = TicketStatus.PAID },
                            label = { Text("Pagado") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF81C784),
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        FilterChip(
                            selected = status == TicketStatus.AVAILABLE,
                            onClick = { status = TicketStatus.AVAILABLE },
                            label = { Text("Disponible") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (ticket.status != TicketStatus.AVAILABLE) {
                    TextButton(
                        onClick = {
                            onConfirm(ticket.copy(
                                customerName = null,
                                customerPhone = null,
                                status = TicketStatus.AVAILABLE
                            ))
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Liberar Boleta")
                    }
                }
                TextButton(onClick = {
                    onConfirm(ticket.copy(
                        customerName = if (status == TicketStatus.AVAILABLE) null else name,
                        customerPhone = if (status == TicketStatus.AVAILABLE) null else phone,
                        status = status
                    ))
                }) {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
