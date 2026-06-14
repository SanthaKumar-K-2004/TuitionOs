package com.example.data

import android.content.Context
import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Cloud repository that syncs local Room data with Supabase.
 *
 * Architecture: Room (offline cache) → Supabase (cloud source of truth)
 * - All reads come from Room (fast, works offline)
 * - All writes go to both Room AND Supabase
 * - On app start, pull latest from Supabase into Room
 *
 * Each user's data is scoped by their Supabase user_id (RLS).
 */
class CloudRepository(private val context: Context) {

    companion object {
        private const val TAG = "CloudRepo"
    }

    private val localRepo = TuitionRepository(context)

    // ============================================
    // SERIALIZABLE MODELS (matching Supabase tables)
    // ============================================

    @Serializable
    data class CloudStudent(
        val id: Int? = null,
        val user_id: String? = null,
        val full_name: String = "",
        val standard: String = "",
        val parent_name: String = "",
        val parent_phone: String = "",
        val monthly_fee: Double = 0.0,
        val batch_name: String = "",
        val status: String = "Pending",
        val student_id: String = "",
        val avatar_url: String = "",
        val attendance_percentage: Int = 100,
        val term_mid_test_score: Int = 0
    )

    @Serializable
    data class CloudBatch(
        val id: Int? = null,
        val user_id: String? = null,
        val name: String = "",
        val subject: String = "",
        val days_of_week: String = "",
        val start_time: String = "",
        val end_time: String = "",
        val status: String = "ACTIVE",
        val student_count: Int = 0
    )

    @Serializable
    data class CloudLead(
        val id: Int? = null,
        val user_id: String? = null,
        val inquirer_name: String = "",
        val standard: String = "",
        val source: String = "",
        val status: String = "NEW",
        val phone: String = ""
    )

    @Serializable
    data class CloudFeeHistory(
        val id: Int? = null,
        val user_id: String? = null,
        val student_name: String = "",
        val month: String = "",
        val installment: String = "",
        val amount: Double = 0.0,
        val status: String = "",
        val due_date: String = "",
        val outstanding_balance: Double = 0.0
    )

    @Serializable
    data class CloudAttendanceRecord(
        val id: Int? = null,
        val user_id: String? = null,
        val batch_id: Int = 0,
        val date: String = "",
        val student_name: String = "",
        val is_present: Boolean = true
    )

    @Serializable
    data class CloudStaff(
        val id: Int? = null,
        val user_id: String? = null,
        val name: String = "",
        val role: String = "",
        val tamil_role: String = "",
        val assigned_batches: String = "",
        val responsibilities: String = "",
        val avatar_url: String = "",
        val phone: String = "",
        val whatsapp: String = ""
    )

    @Serializable
    data class CloudSettings(
        val id: Int? = null,
        val user_id: String? = null,
        val org_name: String = "",
        val center_id: String = "",
        val contact_phone: String = "",
        val upi_id: String = "",
        val language: String = "English",
        val plan_name: String = "",
        val renew_date: String = "",
        val max_students: Int = 0,
        val active_staff_count: Int = 0,
        val profile_photo_path: String = ""
    )

    // ============================================
    // STUDENTS - Cloud CRUD
    // ============================================

