package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ScreenshotEntity
import com.example.util.CitationExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ReConnect", appName)
  }

  @Test
  fun `verify citation exporter json and csv outputs`() {
    val sample = ScreenshotEntity(
      id = 1,
      imageUri = "test_uri",
      platform = "Instagram",
      postType = "Story",
      citationTimestamp = "2026-11-02T16:00:00Z",
      citationSourceUrl = "https://instagram.com/p/test12345",
      confidenceScore = 0.94,
      peopleSummary = "1 person detected, smiling",
      landmarksAndBackground = "Stanford Quad arches",
      extractedHandles = "@alex_smith",
      mutualFriends = "Followed by @sarah and 3 others",
      captionText = "Great memories!",
      reconnectionQueries = "https://google.com/search?q=alex_smith"
    )

    assertTrue(sample.isHighAccuracy)
    assertEquals(94, sample.accuracyPercent)

    val json = CitationExporter.generateJson(listOf(sample))
    assertTrue(json.contains("ReConnect"))
    assertTrue(json.contains("accuracyBenchmarkMet\": true"))
    assertTrue(json.contains("@alex_smith"))

    val csv = CitationExporter.generateCsv(listOf(sample))
    assertTrue(csv.contains("Instagram"))
    assertTrue(csv.contains("94%"))
    assertTrue(csv.contains("https://instagram.com/p/test12345"))
  }
}
