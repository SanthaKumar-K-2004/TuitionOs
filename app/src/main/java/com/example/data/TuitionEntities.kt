package com.example.data

data class StudentEntity(
    val id: Int = 0,
    val fullName: String = "",
    val standard: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val monthlyFee: Double = 0.0,
    val batchName: String = "",
    val status: String = "", // "Paid", "Pending", "Overdue"
    val studentId: String = "",
    val avatarUrl: String = "",
    val attendancePercentage: Int = 100,
    val termMidTestScore: Int = 0
)

data class BatchEntity(
    val id: Int = 0,
    val name: String = "",
    val subject: String = "",
    val daysOfWeek: String = "", // e.g. "Mon, Wed, Fri"
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "", // "ONGOING", "UPCOMING", "ACTIVE"
    val studentCount: Int = 0
)

data class AttendanceRecordEntity(
    val id: Int = 0,
    val batchId: Int = 0,
    val date: String = "",
    val studentName: String = "",
    val isPresent: Boolean = true
)

data class FeeHistoryEntity(
    val id: Int = 0,
    val studentName: String = "",
    val month: String = "",
    val installment: String = "",
    val amount: Double = 0.0,
    val status: String = "", // "Paid", "Pending", "Overdue"
    val dueDate: String = "2026-06-15",
    val outstandingBalance: Double = 0.0
)

data class LeadEntity(
    val id: Int = 0,
    val inquirerName: String = "",
    val standard: String = "",
    val source: String = "", // "WhatsApp Inquiry", "Walk-in", "Website Form"
    val status: String = "", // "NEW", "CONTACTED", "ADMITTED"
    val phone: String = ""
)

data class StaffEntity(
    val id: Int = 0,
    val name: String = "",
    val role: String = "",
    val tamilRole: String = "",
    val assignedBatches: String = "", // comma-separated
    val responsibilities: String = "", // comma-separated
    val avatarUrl: String = "",
    val phone: String = "",
    val whatsapp: String = ""
)

data class SettingsEntity(
    val id: Int = 1,
    val orgName: String = "",
    val centerId: String = "",
    val contactPhone: String = "",
    val upiId: String = "",
    val language: String = "English", // "en" or "ta"
    val planName: String = "",
    val renewDate: String = "",
    val maxStudents: Int = 0,
    val activeStaffCount: Int = 0,
    val profilePhotoPath: String = ""
)
