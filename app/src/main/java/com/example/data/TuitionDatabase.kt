package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        StudentRoomEntity::class,
        BatchRoomEntity::class,
        AttendanceRecordRoomEntity::class,
        FeeHistoryRoomEntity::class,
        LeadRoomEntity::class,
        StaffRoomEntity::class,
        SettingsRoomEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class TuitionDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun batchDao(): BatchDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao
    abstract fun feeHistoryDao(): FeeHistoryDao
    abstract fun leadDao(): LeadDao
    abstract fun staffDao(): StaffDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: TuitionDatabase? = null

        fun getInstance(context: Context): TuitionDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TuitionDatabase::class.java,
                    "tuition_os.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
