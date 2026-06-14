package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun findById(userId: Int): UserEntity?
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun observeAll(): Flow<List<StudentRoomEntity>>

    @Query("SELECT * FROM students")
    suspend fun getAllSync(): List<StudentRoomEntity>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Int): StudentRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(student: StudentRoomEntity): Long

    @Update
    suspend fun update(student: StudentRoomEntity)

    @Delete
    suspend fun delete(student: StudentRoomEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY name ASC")
    fun observeAll(): Flow<List<BatchRoomEntity>>

    @Query("SELECT * FROM batches")
    suspend fun getAllSync(): List<BatchRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(batch: BatchRoomEntity): Long

    @Update
    suspend fun update(batch: BatchRoomEntity)

    @Delete
    suspend fun delete(batch: BatchRoomEntity)

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM batches")
    suspend fun deleteAll()
}

@Dao
interface AttendanceRecordDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun observeAll(): Flow<List<AttendanceRecordRoomEntity>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllSync(): List<AttendanceRecordRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecordRoomEntity): Long

    @Update
    suspend fun update(record: AttendanceRecordRoomEntity)

    @Delete
    suspend fun delete(record: AttendanceRecordRoomEntity)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()
}

@Dao
interface FeeHistoryDao {
    @Query("SELECT * FROM fee_history ORDER BY dueDate DESC")
    fun observeAll(): Flow<List<FeeHistoryRoomEntity>>

    @Query("SELECT * FROM fee_history")
    suspend fun getAllSync(): List<FeeHistoryRoomEntity>

    @Query("SELECT * FROM fee_history WHERE studentName = :studentName ORDER BY dueDate DESC")
    fun observeForStudent(studentName: String): Flow<List<FeeHistoryRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(fee: FeeHistoryRoomEntity): Long

    @Delete
    suspend fun delete(fee: FeeHistoryRoomEntity)

    @Query("DELETE FROM fee_history WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM fee_history WHERE studentName = :studentName")
    suspend fun deleteByStudentName(studentName: String)

    @Query("DELETE FROM fee_history")
    suspend fun deleteAll()
}

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY id DESC")
    fun observeAll(): Flow<List<LeadRoomEntity>>

    @Query("SELECT * FROM leads")
    suspend fun getAllSync(): List<LeadRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lead: LeadRoomEntity): Long

    @Update
    suspend fun update(lead: LeadRoomEntity)

    @Delete
    suspend fun delete(lead: LeadRoomEntity)

    @Query("DELETE FROM leads WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM leads")
    suspend fun deleteAll()
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun observeAll(): Flow<List<StaffRoomEntity>>

    @Query("SELECT * FROM staff")
    suspend fun getAllSync(): List<StaffRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(staff: StaffRoomEntity): Long

    @Update
    suspend fun update(staff: StaffRoomEntity)

    @Delete
    suspend fun delete(staff: StaffRoomEntity)

    @Query("DELETE FROM staff WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM staff")
    suspend fun deleteAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<SettingsRoomEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SettingsRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsRoomEntity): Long

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}
