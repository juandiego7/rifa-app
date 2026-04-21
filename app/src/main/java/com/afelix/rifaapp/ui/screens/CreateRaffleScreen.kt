package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.domain.model.Raffle
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRaffleScreen(
    onBack: () -> Unit,
    onSave: (Raffle) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var digits by remember { mutableStateOf("2") }
    var maxNumber by remember { mutableStateOf("100") }
    var ticketValue by remember { mutableStateOf("") }
    var prizeValue by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7) // Predeterminado: dentro de una semana
        }.timeInMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Rifa") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción del premio") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = digits,
                    onValueChange = { digits = it },
                    label = { Text("Dígitos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxNumber,
                    onValueChange = { maxNumber = it },
                    label = { Text("Cant. Boletas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = ticketValue,
                onValueChange = { ticketValue = it },
                label = { Text("Valor de la boleta") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = prizeValue,
                onValueChange = { prizeValue = it },
                label = { Text("Valor del premio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = DateFormatter.format(datePickerState.selectedDateMillis ?: System.currentTimeMillis()),
                onValueChange = { },
                label = { Text("Fecha del sorteo") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                }
            )
            
            Button(
                onClick = {
                    val raffle = Raffle(
                        title = title,
                        description = description,
                        digits = digits.toIntOrNull() ?: 2,
                        maxNumber = maxNumber.toIntOrNull() ?: 100,
                        ticketValue = ticketValue.toDoubleOrNull() ?: 0.0,
                        prizeValue = prizeValue.toDoubleOrNull() ?: 0.0,
                        drawDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    )
                    onSave(raffle)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && maxNumber.isNotBlank() && digits.isNotBlank()
            ) {
                Text("Crear Rifa")
            }
        }
    }
}
