package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.local.dao.LeadDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.PipelineStatus

class Converters {
    @TypeConverter
    fun fromPipelineStatus(status: PipelineStatus): String = status.name

    @TypeConverter
    fun toPipelineStatus(value: String): PipelineStatus = try {
        PipelineStatus.valueOf(value)
    } catch (e: Exception) {
        PipelineStatus.NEW
    }
}

@Database(
    entities = [LeadEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gemi_leads_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
