package com.example.app_medimate.roomDb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medicamentos(
    val nomeMedicamento:String,
    val dosis: String,
    val frecuencia: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
