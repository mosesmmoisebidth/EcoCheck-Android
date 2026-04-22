package com.moses.inspectionapp.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.caverock.androidsvg.SVG
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.data.model.toReportLabel
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object PdfGenerator {
    data class QuestionAnswer(
        val number: Int,
        val question: String,
        val answer: String,
        val marksAwarded: Int,
        val maxMarks: Int,
    )

    private data class TocItem(
        val title: String,
        val pageLabel: String,
    )

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val PAGE_MARGIN = 40f
    private const val FOOTER_LINE_Y = PAGE_HEIGHT - 34f
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - 56f
    private const val LINE_GAP = 18f

    fun generateInspectionPdf(
        context: Context,
        inspection: Inspection,
        facility: Facility?,
        inspectionTypeName: String,
        questionAnswers: List<QuestionAnswer>,
    ): File {
        val document = PdfDocument()
        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
        )
        var canvas = page.canvas
        var y = PAGE_MARGIN

        val coatOfArmsBitmap = loadCoatOfArmsBitmap(context = context, targetWidth = 58, targetHeight = 58)

        val bannerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val bannerSubtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D8E4FF")
            textSize = 11.5f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F2E66")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            textSize = 10.8f
        }
        val bodyStrongPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 10.9f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5B6B88")
            textSize = 9.6f
        }
        val tocPagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F2E66")
            textSize = 10.6f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val answerYesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E9F6E")
            textSize = 9.8f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val answerNoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C0392B")
            textSize = 9.8f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D9E1EE")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val sectionChipFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EAF1FB")
            style = Paint.Style.FILL
        }
        val tocCardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        val rowFillA = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        val rowFillB = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F6F8FC")
            style = Paint.Style.FILL
        }
        val tableHeaderFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EAF1FB")
            style = Paint.Style.FILL
        }
        val noteCardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFD")
            style = Paint.Style.FILL
        }
        val metricsCardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFD")
            style = Paint.Style.FILL
        }
        val pageLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D9E1EE")
            strokeWidth = 1f
        }
        val tocDotsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9BB0CF")
            strokeWidth = 1f
        }

        val compliantCount = questionAnswers.count { it.answer.equals("Yes", ignoreCase = true) }
        val totalEarnedMarks = questionAnswers.sumOf { it.marksAwarded }
        val totalPossibleMarks = questionAnswers.sumOf { it.maxMarks }
        val scoreOutOf100 = if (totalPossibleMarks == 0) 0 else {
            ((totalEarnedMarks.toFloat() / totalPossibleMarks.toFloat()) * 100f).roundToInt()
        }

        val questionPages = estimateQuestionSectionPages(
            items = questionAnswers,
            bodyPaint = bodyPaint,
            answerYesPaint = answerYesPaint,
            answerNoPaint = answerNoPaint,
        )
        val notesPage = 4 + questionPages
        val tocItems = listOf(
            TocItem(title = "Facility Details", pageLabel = "2"),
            TocItem(title = "Inspection Summary", pageLabel = "3"),
            TocItem(title = "Questions and Responses", pageLabel = "4"),
            TocItem(title = "Inspector Notes", pageLabel = notesPage.toString()),
        )

        fun drawFooter() {
            canvas.drawLine(PAGE_MARGIN, FOOTER_LINE_Y, PAGE_WIDTH - PAGE_MARGIN, FOOTER_LINE_Y, pageLinePaint)
            val footer = "Generated by EcoCheck - Page $pageNumber"
            canvas.drawText(footer, PAGE_MARGIN, PAGE_HEIGHT - 18f, mutedPaint)
        }

        fun finishCurrentPage() {
            drawFooter()
            document.finishPage(page)
        }

        fun drawPageSectionHeader(sectionTitle: String) {
            val chipTop = y
            val chipBottom = chipTop + 26f
            val chipRect = RectF(PAGE_MARGIN, chipTop, PAGE_WIDTH - PAGE_MARGIN, chipBottom)
            canvas.drawRoundRect(chipRect, 10f, 10f, sectionChipFill)
            canvas.drawRoundRect(chipRect, 10f, 10f, borderPaint)
            canvas.drawText(sectionTitle, PAGE_MARGIN + 12f, chipTop + 17f, sectionPaint)
            y = chipBottom + 12f
        }

        fun newPage(sectionTitle: String? = null) {
            finishCurrentPage()
            pageNumber += 1
            page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
            )
            canvas = page.canvas
            y = PAGE_MARGIN
            if (!sectionTitle.isNullOrBlank()) {
                drawPageSectionHeader(sectionTitle)
            }
        }

        fun drawFirstPageBanner() {
            val top = y
            val bottom = top + 94f
            val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F2E66")
                style = Paint.Style.FILL
            }
            val bannerRect = RectF(PAGE_MARGIN, top, PAGE_WIDTH - PAGE_MARGIN, bottom)
            canvas.drawRoundRect(bannerRect, 12f, 12f, bannerPaint)

            canvas.drawText("Republic of Rwanda", PAGE_MARGIN + 14f, top + 33f, bannerTitlePaint)
            canvas.drawText("Inspection Report", PAGE_MARGIN + 14f, top + 56f, bannerSubtitlePaint)

            val emblemCardRight = PAGE_WIDTH - PAGE_MARGIN - 12f
            val emblemCardLeft = emblemCardRight - 76f
            val emblemCardTop = top + 9f
            val emblemCardBottom = emblemCardTop + 76f
            val emblemCardRect = RectF(emblemCardLeft, emblemCardTop, emblemCardRight, emblemCardBottom)
            val emblemCardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFFFFF")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(emblemCardRect, 10f, 10f, emblemCardFill)

            if (coatOfArmsBitmap != null) {
                val iconLeft = emblemCardLeft + 9f
                val iconTop = emblemCardTop + 9f
                canvas.drawBitmap(coatOfArmsBitmap, iconLeft, iconTop, null)
            } else {
                val fallbackCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#0F2E66")
                    style = Paint.Style.FILL
                }
                val fallbackText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 20f
                    typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                }
                val cx = (emblemCardLeft + emblemCardRight) / 2f
                val cy = (emblemCardTop + emblemCardBottom) / 2f
                canvas.drawCircle(cx, cy, 24f, fallbackCircle)
                canvas.drawText("RW", cx - 14f, cy + 7f, fallbackText)
            }

            val bandTop = bottom + 8f
            val bandWidth = PAGE_WIDTH - PAGE_MARGIN * 2
            val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00A1DE") }
            val yellowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FAD201") }
            val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#20603D") }
            canvas.drawRect(PAGE_MARGIN, bandTop, PAGE_MARGIN + bandWidth, bandTop + 6f, bluePaint)
            canvas.drawRect(PAGE_MARGIN, bandTop + 6f, PAGE_MARGIN + bandWidth, bandTop + 10f, yellowPaint)
            canvas.drawRect(PAGE_MARGIN, bandTop + 10f, PAGE_MARGIN + bandWidth, bandTop + 14f, greenPaint)

            y = bandTop + 28f
        }

        fun drawSectionTitle(title: String) {
            if (y + 28f > CONTENT_BOTTOM) {
                newPage()
            }
            canvas.drawText(title, PAGE_MARGIN, y, sectionPaint)
            y += 10f
            canvas.drawLine(PAGE_MARGIN, y, PAGE_WIDTH - PAGE_MARGIN, y, pageLinePaint)
            y += 14f
        }

        fun drawTocCard(items: List<TocItem>) {
            val rowHeight = 24f
            val cardTop = y
            val cardBottom = cardTop + 16f + rowHeight * items.size + 10f
            val cardRect = RectF(PAGE_MARGIN, cardTop, PAGE_WIDTH - PAGE_MARGIN, cardBottom)
            canvas.drawRoundRect(cardRect, 10f, 10f, tocCardFill)
            canvas.drawRoundRect(cardRect, 10f, 10f, borderPaint)

            val leftX = PAGE_MARGIN + 14f
            val rightX = PAGE_WIDTH - PAGE_MARGIN - 14f
            var rowTop = cardTop + 10f

            items.forEachIndexed { index, item ->
                val baseline = rowTop + 15f
                val title = "${index + 1}. ${item.title}"
                val page = item.pageLabel

                canvas.drawText(title, leftX, baseline, bodyStrongPaint)
                val pageWidth = tocPagePaint.measureText(page)
                val pageX = rightX - pageWidth

                val leaderStart = leftX + bodyStrongPaint.measureText(title) + 8f
                val leaderEnd = pageX - 8f
                var dotX = leaderStart
                while (dotX < leaderEnd) {
                    canvas.drawPoint(dotX, baseline - 3f, tocDotsPaint)
                    dotX += 4f
                }

                canvas.drawText(page, pageX, baseline, tocPagePaint)

                if (index < items.lastIndex) {
                    val dividerY = rowTop + rowHeight
                    canvas.drawLine(leftX, dividerY, rightX, dividerY, pageLinePaint)
                }
                rowTop += rowHeight
            }
            y = cardBottom + 12f
        }

        fun drawParagraph(
            text: String,
            paint: Paint = bodyPaint,
            spacingAfter: Float = 8f,
            overflowSectionTitle: String? = null,
        ) {
            val wrapped = wrapText(text, paint, PAGE_WIDTH - PAGE_MARGIN * 2)
            val required = wrapped.size * LINE_GAP + spacingAfter
            if (y + required > CONTENT_BOTTOM) {
                newPage(overflowSectionTitle)
            }
            wrapped.forEach { line ->
                canvas.drawText(line, PAGE_MARGIN, y, paint)
                y += LINE_GAP
            }
            y += spacingAfter
        }

        fun drawKeyValue(
            label: String,
            value: String,
            overflowSectionTitle: String,
        ) {
            val valueLines = wrapText(
                text = value.ifBlank { "-" },
                paint = bodyPaint,
                maxWidth = PAGE_WIDTH - PAGE_MARGIN * 2 - 130f,
            )
            val rowHeight = max(24f, valueLines.size * 14f + 8f)
            val required = rowHeight + 2f
            if (y + required > CONTENT_BOTTOM) {
                newPage("$overflowSectionTitle (cont.)")
            }

            val rowTop = y - 12f
            val rowBottom = rowTop + rowHeight
            val rowRect = RectF(PAGE_MARGIN, rowTop, PAGE_WIDTH - PAGE_MARGIN, rowBottom)
            canvas.drawRoundRect(rowRect, 8f, 8f, noteCardFill)
            canvas.drawRoundRect(rowRect, 8f, 8f, borderPaint)

            canvas.drawText("$label:", PAGE_MARGIN + 10f, rowTop + 17f, mutedPaint)
            var valueY = rowTop + 17f
            valueLines.forEach { line ->
                canvas.drawText(line, PAGE_MARGIN + 130f, valueY, bodyStrongPaint)
                valueY += 14f
            }
            y = rowBottom + 10f
        }

        fun drawSummaryMetricsCard() {
            val top = y
            val bottom = top + 86f
            val rect = RectF(PAGE_MARGIN, top, PAGE_WIDTH - PAGE_MARGIN, bottom)
            canvas.drawRoundRect(rect, 10f, 10f, metricsCardFill)
            canvas.drawRoundRect(rect, 10f, 10f, borderPaint)

            val third = (PAGE_WIDTH - PAGE_MARGIN * 2) / 3f
            val oneX = PAGE_MARGIN + 14f
            val twoX = PAGE_MARGIN + third + 14f
            val threeX = PAGE_MARGIN + third * 2 + 14f

            canvas.drawText("Questions", oneX, top + 24f, mutedPaint)
            canvas.drawText(questionAnswers.size.toString(), oneX, top + 48f, bodyStrongPaint)

            canvas.drawText("Compliant", twoX, top + 24f, mutedPaint)
            canvas.drawText(compliantCount.toString(), twoX, top + 48f, bodyStrongPaint)

            canvas.drawText("Score", threeX, top + 24f, mutedPaint)
            canvas.drawText("$scoreOutOf100 / 100", threeX, top + 48f, bodyStrongPaint)

            canvas.drawLine(PAGE_MARGIN + third, top + 14f, PAGE_MARGIN + third, bottom - 14f, pageLinePaint)
            canvas.drawLine(PAGE_MARGIN + third * 2, top + 14f, PAGE_MARGIN + third * 2, bottom - 14f, pageLinePaint)
            y = bottom + 10f
        }

        fun drawQuestionTableHeader() {
            if (y + 28f > CONTENT_BOTTOM) {
                newPage("Questions and Responses (cont.)")
            }
            val top = y
            val bottom = y + 24f
            val rect = RectF(PAGE_MARGIN, top, PAGE_WIDTH - PAGE_MARGIN, bottom)
            canvas.drawRoundRect(rect, 8f, 8f, tableHeaderFill)
            canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
            canvas.drawText("No.", PAGE_MARGIN + 8f, top + 16f, mutedPaint)
            canvas.drawText("Question", PAGE_MARGIN + 36f, top + 16f, mutedPaint)
            val rightLabel = "Answer / Marks"
            val rightWidth = mutedPaint.measureText(rightLabel)
            canvas.drawText(rightLabel, PAGE_WIDTH - PAGE_MARGIN - rightWidth - 8f, top + 16f, mutedPaint)
            y = bottom + 8f
        }

        fun drawQuestionRow(item: QuestionAnswer, index: Int) {
            val answerText = "${item.answer} - ${item.marksAwarded}/${item.maxMarks} Marks"
            val answerPaint = if (item.answer.equals("No", ignoreCase = true)) answerNoPaint else answerYesPaint
            val answerWidth = answerPaint.measureText(answerText)
            val questionMaxWidth = PAGE_WIDTH - PAGE_MARGIN * 2 - answerWidth - 74f
            val questionLines = wrapText(item.question, bodyPaint, questionMaxWidth)
            val rowHeight = max(42f, 18f + questionLines.size * 14f)

            if (y + rowHeight > CONTENT_BOTTOM) {
                newPage("Questions and Responses (cont.)")
                drawQuestionTableHeader()
            }

            val rowTop = y
            val rowBottom = y + rowHeight
            val rect = RectF(PAGE_MARGIN, rowTop, PAGE_WIDTH - PAGE_MARGIN, rowBottom)
            canvas.drawRoundRect(rect, 8f, 8f, if (index % 2 == 0) rowFillA else rowFillB)
            canvas.drawRoundRect(rect, 8f, 8f, borderPaint)

            canvas.drawText(item.number.toString(), PAGE_MARGIN + 8f, rowTop + 16f, mutedPaint)
            var questionY = rowTop + 16f
            questionLines.forEach { line ->
                canvas.drawText(line, PAGE_MARGIN + 36f, questionY, bodyPaint)
                questionY += 14f
            }

            canvas.drawText(
                answerText,
                PAGE_WIDTH - PAGE_MARGIN - answerWidth - 8f,
                rowTop + 16f,
                answerPaint,
            )
            y = rowBottom + 8f
        }

        fun drawNotesBlock(title: String, content: String) {
            val text = content.ifBlank { "-" }
            val wrapped = wrapText(text, bodyPaint, PAGE_WIDTH - PAGE_MARGIN * 2 - 20f)
            val needed = 34f + wrapped.size * 14f + 16f
            if (y + needed > CONTENT_BOTTOM) {
                newPage("Inspector Notes (cont.)")
            }

            val top = y
            val bottom = top + needed
            val rect = RectF(PAGE_MARGIN, top, PAGE_WIDTH - PAGE_MARGIN, bottom)
            canvas.drawRoundRect(rect, 10f, 10f, noteCardFill)
            canvas.drawRoundRect(rect, 10f, 10f, borderPaint)

            canvas.drawText(title, PAGE_MARGIN + 12f, top + 20f, sectionPaint)
            var lineY = top + 40f
            wrapped.forEach { line ->
                canvas.drawText(line, PAGE_MARGIN + 12f, lineY, bodyPaint)
                lineY += 14f
            }
            y = bottom + 12f
        }

        drawFirstPageBanner()
        drawSectionTitle("Table of Contents")
        drawTocCard(tocItems)
        drawParagraph("Report reference: ${inspection.id}", mutedPaint, spacingAfter = 2f)
        drawParagraph("Generated on: ${formatDateTime(inspection.createdAt)}", mutedPaint, spacingAfter = 0f)

        newPage("Facility Details")
        drawKeyValue("Facility Name", inspection.facilityName, "Facility Details")
        drawKeyValue("TIN", facility?.tin.orEmpty(), "Facility Details")
        drawKeyValue("District", facility?.district.orEmpty(), "Facility Details")
        drawKeyValue("Sector", facility?.sector.orEmpty(), "Facility Details")
        drawKeyValue("Cell", facility?.cell.orEmpty(), "Facility Details")
        drawKeyValue("Village", facility?.village.orEmpty(), "Facility Details")
        drawKeyValue("Owner / Manager", facility?.ownerName.orEmpty(), "Facility Details")
        drawKeyValue("Owner Phone", facility?.ownerPhone.orEmpty(), "Facility Details")

        newPage("Inspection Summary")
        drawSummaryMetricsCard()
        drawKeyValue("Inspection Type", inspectionTypeName, "Inspection Summary")
        drawKeyValue("Visit Type", enumLabel(inspection.visitType.name), "Inspection Summary")
        drawKeyValue("Decision", inspection.decision.toReportLabel(), "Inspection Summary")
        drawKeyValue("Inspection Team", inspection.teamMembers.joinToString(", "), "Inspection Summary")
        drawKeyValue("Total Marks", "$totalEarnedMarks / ${totalPossibleMarks.coerceAtLeast(1)}", "Inspection Summary")
        drawKeyValue("Final Score", "$scoreOutOf100 / 100", "Inspection Summary")
        drawKeyValue("Charge Amount", "${inspection.adjustmentAmount} RWF", "Inspection Summary")

        newPage("Questions and Responses")
        if (questionAnswers.isEmpty()) {
            drawParagraph(
                text = "No question responses were available for this inspection.",
                overflowSectionTitle = "Questions and Responses",
            )
        } else {
            drawQuestionTableHeader()
            questionAnswers.forEachIndexed { index, item ->
                drawQuestionRow(item = item, index = index)
            }
        }

        newPage("Inspector Notes")
        drawNotesBlock("Comments", inspection.comments)
        drawNotesBlock("Recommendations", inspection.recommendations)
        if (inspection.adjustmentReason.isNotBlank()) {
            drawNotesBlock("Adjustment Reason", inspection.adjustmentReason)
        }

        finishCurrentPage()

        val file = File(context.cacheDir, "inspection_${inspection.id}_report.pdf")
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()
        return file
    }

    private fun estimateQuestionSectionPages(
        items: List<QuestionAnswer>,
        bodyPaint: Paint,
        answerYesPaint: Paint,
        answerNoPaint: Paint,
    ): Int {
        if (items.isEmpty()) return 1

        val sectionHeaderCost = 26f + 12f
        val tableHeaderCost = 24f + 8f
        var y = PAGE_MARGIN + sectionHeaderCost + tableHeaderCost
        var pages = 1

        items.forEach { item ->
            val yesText = "Yes - ${item.maxMarks}/${item.maxMarks} Marks"
            val noText = "No - 0/${item.maxMarks} Marks"
            val answerWidth = max(answerYesPaint.measureText(yesText), answerNoPaint.measureText(noText))
            val questionMaxWidth = PAGE_WIDTH - PAGE_MARGIN * 2 - answerWidth - 74f
            val questionLines = wrapText(item.question, bodyPaint, questionMaxWidth)
            val rowHeight = max(42f, 18f + questionLines.size * 14f) + 8f

            if (y + rowHeight > CONTENT_BOTTOM) {
                pages += 1
                y = PAGE_MARGIN + sectionHeaderCost + tableHeaderCost
            }
            y += rowHeight
        }
        return pages
    }

    private fun loadCoatOfArmsBitmap(
        context: Context,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap? {
        return runCatching {
            context.resources.openRawResource(R.raw.coat_of_arms_rwanda).use { stream ->
                val svg = SVG.getFromInputStream(stream)

                val docWidth = svg.documentWidth.takeIf { it.isFinite() && it > 0f } ?: 670f
                val docHeight = svg.documentHeight.takeIf { it.isFinite() && it > 0f } ?: 732f

                if (svg.documentViewBox == null) {
                    svg.setDocumentViewBox(0f, 0f, docWidth, docHeight)
                }

                val renderWidth = max(docWidth.roundToInt(), targetWidth * 4)
                val renderHeight = max(docHeight.roundToInt(), targetHeight * 4)

                val largeBitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                val largeCanvas = Canvas(largeBitmap)
                largeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                svg.setDocumentWidth(renderWidth.toFloat())
                svg.setDocumentHeight(renderHeight.toFloat())
                svg.renderToCanvas(largeCanvas)

                val trimmed = trimTransparentBounds(largeBitmap) ?: return@use null
                Bitmap.createScaledBitmap(trimmed, targetWidth, targetHeight, true)
            }
        }.getOrNull()
    }

    private fun trimTransparentBounds(bitmap: Bitmap): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        var left = width
        var top = height
        var right = -1
        var bottom = -1

        for (y in 0 until height) {
            for (x in 0 until width) {
                val alpha = bitmap.getPixel(x, y) ushr 24
                if (alpha > 6) {
                    if (x < left) left = x
                    if (y < top) top = y
                    if (x > right) right = x
                    if (y > bottom) bottom = y
                }
            }
        }

        if (right < left || bottom < top) return null
        return Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("-")
        val words = text.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        fun flush() {
            if (current.isNotEmpty()) {
                lines += current.toString()
                current = StringBuilder()
            }
        }

        words.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isEmpty()) {
                    lines += splitLongWord(word, paint, maxWidth)
                } else {
                    flush()
                    if (paint.measureText(word) <= maxWidth) {
                        current = StringBuilder(word)
                    } else {
                        lines += splitLongWord(word, paint, maxWidth)
                    }
                }
            }
        }
        flush()
        return lines.ifEmpty { listOf("-") }
    }

    private fun splitLongWord(word: String, paint: Paint, maxWidth: Float): List<String> {
        if (word.isBlank()) return emptyList()
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < word.length) {
            var end = start + 1
            while (end <= word.length && paint.measureText(word.substring(start, end)) <= maxWidth) {
                end += 1
            }
            val safeEnd = max(start + 1, end - 1)
            chunks += word.substring(start, safeEnd)
            start = safeEnd
        }
        return chunks
    }
}
