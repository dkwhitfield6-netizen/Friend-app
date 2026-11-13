package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ScreenshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
  @Query("SELECT * FROM screenshots ORDER BY createdAtMillis DESC")
  fun getAllScreenshots(): Flow<List<ScreenshotEntity>>

  @Query(
    """
    SELECT * FROM screenshots 
    WHERE platform LIKE '%' || :query || '%' 
       OR extractedHandles LIKE '%' || :query || '%' 
       OR peopleSummary LIKE '%' || :query || '%' 
       OR landmarksAndBackground LIKE '%' || :query || '%' 
       OR mutualFriends LIKE '%' || :query || '%' 
       OR captionText LIKE '%' || :query || '%' 
    ORDER BY createdAtMillis DESC
    """
  )
  fun searchScreenshots(query: String): Flow<List<ScreenshotEntity>>

  @Query("SELECT * FROM screenshots WHERE platform = :platform ORDER BY createdAtMillis DESC")
  fun getScreenshotsByPlatform(platform: String): Flow<List<ScreenshotEntity>>

  @Query("SELECT * FROM screenshots WHERE id = :id LIMIT 1")
  suspend fun getScreenshotById(id: Long): ScreenshotEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScreenshot(entity: ScreenshotEntity): Long

  @Delete
  suspend fun deleteScreenshot(entity: ScreenshotEntity)

  @Query("DELETE FROM screenshots WHERE id = :id")
  suspend fun deleteScreenshotById(id: Long)

  @Query("SELECT COUNT(*) FROM screenshots")
  fun getCount(): Flow<Int>

  @Query("SELECT AVG(confidenceScore) FROM screenshots")
  fun getAverageAccuracy(): Flow<Double?>
}
