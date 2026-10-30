package com.cobalto.core.common.util.pdf

import com.cobalto.core.common.domain.model.Resource

interface PdfGenerator {
    fun generateResourcesPdf(
        resources: List<Resource>,
        quantities: Map<String, Double> = emptyMap(),
        title: String = "Listado de Materiales Seleccionados"
    ): String?
}
