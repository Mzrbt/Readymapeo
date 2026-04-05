package com.dev.readymapeo.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.dev.readymapeo.ui.viewmodels.ClubViewModel

@Composable
fun AddClubForm(viewModel: ClubViewModel){
    val name = remember { mutableStateOf("") }
    val street = remember { mutableStateOf("") }
    val city = remember { mutableStateOf("") }
    val postalCode = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val ffsoId = remember { mutableStateOf("") }



    Column() {
        Row() {
            TextField(value = name.value, onValueChange = {name.value = it}, label = { Text("Nom") })
        }
        Row() {
            TextField(value = street.value, onValueChange = {street.value = it}, label = { Text("Adresse") })
        }
        Row() {
            TextField(value = city.value, onValueChange = {city.value = it}, label = { Text("Ville") })
        }
        Row() {
            TextField(value = postalCode.value, onValueChange = {postalCode.value = it}, label = { Text("Code postal") })
        }
        Row() {
            TextField(value = description.value, onValueChange = {description.value = it}, label = { Text("Description") })
        }
        Row() {
            TextField(value = ffsoId.value, onValueChange = {ffsoId.value = it}, label = { Text("Identifiant FFSO") })
        }

        Button(onClick = {}) {
            Text("Ajouter")
        }

    }
}