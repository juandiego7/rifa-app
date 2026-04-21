package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afelix.rifaapp.domain.model.Raffle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleListScreen(
    raffles: List<Raffle>,
    onRaffleClick: (Raffle) -> Unit,
    onCreateRaffleClick: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(raffles) { raffle ->
                RaffleItem(raffle = raffle, onClick = { onRaffleClick(raffle) })
            }
        }
    }
}

@Composable
fun RaffleItem(raffle: Raffle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = raffle.title, style = MaterialTheme.typography.titleLarge)
            Text(text = raffle.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Estado: ${raffle.status.name}", style = MaterialTheme.typography.labelMedium)
                Text(text = "Valor: $${raffle.ticketValue}", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
