package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.ui.components.AdBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DrawWinnerDialog(
    raffle: Raffle,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var mode by remember { mutableStateOf<SelectionMode?>(null) }
    var manualNumber by remember { mutableStateOf("") }
    var animatingNumber by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Rifa", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mode == null) {
                    Text("Elige cómo deseas establecer el número ganador:", textAlign = TextAlign.Center)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { mode = SelectionMode.RANDOM },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Casino, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Azar")
                        }
                        OutlinedButton(
                            onClick = { mode = SelectionMode.MANUAL },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Manual")
                        }
                    }
                } else if (mode == SelectionMode.MANUAL) {
                    OutlinedTextField(
                        value = manualNumber,
                        onValueChange = { if (it.all { c -> c.isDigit() }) manualNumber = it },
                        label = { Text("Número Ganador") },
                        placeholder = { Text("Ej. 42") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (mode == SelectionMode.RANDOM) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = animatingNumber.toString().padStart(raffle.digits, '0'),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        if (!isAnimating) {
                            Button(onClick = {
                                scope.launch {
                                    isAnimating = true
                                    repeat(30) { i ->
                                        animatingNumber = Random.nextInt(raffle.maxNumber)
                                        delay(50L + i * 5)
                                    }
                                    isAnimating = false
                                }
                            }) {
                                Text("¡Girar Ruleta!")
                            }
                        } else {
                            Text("Sorteando...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                
                // Add Banner inside scrollable content
                Spacer(modifier = Modifier.height(16.dp))
                AdBanner()
            }
        },
        confirmButton = {
            if (mode != null && !isAnimating) {
                TextButton(
                    enabled = mode == SelectionMode.RANDOM || manualNumber.isNotBlank(),
                    onClick = {
                        val winner = if (mode == SelectionMode.RANDOM) animatingNumber else manualNumber.toInt()
                        onConfirm(winner)
                    }
                ) {
                    Text("Confirmar Ganador")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (mode == null) onDismiss() else mode = null }) {
                Text(if (mode == null) "Cancelar" else "Volver")
            }
        }
    )
}

enum class SelectionMode {
    MANUAL, RANDOM
}
