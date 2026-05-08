package com.afelix.rifaapp.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.core.util.Country
import com.afelix.rifaapp.core.util.CountryService
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TicketAssignmentDialog(
    raffle: Raffle,
    tickets: List<Ticket>,
    onDismiss: () -> Unit,
    onConfirm: (List<Ticket>) -> Unit
) {
    val context = LocalContext.current
    val firstTicket = tickets.firstOrNull()
    
    // Detect if all tickets have the same owner to pre-fill
    val allSameName = tickets.map { it.customerName }.distinct().let { it.size == 1 && it.first() != null }
    val allSamePhone = tickets.map { it.customerPhone }.distinct().let { it.size == 1 && it.first() != null }
    val sameOwner = allSameName && allSamePhone

    var name by remember { mutableStateOf(if (sameOwner || tickets.size == 1) firstTicket?.customerName ?: "" else "") }
    
    // Use dynamic CountryService instead of hardcoded list
    val countries = CountryService.allCountries
    val defaultCountry = countries.find { it.isoCode == "CO" } ?: countries.first()
    
    // Parse existing phone if any (format expected: +CC Number)
    val existingPhone = if (sameOwner || tickets.size == 1) firstTicket?.customerPhone ?: "" else ""
    val detectedCountry = countries.find { existingPhone.startsWith(it.dialCode) } ?: defaultCountry
    
    var selectedCountry by remember { mutableStateOf(detectedCountry) }
    var phoneNumber by remember { 
        mutableStateOf(
            if (existingPhone.startsWith(selectedCountry.dialCode)) {
                existingPhone.removePrefix(selectedCountry.dialCode).trim()
            } else {
                existingPhone
            }
        ) 
    }
    
    // Default to RESERVED if multiple tickets (different owners) or if first ticket is available
    val allSameStatus = tickets.map { it.status }.distinct().size == 1
    val initialStatus = if ((sameOwner || tickets.size == 1) && firstTicket?.status != TicketStatus.AVAILABLE) {
        firstTicket?.status ?: TicketStatus.RESERVED
    } else if (allSameStatus) {
        tickets.first().status
    } else {
        TicketStatus.RESERVED
    }
    var status by remember { mutableStateOf(initialStatus) }

    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
            context.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val contactId = cursor.getString(idIndex)
                    name = cursor.getString(nameIndex)

                    val phonesProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        phonesProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val rawPhone = phoneCursor.getString(0).replace(" ", "").replace("-", "")
                            
                            // Try to match country dial code from contact
                            val matchedCountry = countries.find { country -> rawPhone.startsWith(country.dialCode) }
                            if (matchedCountry != null) {
                                selectedCountry = matchedCountry
                                phoneNumber = rawPhone.removePrefix(matchedCountry.dialCode).filter { it.isDigit() }
                            } else {
                                phoneNumber = rawPhone.filter { it.isDigit() }
                            }
                        }
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) contactPickerLauncher.launch(null)
    }

    val shareTicket = {
        val numbers = tickets.joinToString(", ") { it.number.toString().padStart(raffle.digits, '0') }
        val prize = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description
        val total = CurrencyFormatter.format(raffle.ticketValue * tickets.size)
        val date = DateFormatter.format(raffle.drawDate)

        val message = """
            🎟️ *TICKET DE RIFA* 🎟️
            
            *Premio:* $prize
            *Cliente:* $name
            *Números:* $numbers
            *Fecha Sorteo:* $date
            *Total a pagar:* $total
            
            ¡Gracias por participar y mucha suerte! 🍀
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Ticket"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (tickets.size == 1) {
                    "Boleta #${tickets.first().number.toString().padStart(raffle.digits, '0')}"
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
                        digits = raffle.digits,
                        onClick = {},
                        modifier = Modifier.size(80.dp),
                        showName = false
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tickets.forEach { ticket ->
                            TicketCircle(
                                ticket = ticket.copy(status = status),
                                digits = raffle.digits,
                                onClick = {},
                                showBadge = false,
                                showName = false,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del cliente") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                    contactPickerLauncher.launch(null)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                }
                            }) {
                                Icon(Icons.Default.ContactPage, contentDescription = "Buscar contacto")
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        
                        Box(modifier = Modifier.weight(0.4f)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .height(56.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = OutlinedTextFieldDefaults.shape
                                    )
                                    .clickable { expanded = true },
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDropDown, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(text = selectedCountry.flag, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = selectedCountry.dialCode, 
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.width(280.dp)
                            ) {
                                countries.forEach { country ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = country.flag, modifier = Modifier.width(24.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = country.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = country.dialCode, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            selectedCountry = country
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.all { char -> char.isDigit() }) phoneNumber = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.weight(0.6f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                    
                    Text("Estado:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = status == TicketStatus.RESERVED,
                            onClick = { status = TicketStatus.RESERVED },
                            label = { Text("Reservado") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFD54F), selectedLabelColor = Color.Black),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        FilterChip(
                            selected = status == TicketStatus.PAID,
                            onClick = { status = TicketStatus.PAID },
                            label = { Text("Pagado") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF81C784), selectedLabelColor = Color.Black),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        FilterChip(
                            selected = status == TicketStatus.AVAILABLE,
                            onClick = { status = TicketStatus.AVAILABLE },
                            label = { Text("Disponible") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Solo mostramos compartir si el estado no es Disponible y hay datos de contacto
                if (status != TicketStatus.AVAILABLE && name.isNotBlank() && phoneNumber.isNotBlank()) {
                    IconButton(
                        onClick = shareTicket,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                if (tickets.all { it.status != TicketStatus.AVAILABLE }) {
                    TextButton(
                        onClick = {
                            onConfirm(tickets.map { it.copy(customerName = null, customerPhone = null, status = TicketStatus.AVAILABLE) })
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Liberar")
                    }
                }
                TextButton(
                    enabled = (name.isNotBlank() && phoneNumber.isNotBlank()) || status == TicketStatus.AVAILABLE,
                    onClick = {
                        val fullPhone = "${selectedCountry.dialCode} ${phoneNumber.trim()}"
                        onConfirm(tickets.map {
                            it.copy(
                                customerName = if (status == TicketStatus.AVAILABLE) null else name,
                                customerPhone = if (status == TicketStatus.AVAILABLE) null else fullPhone,
                                status = status
                            )
                        })
                    }
                ) {
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
