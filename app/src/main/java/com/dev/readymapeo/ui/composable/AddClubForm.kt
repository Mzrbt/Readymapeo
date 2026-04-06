package com.dev.readymapeo.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nouveau Club",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Nom *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = street.value,
            onValueChange = { street.value = it },
            label = { Text("Adresse *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = postalCode.value,
                onValueChange = { postalCode.value = it },
                label = { Text("Code postal *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text("Ville *") },
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = ffsoId.value,
            onValueChange = { ffsoId.value = it },
            label = { Text("Identifiant FFSO") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

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

                    if (success) {
                        name.value = ""
                        street.value = ""
                        city.value = ""
                        postalCode.value = ""
                        description.value = ""
                        ffsoId.value = ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isLoading.value) "Enregistrement…" else "Ajouter le club",
                fontWeight = FontWeight.Bold
            )
        }

        feedback.value?.let { msg ->
            val isError = msg.contains("Erreur") || msg.contains("Veuillez")
            Text(
                text = msg,
                color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}