package com.dev.readymapeo.ui.composable

import ads_mobile_sdk.h6
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.dev.readymapeo.ui.viewmodels.ClubViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubListScreen(viewModel: ClubViewModel, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("ReadyMapeo - Clubs") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(onClick = { viewModel.sync() }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Synchroniser (GET)")
            }
            val syncMessage = viewModel.syncMessage.value

            if (syncMessage != null) {
                Toast.makeText(LocalContext.current, syncMessage, Toast.LENGTH_LONG).show()
                viewModel.clearSyncMessage()
            }
            LazyColumn {
                items(viewModel.clubs) { club ->
                    Card( /* tes modifiers */ ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = club.name) // Affichage normal du club


                            if (club.isDirty) {
                                Text(
                                    text = "Non synchronisé",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            else if (club.id < 0) {
                                Text(
                                    text = "En attente d'acceptation",
                                    color = Color.Blue,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

