package com.moses.inspectionapp.data.repository

import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.Fault
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.data.model.InspectionType
import com.moses.inspectionapp.data.model.PendingCounts
import com.moses.inspectionapp.data.model.Stats
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.model.UserProfile
import com.moses.inspectionapp.data.model.VisitType

object SampleData {
    private val now = System.currentTimeMillis()
    val user = UserProfile(
        id = "u1",
        fullName = "Alice N.",
        email = "alice.inspector@kigali.rw",
        role = "HSO",
        district = "Gasabo",
        sector = "Kacyiru",
    )

    val facilities = listOf(
        Facility(
            id = "f1",
            name = "Sunrise Restobar",
            tin = "102938475",
            ownerName = "Alice M.",
            ownerPhone = "+250 78 000 0000",
            ownerEmail = "owner@sunrise.rw",
            district = "Gasabo",
            sector = "Kacyiru",
            cell = "Kacyiru",
            village = "Gishushu",
            latitude = -1.9536,
            longitude = 30.0617,
            photoPath = null,
            createdAt = now - 6 * 60 * 60 * 1000,
            createdBy = "u1",
            updatedAt = now - 2 * 60 * 60 * 1000,
            syncStatus = SyncStatus.SYNCED,
        ),
        Facility(
            id = "f2",
            name = "Green Bowl",
            tin = "564738291",
            ownerName = "Joseph K.",
            ownerPhone = "+250 78 111 2222",
            ownerEmail = "manager@greenbowl.rw",
            district = "Gasabo",
            sector = "Remera",
            cell = "Remera",
            village = "Rukiri I",
            latitude = -1.9512,
            longitude = 30.1053,
            photoPath = null,
            createdAt = now - 2 * 24 * 60 * 60 * 1000,
            createdBy = "u1",
            updatedAt = now - 2 * 24 * 60 * 60 * 1000,
            syncStatus = SyncStatus.PENDING,
        ),
    )

    val inspections = listOf(
        Inspection(
            id = "i1",
            facilityId = "f1",
            facilityName = "Sunrise Restobar",
            visitType = VisitType.WARNING_VISIT,
            teamMembers = listOf("Alice N.", "Eric M."),
            inspectionTypeId = "food_safety",
            faultCount = 3,
            totalFine = 18000,
            adjustmentAmount = 0,
            adjustmentReason = "",
            decision = Decision.WARNING,
            comments = "Improve storage temperature logs.",
            recommendations = "Train kitchen staff weekly.",
            photoPaths = emptyList(),
            createdAt = now - 5 * 60 * 60 * 1000,
            createdBy = "u1",
            updatedAt = now - 5 * 60 * 60 * 1000,
            syncStatus = SyncStatus.SYNCED,
        ),
        Inspection(
            id = "i2",
            facilityId = "f2",
            facilityName = "Green Bowl",
            visitType = VisitType.FIRST_VISIT,
            teamMembers = listOf("Alice N."),
            inspectionTypeId = "food_safety",
            faultCount = 2,
            totalFine = 7000,
            adjustmentAmount = 0,
            adjustmentReason = "",
            decision = Decision.WARNING,
            comments = "Handwashing station missing soap.",
            recommendations = "Install soap dispensers.",
            photoPaths = emptyList(),
            createdAt = now - 2 * 60 * 60 * 1000,
            createdBy = "u1",
            updatedAt = now - 2 * 60 * 60 * 1000,
            syncStatus = SyncStatus.PENDING,
        ),
    )

    val faults = listOf(
        Fault(
            id = "fault1",
            inspectionTypeId = "food_safety",
            name = "Handwashing station missing soap",
            standardFine = 3000,
            active = true,
        ),
        Fault(
            id = "fault2",
            inspectionTypeId = "food_safety",
            name = "Dirty kitchen surfaces",
            standardFine = 5000,
            active = true,
        ),
        Fault(
            id = "fault3",
            inspectionTypeId = "food_safety",
            name = "Expired food in storage",
            standardFine = 7000,
            active = true,
        ),
        Fault(
            id = "fault4",
            inspectionTypeId = "food_safety",
            name = "Missing hygiene certificates",
            standardFine = 3000,
            active = true,
        ),
    )

    val inspectionTypes = listOf(
        InspectionType(
            id = "food_safety",
            code = "FOOD_SAFETY",
            name = "Food Safety (Restobar)",
            active = true,
        ),
        InspectionType(
            id = "water_quality",
            code = "WATER_QUALITY",
            name = "Water Quality",
            active = true,
        ),
        InspectionType(
            id = "sanitation_hygiene",
            code = "SANITATION_HYGIENE",
            name = "Sanitation & Hygiene",
            active = true,
        ),
        InspectionType(
            id = "vector_control",
            code = "VECTOR_CONTROL",
            name = "Vector Control",
            active = true,
        ),
        InspectionType(
            id = "school_health",
            code = "SCHOOL_HEALTH",
            name = "School Health",
            active = true,
        ),
        InspectionType(
            id = "healthcare_facility",
            code = "HEALTHCARE_FACILITY",
            name = "Healthcare Facility",
            active = true,
        ),
        InspectionType(
            id = "public_building",
            code = "PUBLIC_BUILDING",
            name = "Public Building",
            active = true,
        ),
    )

    val stats = Stats(
        todayInspections = 4,
        weekInspections = 18,
        totalFines = 120000,
    )

    val pending = PendingCounts(
        facilities = 1,
        inspections = 2,
    )
}
