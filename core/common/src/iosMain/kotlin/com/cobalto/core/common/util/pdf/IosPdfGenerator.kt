package com.cobalto.core.common.util.pdf

import com.cobalto.core.common.domain.model.Resource

class IosPdfGenerator : PdfGenerator {
    override fun generateResourcesPdf(
        resources: List<Resource>,
        quantities: Map<String, Double>,
        title: String
    ): String? {
        // Implementation for iOS using UIGraphicsPDFRenderer or similar
        // For now, a placeholder that prints to console
        println("PDF generation started on iOS for ${resources.size} items")
        return null
    }
}
