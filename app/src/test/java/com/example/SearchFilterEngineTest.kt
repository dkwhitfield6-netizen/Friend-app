package com.example

import com.example.data.model.ScreenshotEntity
import com.example.util.DashboardFilterState
import com.example.util.SearchFilterEngine
import com.example.util.SearchScope
import com.example.util.SortOrder
import com.example.util.TimestampPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFilterEngineTest {

  private val nowMillis = 1793664000000L // 2026-11-03T00:00:00Z

  private val sampleRecords = listOf(
    ScreenshotEntity(
      id = 1,
      imageUri = "uri1",
      platform = "Instagram",
      postType = "Story & Feed",
      citationTimestamp = "2026-10-18T14:32:00Z (2w ago)",
      citationSourceUrl = "https://www.instagram.com/p/DB18xYmPq9z/",
      confidenceScore = 0.94,
      peopleSummary = "2 people in frame, graduation attire, smiling",
      landmarksAndBackground = "Stanford University Quad & Memorial Church arches",
      extractedHandles = "@maya_lin_art, @jordan.codes",
      mutualFriends = "@devon_k, @alyssa_w",
      captionText = "Catching up in the Bay Area #alumni #classof2021",
      reconnectionQueries = "https://www.google.com/search?q=maya_lin_art",
      createdAtMillis = 1792333920000L // Oct 18
    ),
    ScreenshotEntity(
      id = 2,
      imageUri = "uri2",
      platform = "X (Twitter)",
      postType = "Thread / Screenshot",
      citationTimestamp = "2026-10-29T19:15:22Z (4d ago)",
      citationSourceUrl = "https://x.com/tech_marcus/status/1851349827102938471",
      confidenceScore = 0.91,
      peopleSummary = "1 person, speaker profile, keynote stage",
      landmarksAndBackground = "TechCrunch Disrupt San Francisco mainstage backdrop",
      extractedHandles = "@tech_marcus, @ai_founders_club",
      mutualFriends = "@elena_ux, @david_sf_founder",
      captionText = "Demoing vector search engine live on stage #vectorsearch",
      reconnectionQueries = "https://www.google.com/search?q=tech_marcus",
      createdAtMillis = 1793301322000L // Oct 29
    ),
    ScreenshotEntity(
      id = 3,
      imageUri = "uri3",
      platform = "TikTok",
      postType = "Animated GIF / Clip",
      citationTimestamp = "2026-11-01T08:44:10Z (1d ago)",
      citationSourceUrl = "https://www.tiktok.com/@charlie_hikes/video/739482019482019",
      confidenceScore = 0.89,
      peopleSummary = "Group of 3 hikers, outdoor gear",
      landmarksAndBackground = "Half Dome summit, Yosemite National Park",
      extractedHandles = "@charlie_hikes, @tanya_trail",
      mutualFriends = "@outdoor_club_norcal, @kevin_b",
      captionText = "Sunrise at sub-dome! #hiking #yosemite",
      reconnectionQueries = "https://www.google.com/search?q=charlie_hikes",
      createdAtMillis = 1793522650000L // Nov 1
    ),
    ScreenshotEntity(
      id = 4,
      imageUri = "uri4",
      platform = "LinkedIn",
      postType = "Update / Article",
      citationTimestamp = "2026-10-15T11:20:00Z (3w ago)",
      citationSourceUrl = "https://www.linkedin.com/posts/sarah-chen-design",
      confidenceScore = 0.96,
      peopleSummary = "Professional headshot, studio portrait lighting",
      landmarksAndBackground = "Modern office interior with collaborative whiteboard",
      extractedHandles = "sarah-chen-design, @chen_designs",
      mutualFriends = "Alex Rivera, Michael Torres",
      captionText = "Design tokens framework for Stanford alumni Stanford showcase #designsystems",
      reconnectionQueries = "https://www.google.com/search?q=sarah-chen-design",
      createdAtMillis = 1792063200000L // Oct 15
    )
  )

  @Test
  fun `search by name scope matches handles and mutuals only`() {
    val results = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(
        searchQuery = "Alex Rivera",
        searchScope = SearchScope.NAME
      ),
      nowMillis = nowMillis
    )
    assertEquals(1, results.size)
    assertEquals(4L, results.first().entity.id)
    assertTrue(results.first().matchedFields.contains("Mutuals"))
  }

  @Test
  fun `search by landmarks scope isolates landmark matches from caption mentions`() {
    val results = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(
        searchQuery = "Stanford",
        searchScope = SearchScope.LANDMARKS
      ),
      nowMillis = nowMillis
    )
    // Only record 1 has Stanford in landmarksAndBackground (record 4 only has it in captionText)
    assertEquals(1, results.size)
    assertEquals(1L, results.first().entity.id)
    assertTrue(results.first().matchedFields.contains("Landmark"))
  }

  @Test
  fun `search by keywords scope matches extracted hashtags and keywords`() {
    val results = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(
        searchQuery = "#alumni",
        searchScope = SearchScope.KEYWORDS
      ),
      nowMillis = nowMillis
    )
    assertEquals(1, results.size)
    assertEquals(1L, results.first().entity.id)
  }

  @Test
  fun `multi-platform filter returns union of selected platforms`() {
    val results = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(
        selectedPlatforms = setOf("Instagram", "TikTok")
      ),
      nowMillis = nowMillis
    )
    assertEquals(2, results.size)
    assertTrue(results.any { it.entity.platform == "Instagram" })
    assertTrue(results.any { it.entity.platform == "TikTok" })
  }

  @Test
  fun `timestamp range preset and custom range filter accurately`() {
    val last7Days = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(
        timestampPreset = TimestampPreset.LAST_7_DAYS
      ),
      nowMillis = nowMillis
    )
    // Oct 29 (5 days before Nov 3) and Nov 1 (2 days before Nov 3)
    assertEquals(2, last7Days.size)

    // Custom range covering Oct 15 - Oct 20
    val oct15 = 1792022400000L
    val oct20 = 1792454400000L
    val customRange = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(
        timestampPreset = TimestampPreset.CUSTOM,
        customStartMillis = oct15,
        customEndMillis = oct20
      ),
      nowMillis = nowMillis
    )
    assertEquals(2, customRange.size)
    assertTrue(customRange.any { it.entity.id == 1L })
    assertTrue(customRange.any { it.entity.id == 4L })
  }

  @Test
  fun `sorting by relevance, date added, and accuracy score orders correctly`() {
    val byAccuracy = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(sortOrder = SortOrder.ACCURACY_SCORE),
      nowMillis = nowMillis
    )
    assertEquals(listOf(4L, 1L, 2L, 3L), byAccuracy.map { it.entity.id })

    val byNewest = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(sortOrder = SortOrder.DATE_ADDED_NEWEST),
      nowMillis = nowMillis
    )
    assertEquals(listOf(3L, 2L, 1L, 4L), byNewest.map { it.entity.id })

    val byOldest = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(sortOrder = SortOrder.DATE_ADDED_OLDEST),
      nowMillis = nowMillis
    )
    assertEquals(listOf(4L, 1L, 2L, 3L), byOldest.map { it.entity.id })

    // Relevance: "Stanford" matches record 1 in Landmark (+55) vs record 4 in Keyword (+35)
    val byRelevance = SearchFilterEngine.filterAndSort(
      records = sampleRecords,
      filterState = DashboardFilterState(
        searchQuery = "Stanford",
        searchScope = SearchScope.ALL,
        sortOrder = SortOrder.RELEVANCE
      ),
      nowMillis = nowMillis
    )
    assertEquals(listOf(1L, 4L), byRelevance.map { it.entity.id })
  }
}
