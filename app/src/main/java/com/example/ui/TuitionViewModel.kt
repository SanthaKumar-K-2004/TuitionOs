package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TuitionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TuitionRepository(application)
    private val securePrefs = SecurePreferencesManager(application)
    private val cloudRepo = CloudRepository(application)
    private val cloudAuth = CloudAuthManager()

    companion object {
        private const val TAG = "ViewModel"
    }

    /**
     * Get current Supabase user ID, or null if not in cloud mode.
     */
    private suspend fun currentUserId(): String? {
        if (!SupabaseConfig.isConfigured) return null
        return try {
            supabaseClient.auth.currentUserOrNull()?.id
        } catch (_: Exception) { null }
    }

    /**
     * Fire-and-forget cloud sync. Never blocks the UI.
     */
    private fun syncToCloud(action: suspend (userId: String) -> Unit) {
        if (!SupabaseConfig.isConfigured) return
        viewModelScope.launch {
            try {
                val uid = currentUserId() ?: return@launch
                action(uid)
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync failed (non-critical): ${e.message}")
            }
        }
    }

    /**
     * Pull all data from Supabase into local Room on app start.
     * Call this once after login/session restore.
     */
    fun syncFromCloud() {
        if (!SupabaseConfig.isConfigured) return
        viewModelScope.launch {
            try {
                val uid = currentUserId() ?: return@launch

                // 1. Students
                cloudRepo.fetchStudentsFromCloud(uid).onSuccess { list ->
                    list.forEach { cs ->
                        repository.insertStudent(StudentEntity(
                            fullName = cs.full_name, standard = cs.standard,
                            parentName = cs.parent_name, parentPhone = cs.parent_phone,
                            monthlyFee = cs.monthly_fee, batchName = cs.batch_name,
                            status = cs.status, studentId = cs.student_id,
                            avatarUrl = cs.avatar_url,
                            attendancePercentage = cs.attendance_percentage,
                            termMidTestScore = cs.term_mid_test_score
                        ))
                    }
                    Log.d(TAG, "Synced ${list.size} students from cloud")
                }

                // 2. Batches
                cloudRepo.fetchBatchesFromCloud(uid).onSuccess { list ->
                    list.forEach { cb ->
                        repository.insertBatch(BatchEntity(
                            name = cb.name, subject = cb.subject,
                            daysOfWeek = cb.days_of_week, startTime = cb.start_time,
                            endTime = cb.end_time, status = cb.status,
                            studentCount = cb.student_count
                        ))
                    }
                    Log.d(TAG, "Synced ${list.size} batches from cloud")
                }

                // 3. Leads
                cloudRepo.fetchLeadsFromCloud(uid).onSuccess { list ->
                    list.forEach { cl ->
                        repository.insertLead(LeadEntity(
                            inquirerName = cl.inquirer_name, standard = cl.standard,
                            source = cl.source, status = cl.status, phone = cl.phone
                        ))
                    }
                    Log.d(TAG, "Synced ${list.size} leads from cloud")
                }

                // 4. Fee History
                cloudRepo.fetchFeeHistoryFromCloud(uid).onSuccess { list ->
                    list.forEach { cf ->
                        repository.insertFeeHistory(FeeHistoryEntity(
                            studentName = cf.student_name, month = cf.month,
                            installment = cf.installment, amount = cf.amount,
                            status = cf.status, dueDate = cf.due_date,
                            outstandingBalance = cf.outstanding_balance
                        ))
                    }
                    Log.d(TAG, "Synced ${list.size} fee records from cloud")
                }

                // 5. Attendance Records
                cloudRepo.fetchAttendanceFromCloud(uid).onSuccess { list ->
                    list.forEach { ca ->
                        repository.insertAttendanceRecord(AttendanceRecordEntity(
                            batchId = ca.batch_id, date = ca.date,
                            studentName = ca.student_name, isPresent = ca.is_present
                        ))
                    }
                    Log.d(TAG, "Synced ${list.size} attendance records from cloud")
                }

                // 6. Staff
                cloudRepo.fetchStaffFromCloud(uid).onSuccess { list ->
                    list.forEach { cs ->
                        repository.insertStaff(StaffEntity(
                            name = cs.name, role = cs.role, tamilRole = cs.tamil_role,
                            assignedBatches = cs.assigned_batches,
                            responsibilities = cs.responsibilities,
                            avatarUrl = cs.avatar_url, phone = cs.phone,
                            whatsapp = cs.whatsapp
                        ))
                    }
                    Log.d(TAG, "Synced ${list.size} staff from cloud")
                }

                // 7. Settings
                cloudRepo.fetchSettingsFromCloud(uid).onSuccess { cs ->
                    cs?.let {
                        repository.insertSettings(SettingsEntity(
                            orgName = it.org_name, centerId = it.center_id,
                            contactPhone = it.contact_phone, upiId = it.upi_id,
                            language = it.language, planName = it.plan_name,
                            renewDate = it.renew_date, maxStudents = it.max_students,
                            activeStaffCount = it.active_staff_count,
                            profilePhotoPath = it.profile_photo_path
                        ))
                        Log.d(TAG, "Synced settings from cloud")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cloud fetch failed (non-critical): ${e.message}")
            }
        }
    }

    /**
     * Clear all local data on logout to prevent data leakage between users.
     */
    fun clearAllData() {
        viewModelScope.launch {
            try {
                repository.clearAllUserData()
                Log.d(TAG, "All local user data cleared")
            } catch (e: Exception) {
                Log.w(TAG, "Clear data failed: ${e.message}")
            }
        }
    }

    val students: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val attendanceRecords: StateFlow<List<AttendanceRecordEntity>> = repository.allAttendanceRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val feeHistory: StateFlow<List<FeeHistoryEntity>> = repository.allFeeHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val batches: StateFlow<List<BatchEntity>> = repository.allBatches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val leads: StateFlow<List<LeadEntity>> = repository.allLeads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val staff: StateFlow<List<StaffEntity>> = repository.allStaff
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<SettingsEntity?> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun addStudent(
        fullName: String,
        standard: String,
        parentName: String,
        parentPhone: String,
        monthlyFee: Double,
        batchName: String,
        status: String = "Pending",
        avatarUrl: String = ""
    ) {
        viewModelScope.launch {
            val shortId = "TUI-" + java.util.UUID.randomUUID().toString().take(8).uppercase()
            val newStudent = StudentEntity(
                fullName = fullName,
                standard = standard,
                parentName = parentName,
                parentPhone = parentPhone,
                monthlyFee = monthlyFee,
                batchName = batchName,
                status = status,
                studentId = shortId,
                avatarUrl = avatarUrl,
                attendancePercentage = 100,
                termMidTestScore = 0
            )
            repository.insertStudent(newStudent)
            
            // Add a base fee history item for this student
            val baseFee = FeeHistoryEntity(
                studentName = fullName,
                month = "Current Term",
                installment = "Term 1 Installment",
                amount = monthlyFee,
                status = status
            )
            repository.insertFeeHistory(baseFee)

            // Sync to cloud
            syncToCloud { uid ->
                cloudRepo.syncStudentToCloud(newStudent, uid)
                cloudRepo.syncFeeToCloud(baseFee, uid)
            }
        }
    }

    fun updateStudentStatus(student: StudentEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = student.copy(status = newStatus)
            repository.updateStudent(updated)
            syncToCloud { uid -> cloudRepo.syncStudentToCloud(updated, uid) }
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            syncToCloud { uid -> cloudRepo.syncStudentToCloud(student, uid) }
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            // Also delete related fee history locally
            repository.deleteFeeHistoryByStudentName(student.fullName)
            // Sync deletions to cloud
            syncToCloud { uid ->
                cloudRepo.deleteStudentFromCloud(student.studentId, uid)
            }
        }
    }

    fun deleteStudentById(id: Int) {
        viewModelScope.launch {
            // Find student first for cloud sync
            val studentsList = students.value
            val student = studentsList.find { it.id == id }
            if (student != null) {
                repository.deleteFeeHistoryByStudentName(student.fullName)
                syncToCloud { uid -> cloudRepo.deleteStudentFromCloud(student.studentId, uid) }
            }
            repository.deleteStudentById(id)
        }
    }

    fun deleteBatch(batch: BatchEntity) {
        viewModelScope.launch {
            repository.deleteBatch(batch)
            syncToCloud { uid -> cloudRepo.deleteBatchFromCloud(batch.id, uid) }
        }
    }

    fun deleteLead(lead: LeadEntity) {
        viewModelScope.launch {
            repository.deleteLead(lead)
            syncToCloud { uid -> cloudRepo.deleteLeadFromCloud(lead.id, uid) }
        }
    }

    fun deleteStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
            syncToCloud { uid -> cloudRepo.deleteStaffFromCloud(staff.id, uid) }
        }
    }

    fun deleteFeeHistory(fee: FeeHistoryEntity) {
        viewModelScope.launch {
            repository.deleteFeeHistory(fee)
            syncToCloud { uid -> cloudRepo.deleteFeeFromCloud(fee.id, uid) }
        }
    }

    fun deleteAttendanceRecord(record: AttendanceRecordEntity) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(record)
            syncToCloud { uid -> cloudRepo.deleteAttendanceFromCloud(record.id, uid) }
        }
    }

    fun updateStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repository.updateStaff(staff)
            syncToCloud { uid -> cloudRepo.syncStaffToCloud(staff, uid) }
        }
    }

    fun updateBatch(batch: BatchEntity) {
        viewModelScope.launch {
            repository.updateBatch(batch)
            syncToCloud { uid -> cloudRepo.syncBatchToCloud(batch, uid) }
        }
    }

    fun addBatch(
        name: String,
        subject: String,
        daysOfWeek: String,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            val newBatch = BatchEntity(
                name = name,
                subject = subject,
                daysOfWeek = daysOfWeek,
                startTime = startTime,
                endTime = endTime,
                status = "ACTIVE",
                studentCount = 0
            )
            repository.insertBatch(newBatch)
            syncToCloud { uid -> cloudRepo.syncBatchToCloud(newBatch, uid) }
        }
    }

    fun addLead(inquirerName: String, standard: String, source: String, status: String, phone: String = "") {
        viewModelScope.launch {
            val newLead = LeadEntity(
                inquirerName = inquirerName,
                standard = standard,
                source = source,
                status = status,
                phone = phone
            )
            repository.insertLead(newLead)
            syncToCloud { uid -> cloudRepo.syncLeadToCloud(newLead, uid) }
        }
    }

    fun updateLeadStatus(lead: LeadEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = lead.copy(status = newStatus)
            repository.updateLead(updated)
            syncToCloud { uid -> cloudRepo.syncLeadToCloud(updated, uid) }
        }
    }

    fun updateLead(lead: LeadEntity) {
        viewModelScope.launch {
            repository.updateLead(lead)
            syncToCloud { uid -> cloudRepo.syncLeadToCloud(lead, uid) }
        }
    }

    fun addStaff(
        name: String,
        role: String,
        tamilRole: String,
        assignedBatches: String,
        responsibilities: String,
        phone: String,
        whatsapp: String
    ) {
        viewModelScope.launch {
            val newStaff = StaffEntity(
                name = name,
                role = role,
                tamilRole = tamilRole,
                assignedBatches = assignedBatches,
                responsibilities = responsibilities,
                phone = phone,
                whatsapp = whatsapp
            )
            repository.insertStaff(newStaff)
            syncToCloud { uid -> cloudRepo.syncStaffToCloud(newStaff, uid) }
        }
    }

    fun addAttendanceRecord(batchId: Int, date: String, studentName: String, isPresent: Boolean) {
        viewModelScope.launch {
            val record = AttendanceRecordEntity(
                batchId = batchId,
                date = date,
                studentName = studentName,
                isPresent = isPresent
            )
            repository.insertAttendanceRecord(record)
            syncToCloud { uid -> cloudRepo.syncAttendanceToCloud(record, uid) }
        }
    }

    fun updateAttendanceRecord(record: AttendanceRecordEntity, newIsPresent: Boolean) {
        viewModelScope.launch {
            val updated = record.copy(isPresent = newIsPresent)
            repository.updateAttendanceRecord(updated)
            syncToCloud { uid -> cloudRepo.syncAttendanceToCloud(updated, uid) }
        }
    }

    private fun calculateAttendancePercentage(
        student: StudentEntity,
        attendanceRecords: List<AttendanceRecordEntity>,
        batchId: Int
    ): Int {
        val studentRecords = attendanceRecords.filter {
            it.batchId == batchId && it.studentName == student.fullName
        }
        if (studentRecords.isEmpty()) return student.attendancePercentage.coerceIn(0, 100)
        val presentCount = studentRecords.count { it.isPresent }
        return ((presentCount * 100.0) / studentRecords.size).toInt().coerceIn(0, 100)
    }

    fun saveBatchAttendance(
        batchId: Int,
        selectedDate: String,
        attendanceStates: Map<Int, Boolean>,
        assignedStudents: List<StudentEntity>,
        currentRecords: List<AttendanceRecordEntity>
    ) {
        viewModelScope.launch {
            val mergedRecords = currentRecords.toMutableList()

            assignedStudents.forEach { student ->
                val isPresent = attendanceStates[student.id] ?: true
                val record = AttendanceRecordEntity(
                    batchId = batchId,
                    date = selectedDate,
                    studentName = student.fullName,
                    isPresent = isPresent
                )
                repository.insertAttendanceRecord(record)
                syncToCloud { uid -> cloudRepo.syncAttendanceToCloud(record, uid) }

                val existingIndex = mergedRecords.indexOfFirst {
                    it.batchId == batchId && it.date == selectedDate && it.studentName == student.fullName
                }
                if (existingIndex >= 0) mergedRecords[existingIndex] = record else mergedRecords.add(record)
            }

            assignedStudents.forEach { student ->
                val percentage = calculateAttendancePercentage(student, mergedRecords, batchId)
                if (percentage != student.attendancePercentage) {
                    updateStudent(student.copy(attendancePercentage = percentage))
                }
            }
        }
    }

    fun getFeeHistoryForStudent(studentName: String): Flow<List<FeeHistoryEntity>> {
        return repository.getFeeHistoryForStudent(studentName)
    }

    fun addFeeHistory(
        studentName: String,
        month: String,
        installment: String,
        amount: Double,
        status: String,
        dueDate: String = "2026-06-15",
        outstandingBalance: Double = 0.0
    ) {
        viewModelScope.launch {
            val fee = FeeHistoryEntity(
                studentName = studentName,
                month = month,
                installment = installment,
                amount = amount,
                status = status,
                dueDate = dueDate,
                outstandingBalance = outstandingBalance
            )
            repository.insertFeeHistory(fee)
            syncToCloud { uid -> cloudRepo.syncFeeToCloud(fee, uid) }
        }
    }

    fun updateFeeHistory(fee: FeeHistoryEntity) {
        viewModelScope.launch {
            repository.insertFeeHistory(fee)
            syncToCloud { uid -> cloudRepo.syncFeeToCloud(fee, uid) }
        }
    }

    fun updateSettingsLanguage(language: String) {
        viewModelScope.launch {
            val currentSettings = settings.value ?: SettingsEntity(
                orgName = "My Tuition Center",
                centerId = "CEN-${(1000..9999).random()}",
                contactPhone = "",
                upiId = "",
                language = "English",
                planName = "Free Tier",
                renewDate = "",
                maxStudents = 100,
                activeStaffCount = 1
            )
            val updated = currentSettings.copy(language = language)
            repository.insertSettings(updated)
            syncToCloud { uid -> cloudRepo.syncSettingsToCloud(updated, uid) }
        }
    }

    fun updateSettings(
        orgName: String? = null,
        contactPhone: String? = null,
        upiId: String? = null,
        planName: String? = null
    ) {
        viewModelScope.launch {
            val currentSettings = settings.value ?: SettingsEntity(
                orgName = "My Tuition Center",
                centerId = "CEN-${(1000..9999).random()}",
                contactPhone = "",
                upiId = "",
                language = "English",
                planName = "Free Tier",
                renewDate = "",
                maxStudents = 100,
                activeStaffCount = 1
            )
            val updated = currentSettings.copy(
                orgName = orgName ?: currentSettings.orgName,
                contactPhone = contactPhone ?: currentSettings.contactPhone,
                upiId = upiId ?: currentSettings.upiId,
                planName = planName ?: currentSettings.planName
            )
            repository.insertSettings(updated)
            syncToCloud { uid -> cloudRepo.syncSettingsToCloud(updated, uid) }
        }
    }

    fun insertSettings(settingsEntity: SettingsEntity) {
        viewModelScope.launch {
            repository.insertSettings(settingsEntity)
            syncToCloud { uid -> cloudRepo.syncSettingsToCloud(settingsEntity, uid) }
        }
    }

    // --- AI CHAT COMPONENT STATE & LOGIC ---

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(text = "Hello! I am your TuitionOS AI assistant. Ask me questions about student data, active staff, or fee balances (e.g. 'Who owes the most fees?')", isUser = false)
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun getGeminiApiKey(): String = securePrefs.getGeminiApiKey()

    fun saveGeminiApiKey(key: String) = securePrefs.saveGeminiApiKey(key)

    fun getGroqApiKey(): String = securePrefs.getGroqApiKey()

    fun saveGroqApiKey(key: String) = securePrefs.saveGroqApiKey(key)

    fun getActiveAiService(): String = securePrefs.getActiveAiService()

    fun saveActiveAiService(service: String) = securePrefs.saveActiveAiService(service)

    fun sendMessageToAi(question: String) {
        if (question.isBlank()) return
        
        val userMsg = ChatMessage(text = question, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        
        _isAiThinking.value = true
        
        viewModelScope.launch {
            val service = getActiveAiService()
            val apiKey = if (service == "Gemini") getGeminiApiKey() else getGroqApiKey()
            
            val response = try {
                AiChatManager.askAi(
                    apiKey = apiKey,
                    service = service,
                    question = question,
                    students = students.value,
                    feeHistory = feeHistory.value,
                    staff = staff.value,
                    batches = batches.value
                )
            } catch (e: Exception) {
                "Error processing request: ${e.localizedMessage}"
            }
            
            _chatMessages.value = _chatMessages.value + ChatMessage(text = response, isUser = false)
            _isAiThinking.value = false
        }
    }

    fun clearChatHistory() {
        val defaultMsg = ChatMessage(
            text = "Conversation history cleared. Ask me any details about students, fee status, or batches!",
            isUser = false
        )
        _chatMessages.value = listOf(defaultMsg)
    }
}
