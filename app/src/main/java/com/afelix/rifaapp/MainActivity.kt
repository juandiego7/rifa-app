package com.afelix.rifaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.afelix.rifaapp.data.local.RaffleDatabase
import com.afelix.rifaapp.data.repository.RaffleRepositoryImpl
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.ui.screens.*
import com.afelix.rifaapp.ui.theme.RifaAppTheme
import com.afelix.rifaapp.ui.viewmodel.RifaViewModel
import com.afelix.rifaapp.ui.viewmodel.RifaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
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
                
                var ticketsToAssign by remember { mutableStateOf<List<Ticket>?>(null) }
                var ticketsToPreview by remember { mutableStateOf<List<Ticket>?>(null) }

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        val raffles by viewModel.raffles.collectAsState()
                        RaffleListScreen(
                            raffles = raffles,
                            onRaffleClick = { raffle ->
                                viewModel.selectRaffle(raffle)
                                navController.navigate("detail")
                            },
                            onCreateRaffleClick = {
                                navController.navigate("create")
                            },
                            onDeleteRaffle = { raffle ->
                                viewModel.deleteRaffle(raffle)
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
                        
                        RaffleDetailScreen(
                            raffle = raffle,
                            tickets = tickets,
                            stats = stats,
                            onBack = { navController.popBackStack() },
                            onTicketsAssign = { selectedTickets ->
                                ticketsToAssign = selectedTickets
                            }
                        )
                        
                        ticketsToAssign?.let { selectedTickets ->
                            TicketAssignmentDialog(
                                tickets = selectedTickets,
                                digits = raffle?.digits ?: 2,
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
                }
            }
        }
    }
}
