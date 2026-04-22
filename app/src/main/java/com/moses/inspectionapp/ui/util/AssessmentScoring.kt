package com.moses.inspectionapp.ui.util

import kotlin.math.roundToInt

const val MARKS_PER_QUESTION = 10
const val SCORE_SCALE = 100

data class AssessmentScore(
    val totalQuestions: Int,
    val compliantAnswers: Int,
    val failedAnswers: Int,
    val rawScore: Int,
    val rawMax: Int,
    val scoreOutOf100: Int,
)

fun calculateAssessmentScore(totalQuestions: Int, failedAnswers: Int): AssessmentScore {
    val safeTotal = totalQuestions.coerceAtLeast(0)
    val safeFailed = failedAnswers.coerceIn(0, safeTotal)
    val compliant = (safeTotal - safeFailed).coerceAtLeast(0)
    val rawMax = safeTotal * MARKS_PER_QUESTION
    val rawScore = compliant * MARKS_PER_QUESTION
    val outOf100 = if (rawMax == 0) 0 else ((rawScore.toFloat() / rawMax.toFloat()) * SCORE_SCALE).roundToInt()
    return AssessmentScore(
        totalQuestions = safeTotal,
        compliantAnswers = compliant,
        failedAnswers = safeFailed,
        rawScore = rawScore,
        rawMax = rawMax,
        scoreOutOf100 = outOf100,
    )
}
