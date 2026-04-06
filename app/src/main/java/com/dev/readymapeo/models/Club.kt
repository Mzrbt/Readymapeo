package com.dev.readymapeo.models

data class Club(
    val id: Int,
    val name: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val description: String,
    val ffsoId: String = "",
    val isDirty: Boolean = false
)
