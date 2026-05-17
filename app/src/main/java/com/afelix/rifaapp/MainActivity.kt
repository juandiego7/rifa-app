package com.afelix.rifaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.afelix.rifaapp.core.util.PdfExporter
import com.afelix.rifaapp.data.local.RaffleDatabase
import com.afelix.rifaapp.data.repository.RaffleRepositoryImpl
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.ui.components.AdBanner
import com.afelix.rifaapp.ui.screens.*
import com.afelix.rifaapp.ui.theme.RifaAppTheme
import com.afelix.rifaapp.ui.viewmodel.AuthState
import com.afelix.rifaapp.ui.viewmodel.AuthViewModel
import com.afelix.rifaapp.ui.viewmodel.RifaViewModel
import com.afelix.rifaapp.ui.viewmodel.RifaViewModelFactory
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        val db = Room.databaseBuilder(
            applicationContext,
            RaffleDatabase::class.java, "rifa-db"
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
        
        val repository = RaffleRepositoryImpl(db.raffleDao)
        val viewModelFactory = RifaViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            RifaAppTheme {
                val navController = rememberNavController()
                val viewModel: RifaViewModel = viewModel(factory = viewModelFactory)
                val authViewModel: AuthViewModel = viewModel()
                
                val authState by authViewModel.authState.collectAsState()
                val startDestination = if (authState is AuthState.Authenticated || authState is AuthState.Guest) "list" else "auth"

                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated) {
                        viewModel.onUserLogin()
                    } else if (authState is AuthState.Guest || authState is AuthState.Initial) {
                        viewModel.onUserLogout()
                    }
                }

                var ticketsToAssign by remember { mutableStateOf<List<Ticket>?>(null) }
                var ticketsToPreview by remember { mutableStateOf<List<Ticket>?>(null) }

                // Determine if we should show the ad banner
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route
                // Show banner on all main routes except Auth
                val showAdBanner = currentRoute != "auth"

                Scaffold(
                    modifier = Modifier.navigationBarsPadding(),
                    bottomBar = {
                        if (showAdBanner && currentRoute != "auth") {
                            AdBanner()
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController, 
                        startDestination = startDestination,
                        modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
                    ) {
                        composable("auth") {
                            AuthScreen(
                                viewModel = authViewModel,
                                onAuthSuccess = {
                                    viewModel.onUserLogin() // Actualiza filtro y sincroniza
                                    navController.navigate("list") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("list") {
                            val raffles by viewModel.raffles.collectAsState()
                            val isRefreshing by viewModel.isRefreshing.collectAsState()
                            
                            RaffleListScreen(
                                raffles = raffles,
                                authViewModel = authViewModel,
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.syncAllToCloud() },
                                onRaffleClick = { raffle ->
                                    viewModel.selectRaffle(raffle)
                                    navController.navigate("detail")
                                },
                                onCreateRaffleClick = {
                                    navController.navigate("create")
                                },
                                onDeleteRaffle = { raffle ->
                                    viewModel.deleteRaffle(raffle)
                                },
                                onLogout = {
                                    authViewModel.signOut(applicationContext)
                                    navController.navigate("auth") {
                                        popUpTo("list") { inclusive = true }
                                    }
                                },
                                onJoinRaffle = { code ->
                                    viewModel.joinRaffle(code)
                                },
                                pendingInvitations = viewModel.pendingInvitations.collectAsState().value,
                                onInvitationResponse = { id, accept ->
                                    viewModel.respondToInvitation(id, accept)
                                },
                                onQuickPdf = { raffle ->
                                    // Sincronizamos tickets y exportamos
                                    viewModel.selectRaffle(raffle)
                                    PdfExporter.exportRaffleToPdf(applicationContext, raffle, viewModel.tickets.value)
                                },
                                onQuickMarketing = { raffle ->
                                    viewModel.selectRaffle(raffle)
                                    navController.navigate("marketing")
                                }
                            )
                        }
                        composable("create") {
                            CreateRaffleScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { raffle ->
                                    viewModel.createRaffle(raffle)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("detail") {
                            val raffle by viewModel.selectedRaffle.collectAsState()
                            val tickets by viewModel.tickets.collectAsState()
                            val stats by viewModel.stats.collectAsState()
                            var showDrawWinner by remember { mutableStateOf(false) }
                            
                            RaffleDetailScreen(
                                raffle = raffle,
                                tickets = tickets,
                                stats = stats,
                                onBack = { navController.popBackStack() },
                                onTicketsAssign = { selectedTickets ->
                                    ticketsToAssign = selectedTickets
                                },
                                onTicketsShare = { selectedTickets ->
                                    ticketsToPreview = selectedTickets
                                },
                                onMarketingClick = {
                                    navController.navigate("marketing")
                                },
                                onDrawWinner = {
                                    showDrawWinner = true
                                },
                                onInviteCollaborator = { email ->
                                    val raffle = viewModel.selectedRaffle.value
                                    if (raffle?.cloudId != null) {
                                        viewModel.sendInvitation(raffle.cloudId, raffle.title, email)
                                    }
                                },
                                collaborators = viewModel.collaborators.collectAsState().value,
                                onRemoveCollaborator = { collab ->
                                    val raffle = viewModel.selectedRaffle.value
                                    if (raffle?.cloudId != null) {
                                        viewModel.removeCollaborator(raffle.cloudId, collab.uid, collab.email)
                                    }
                                }
                            )

                            if (showDrawWinner && raffle != null) {
                                DrawWinnerDialog(
                                    raffle = raffle!!,
                                    onDismiss = { showDrawWinner = false },
                                    onConfirm = { winnerNumber ->
                                        viewModel.setWinningNumber(raffle!!, winnerNumber)
                                        showDrawWinner = false
                                    }
                                )
                            }
                            
                            ticketsToAssign?.let { selectedTickets ->
                                TicketAssignmentDialog(
                                    raffle = raffle!!,
                                    tickets = selectedTickets,
                                    onDismiss = { ticketsToAssign = null },
                                    onConfirm = { updatedTickets ->
                                        viewModel.updateTickets(updatedTickets)
                                        ticketsToAssign = null
                                        // Solo mostramos el ticket si se asignó a alguien (no si se liberó)
                                        if (updatedTickets.any { it.status != com.afelix.rifaapp.domain.model.TicketStatus.AVAILABLE }) {
                                            ticketsToPreview = updatedTickets
                                        }
                                    }
                                )
                            }

                            if (ticketsToPreview != null && raffle != null) {
                                TicketPreviewDialog(
                                    raffle = raffle!!,
                                    tickets = ticketsToPreview!!,
                                    onDismiss = { ticketsToPreview = null }
                                )
                            }
                        }
                        composable("marketing") {
                            val raffle by viewModel.selectedRaffle.collectAsState()
                            val tickets by viewModel.tickets.collectAsState()
                            
                            MarketingScreen(
                                raffle = raffle,
                                tickets = tickets,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
