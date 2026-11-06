package com.cobalto.core.database.decimal_format

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DecimalFormatDao {
    @Query("SELECT * FROM DecimalFormat ORDER BY codigo ASC")
    fun getAllDecimalFormats(): Flow<List<DecimalFormatEntity>>

    @Query("SELECT * FROM DecimalFormat WHERE codigo = :codigo LIMIT 1")
    suspend fun getDecimalFormatByCodigo(codigo: String): DecimalFormatEntity?

    @Query("SELECT * FROM DecimalFormat WHERE decimalId = :decimalId LIMIT 1")
    suspend fun getDecimalFormatById(decimalId: String): DecimalFormatEntity?

    @Upsert
    suspend fun insertDecimalFormats(formats: List<DecimalFormatEntity>)

    @Upsert
    suspend fun insertDecimalFormat(format: DecimalFormatEntity)

    @Query("DELETE FROM DecimalFormat WHERE decimalId = :decimalId")
    suspend fun deleteDecimalFormat(decimalId: String)

    @Query("DELETE FROM DecimalFormat")
    suspend fun clearAll()
}
