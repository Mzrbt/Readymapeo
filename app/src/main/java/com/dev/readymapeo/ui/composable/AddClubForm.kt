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
fun AddClubForm(viewModel: ClubViewModel) {
    val name = remember { mutableStateOf("") }
    val street = remember { mutableStateOf("") }
    val city = remember { mutableStateOf("") }
    val postalCode = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val ffsoId = remember { mutableStateOf("") }
    val feedback = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(false) }

    Column {
        Row { TextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Nom") }) }
        Row { TextField(value = street.value, onValueChange = { street.value = it }, label = { Text("Adresse") }) }
        Row { TextField(value = city.value, onValueChange = { city.value = it }, label = { Text("Ville") }) }
        Row { TextField(value = postalCode.value, onValueChange = { postalCode.value = it }, label = { Text("Code postal") }) }
        Row { TextField(value = description.value, onValueChange = { description.value = it }, label = { Text("Description") }) }
        Row { TextField(value = ffsoId.value, onValueChange = { ffsoId.value = it }, label = { Text("Identifiant FFSO") }) }

        Button(
            enabled = !isLoading.value,
            onClick = {
                if (name.value.isBlank() || street.value.isBlank() ||
                    city.value.isBlank() || postalCode.value.isBlank()
                ) {
                    feedback.value = "Veuillez remplir tous les champs obligatoires."
                    return@Button
                }
                isLoading.value = true
                viewModel.addClub(
                    name.value, street.value, city.value,
                    postalCode.value, description.value, ffsoId.value
                ) { success ->
                    isLoading.value = false
                    feedback.value = if (success) "Club ajouté avec succès !" else "Erreur lors de l'ajout."
                }
            }
        ) {
            Text(if (isLoading.value) "Envoi…" else "Ajouter")
        }

        feedback.value?.let { Text(it) }
    }
}