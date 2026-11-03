package com.duoc.rotompedia.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    @Transaction
    @Query("SELECT * FROM equipos WHERE usuario = :usuario ORDER BY createdAt DESC")
    fun obtenerEquiposPorUsuario(usuario: String): Flow<List<EquipoConMiembros>>

    @Transaction
    @Query("SELECT * FROM equipos WHERE id = :equipoId AND usuario = :usuario")
    suspend fun obtenerEquipoPorId(equipoId: Long, usuario: String): EquipoConMiembros?

    @Transaction
    @Query("SELECT * FROM equipos WHERE id = :equipoId AND usuario = :usuario")
    fun observarEquipoPorId(equipoId: Long, usuario: String): Flow<EquipoConMiembros?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: EquipoPokemon): Long

    @Query("UPDATE equipos SET nombre = :nombre WHERE id = :equipoId AND usuario = :usuario")
    suspend fun renombrar(equipoId: Long, usuario: String, nombre: String)

    @Query("DELETE FROM equipos WHERE id = :equipoId AND usuario = :usuario")
    suspend fun eliminarEquipoPorId(equipoId: Long, usuario: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMiembro(miembro: MiembroEquipo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMiembros(miembros: List<MiembroEquipo>)

    @Query("SELECT * FROM miembros_equipo WHERE id = :miembroId")
    suspend fun miembro(miembroId: Long): MiembroEquipo?

    @Query("DELETE FROM miembros_equipo WHERE equipoId = :equipoId AND slot = :slot AND EXISTS (SELECT 1 FROM equipos WHERE id = :equipoId AND usuario = :usuario)")
    suspend fun eliminarMiembroPorSlot(equipoId: Long, slot: Int, usuario: String)

    @Transaction
    suspend fun guardarMiembro(usuario: String, value: MiembroEquipo, replaceSlot: Boolean): Boolean {
        if (obtenerEquipoPorId(value.equipoId, usuario) == null || value.slot !in 0..5) return false
        if (value.id != 0L) {
            val previous = miembro(value.id) ?: return false
            if (previous.equipoId != value.equipoId || previous.slot != value.slot) return false
        }
        if (replaceSlot) {
            if (value.id != 0L) return false
            eliminarMiembroPorSlot(value.equipoId, value.slot, usuario)
        }
        insertarMiembro(value)
        return true
    }
}
