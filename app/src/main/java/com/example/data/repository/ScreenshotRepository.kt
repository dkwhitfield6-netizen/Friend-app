package com.example.data.repository

import com.example.data.db.ScreenshotDao
import com.example.data.model.ScreenshotEntity
import kotlinx.coroutines.flow.Flow

class ScreenshotRepository(private val dao: ScreenshotDao) {
  val allScreenshots: Flow<List<ScreenshotEntity>> = dao.getAllScreenshots()
  val count: Flow<Int> = dao.getCount()
  val averageAccuracy: Flow<Double?> = dao.getAverageAccuracy()

  fun search(query: String): Flow<List<ScreenshotEntity>> {
    return if (query.isBlank()) {
      dao.getAllScreenshots()
    } else {
      dao.searchScreenshots(query.trim())
    }
  }

  fun filterByPlatform(platform: String): Flow<List<ScreenshotEntity>> {
    return if (platform.equals("All", ignoreCase = true)) {
      dao.getAllScreenshots()
    } else {
      dao.getScreenshotsByPlatform(platform)
    }
  }

  suspend fun insert(entity: ScreenshotEntity): Long = dao.insertScreenshot(entity)

  suspend fun deleteById(id: Long) = dao.deleteScreenshotById(id)

  suspend fun seedInitialDataIfEmpty() {
    // Initial seeded demonstrations of indexed social screenshots for immediate discovery & analytics
    val samples = listOf(
      ScreenshotEntity(
        imageUri = "sample_ig_reconnect",
        platform = "Instagram",
        postType = "Story & Feed",
        citationTimestamp = "2026-10-18T14:32:00Z (2w ago)",
        citationSourceUrl = "https://www.instagram.com/p/DB18xYmPq9z/",
        confidenceScore = 0.94,
        peopleSummary = "2 people in frame, high-fidelity facial features, casual graduation attire, smiling",
        landmarksAndBackground = "Stanford University Quad & Memorial Church arches, sunny afternoon lighting",
        extractedHandles = "@maya_lin_art, @jordan.codes",
        mutualFriends = "@devon_k, @alyssa_w, @sam_reed_99",
        captionText = "Can't believe it's been 5 years since senior project days! Catching up in the Bay Area #alumni #classof2021",
        reconnectionQueries = "https://www.google.com/search?q=maya_lin_art+stanford+alumni",
        createdAtMillis = 1792333920000L // 2026-10-18
      ),
      ScreenshotEntity(
        imageUri = "sample_x_reconnect",
        platform = "X (Twitter)",
        postType = "Thread / Screenshot",
        citationTimestamp = "2026-10-29T19:15:22Z (4d ago)",
        citationSourceUrl = "https://x.com/tech_marcus/status/1851349827102938471",
        confidenceScore = 0.91,
        peopleSummary = "1 person, speaker profile, keynote stage, glasses, microphone attached",
        landmarksAndBackground = "TechCrunch Disrupt San Francisco mainstage backdrop, neon blue luminescent lighting",
        extractedHandles = "@tech_marcus, @ai_founders_club",
        mutualFriends = "@elena_ux, @david_sf_founder",
        captionText = "Demoing the new open-source vector search engine live on stage. Ping me if you're attending!",
        reconnectionQueries = "https://www.google.com/search?q=tech_marcus+TechCrunch+Disrupt+San+Francisco",
        createdAtMillis = 1793301322000L // 2026-10-29
      ),
      ScreenshotEntity(
        imageUri = "sample_tiktok_reconnect",
        platform = "TikTok",
        postType = "Animated GIF / Clip",
        citationTimestamp = "2026-11-01T08:44:10Z (1d ago)",
        citationSourceUrl = "https://www.tiktok.com/@charlie_hikes/video/739482019482019",
        confidenceScore = 0.89,
        peopleSummary = "Group of 3 hikers, outdoor gear, backpacks, high-contrast facial recognition landmarks",
        landmarksAndBackground = "Half Dome summit, Yosemite National Park, granite cliffs and pine forest horizon",
        extractedHandles = "@charlie_hikes, @tanya_trail",
        mutualFriends = "@outdoor_club_norcal, @kevin_b",
        captionText = "Sunrise at sub-dome! Never hike with anyone who won't wake up at 3am haha",
        reconnectionQueries = "https://www.google.com/search?q=charlie_hikes+Yosemite+Half+Dome",
        createdAtMillis = 1793522650000L // 2026-11-01
      ),
      ScreenshotEntity(
        imageUri = "sample_linkedin_reconnect",
        platform = "LinkedIn",
        postType = "Update / Article",
        citationTimestamp = "2026-10-15T11:20:00Z (3w ago)",
        citationSourceUrl = "https://www.linkedin.com/posts/sarah-chen-design_designsystems-accessibility-activity-72548291048",
        confidenceScore = 0.96,
        peopleSummary = "Professional headshot, clear facial profile, studio portrait lighting, navy blazer",
        landmarksAndBackground = "Modern office interior with collaborative whiteboard and design mockups",
        extractedHandles = "sarah-chen-design, @chen_designs",
        mutualFriends = "Alex Rivera (Design Lead), Michael Torres",
        captionText = "Thrilled to share our new design tokens framework for accessible Android apps! Check out our open-source repo.",
        reconnectionQueries = "https://www.google.com/search?q=sarah-chen-design+LinkedIn+design+systems",
        createdAtMillis = 1792063200000L // 2026-10-15
      )
    )

    for (sample in samples) {
      dao.insertScreenshot(sample)
    }
  }
}
