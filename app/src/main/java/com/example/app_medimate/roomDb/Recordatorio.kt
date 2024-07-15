package com.example.app_medimate.roomDb


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Medicamentos::class,
        parentColumns = ["id"],
        childColumns = ["medicamentoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Recordatorio(
    val medicamentoId: Int,
    val hora: String,
    val minutos : String,
    val frequncia: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)