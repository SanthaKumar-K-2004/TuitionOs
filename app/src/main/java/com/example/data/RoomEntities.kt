package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ===== User Entity (for local auth) =====

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val email: String = "",
    val passwordHash: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ===== Student =====

@Entity(
    tableName = "students",
    indices = [Index("batchName"), Index("status")]
)
data class StudentRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String = "",
    val standard: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val monthlyFee: Double = 0.0,
    val batchName: String = "",
    val status: String = "",
    val studentId: String = "",
    val avatarUrl: String = "",
    val attendancePercentage: Int = 100,
    val termMidTestScore: Int = 0
)

// ===== Batch =====

@Entity(
    tableName = "batches",
    indices = [Index("status")]
)
data class BatchRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val subject: String = "",
    val daysOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "",
    val studentCount: Int = 0
)

// ===== Attendance Record =====

@Entity(
    tableName = "attendance_records",
    indices = [
        Index("batchId"),
        Index("date"),
        Index(value = ["batchId", "date", "studentName"], unique = true)
    ]
)
data class AttendanceRecordRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val batchId: Int = 0,
    val date: String = "",
    val studentName: String = "",
    val isPresent: Boolean = true
)

// ===== Fee History =====

@Entity(
    tableName = "fee_history",
    indices = [Index("studentName"), Index("status")]
)
data class FeeHistoryRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String = "",
    val month: String = "",
    val installment: String = "",
    val amount: Double = 0.0,
    val status: String = "",
    val dueDate: String = "",
    val outstandingBalance: Double = 0.0
)

// ===== Lead =====

@Entity(
    tableName = "leads",
    indices = [Index("status")]
)
data class LeadRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inquirerName: String = "",
    val standard: String = "",
    val source: String = "",
    val status: String = "",
    val phone: String = ""
)

// ===== Staff =====

@Entity(tableName = "staff")
data class StaffRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val role: String = "",
    val tamilRole: String = "",
    val assignedBatches: String = "",
    val responsibilities: String = "",
    val avatarUrl: String = "",
    val phone: String = "",
    val whatsapp: String = ""
)

// ===== Settings (single-row table) =====

@Entity(tableName = "settings")
data class SettingsRoomEntity(
    @PrimaryKey val id: Int = 1,
    val orgName: String = "",
    val centerId: String = "",
    val contactPhone: String = "",
    val upiId: String = "",
    val language: String = "English",
    val planName: String = "",
    val renewDate: String = "",
    val maxStudents: Int = 0,
    val activeStaffCount: Int = 0,
    val profilePhotoPath: String = ""
)

// ===================================================================
// Mapper extension functions: Room entities <-> Domain entities
// ===================================================================

// Student
fun StudentRoomEntity.toDomain() = StudentEntity(
    id = id, fullName = fullName, standard = standard,
    parentName = parentName, parentPhone = parentPhone,
    monthlyFee = monthlyFee, batchName = batchName, status = status,
    studentId = studentId, avatarUrl = avatarUrl,
    attendancePercentage = attendancePercentage,
    termMidTestScore = termMidTestScore
)

fun StudentEntity.toRoom() = StudentRoomEntity(
    id = id, fullName = fullName, standard = standard,
    parentName = parentName, parentPhone = parentPhone,
    monthlyFee = monthlyFee, batchName = batchName, status = status,
    studentId = studentId, avatarUrl = avatarUrl,
    attendancePercentage = attendancePercentage,
    termMidTestScore = termMidTestScore
)

// Batch
fun BatchRoomEntity.toDomain() = BatchEntity(
    id = id, name = name, subject = subject,
    daysOfWeek = daysOfWeek, startTime = startTime,
    endTime = endTime, status = status, studentCount = studentCount
)

fun BatchEntity.toRoom() = BatchRoomEntity(
    id = id, name = name, subject = subject,
    daysOfWeek = daysOfWeek, startTime = startTime,
    endTime = endTime, status = status, studentCount = studentCount
)

// Attendance
fun AttendanceRecordRoomEntity.toDomain() = AttendanceRecordEntity(
    id = id, batchId = batchId, date = date,
    studentName = studentName, isPresent = isPresent
)

fun AttendanceRecordEntity.toRoom() = AttendanceRecordRoomEntity(
    id = id, batchId = batchId, date = date,
    studentName = studentName, isPresent = isPresent
)

// Fee History
fun FeeHistoryRoomEntity.toDomain() = FeeHistoryEntity(
    id = id, studentName = studentName, month = month,
    installment = installment, amount = amount, status = status,
    dueDate = dueDate, outstandingBalance = outstandingBalance
)

fun FeeHistoryEntity.toRoom() = FeeHistoryRoomEntity(
    id = id, studentName = studentName, month = month,
    installment = installment, amount = amount, status = status,
    dueDate = dueDate, outstandingBalance = outstandingBalance
)

// Lead
fun LeadRoomEntity.toDomain() = LeadEntity(
    id = id, inquirerName = inquirerName, standard = standard,
    source = source, status = status, phone = phone
)

fun LeadEntity.toRoom() = LeadRoomEntity(
    id = id, inquirerName = inquirerName, standard = standard,
    source = source, status = status, phone = phone
)

// Staff
fun StaffRoomEntity.toDomain() = StaffEntity(
    id = id, name = name, role = role, tamilRole = tamilRole,
    assignedBatches = assignedBatches, responsibilities = responsibilities,
    avatarUrl = avatarUrl, phone = phone, whatsapp = whatsapp
)

fun StaffEntity.toRoom() = StaffRoomEntity(
    id = id, name = name, role = role, tamilRole = tamilRole,
    assignedBatches = assignedBatches, responsibilities = responsibilities,
    avatarUrl = avatarUrl, phone = phone, whatsapp = whatsapp
)

// Settings
fun SettingsRoomEntity.toDomain() = SettingsEntity(
    id = id, orgName = orgName, centerId = centerId,
    contactPhone = contactPhone, upiId = upiId, language = language,
    planName = planName, renewDate = renewDate,
    maxStudents = maxStudents, activeStaffCount = activeStaffCount,
    profilePhotoPath = profilePhotoPath
)

fun SettingsEntity.toRoom() = SettingsRoomEntity(
    id = id, orgName = orgName, centerId = centerId,
    contactPhone = contactPhone, upiId = upiId, language = language,
    planName = planName, renewDate = renewDate,
    maxStudents = maxStudents, activeStaffCount = activeStaffCount,
    profilePhotoPath = profilePhotoPath
)
