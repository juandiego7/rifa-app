package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.RaffleStatus
import com.afelix.rifaapp.ui.viewmodel.AuthState
import com.afelix.rifaapp.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleListScreen(
    raffles: List<Raffle>,
    authViewModel: AuthViewModel,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRaffleClick: (Raffle) -> Unit,
    onCreateRaffleClick: () -> Unit,
    onDeleteRaffle: (Raffle) -> Unit,
    onLogout: () -> Unit,
    onJoinRaffle: (String) -> Unit,
    pendingInvitations: List<Map<String, Any>>,
    onInvitationResponse: (String, Boolean) -> Unit
) {
    var raffleToDelete by remember { mutableStateOf<Raffle?>(null) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    var showInvitationsDialog by remember { mutableStateOf(false) }

    if (showInvitationsDialog) {
        AlertDialog(
            onDismissRequest = { showInvitationsDialog = false },
            title = { Text("Invitaciones de Colaboración") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pendingInvitations.isEmpty()) {
                        Text("No tienes invitaciones pendientes.")
                    } else {
                        pendingInvitations.forEach { invite ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = invite["raffleTitle"] as? String ?: "Rifa sin título",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Dueño: ${invite["ownerEmail"]}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                onInvitationResponse(invite["id"] as String, true)
                                                showInvitationsDialog = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Aceptar", fontSize = 11.sp) }
                                        OutlinedButton(
                                            onClick = {
                                                onInvitationResponse(invite["id"] as String, false)
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Rechazar", fontSize = 11.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showInvitationsDialog = false }) { Text("Cerrar") }
            }
        )
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Unirse a una Rifa") },
            text = {
                Column {
                    Text("Ingresa el código compartido por el dueño de la rifa:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it },
                        label = { Text("Código de Colaboración") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = joinCode.isNotBlank(),
                    onClick = {
                        onJoinRaffle(joinCode)
                        showJoinDialog = false
                        joinCode = ""
                    }
                ) {
                    Text("Unirse")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if ( raffleToDelete != null ) {
        AlertDialog(
            onDismissRequest = { raffleToDelete = null },
            title = { Text("Eliminar Rifa") },
            text = { Text("¿Estás seguro de que deseas eliminar esta rifa? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        raffleToDelete?.let { onDeleteRaffle(it) }
                        raffleToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { raffleToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val authState by authViewModel.authState.collectAsState()
    val currentUser = if (authState is AuthState.Authenticated) {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Mis Rifas")
                        if (currentUser != null) {
                            Text(
                                text = currentUser.email ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Modo Invitado",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    if (currentUser != null) {
                        IconButton(onClick = { showInvitationsDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (pendingInvitations.isNotEmpty()) {
                                        Badge { Text(pendingInvitations.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Invitaciones")
                            }
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                            contentDescription = if (currentUser != null) "Cerrar Sesión" else "Iniciar Sesión"
                        )
                    }
                    IconButton(
                        onClick = onCreateRaffleClick,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Crear Rifa")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(raffles) { raffle ->
                    RaffleItem(
                        raffle = raffle,
                        onClick = { onRaffleClick(raffle) },
                        onDelete = { raffleToDelete = raffle }
                    )
                }
            }
        }
    }
}

@Composable
fun RaffleItem(raffle: Raffle, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Titulo + Eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = raffle.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Eliminar", 
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 1. Full Width Prize & Winner Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF3E5F5),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            tint = Color(0xFF7B1FA2), 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PREMIO", 
                            style = MaterialTheme.typography.labelMedium, 
                            color = Color(0xFF7B1FA2), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4A148C),
                        textAlign = TextAlign.Center
                    )

                    if (raffle.status == RaffleStatus.FINISHED && raffle.winningNumber != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF7B1FA2).copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "GANADOR: ${raffle.winningNumber.toString().padStart(raffle.digits, '0')}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF57F17)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Financial and Progress Row: Combined for maximum impact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recaudado
                InfoBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Recaudado",
                    value = CurrencyFormatter.format(raffle.stats?.moneyCollected ?: 0.0),
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    isLarge = true,
                    extraLargeValue = true
                )

                // Central Circular Progress
                raffle.stats?.let { stats ->
                    val occupied = stats.soldTickets + stats.reservedTickets
                    val progress = if (stats.totalTickets > 0) occupied.toFloat() / stats.totalTickets else 0f
                    
                    Box(
                        modifier = Modifier.size(60.dp), // Comfortable size between boxes
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFF1F1F1),
                            strokeWidth = 5.dp,
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$occupied",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32),
                                lineHeight = 13.sp
                            )
                            HorizontalDivider(
                                modifier = Modifier.width(26.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                            Text(
                                text = "${stats.totalTickets}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }

                // Valor Boleta
                InfoBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.MonetizationOn,
                    label = "Valor Boleta",
                    value = CurrencyFormatter.format(raffle.ticketValue),
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    isLarge = true,
                    extraLargeValue = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom Row: Date + Status (Progress moved out)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Draw Date
                Text(
                    text = "Sorteo: ${DateFormatter.format(raffle.drawDate)}",
                    style = MaterialTheme.typography.bodyLarge, // Increased date size
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
                
                // Status Badge
                Surface(
                    color = if (raffle.status == RaffleStatus.ACTIVE) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (raffle.status == RaffleStatus.ACTIVE) "ACTIVA" else "CERRADA",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), // More padding
                        style = MaterialTheme.typography.titleMedium, // Larger status
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (raffle.status == RaffleStatus.ACTIVE) Color(0xFF1976D2) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBox(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    isLarge: Boolean = false,
    extraLargeValue: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = if (isLarge) 10.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = contentColor, 
                modifier = Modifier.size(if (isLarge) 20.dp else 16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.labelSmall, 
                    fontSize = if (isLarge) 12.sp else 9.sp, // Aumentado de 10.sp a 12.sp
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold, 
                    color = contentColor,
                    maxLines = 1,
                    fontSize = if (extraLargeValue) 16.sp else if (isLarge) 13.sp else 11.sp
                )
            }
        }
    }
}
