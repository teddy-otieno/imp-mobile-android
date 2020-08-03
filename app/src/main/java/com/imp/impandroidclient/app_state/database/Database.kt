package com.imp.impandroidclient.app_state.database

import android.content.Context
import android.graphics.Bitmap
import androidx.room.*


@Entity
data class SessionCredentials(
    @PrimaryKey val pk: Int,
    @ColumnInfo(name = "uid") val uid: Int,
    @ColumnInfo(name = "refreshToken") val refreshToken: String?
)

@Entity(tableName = "Users")
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int
)

@Entity(tableName = "Submissions",
    foreignKeys = [ForeignKey(entity = User::class,
        parentColumns = ["uid"], childColumns = ["userUid"])])
data class Submission (
    @PrimaryKey val uid: Int,
    @ColumnInfo val postCaption: String,
    @ColumnInfo val feeRates: Int,
    @ColumnInfo val timeOfSubmission: String,
    @ColumnInfo val campaignId: String,
    val userUid: Int
)

@Entity(tableName = "SubmissionImages",
    foreignKeys = [ForeignKey(entity = Submission::class,
        parentColumns = ["uid"], childColumns = ["submissionId"], onDelete = ForeignKey.CASCADE)])
data class SubmissionImage (
   @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo val imagePath : String,
    @ColumnInfo val submissionId: Int
)
{
    @Ignore val image: Bitmap

    init {
        image = loadImage()
    }

    private fun loadImage(): Bitmap {
        TODO("Not Implemented")
    }
}


@Dao
interface SessionCredentialsDao {
    @Query("SELECT refreshToken FROM SessionCredentials WHERE uid=0;")
    fun getCurrentCredentials(): String?

    @Query("UPDATE SessionCredentials SET refreshToken = (:token) WHERE uid=0;")
    fun addRefreshToken(token: String)

    @Query("INSERT INTO SessionCredentials (uid, refreshToken) VALUES (0, :token)")
    fun insertToken(token: String)
}

@Dao
interface UserDao { }

@Dao interface SubmissionsDao {

    @Query("SELECT * FROM Submissions WHERE uid = (:id)")
    fun getSubmissionById(id: Int): Submission

    @Query("SELECT * FROM SubmissionImages WHERE submissionId = (:id)")
    fun getSubmissionImageForSubmission(id: Int) : List<SubmissionImage>
}

@Database(entities = [
    SessionCredentials::class,
    User::class,
    Submission::class,
    SubmissionImage::class
], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionCredentialsDao
    abstract fun userDao() : UserDao
    abstract fun submissionQueries() : SubmissionsDao


    companion object {
        private const val dbName = "imp-client-db"
        private lateinit var database: AppDatabase

        fun createDatabase(context: Context) {
            //context.deleteDatabase(dbName) //FIXME: Remove this during release

            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                dbName)
                .fallbackToDestructiveMigration()
                .build()
        }

        fun getDatabaseInstance(): AppDatabase =  database
    }
}

