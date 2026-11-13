package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val imageUri: String,
  val platform: String,
  val postType: String,
  val citationTimestamp: String,
  val citationSourceUrl: String,
  val confidenceScore: Double,
  val peopleSummary: String,
  val landmarksAndBackground: String,
  val extractedHandles: String,
  val mutualFriends: String,
  val captionText: String,
  val reconnectionQueries: String,
  val createdAtMillis: Long = System.currentTimeMillis()
) {
  val isHighAccuracy: Boolean
    get() = confidenceScore >= 0.85

  val accuracyPercent: Int
    get() = (confidenceScore * 100).toInt().coerceIn(0, 100)

  val handlesList: List<String>
    get() = extractedHandles.split(",").map { it.trim() }.filter { it.isNotEmpty() }

  val mutualFriendsList: List<String>
    get() = mutualFriends.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
