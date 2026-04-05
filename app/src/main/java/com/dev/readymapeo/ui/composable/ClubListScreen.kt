package com.dev.readymapeo.ui.composable

import ads_mobile_sdk.h6
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
            LazyColumn {
                items(viewModel.clubs) { club ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(club.name)
                            Text("${club.postalCode} ${club.city}")

                        }
                    }
                }
            }
        }
    }
}

