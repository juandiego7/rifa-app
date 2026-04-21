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
    var description by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("") }
    var maxNumber by remember { mutableStateOf("100") }
    var ticketValue by remember { mutableStateOf("") }

    var descriptionTouched by remember { mutableStateOf(false) }
    var prizeTouched by remember { mutableStateOf(false) }
    var maxNumberTouched by remember { mutableStateOf(false) }
    var ticketValueTouched by remember { mutableStateOf(false) }
    
    val lettersOnly = remember { Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$") }
    val alphanumeric = remember { Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]*$") }
    val numbersOnly = remember { Regex("^[0-9]*$") }
    val decimalOnly = remember { Regex("^[0-9]*\\.?[0-9]*$") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
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
                value = description,
                onValueChange = { 
                    descriptionTouched = true
                    if (it.matches(lettersOnly)) description = it 
                },
                label = { Text("Descripción de la Rifa") },
                placeholder = { Text("Solo letras") },
                modifier = Modifier.fillMaxWidth(),
                isError = descriptionTouched && description.isBlank()
            )
            OutlinedTextField(
                value = prize,
                onValueChange = { 
                    prizeTouched = true
                    if (it.matches(alphanumeric)) prize = it 
                },
                label = { Text("Premio") },
                placeholder = { Text("Letras o monto de dinero") },
                modifier = Modifier.fillMaxWidth(),
                isError = prizeTouched && prize.isBlank()
            )
            
            OutlinedTextField(
                value = maxNumber,
                onValueChange = { 
                    maxNumberTouched = true
                    if (it.matches(numbersOnly)) {
                        if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 10000) {
                            maxNumber = it
                        }
                    }
                },
                label = { Text("Cantidad de Boletas (Máx. 10.000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = maxNumberTouched && (maxNumber.isBlank() || (maxNumber.toIntOrNull() ?: 0) == 0)
            )
            
            OutlinedTextField(
                value = ticketValue,
                onValueChange = { 
                    ticketValueTouched = true
                    if (it.matches(decimalOnly)) ticketValue = it 
                },
                label = { Text("Valor de la Boleta") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = ticketValueTouched && ticketValue.isBlank()
            )

            OutlinedTextField(
                value = DateFormatter.format(datePickerState.selectedDateMillis ?: System.currentTimeMillis()),
                onValueChange = { },
                label = { Text("Fecha del Sorteo") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                }
            )
            
            val isFormValid = description.isNotBlank() && 
                             prize.isNotBlank() && 
                             maxNumber.isNotBlank() && 
                             (maxNumber.toIntOrNull() ?: 0) > 0 &&
                             ticketValue.isNotBlank()

            Button(
                onClick = {
                    val count = maxNumber.toIntOrNull() ?: 0
                    val calculatedDigits = if (count > 0) (count - 1).toString().length else 1
                    val prizeAsAmount = prize.toDoubleOrNull()
                    
                    val raffle = Raffle(
                        title = description,
                        description = prize, // Guardamos el texto original (ya sea "Moto" o "1000000")
                        digits = calculatedDigits,
                        maxNumber = count,
                        ticketValue = ticketValue.toDoubleOrNull() ?: 0.0,
                        prizeValue = prizeAsAmount ?: 0.0, // Room guardará el número si existe
                        drawDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    )
                    onSave(raffle)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Crear Rifa")
            }
        }
    }
}
