package com.example.data

import android.content.Context
import com.example.ui.util.ValidationUtils as V
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TuitionRepository(context: Context) {

    private val db = TuitionDatabase.getInstance(context)
    private val studentDao = db.studentDao()
    private val batchDao = db.batchDao()
    private val attendanceDao = db.attendanceRecordDao()
    private val feeHistoryDao = db.feeHistoryDao()
    private val leadDao = db.leadDao()
    private val staffDao = db.staffDao()
    private val settingsDao = db.settingsDao()

    // ===== Reactive Flows =====

    val allStudents: Flow<List<StudentEntity>> =
        studentDao.observeAll().map { list -> list.map { it.toDomain() } }

    val allBatches: Flow<List<BatchEntity>> =
        batchDao.observeAll().map { list -> list.map { it.toDomain() } }

    val allAttendanceRecords: Flow<List<AttendanceRecordEntity>> =
        attendanceDao.observeAll().map { list -> list.map { it.toDomain() } }

    val allFeeHistory: Flow<List<FeeHistoryEntity>> =
        feeHistoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    val allLeads: Flow<List<LeadEntity>> =
        leadDao.observeAll().map { list -> list.map { it.toDomain() } }

    val allStaff: Flow<List<StaffEntity>> =
        staffDao.observeAll().map { list -> list.map { it.toDomain() } }

    val settings: Flow<SettingsEntity?> =
        settingsDao.observeSettings().map { it?.toDomain() }

    // ===== Student CRUD =====

    suspend fun insertStudent(student: StudentEntity) {
        require(student.fullName.isNotBlank()) { "Student name is required" }
        require(student.standard.isNotBlank()) { "Class is required" }
        require(student.fullName.length <= V.MAX_NAME_LENGTH) { "Student name too long" }
        if (student.parentPhone.isNotBlank()) require(V.isValidPhone(student.parentPhone)) { "Invalid phone number" }
        require(student.monthlyFee >= 0) { "Monthly fee must be non-negative" }
        val insertedId = studentDao.upsert(student.toRoom())
        // If id was 0 (new), update with the generated id
        if (student.id == 0) {
            val withId = student.copy(id = insertedId.toInt())
            studentDao.upsert(withId.toRoom())
        }
    }

    suspend fun updateStudent(student: StudentEntity) {
        require(student.fullName.isNotBlank()) { "Student name is required" }
        require(student.standard.isNotBlank()) { "Class is required" }
        require(student.fullName.length <= V.MAX_NAME_LENGTH) { "Student name too long" }
        if (student.parentPhone.isNotBlank()) require(V.isValidPhone(student.parentPhone)) { "Invalid phone number" }
        require(student.monthlyFee >= 0) { "Monthly fee must be non-negative" }
        studentDao.update(student.toRoom())
    }

    suspend fun deleteStudent(student: StudentEntity) {
        studentDao.delete(student.toRoom())
    }

    suspend fun deleteStudentById(id: Int) {
        studentDao.deleteById(id)
    }

    // ===== Batch CRUD =====

    suspend fun insertBatch(batch: BatchEntity) {
        require(batch.name.isNotBlank()) { "Batch name is required" }
        require(batch.subject.isNotBlank()) { "Subject is required" }
        require(batch.name.length <= V.MAX_BATCH_NAME_LENGTH) { "Batch name too long" }
        require(batch.subject.length <= V.MAX_SUBJECT_LENGTH) { "Subject too long" }
        val insertedId = batchDao.upsert(batch.toRoom())
        if (batch.id == 0) {
            val withId = batch.copy(id = insertedId.toInt())
            batchDao.upsert(withId.toRoom())
        }
    }

    suspend fun updateBatch(batch: BatchEntity) {
        require(batch.name.isNotBlank()) { "Batch name is required" }
        require(batch.subject.isNotBlank()) { "Subject is required" }
        require(batch.name.length <= V.MAX_BATCH_NAME_LENGTH) { "Batch name too long" }
        require(batch.subject.length <= V.MAX_SUBJECT_LENGTH) { "Subject too long" }
        batchDao.update(batch.toRoom())
    }

    suspend fun deleteBatch(batch: BatchEntity) {
        batchDao.delete(batch.toRoom())
    }

    suspend fun deleteBatchById(id: Int) {
        batchDao.deleteById(id)
    }

    // ===== Attendance =====

    suspend fun insertAttendanceRecord(record: AttendanceRecordEntity) {
        attendanceDao.insert(record.toRoom())
    }

    suspend fun updateAttendanceRecord(record: AttendanceRecordEntity) {
        attendanceDao.update(record.toRoom())
    }

    suspend fun deleteAttendanceRecord(record: AttendanceRecordEntity) {
        attendanceDao.delete(record.toRoom())
    }

    suspend fun deleteAttendanceRecordById(id: Int) {
        attendanceDao.deleteById(id)
    }

    // ===== Fee History =====

    fun getFeeHistoryForStudent(studentName: String): Flow<List<FeeHistoryEntity>> =
        feeHistoryDao.observeForStudent(studentName)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insertFeeHistory(fee: FeeHistoryEntity) {
        require(fee.studentName.isNotBlank()) { "Student name is required" }
        require(fee.amount >= 0) { "Amount must be non-negative" }
        require(fee.outstandingBalance >= 0) { "Outstanding balance must be non-negative" }
        if (fee.dueDate.isNotBlank()) require(V.isValidDate(fee.dueDate)) { "Invalid due date format" }
        val insertedId = feeHistoryDao.upsert(fee.toRoom())
        if (fee.id == 0) {
            val withId = fee.copy(id = insertedId.toInt())
            feeHistoryDao.upsert(withId.toRoom())
        }
    }

    suspend fun deleteFeeHistory(fee: FeeHistoryEntity) {
        feeHistoryDao.delete(fee.toRoom())
    }

    suspend fun deleteFeeHistoryById(id: Int) {
        feeHistoryDao.deleteById(id)
    }

    suspend fun deleteFeeHistoryByStudentName(studentName: String) {
        feeHistoryDao.deleteByStudentName(studentName)
    }

    // ===== Lead CRUD =====

    suspend fun insertLead(lead: LeadEntity) {
        require(lead.inquirerName.isNotBlank()) { "Inquirer name is required" }
        require(lead.inquirerName.length <= V.MAX_NAME_LENGTH) { "Name too long" }
        if (lead.phone.isNotBlank()) require(V.isValidPhone(lead.phone)) { "Invalid phone number" }
        val insertedId = leadDao.upsert(lead.toRoom())
        if (lead.id == 0) {
            val withId = lead.copy(id = insertedId.toInt())
            leadDao.upsert(withId.toRoom())
        }
    }

    suspend fun updateLead(lead: LeadEntity) {
        require(lead.inquirerName.isNotBlank()) { "Inquirer name is required" }
        require(lead.inquirerName.length <= V.MAX_NAME_LENGTH) { "Name too long" }
        if (lead.phone.isNotBlank()) require(V.isValidPhone(lead.phone)) { "Invalid phone number" }
        leadDao.update(lead.toRoom())
    }

    suspend fun deleteLead(lead: LeadEntity) {
        leadDao.delete(lead.toRoom())
    }

    suspend fun deleteLeadById(id: Int) {
        leadDao.deleteById(id)
    }

    // ===== Staff CRUD =====

    suspend fun insertStaff(staff: StaffEntity) {
        require(staff.name.isNotBlank()) { "Staff name is required" }
        require(staff.role.isNotBlank()) { "Role is required" }
        require(staff.name.length <= V.MAX_NAME_LENGTH) { "Staff name too long" }
        require(staff.role.length <= V.MAX_ROLE_LENGTH) { "Role too long" }
        if (staff.phone.isNotBlank()) require(V.isValidPhone(staff.phone)) { "Invalid phone number" }
        if (staff.whatsapp.isNotBlank()) require(V.isValidPhone(staff.whatsapp)) { "Invalid WhatsApp number" }
        val insertedId = staffDao.upsert(staff.toRoom())
        if (staff.id == 0) {
            val withId = staff.copy(id = insertedId.toInt())
            staffDao.upsert(withId.toRoom())
        }
    }

    suspend fun updateStaff(staff: StaffEntity) {
        require(staff.name.isNotBlank()) { "Staff name is required" }
        require(staff.role.isNotBlank()) { "Role is required" }
        require(staff.name.length <= V.MAX_NAME_LENGTH) { "Staff name too long" }
        require(staff.role.length <= V.MAX_ROLE_LENGTH) { "Role too long" }
        if (staff.phone.isNotBlank()) require(V.isValidPhone(staff.phone)) { "Invalid phone number" }
        if (staff.whatsapp.isNotBlank()) require(V.isValidPhone(staff.whatsapp)) { "Invalid WhatsApp number" }
        staffDao.update(staff.toRoom())
    }

    suspend fun deleteStaff(staff: StaffEntity) {
        staffDao.delete(staff.toRoom())
    }

    suspend fun deleteStaffById(id: Int) {
        staffDao.deleteById(id)
    }

    // ===== Settings =====

    suspend fun insertSettings(settings: SettingsEntity) {
        if (settings.orgName.isNotBlank()) require(settings.orgName.length <= V.MAX_ORG_NAME_LENGTH) { "Organization name too long" }
        if (settings.contactPhone.isNotBlank()) require(V.isValidPhone(settings.contactPhone)) { "Invalid phone number" }
        if (settings.upiId.isNotBlank()) require(V.isValidUpi(settings.upiId)) { "Invalid UPI ID" }
        require(settings.maxStudents >= 0) { "Max students must be non-negative" }
        settingsDao.upsert(settings.toRoom())
    }

    // ===== Bulk cleanup (for logout / user switch) =====

    suspend fun clearAllUserData() {
        studentDao.deleteAll()
        batchDao.deleteAll()
        attendanceDao.deleteAll()
        feeHistoryDao.deleteAll()
        leadDao.deleteAll()
        staffDao.deleteAll()
        settingsDao.deleteAll()
    }
}
