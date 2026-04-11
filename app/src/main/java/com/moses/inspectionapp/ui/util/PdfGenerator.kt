package com.moses.inspectionapp.ui.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.Inspection
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generateInspectionPdf(
        context: Context,
        inspection: Inspection,
        facility: Facility?,
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 14f }

        var y = 40f
        fun line(text: String) {
            canvas.drawText(text, 40f, y, paint)
            y += 22f
        }

        line("Inspection Report")
        line("Facility: ${inspection.facilityName}")
        line("TIN: ${facility?.tin ?: "-"}")
        line("Visit Type: ${enumLabel(inspection.visitType.name)}")
        line("Decision: ${enumLabel(inspection.decision.name)}")
        line("Faults: ${inspection.faultCount}")
        line("Adjustment: ${inspection.adjustmentAmount} RWF")
        line("Total Fine: ${inspection.totalFine} RWF")
        if (inspection.comments.isNotBlank()) {
            line("Comments: ${inspection.comments}")
        }
        if (inspection.recommendations.isNotBlank()) {
            line("Recommendations: ${inspection.recommendations}")
        }

        document.finishPage(page)
        val file = File(context.cacheDir, "inspection_${inspection.id}.pdf")
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()
        return file
    }
}
