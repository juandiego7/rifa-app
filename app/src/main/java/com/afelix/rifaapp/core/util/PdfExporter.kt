package com.afelix.rifaapp.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.afelix.rifaapp.domain.model.Raffle
import com.afelix.rifaapp.domain.model.Ticket
import com.afelix.rifaapp.domain.model.TicketStatus
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun exportRaffleToPdf(context: Context, raffle: Raffle, tickets: List<Ticket>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val textPaint = Paint()
        
        val startX = 40f
        var currentY = 50f
        
        // --- Page 1 ---
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        
        // Header
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 20f
        textPaint.color = Color.BLACK
        canvas.drawText("REPORTE DE RIFA", startX, currentY, textPaint)
        
        currentY += 30f
        textPaint.textSize = 16f
        canvas.drawText(raffle.title, startX, currentY, textPaint)
        
        currentY += 25f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 12f
        textPaint.color = Color.DKGRAY
        canvas.drawText("Fecha de Sorteo: ${DateFormatter.format(raffle.drawDate)}", startX, currentY, textPaint)
        
        currentY += 20f
        val prize = if (raffle.prizeValue > 0) CurrencyFormatter.format(raffle.prizeValue) else raffle.description
        canvas.drawText("Premio: $prize", startX, currentY, textPaint)
        
        currentY += 30f
        paint.color = Color.LTGRAY
        canvas.drawLine(startX, currentY, 555f, currentY, paint)
        
        // Summary
        currentY += 25f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 14f
        textPaint.color = Color.BLACK
        canvas.drawText("Resumen de Ventas", startX, currentY, textPaint)
        
        currentY += 20f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 12f
        val sold = tickets.count { it.status == TicketStatus.PAID }
        val reserved = tickets.count { it.status == TicketStatus.RESERVED }
        val totalAmount = raffle.ticketValue * sold
        
        canvas.drawText("Boletas Pagadas: $sold", startX, currentY, textPaint)
        currentY += 15f
        canvas.drawText("Boletas Reservadas: $reserved", startX, currentY, textPaint)
        currentY += 15f
        canvas.drawText("Recaudado Total: ${CurrencyFormatter.format(totalAmount)}", startX, currentY, textPaint)
        
        currentY += 30f
        paint.color = Color.LTGRAY
        canvas.drawLine(startX, currentY, 555f, currentY, paint)
        
        // Table Header
        currentY += 30f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("No.", startX, currentY, textPaint)
        canvas.drawText("Cliente", startX + 50f, currentY, textPaint)
        canvas.drawText("Teléfono", startX + 250f, currentY, textPaint)
        canvas.drawText("Estado", startX + 420f, currentY, textPaint)
        
        currentY += 10f
        canvas.drawLine(startX, currentY, 555f, currentY, paint)
        
        // Data
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val assignedTickets = tickets.filter { it.status != TicketStatus.AVAILABLE }
            .sortedBy { it.number }
            
        var pageNumber = 1
        for (ticket in assignedTickets) {
            currentY += 20f
            
            if (currentY > 800f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
                
                // Re-draw table header on new page
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("No.", startX, currentY, textPaint)
                canvas.drawText("Cliente", startX + 50f, currentY, textPaint)
                canvas.drawText("Teléfono", startX + 250f, currentY, textPaint)
                canvas.drawText("Estado", startX + 420f, currentY, textPaint)
                currentY += 10f
                canvas.drawLine(startX, currentY, 555f, currentY, paint)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                currentY += 20f
            }
            
            val numStr = ticket.number.toString().padStart(raffle.digits, '0')
            canvas.drawText(numStr, startX, currentY, textPaint)
            canvas.drawText(ticket.customerName ?: "S/N", startX + 50f, currentY, textPaint)
            canvas.drawText(ticket.customerPhone ?: "-", startX + 250f, currentY, textPaint)
            
            val statusText = if (ticket.status == TicketStatus.PAID) "PAGADO" else "RESERVADO"
            textPaint.color = if (ticket.status == TicketStatus.PAID) Color.parseColor("#2E7D32") else Color.parseColor("#E65100")
            canvas.drawText(statusText, startX + 420f, currentY, textPaint)
            textPaint.color = Color.BLACK
        }
        
        pdfDocument.finishPage(page)
        
        try {
            val fileName = "Reporte_${raffle.title.replace(" ", "_")}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            sharePdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte PDF"))
    }
}
