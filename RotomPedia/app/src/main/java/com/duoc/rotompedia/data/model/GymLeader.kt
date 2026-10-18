package com.duoc.rotompedia.data.model

/**
 * Modelo de Líder de Gimnasio para uso local (sin red).
 */
data class GymLeader(
    val name: String,
    val region: String,
    val typeSpecialty: String,
    val badgeName: String,
    val city: String = "",
    val signaturePokemon: String = "",
    val imageUrl: String = "",
    val description: String = ""
)