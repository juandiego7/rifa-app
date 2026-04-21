package com.afelix.rifaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            RaffleDatabase::class.java, "rifa-db"
        ).build()
        
        val repository = RaffleRepositoryImpl(db.raffleDao)
        val viewModelFactory = RifaViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            RifaAppTheme {
                val navController = rememberNavController()
                val viewModel: RifaViewModel = viewModel(factory = viewModelFactory)
                
                var ticketToAssign by remember { mutableStateOf<Ticket?>(null) }

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
                            onTicketClick = { ticket ->
                                ticketToAssign = ticket
                            }
                        )
                        
                        ticketToAssign?.let { ticket ->
                            TicketAssignmentDialog(
                                ticket = ticket,
                                onDismiss = { ticketToAssign = null },
                                onConfirm = { updatedTicket ->
                                    viewModel.updateTicket(updatedTicket)
                                    ticketToAssign = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
