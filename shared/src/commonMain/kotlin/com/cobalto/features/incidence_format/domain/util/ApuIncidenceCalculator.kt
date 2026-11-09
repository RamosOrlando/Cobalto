package com.cobalto.features.incidence_format.domain.util

import com.cobalto.features.incidence_format.domain.model.IncidenceFormat

data class EvaluatedIncidence(
    val incidence: IncidenceFormat,
    val calculatedValue: Double
)

object ApuIncidenceCalculator {

    /**
     * Evaluates a list of [IncidenceFormat] rules against the APU section subtotals.
     * Guarantees evaluation in topological/sequential order of [incidenceId] ("A", "B", "C"... "M").
     *
     * @param subtotalMaterials Subtotal from Section 1 (Materiales).
     * @param subtotalLabor Subtotal from Section 2 (Mano de Obra).
     * @param subtotalEquipment Subtotal from Section 3 (Herramientas y Equipo).
     * @param incidences List of incidence rules from IncidenceFormat table.
     * @return List of [EvaluatedIncidence] containing the evaluated monetary values.
     */
    fun calculateIncidences(
        subtotalMaterials: Double,
        subtotalLabor: Double,
        subtotalEquipment: Double,
        incidences: List<IncidenceFormat>
    ): List<EvaluatedIncidence> {
        if (incidences.isEmpty()) {
            return emptyList()
        }

        // Sort by incidenceId ("A", "B", "C", "D", "E", "F"...) to guarantee
        // base values ("A", "B", "C") are computed BEFORE dependent formulas ("E", "F", "G"...)
        val sortedIncidences = incidences.sortedBy { it.incidenceId.uppercase() }

        val valueMap = mutableMapOf<String, Double>()
        val evaluatedMap = mutableMapOf<String, EvaluatedIncidence>()

        sortedIncidences.forEach { item ->
            val id = item.incidenceId.uppercase()
            val value = when {
                item.formula.trim() == "1" -> {
                    when (id) {
                        "A" -> subtotalMaterials
                        "B" -> subtotalLabor
                        "C" -> subtotalEquipment
                        else -> {
                            when (item.codigo.uppercase()) {
                                "SUBT-MAT" -> subtotalMaterials
                                "SUBT-MO" -> subtotalLabor
                                "SUBT-HE" -> subtotalEquipment
                                else -> 0.0
                            }
                        }
                    }
                }
                else -> {
                    val tokens = item.formula.split("+").map { it.trim().uppercase() }
                    val baseSum = tokens.sumOf { token ->
                        valueMap[token] ?: 0.0
                    }
                    val factor = if (item.percent > 0.0) item.percent / 100.0 else 1.0
                    baseSum * factor
                }
            }

            valueMap[id] = value
            evaluatedMap[id] = EvaluatedIncidence(incidence = item, calculatedValue = value)
        }

        return sortedIncidences.mapNotNull { evaluatedMap[it.incidenceId.uppercase()] }
    }
}
