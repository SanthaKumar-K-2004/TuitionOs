package com.example.data

import android.content.Context
import android.net.Uri
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataExportImportManager(private val context: Context) {

    private val db = TuitionDatabase.getInstance(context)

    data class ExportBundle(
        val version: Int = 1,
        val exportDate: String = "",
        val students: List<StudentRoomEntity> = emptyList(),
        val batches: List<BatchRoomEntity> = emptyList(),
        val attendanceRecords: List<AttendanceRecordRoomEntity> = emptyList(),
        val feeHistory: List<FeeHistoryRoomEntity> = emptyList(),
        val leads: List<LeadRoomEntity> = emptyList(),
        val staff: List<StaffRoomEntity> = emptyList(),
        val settings: SettingsRoomEntity? = null
    )

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val bundleAdapter: JsonAdapter<ExportBundle> =
        moshi.adapter(ExportBundle::class.java)

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val bundle = ExportBundle(
            exportDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()),
            students = db.studentDao().getAllSync(),
            batches = db.batchDao().getAllSync(),
            attendanceRecords = db.attendanceRecordDao().getAllSync(),
            feeHistory = db.feeHistoryDao().getAllSync(),
            leads = db.leadDao().getAllSync(),
            staff = db.staffDao().getAllSync(),
            settings = db.settingsDao().getSettings()
        )
        bundleAdapter.indent("  ").toJson(bundle)
    }

    suspend fun exportToFile(uri: Uri) = withContext(Dispatchers.IO) {
        val json = exportToJson()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray(Charsets.UTF_8))
        } ?: throw IOException("Cannot open output stream for URI: $uri")
    }

    suspend fun importFromJson(json: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val bundle = bundleAdapter.fromJson(json)
                ?: return@withContext Result.failure(IOException("Invalid JSON format"))

            // Clear all existing data
            db.studentDao().deleteAll()
            db.batchDao().deleteAll()
            db.attendanceRecordDao().deleteAll()
            db.feeHistoryDao().deleteAll()
            db.leadDao().deleteAll()
            db.staffDao().deleteAll()
            db.settingsDao().deleteAll()

            var count = 0

            // Insert all imported entities
            bundle.students.forEach { db.studentDao().upsert(it); count++ }
            bundle.batches.forEach { db.batchDao().upsert(it); count++ }
            bundle.attendanceRecords.forEach { db.attendanceRecordDao().insert(it); count++ }
            bundle.feeHistory.forEach { db.feeHistoryDao().upsert(it); count++ }
            bundle.leads.forEach { db.leadDao().upsert(it); count++ }
            bundle.staff.forEach { db.staffDao().upsert(it); count++ }
            bundle.settings?.let { db.settingsDao().upsert(it); count++ }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromFile(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        val json = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.readText()
            ?: return@withContext Result.failure(IOException("Cannot read file at URI: $uri"))
        importFromJson(json)
    }
}
