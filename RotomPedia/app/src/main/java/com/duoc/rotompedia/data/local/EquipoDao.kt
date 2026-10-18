package com.duoc.rotompedia.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    @Transaction
    @Query("SELECT * FROM equipos WHERE usuario = :usuario ORDER BY createdAt DESC")
    fun obtenerEquiposPorUsuario(usuario: String): Flow<List<EquipoConMiembros>>

    @Transaction
    @Query("SELECT * FROM equipos WHERE id = :equipoId")
    suspend fun obtenerEquipoPorId(equipoId: Long): EquipoConMiembros?

    @Transaction
    @Query("SELECT * FROM equipos WHERE id = :equipoId")
    fun observarEquipoPorId(equipoId: Long): Flow<EquipoConMiembros?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: EquipoPokemon): Long

    @Update
    suspend fun actualizarEquipo(equipo: EquipoPokemon)

    @Delete
    suspend fun eliminarEquipo(equipo: EquipoPokemon)

    @Query("DELETE FROM equipos WHERE id = :equipoId")
    suspend fun eliminarEquipoPorId(equipoId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMiembro(miembro: MiembroEquipo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMiembros(miembros: List<MiembroEquipo>)

    @Query("DELETE FROM miembros_equipo WHERE id = :miembroId")
    suspend fun eliminarMiembro(miembroId: Long)

    @Query("DELETE FROM miembros_equipo WHERE equipoId = :equipoId AND slot = :slot")
    suspend fun eliminarMiembroPorSlot(equipoId: Long, slot: Int)

    @Query("DELETE FROM miembros_equipo WHERE equipoId = :equipoId")
    suspend fun eliminarTodosLosMiembros(equipoId: Long)
}
