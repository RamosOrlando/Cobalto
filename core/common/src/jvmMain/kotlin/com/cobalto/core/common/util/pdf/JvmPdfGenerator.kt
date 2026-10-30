package com.cobalto.core.common.util.pdf

import com.cobalto.core.common.domain.model.Resource

class JvmPdfGenerator : PdfGenerator {
    override fun generateResourcesPdf(
        resources: List<Resource>,
        quantities: Map<String, Double>,
        title: String
    ): String? {
        println("PDF generation started on JVM/Desktop for ${resources.size} items")
        return null
    }
}
