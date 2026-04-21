package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TicketAssignmentDialog(
    tickets: List<Ticket>,
    digits: Int,
    onDismiss: () -> Unit,
    onConfirm: (List<Ticket>) -> Unit
) {
    // If multiple tickets, start with empty fields or common values
    val firstTicket = tickets.firstOrNull()
    var name by remember { mutableStateOf(if (tickets.size == 1) firstTicket?.customerName ?: "" else "") }
    var phone by remember { mutableStateOf(if (tickets.size == 1) firstTicket?.customerPhone ?: "" else "") }
    var status by remember { mutableStateOf(if (tickets.size == 1) firstTicket?.status ?: TicketStatus.RESERVED else TicketStatus.RESERVED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (tickets.size == 1) {
                    "Boleta #${tickets.first().number.toString().padStart(digits, '0')}"
                } else {
                    "Asignar ${tickets.size} Boletas"
                },
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
                if (tickets.size == 1) {
                    TicketCircle(
                        ticket = tickets.first().copy(status = status),
                        digits = digits,
                        onClick = {},
                        modifier = Modifier.size(80.dp)
                    )
                } else {
                    Text(
                        text = tickets.joinToString(", ") { it.number.toString().padStart(digits, '0') },
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

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
                if (tickets.all { it.status != TicketStatus.AVAILABLE }) {
                    TextButton(
                        onClick = {
                            val updatedTickets = tickets.map {
                                it.copy(
                                    customerName = null,
                                    customerPhone = null,
                                    status = TicketStatus.AVAILABLE
                                )
                            }
                            onConfirm(updatedTickets)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Liberar Todas")
                    }
                }
                TextButton(onClick = {
                    val updatedTickets = tickets.map {
                        it.copy(
                            customerName = if (status == TicketStatus.AVAILABLE) null else name,
                            customerPhone = if (status == TicketStatus.AVAILABLE) null else phone,
                            status = status
                        )
                    }
                    onConfirm(updatedTickets)
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
