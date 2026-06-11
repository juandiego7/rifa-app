package com.afelix.rifaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y Privacidad") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text("Términos y Condiciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "1. Propósito: Rifa App es una herramienta tecnológica para la gestión de sorteos personales y colaborativos. El usuario es el único responsable de la legalidad de los sorteos que organice.\n" +
                "2. Uso de la Plataforma: El organizador se compromete a cumplir con las leyes locales vigentes sobre juegos de azar y rifas en su jurisdicción.\n" +
                "3. Responsabilidad: AFELIX no se hace responsable por el incumplimiento en la entrega de premios, disputas entre organizadores y compradores, o mal manejo de los fondos recaudados.\n" +
                "4. Colaboración: Al invitar a un colaborador, el dueño de la rifa asume la responsabilidad por las acciones de venta que este realice dentro de la aplicación.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(24.dp))

            Text("Política de Privacidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "1. Datos Recopilados: Recopilamos su nombre y correo electrónico a través de Google para identificar su cuenta y sincronizar sus rifas en la nube.\n" +
                "2. Datos de Clientes: Los números de teléfono y nombres de los compradores que usted ingrese son almacenados localmente y en la nube de Firebase con el único fin de gestionar su rifa.\n" +
                "3. Seguridad: Implementamos medidas de seguridad para proteger su información, pero no podemos garantizar seguridad absoluta frente a ataques externos.\n" +
                "4. Publicidad: La aplicación utiliza Google AdMob para mostrar anuncios. Google puede recopilar identificadores de publicidad para personalizar los anuncios que usted ve.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(32.dp))
            Text(
                "Al utilizar Rifa App, usted acepta los términos aquí descritos.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