    suspend fun syncStudentToCloud(student: StudentEntity, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                val cloud = CloudStudent(
                    full_name = student.fullName,
                    standard = student.standard,
                    parent_name = student.parentName,
                    parent_phone = student.parentPhone,
                    monthly_fee = student.monthlyFee,
                    batch_name = student.batchName,
                    status = student.status,
                    student_id = student.studentId,
                    avatar_url = student.avatarUrl,
                    attendance_percentage = student.attendancePercentage,
                    term_mid_test_score = student.termMidTestScore,
                    user_id = userId
                )
                supabaseClient.from("students").upsert(cloud)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Student sync failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchStudentsFromCloud(userId: String): Result<List<CloudStudent>> {
        if (!SupabaseConfig.isConfigured) return Result.success(emptyList())
        return try {
            val students = withContext(Dispatchers.IO) {
                supabaseClient.from("students").select {
                    filter { CloudStudent::user_id eq userId }
                }.decodeList<CloudStudent>()
            }
            Result.success(students)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch students failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchBatchesFromCloud(userId: String): Result<List<CloudBatch>> {
        if (!SupabaseConfig.isConfigured) return Result.success(emptyList())
        return try {
            val batches = withContext(Dispatchers.IO) {
                supabaseClient.from("batches").select {
                    filter { CloudBatch::user_id eq userId }
                }.decodeList<CloudBatch>()
            }
            Result.success(batches)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch batches failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchLeadsFromCloud(userId: String): Result<List<CloudLead>> {
        if (!SupabaseConfig.isConfigured) return Result.success(emptyList())
        return try {
            val leads = withContext(Dispatchers.IO) {
                supabaseClient.from("leads").select {
                    filter { CloudLead::user_id eq userId }
                }.decodeList<CloudLead>()
            }
            Result.success(leads)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch leads failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchFeeHistoryFromCloud(userId: String): Result<List<CloudFeeHistory>> {
        if (!SupabaseConfig.isConfigured) return Result.success(emptyList())
        return try {
            val fees = withContext(Dispatchers.IO) {
                supabaseClient.from("fee_history").select {
                    filter { CloudFeeHistory::user_id eq userId }
                }.decodeList<CloudFeeHistory>()
            }
            Result.success(fees)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch fee history failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAttendanceFromCloud(userId: String): Result<List<CloudAttendanceRecord>> {
        if (!SupabaseConfig.isConfigured) return Result.success(emptyList())
        return try {
            val records = withContext(Dispatchers.IO) {
                supabaseClient.from("attendance_records").select {
                    filter { CloudAttendanceRecord::user_id eq userId }
                }.decodeList<CloudAttendanceRecord>()
            }
            Result.success(records)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch attendance failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchStaffFromCloud(userId: String): Result<List<CloudStaff>> {
        if (!SupabaseConfig.isConfigured) return Result.success(emptyList())
        return try {
            val staffList = withContext(Dispatchers.IO) {
                supabaseClient.from("staff").select {
                    filter { CloudStaff::user_id eq userId }
                }.decodeList<CloudStaff>()
            }
            Result.success(staffList)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch staff failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchSettingsFromCloud(userId: String): Result<CloudSettings?> {
        if (!SupabaseConfig.isConfigured) return Result.success(null)
        return try {
            val settings = withContext(Dispatchers.IO) {
                supabaseClient.from("settings").select {
                    filter { CloudSettings::user_id eq userId }
                    limit(1)
                }.decodeSingleOrNull<CloudSettings>()
            }
            Result.success(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch settings failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // BATCHES - Cloud CRUD
    // ============================================

    suspend fun syncBatchToCloud(batch: BatchEntity, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                val cloud = CloudBatch(
                    name = batch.name,
                    subject = batch.subject,
                    days_of_week = batch.daysOfWeek,
                    start_time = batch.startTime,
                    end_time = batch.endTime,
                    status = batch.status,
                    student_count = batch.studentCount,
                    user_id = userId
                )
                supabaseClient.from("batches").upsert(cloud)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // LEADS - Cloud CRUD
    // ============================================

    suspend fun syncLeadToCloud(lead: LeadEntity, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                val cloud = CloudLead(
                    inquirer_name = lead.inquirerName,
                    standard = lead.standard,
                    source = lead.source,
                    status = lead.status,
                    phone = lead.phone,
                    user_id = userId
                )
                supabaseClient.from("leads").upsert(cloud)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lead sync failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // FEE HISTORY - Cloud CRUD
    // ============================================

    suspend fun syncFeeToCloud(fee: FeeHistoryEntity, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                val cloud = CloudFeeHistory(
                    student_name = fee.studentName,
                    month = fee.month,
                    installment = fee.installment,
                    amount = fee.amount,
                    status = fee.status,
                    due_date = fee.dueDate,
                    outstanding_balance = fee.outstandingBalance,
                    user_id = userId
                )
                supabaseClient.from("fee_history").upsert(cloud)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fee sync failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // ATTENDANCE RECORDS - Cloud CRUD
    // ============================================

    suspend fun syncAttendanceToCloud(record: AttendanceRecordEntity, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                val cloud = CloudAttendanceRecord(
                    batch_id = record.batchId,
                    date = record.date,
                    student_name = record.studentName,
                    is_present = record.isPresent,
                    user_id = userId
                )
                supabaseClient.from("attendance_records").upsert(cloud)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Attendance sync failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // STAFF - Cloud CRUD
    // ============================================

    suspend fun syncStaffToCloud(staff: StaffEntity, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                val cloud = CloudStaff(
                    name = staff.name,
                    role = staff.role,
                    tamil_role = staff.tamilRole,
                    assigned_batches = staff.assignedBatches,
                    responsibilities = staff.responsibilities,
                    avatar_url = staff.avatarUrl,
                    phone = staff.phone,
                    whatsapp = staff.whatsapp,
                    user_id = userId
                )
                supabaseClient.from("staff").upsert(cloud)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Staff sync failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // SETTINGS - Cloud CRUD
    // ============================================

    suspend fun syncSettingsToCloud(settings: SettingsEntity, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                val cloud = CloudSettings(
                    org_name = settings.orgName,
                    center_id = settings.centerId,
                    contact_phone = settings.contactPhone,
                    upi_id = settings.upiId,
                    language = settings.language,
                    plan_name = settings.planName,
                    renew_date = settings.renewDate,
                    max_students = settings.maxStudents,
                    active_staff_count = settings.activeStaffCount,
                    profile_photo_path = settings.profilePhotoPath,
                    user_id = userId
                )
                supabaseClient.from("settings").upsert(cloud)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Settings sync failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // CLOUD DELETE OPERATIONS
    // ============================================

    suspend fun deleteStudentFromCloud(studentId: String, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                supabaseClient.from("students").delete {
                    filter {
                        CloudStudent::user_id eq userId
                        CloudStudent::student_id eq studentId
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud student delete failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBatchFromCloud(batchId: Int, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                supabaseClient.from("batches").delete {
                    filter {
                        CloudBatch::user_id eq userId
                        CloudBatch::id eq batchId
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud batch delete failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteLeadFromCloud(leadId: Int, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                supabaseClient.from("leads").delete {
                    filter {
                        CloudLead::user_id eq userId
                        CloudLead::id eq leadId
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud lead delete failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFeeFromCloud(feeId: Int, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                supabaseClient.from("fee_history").delete {
                    filter {
                        CloudFeeHistory::user_id eq userId
                        CloudFeeHistory::id eq feeId
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud fee delete failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteStaffFromCloud(staffId: Int, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                supabaseClient.from("staff").delete {
                    filter {
                        CloudStaff::user_id eq userId
                        CloudStaff::id eq staffId
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud staff delete failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAttendanceFromCloud(recordId: Int, userId: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) return Result.success(Unit)
        return try {
            withContext(Dispatchers.IO) {
                supabaseClient.from("attendance_records").delete {
                    filter {
                        CloudAttendanceRecord::user_id eq userId
                        CloudAttendanceRecord::id eq recordId
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud attendance delete failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // PHOTO UPLOAD - Supabase Storage
    // ============================================

    suspend fun uploadProfilePhoto(userId: String, filePath: String): Result<String> {
        if (!SupabaseConfig.isConfigured) return Result.failure(Exception("Cloud not configured"))
        return try {
            withContext(Dispatchers.IO) {
                val file = File(filePath)
                if (!file.exists()) return@withContext Result.failure<String>(Exception("File not found"))

                val bytes = file.readBytes()
                val fileName = "${userId}/${file.name}"
                val bucket = supabaseClient.storage.from("profile-photos")
                bucket.upload(fileName, bytes) {
                    upsert = true
                }
                val publicUrl = bucket.publicUrl(fileName)
                Result.success(publicUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Photo upload failed", e)
            Result.failure(e)
        }
    }

    // ============================================
    // LOCAL REPOSITORY ACCESS (delegates)
    // ============================================

    val allStudents = localRepo.allStudents
    val allBatches = localRepo.allBatches
    val allAttendanceRecords = localRepo.allAttendanceRecords
    val allFeeHistory = localRepo.allFeeHistory
    val allLeads = localRepo.allLeads
    val allStaff = localRepo.allStaff
    val settings = localRepo.settings

    suspend fun insertStudent(student: StudentEntity) = localRepo.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = localRepo.updateStudent(student)
    suspend fun deleteStudent(student: StudentEntity) = localRepo.deleteStudent(student)
    suspend fun deleteStudentById(id: Int) = localRepo.deleteStudentById(id)
    suspend fun insertBatch(batch: BatchEntity) = localRepo.insertBatch(batch)
    suspend fun updateBatch(batch: BatchEntity) = localRepo.updateBatch(batch)
    suspend fun deleteBatch(batch: BatchEntity) = localRepo.deleteBatch(batch)
    suspend fun deleteBatchById(id: Int) = localRepo.deleteBatchById(id)
    suspend fun insertAttendanceRecord(record: AttendanceRecordEntity) = localRepo.insertAttendanceRecord(record)
    suspend fun updateAttendanceRecord(record: AttendanceRecordEntity) = localRepo.updateAttendanceRecord(record)
    suspend fun deleteAttendanceRecord(record: AttendanceRecordEntity) = localRepo.deleteAttendanceRecord(record)
    suspend fun insertFeeHistory(fee: FeeHistoryEntity) = localRepo.insertFeeHistory(fee)
    suspend fun deleteFeeHistory(fee: FeeHistoryEntity) = localRepo.deleteFeeHistory(fee)
    fun getFeeHistoryForStudent(name: String) = localRepo.getFeeHistoryForStudent(name)
    suspend fun insertLead(lead: LeadEntity) = localRepo.insertLead(lead)
    suspend fun updateLead(lead: LeadEntity) = localRepo.updateLead(lead)
    suspend fun deleteLead(lead: LeadEntity) = localRepo.deleteLead(lead)
    suspend fun deleteLeadById(id: Int) = localRepo.deleteLeadById(id)
    suspend fun insertStaff(staff: StaffEntity) = localRepo.insertStaff(staff)
    suspend fun updateStaff(staff: StaffEntity) = localRepo.updateStaff(staff)
    suspend fun deleteStaff(staff: StaffEntity) = localRepo.deleteStaff(staff)
    suspend fun insertSettings(settings: SettingsEntity) = localRepo.insertSettings(settings)
    suspend fun clearAllUserData() = localRepo.clearAllUserData()
}
