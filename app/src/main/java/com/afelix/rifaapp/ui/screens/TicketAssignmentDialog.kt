package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@Composable
fun TicketAssignmentDialog(
    ticket: Ticket,
    onDismiss: () -> Unit,
    onConfirm: (Ticket) -> Unit
) {
    var name by remember { mutableStateOf(ticket.customerName ?: "") }
    var phone by remember { mutableStateOf(ticket.customerPhone ?: "") }
    var status by remember { mutableStateOf(ticket.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Boleta #${ticket.number}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del cliente") }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") }
                )
                
                Text("Estado:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == TicketStatus.RESERVED,
                        onClick = { status = TicketStatus.RESERVED },
                        label = { Text("Reservado") }
                    )
                    FilterChip(
                        selected = status == TicketStatus.PAID,
                        onClick = { status = TicketStatus.PAID },
                        label = { Text("Pagado") }
                    )
                    FilterChip(
                        selected = status == TicketStatus.AVAILABLE,
                        onClick = { status = TicketStatus.AVAILABLE },
                        label = { Text("Disponible") }
                    )
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
