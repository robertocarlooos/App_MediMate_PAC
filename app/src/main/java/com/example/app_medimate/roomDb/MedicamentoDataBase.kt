package com.example.app_medimate.roomDb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Medicamentos::class,Recordatorio::class],
    version = 2
)

abstract class MedicamentoDataBase: RoomDatabase() {
abstract val dao: MedicamentosDao
abstract val recordatorioDao: RecordatorioDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `Recordatorio` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `medicamentoId` INTEGER NOT NULL, `hora` TEXT NOT NULL, `mensaje` TEXT NOT NULL, FOREIGN KEY(`medicamentoId`) REFERENCES `Medicamentos`(`id`) ON DELETE CASCADE)"
                )
            }
        }
    }
}