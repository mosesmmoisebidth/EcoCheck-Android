package com.moses.inspectionapp.data.location

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader

data class DistrictItem(
    val districtId: Int,
    val districtName: String,
    val provinceId: Int,
)

data class SectorItem(
    val sectorId: Int,
    val sectorName: String,
    val districtId: Int,
)

data class CellItem(
    val cellId: Int,
    val cellName: String,
    val sectorId: Int,
)

data class VillageItem(
    val villageId: Int,
    val villageName: String,
    val cellId: Int,
)

data class LocationCatalog(
    val districts: List<DistrictItem>,
    val sectors: List<SectorItem>,
    val cells: List<CellItem>,
    val villages: List<VillageItem>,
) {
    fun districtsSorted(): List<DistrictItem> = districts.sortedBy { it.districtName }

    fun sectorsForDistrict(districtId: Int): List<SectorItem> =
        sectors.filter { it.districtId == districtId }.sortedBy { it.sectorName }

    fun cellsForSector(sectorId: Int): List<CellItem> =
        cells.filter { it.sectorId == sectorId }.sortedBy { it.cellName }

    fun villagesForCell(cellId: Int): List<VillageItem> =
        villages.filter { it.cellId == cellId }.sortedBy { it.villageName }
}

object LocationCatalogStore {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Volatile
    private var cached: LocationCatalog? = null

    suspend fun load(context: Context): LocationCatalog = withContext(Dispatchers.IO) {
        cached ?: run {
            val districts = loadList<DistrictItem>(context, "districts.json", DistrictItem::class.java)
            val sectors = loadList<SectorItem>(context, "sectors.json", SectorItem::class.java)
            val cells = loadList<CellItem>(context, "cells.json", CellItem::class.java)
            val villages = loadList<VillageItem>(context, "villages.json", VillageItem::class.java)
            LocationCatalog(districts, sectors, cells, villages).also { cached = it }
        }
    }

    private fun <T> loadList(context: Context, fileName: String, clazz: Class<T>): List<T> {
        val type = Types.newParameterizedType(List::class.java, clazz)
        val adapter = moshi.adapter<List<T>>(type)
        val json = context.assets.open(fileName).bufferedReader().use(BufferedReader::readText)
        return adapter.fromJson(json).orEmpty()
    }
}
