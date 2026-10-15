package com.duoc.rotompediabackend

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class GymLeader(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val region: String,
    val typeSpecialty: String,
    val badgeName: String,
    val imageUrl: String,
    val city: String = "",
    val signaturePokemon: String = "",
    val badgeImageUrl: String = "",
    val description: String = ""
)