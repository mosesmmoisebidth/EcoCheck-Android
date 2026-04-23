package com.moses.inspectionapp.data.remote

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DashboardRepository(
    private val api: InspectionApi = ApiClient.api,
) {
    suspend fun loadInsights(topOffendersLimit: Int = 5): Result<DashboardInsights> {
        return runCatching {
            coroutineScope {
                val statsDeferred = async { api.getDashboardStats() }
                val inspectionsDeferred = async { api.getInspectionsOverTime() }
                val complianceDeferred = async { api.getComplianceTrend() }
                val decisionsDeferred = async { api.getDecisionsBreakdown() }
                val offendersDeferred = async { api.getTopOffenders(limit = topOffendersLimit) }

                val stats = statsDeferred.await().requirePayload("dashboard stats")
                val inspections = inspectionsDeferred.await().requirePayload("inspections over time")
                val compliance = complianceDeferred.await().requirePayload("compliance trend")
                val decisions = decisionsDeferred.await().requirePayload("decisions breakdown")
                val offenders = offendersDeferred.await().requirePayload("top offenders")

                DashboardInsights(
                    stats = stats,
                    inspectionsOverTime = inspections.map {
                        DashboardCountPoint(
                            date = it.date,
                            count = it.count,
                        )
                    },
                    complianceTrend = compliance.map {
                        DashboardCompliancePoint(
                            date = it.date,
                            complianceRate = it.compliance_rate,
                        )
                    },
                    decisionsBreakdown = decisions.map {
                        DashboardDecisionBreakdown(
                            decision = it.decision,
                            count = it.count,
                        )
                    },
                    topOffenders = offenders.map {
                        DashboardTopOffender(
                            facilityId = it.facility_id,
                            facilityName = it.facility_name,
                            sector = it.sector,
                            totalFaults = it.total_faults,
                            totalFines = it.total_fines,
                            inspectionCount = it.inspection_count,
                        )
                    },
                )
            }
        }
    }
}

data class DashboardInsights(
    val stats: DashboardStatsResponse,
    val inspectionsOverTime: List<DashboardCountPoint>,
    val complianceTrend: List<DashboardCompliancePoint>,
    val decisionsBreakdown: List<DashboardDecisionBreakdown>,
    val topOffenders: List<DashboardTopOffender>,
)

data class DashboardCountPoint(
    val date: String,
    val count: Int,
)

data class DashboardCompliancePoint(
    val date: String,
    val complianceRate: Int,
)

data class DashboardDecisionBreakdown(
    val decision: String,
    val count: Int,
)

data class DashboardTopOffender(
    val facilityId: String,
    val facilityName: String,
    val sector: String?,
    val totalFaults: Int,
    val totalFines: Int,
    val inspectionCount: Int,
)

private fun <T> ApiResponse<T>.requirePayload(source: String): T {
    if (!success) {
        throw IllegalStateException(message.ifBlank { "Unable to load $source" })
    }
    return payload
}
