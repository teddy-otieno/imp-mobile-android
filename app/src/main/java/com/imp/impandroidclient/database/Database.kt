package com.imp.impandroidclient.database

import androidx.room.*

@Entity
data class SessionCredentials(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "refreshToken") val refreshToken: String?
)

@Dao
interface SessionCredentialsDao {

    @Query("SELECT refreshToken FROM SessionCredentials")
    fun getCurrentCredentials(): String?

    @Query("INSERT INTO SessionCredentials(refreshToken) values (:token)")
    fun addRefreshToken(token: String)
}


@Database(entities = [SessionCredentials::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionCredentialsDao
}

