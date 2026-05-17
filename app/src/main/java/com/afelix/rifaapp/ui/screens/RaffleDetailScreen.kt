package com.afelix.rifaapp.ui.screens

import android.content.Intent
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afelix.rifaapp.core.util.CurrencyFormatter
import com.afelix.rifaapp.core.util.DateFormatter
import com.afelix.rifaapp.core.util.ImageSharing
import com.afelix.rifaapp.core.util.PdfExporter
import com.afelix.rifaapp.core.util.ViewCaptureWrapper
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.RaffleDashboardStats
import com.afelix.rifaapp.domain.model.RaffleStatus
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus
import com.afelix.rifaapp.ui.components.ShareableGrid
import com.afelix.rifaapp.ui.components.WinnerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleDetailScreen(
    raffle: Raffle?,
    tickets: List<Ticket>,
    stats: RaffleDashboardStats,
    onBack: () -> Unit,
    onTicketsAssign: (List<Ticket>) -> Unit,
    onTicketsShare: (List<Ticket>) -> Unit,
    onMarketingClick: () -> Unit,
    onDrawWinner: () -> Unit,
    onInviteCollaborator: (String) -> Unit,
    collaborators: List<com.afelix.rifaapp.data.remote.Collaborator>,
    onRemoveCollaborator: (com.afelix.rifaapp.data.remote.Collaborator) -> Unit
) {
    if (raffle == null) return

    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val isOwner = auth.currentUser != null && (raffle.ownerId == auth.currentUser?.uid || raffle.userId == auth.currentUser?.uid)

    val context = LocalContext.current
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    var isGridView by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isSelectionMode = selectedIds.isNotEmpty()
    
    val selectedTickets = tickets.filter { it.id in selectedIds }
    val canShareSelection = selectedTickets.isNotEmpty() && 
            selectedTickets.all { it.status != TicketStatus.AVAILABLE } &&
            selectedTickets.map { it.customerName }.distinct().size == 1 &&
            selectedTickets.map { it.customerPhone }.distinct().size == 1

    var winnerCaptureView by remember { mutableStateOf<View?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    val filteredTickets = tickets.filter { ticket ->
        val formattedNumber = ticket.number.toString().padStart(raffle.digits, '0')
        searchQuery.isBlank() || 
        formattedNumber.contains(searchQuery) || 
        (ticket.customerName?.contains(searchQuery, ignoreCase = true) == true)
    }

    val shareWinnerImage: () -> Unit = {
        winnerCaptureView?.let {
            val bitmap = ImageSharing.captureView(it)
            if (bitmap != null) {
                ImageSharing.shareBitmap(context, bitmap, "ganador_rifa_${raffle.id}")
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} seleccionadas") },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                    },
                    actions = {
                        if (canShareSelection) {
                            IconButton(
                                onClick = {
                                    onTicketsShare(selectedTickets)
                                    selectedIds = emptySet()
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir Ticket")
                            }
                        }
                        Button(
                            onClick = {
                                onTicketsAssign(selectedTickets)
                                selectedIds = emptySet()
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(if (selectedTickets.all { it.status == TicketStatus.AVAILABLE }) "Asignar" else "Editar")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(raffle.title, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                    actions = {
                        if (selectedTabIndex == 0) {
                            IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.SearchOff else Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            }
                            IconButton(
                                onClick = { isGridView = !isGridView },
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                    contentDescription = if (isGridView) "Vista de Lista" else "Vista de Cuadrícula"
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Boletas", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ConfirmationNumber, null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Gestión", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Settings, null) }
                )
            }

            if (selectedTabIndex == 0) {
                // TAB 1: TICKETS
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar (Toggleable)
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por número o nombre...", fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    isSearchExpanded = false
                                }) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                        )
                    }
                    
                    LazyVerticalGrid(
                        columns = if (isGridView) GridCells.Adaptive(minSize = 44.dp) else GridCells.Fixed(2),
                        contentPadding = PaddingValues(if (isGridView) 4.dp else 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(if (isGridView) 2.dp else 4.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isGridView) 2.dp else 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredTickets) { ticket ->
                            val isSelected = ticket.id in selectedIds
                            if (isGridView) {
                                TicketCircle(
                                    ticket = ticket,
                                    digits = raffle.digits,
                                    isSelected = isSelected,
                                    showBadge = true,
                                    showName = true,
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedIds = if (isSelected) selectedIds - ticket.id else selectedIds + ticket.id
                                        } else {
                                            onTicketsAssign(listOf(ticket))
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            selectedIds = setOf(ticket.id)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                )
                            } else {
                                TicketListItem(
                                    ticket = ticket,
                                    digits = raffle.digits,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedIds = if (isSelected) selectedIds - ticket.id else selectedIds + ticket.id
                                        } else {
                                            onTicketsAssign(listOf(ticket))
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            selectedIds = setOf(ticket.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // TAB 2: MANAGEMENT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardSection(
                        raffle = raffle, 
                        stats = stats, 
                        onShareWinner = { shareWinnerImage() }
                    )

                    Text("Herramientas de Venta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onMarketingClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Image, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Publicidad")
                        }
                        Button(
                            onClick = { PdfExporter.exportRaffleToPdf(context, raffle, tickets) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Reporte PDF")
                        }
                    }

                    Text("Equipo y Sorteo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (isOwner) {
                        var showInviteDialog by remember { mutableStateOf(false) }
                        var inviteEmail by remember { mutableStateOf("") }
                        
                        if (showInviteDialog) {
                            AlertDialog(
                                onDismissRequest = { showInviteDialog = false },
                                title = { Text("Invitar Colaborador") },
                                text = {
                                    Column {
                                        Text("Ingresa el correo de la persona que te ayudará a vender:")
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = inviteEmail,
                                            onValueChange = { inviteEmail = it },
                                            label = { Text("Correo Electrónico") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        enabled = inviteEmail.isNotBlank(),
                                        onClick = {
                                            onInviteCollaborator(inviteEmail)
                                            showInviteDialog = false
                                            inviteEmail = ""
                                        }
                                    ) { Text("Enviar Invitación") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showInviteDialog = false }) { Text("Cancelar") }
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = { showInviteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Invitar a Vendedor")
                        }
                    }

                    // Collaborators List
                    if (collaborators.isNotEmpty()) {
                        Text("Vendedores Colaboradores", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        collaborators.forEach { collaborator ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = collaborator.email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "Colaborador", style = MaterialTheme.typography.labelSmall)
                                    }
                                    if (isOwner) {
                                        IconButton(onClick = { onRemoveCollaborator(collaborator) }) {
                                            Icon(Icons.Default.PersonRemove, null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isOwner && raffle.status == RaffleStatus.ACTIVE) {
                        Button(
                            onClick = onDrawWinner,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Casino, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Realizar Sorteo Final")
                        }
                    }
                }
            }
        }

        // Hidden capture box for winner card
        Box(modifier = Modifier.width(360.dp).wrapContentHeight().offset(x = 2000.dp)) {
            if (raffle.status == RaffleStatus.FINISHED) {
                ViewCaptureWrapper<View>(onViewReady = { winnerCaptureView = it }) {
                    val winnerTicket = tickets.find { it.number == raffle.winningNumber }
                    WinnerCard(raffle = raffle, winnerTicket = winnerTicket)
                }
            }
        }
    }
}

@Composable
fun DashboardSection(
    raffle: Raffle, 
    stats: RaffleDashboardStats, 
    onShareWinner: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            // Prize and Date Row (Purple Style)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF3E5F5),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PREMIO", 
                                style = MaterialTheme.typography.labelMedium, 
                                color = Color(0xFF7B1FA2), 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = "Sorteo: ${DateFormatter.format(raffle.drawDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    
                    Text(
                        text = if(raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4A148C),
                        textAlign = TextAlign.Center
                    )

                    if (raffle.status == RaffleStatus.FINISHED && raffle.winningNumber != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFF7B1FA2).copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "GANADOR: ${raffle.winningNumber.toString().padStart(raffle.digits, '0')}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF57F17)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = onShareWinner,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share, 
                                    contentDescription = "Compartir Ganador", 
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Financial and Tickets Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    label = "Vendidas",
                    value = stats.soldTickets.toString(),
                    color = Color.Black,
                    backgroundColor = Color(0xFF81C784),
                    isLarge = true
                )
                CompactInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    label = "Reservadas",
                    value = stats.reservedTickets.toString(),
                    color = Color.Black,
                    backgroundColor = Color(0xFFFFD54F),
                    isLarge = true
                )
                CompactInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.RadioButtonUnchecked,
                    label = "Disponibles",
                    value = stats.availableTickets.toString(),
                    color = Color.Black,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    isLarge = true
                )
            }
        }
    }
}

@Composable
fun CompactInfoCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    backgroundColor: Color,
    isLarge: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = if (isLarge) 8.dp else 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(if (isLarge) 16.dp else 12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label, 
                    style = MaterialTheme.typography.labelSmall, 
                    fontSize = if (isLarge) 11.sp else 8.sp, 
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = value, 
                style = MaterialTheme.typography.bodySmall, 
                fontWeight = FontWeight.Black, 
                color = color, 
                fontSize = if (isLarge) 14.sp else 10.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TicketListItem(
    ticket: Ticket,
    digits: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val formattedNumber = ticket.number.toString().padStart(digits, '0')
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual circle without name in list mode
            TicketCircle(
                ticket = ticket,
                digits = digits,
                isSelected = isSelected,
                showBadge = false,
                showName = false, // Hide name in list mode
                onClick = onClick,
                onLongClick = onLongClick,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (ticket.status == TicketStatus.AVAILABLE) "Disponible" else (ticket.customerName ?: "Sin nombre"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (ticket.status == TicketStatus.AVAILABLE) FontWeight.Normal else FontWeight.Bold,
                    color = if (ticket.status == TicketStatus.AVAILABLE) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                if (!ticket.customerPhone.isNullOrBlank()) {
                    Text(
                        text = ticket.customerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TicketCircle(
    ticket: Ticket,
    digits: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    showBadge: Boolean = true,
    showName: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        ticket.status == TicketStatus.AVAILABLE -> MaterialTheme.colorScheme.surfaceVariant
        ticket.status == TicketStatus.RESERVED -> Color(0xFFFFD54F)
        ticket.status == TicketStatus.PAID -> Color(0xFF81C784)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        ticket.status == TicketStatus.AVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.Black
    }
    
    Box(modifier = modifier.padding(1.dp), contentAlignment = Alignment.TopEnd) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = backgroundColor,
            contentColor = contentColor,
            tonalElevation = if (isSelected) 4.dp else 1.dp,
            shadowElevation = if (isSelected) { if (showBadge) 6.dp else 0.dp } else 2.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ticket.number.toString().padStart(digits, '0'),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (digits > 3) {9.sp} else 12.sp,
                        lineHeight = if (digits > 3) {10.sp} else 14.sp
                    )
                    if (showName && !ticket.customerName.isNullOrBlank()) {
                        Text(
                            text = ticket.customerName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            textAlign = TextAlign.Center,
                            lineHeight = 9.sp
                        )
                    }
                }
            }
        }
        
        if (isSelected && showBadge) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = (1).dp, y = (-1).dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
