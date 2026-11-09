package com.cobalto.core.common.util.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.cobalto.core.common.domain.model.Resource
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidPdfGenerator(private val context: Context) : PdfGenerator {

    override fun generateResourcesPdf(
        resources: List<Resource>,
        quantities: Map<String, Double>,
        title: String
    ): String? {
        val pdfDocument = PdfDocument()
        
        // Letter size in points (72 points per inch)
        // 8.5 x 11 inches -> 612 x 792 points
        val pageWidth = 612
        val pageHeight = 792
        var pageNumber = 1
        
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        val paint = Paint()
        
        val margin = 40f
        var currentY = 60f
        
        // Brand Header
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = Color.parseColor("#A04100") // IndustrialOrange
        canvas.drawText("Cobalto", margin, currentY, paint)
        
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Gestión Industrial de Materiales", margin, currentY + 15f, paint)
        
        currentY += 60f
        
        // Document Title
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 18f
        paint.color = Color.BLACK
        canvas.drawText(title, margin, currentY, paint)
        
        currentY += 25f
        
        // Date and Info
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = Color.DKGRAY
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado el: $dateStr", margin, currentY, paint)
        canvas.drawText("Total materiales: ${resources.size}", margin + 400f, currentY, paint)
        
        currentY += 40f
        
        // Table Header Background
        paint.color = Color.parseColor("#F0F0F0")
        canvas.drawRect(margin, currentY - 15f, pageWidth - margin, currentY + 10f, paint)
        
        // Table Header
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.BLACK
        paint.textSize = 10f
        canvas.drawText("MATERIAL", margin + 5f, currentY, paint)
        canvas.drawText("UNIDAD", margin + 200f, currentY, paint)
        canvas.drawText("CANT.", margin + 280f, currentY, paint)
        canvas.drawText("P. UNIT.", margin + 350f, currentY, paint)
        canvas.drawText("TOTAL", margin + 460f, currentY, paint)
        
        currentY += 30f
        
        // Rows
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 9f
        
        var grandTotal = 0.0
        
        for (material in resources) {
            // Check for new page
            if (currentY > pageHeight - margin - 40f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin + 20f
                
                // Re-draw header on new page
                paint.color = Color.parseColor("#F0F0F0")
                canvas.drawRect(margin, currentY - 15f, pageWidth - margin, currentY + 10f, paint)
                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText("MATERIAL (cont.)", margin + 5f, currentY, paint)
                canvas.drawText("UNIDAD", margin + 200f, currentY, paint)
                canvas.drawText("CANT.", margin + 280f, currentY, paint)
                canvas.drawText("P. UNIT.", margin + 350f, currentY, paint)
                canvas.drawText("TOTAL", margin + 460f, currentY, paint)
                currentY += 30f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            
            val unit = material.unit
            val quantity = quantities[material.resourceId] ?: 1.0
            val price = material.price ?: 0.0
            val rowTotal = quantity * price
            grandTotal += rowTotal
            
            val priceStr = "$${String.format("%.2f", price)}"
            val totalStr = "$${String.format("%.2f", rowTotal)}"
            
            // Alternating row background
            if (resources.indexOf(material) % 2 != 0) {
                paint.color = Color.parseColor("#FAFAFA")
                canvas.drawRect(margin, currentY - 12f, pageWidth - margin, currentY + 5f, paint)
            }
            
            paint.color = Color.BLACK
            canvas.drawText(material.name.take(30), margin + 5f, currentY, paint)
            canvas.drawText(unit, margin + 200f, currentY, paint)
            canvas.drawText(quantity.toString(), margin + 280f, currentY, paint)
            canvas.drawText(priceStr, margin + 350f, currentY, paint)
            
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = Color.parseColor("#A04100") // IndustrialOrange
            canvas.drawText(totalStr, margin + 460f, currentY, paint)
            
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.color = Color.BLACK
            
            currentY += 22f
            
            // Draw a subtle line between rows
            paint.color = Color.LTGRAY
            paint.strokeWidth = 0.5f
            canvas.drawLine(margin, currentY - 15f, pageWidth - margin, currentY - 15f, paint)
        }

        // Grand Total Section
        currentY += 10f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 12f
        val grandTotalStr = "TOTAL GENERAL: $${String.format("%.2f", grandTotal)}"
        canvas.drawText(grandTotalStr, margin + 350f, currentY, paint)
        
        // Footer
        paint.textSize = 8f
        paint.color = Color.GRAY
        canvas.drawText("Página $pageNumber", pageWidth / 2f - 20f, pageHeight - 20f, paint)
        
        pdfDocument.finishPage(page)
        
        val fileName = "Cotizacion_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF Generado con éxito", Toast.LENGTH_LONG).show()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        } finally {
            pdfDocument.close()
        }
    }
}
