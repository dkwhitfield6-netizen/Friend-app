package com.example.util

import com.example.data.model.ScreenshotEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class SearchScope(val label: String, val placeholder: String) {
  ALL("All", "Search names, landmarks, keywords, platforms..."),
  NAME("Name / Handle", "Search by friend name, @handle, or mutuals..."),
  LANDMARKS("Landmarks", "Search landmarks, venues, or backgrounds..."),
  KEYWORDS("Keywords", "Search extracted image keywords or captions..."),
  PLATFORM("Platform", "Search social platform or post type...")
}

enum class SortOrder(val label: String) {
  RELEVANCE("Relevance"),
  DATE_ADDED_NEWEST("Date Added"),
  DATE_ADDED_OLDEST("Oldest First"),
  ACCURACY_SCORE("Accuracy Score")
}

enum class TimestampPreset(val label: String) {
  ALL_TIME("All Time"),
  LAST_24_HOURS("Last 24h"),
  LAST_7_DAYS("Last 7 Days"),
  LAST_30_DAYS("Last 30 Days"),
  CUSTOM("Custom Range")
}

data class DashboardFilterState(
  val searchQuery: String = "",
  val searchScope: SearchScope = SearchScope.ALL,
  val selectedPlatforms: Set<String> = emptySet(), // empty = All platforms
  val timestampPreset: TimestampPreset = TimestampPreset.ALL_TIME,
  val customStartMillis: Long? = null,
  val customEndMillis: Long? = null,
  val minAccuracyPercent: Int = 0,
  val sortOrder: SortOrder = SortOrder.DATE_ADDED_NEWEST
) {
  val activeFilterCount: Int
    get() {
      var count = 0
      if (selectedPlatforms.isNotEmpty()) count++
      if (timestampPreset != TimestampPreset.ALL_TIME) count++
      if (minAccuracyPercent > 0) count++
      if (searchScope != SearchScope.ALL) count++
      return count
    }

  val hasAnyActiveFilterOrQuery: Boolean
    get() = searchQuery.isNotBlank() || activeFilterCount > 0
}

data class SearchMatchResult(
  val entity: ScreenshotEntity,
  val relevanceScore: Double,
  val matchedFields: List<String>
)

object SearchFilterEngine {

  val supportedPlatforms = listOf(
    "Instagram",
    "X (Twitter)",
    "TikTok",
    "LinkedIn",
    "Facebook",
    "Discord",
    "Reddit"
  )

  private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }

  private val shortDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

  private val stopWords = setOf(
    "the", "and", "for", "with", "from", "that", "this", "are", "was", "have",
    "has", "had", "not", "but", "you", "your", "our", "out", "about", "been",
    "its", "it's", "can't", "who", "won't", "any", "all", "one", "person",
    "detected", "high", "frame", "into", "over", "during", "when", "they"
  )

  /**
   * Parses the ISO-8601 timestamp prefix from [ScreenshotEntity.citationTimestamp] if available,
   * falling back to [ScreenshotEntity.createdAtMillis].
   */
  fun resolveTimestampMillis(entity: ScreenshotEntity): Long {
    val raw = entity.citationTimestamp.trim()
    val isoCandidate = raw.substringBefore(" ").trim()
    return try {
      synchronized(isoDateFormat) {
        isoDateFormat.parse(isoCandidate)?.time
      } ?: entity.createdAtMillis
    } catch (_: Exception) {
      entity.createdAtMillis
    }
  }

  /**
   * Extracts searchable keywords from caption hashtags, landmarks, post type, and visual summary.
   */
  fun extractImageKeywords(entity: ScreenshotEntity): List<String> {
    val keywords = linkedSetOf<String>()

    // 1. Hashtags from caption
    val hashtagRegex = Regex("#[A-Za-z0-9_]+")
    hashtagRegex.findAll(entity.captionText).forEach { match ->
      keywords.add(match.value)
    }

    // 2. Post type tokens
    entity.postType.split("/", "&", ",").map { it.trim() }.filter { it.length > 2 }.forEach {
      keywords.add(it)
    }

    // 3. Key capitalized or significant terms from landmarks and people summary
    val combinedText = "${entity.landmarksAndBackground} ${entity.peopleSummary} ${entity.captionText}"
    combinedText
      .replace(Regex("[^A-Za-z0-9_\\s-]"), " ")
      .split(Regex("\\s+"))
      .map { it.trim() }
      .filter { token ->
        token.length >= 4 && token.lowercase() !in stopWords && !token.startsWith("http")
      }
      .take(12)
      .forEach { keywords.add(it) }

    return keywords.toList()
  }

  fun formatDateRangeLabel(startMillis: Long?, endMillis: Long?): String {
    if (startMillis == null && endMillis == null) return "Custom Range"
    val startStr = startMillis?.let { synchronized(shortDateFormat) { shortDateFormat.format(Date(it)) } } ?: "Start"
    val endStr = endMillis?.let { synchronized(shortDateFormat) { shortDateFormat.format(Date(it)) } } ?: "Now"
    return "$startStr – $endStr"
  }

  fun filterAndSort(
    records: List<ScreenshotEntity>,
    filterState: DashboardFilterState,
    nowMillis: Long = System.currentTimeMillis()
  ): List<SearchMatchResult> {
    val queryTokens = filterState.searchQuery
      .trim()
      .lowercase()
      .split(Regex("\\s+"))
      .filter { it.isNotEmpty() }

    val (rangeStart, rangeEnd) = resolveDateWindow(filterState, nowMillis)

    val filtered = records.mapNotNull { entity ->
      // 1. Platform filter
      if (filterState.selectedPlatforms.isNotEmpty()) {
        val matchesPlatform = filterState.selectedPlatforms.any {
          it.equals(entity.platform, ignoreCase = true)
        }
        if (!matchesPlatform) return@mapNotNull null
      }

      // 2. Minimum accuracy filter
      if (entity.accuracyPercent < filterState.minAccuracyPercent) {
        return@mapNotNull null
      }

      // 3. Timestamp range filter (matches either citation timestamp or date added)
      if (rangeStart != null || rangeEnd != null) {
        val citationMillis = resolveTimestampMillis(entity)
        val inCitationRange = (rangeStart == null || citationMillis >= rangeStart) &&
          (rangeEnd == null || citationMillis <= rangeEnd)
        val inCreatedRange = (rangeStart == null || entity.createdAtMillis >= rangeStart) &&
          (rangeEnd == null || entity.createdAtMillis <= rangeEnd)
        if (!inCitationRange && !inCreatedRange) {
          return@mapNotNull null
        }
      }

      // 4. Scoped search & relevance scoring
      if (queryTokens.isEmpty()) {
        val baseScore = (entity.confidenceScore * 50.0) +
          (resolveTimestampMillis(entity).toDouble() / 1_000_000_000_000.0)
        SearchMatchResult(
          entity = entity,
          relevanceScore = baseScore,
          matchedFields = emptyList()
        )
      } else {
        scoreEntityForQuery(entity, queryTokens, filterState.searchScope)
      }
    }

    // 5. Sort
    return when (filterState.sortOrder) {
      SortOrder.RELEVANCE -> filtered.sortedWith(
        compareByDescending<SearchMatchResult> { it.relevanceScore }
          .thenByDescending { it.entity.confidenceScore }
          .thenByDescending { it.entity.createdAtMillis }
      )
      SortOrder.DATE_ADDED_NEWEST -> filtered.sortedWith(
        compareByDescending<SearchMatchResult> { it.entity.createdAtMillis }
          .thenByDescending { resolveTimestampMillis(it.entity) }
          .thenByDescending { it.entity.id }
      )
      SortOrder.DATE_ADDED_OLDEST -> filtered.sortedWith(
        compareBy<SearchMatchResult> { it.entity.createdAtMillis }
          .thenBy { resolveTimestampMillis(it.entity) }
          .thenBy { it.entity.id }
      )
      SortOrder.ACCURACY_SCORE -> filtered.sortedWith(
        compareByDescending<SearchMatchResult> { it.entity.confidenceScore }
          .thenByDescending { it.entity.createdAtMillis }
      )
    }
  }

  private fun resolveDateWindow(
    filterState: DashboardFilterState,
    nowMillis: Long
  ): Pair<Long?, Long?> {
    val dayMs = 24L * 60L * 60L * 1000L
    return when (filterState.timestampPreset) {
      TimestampPreset.ALL_TIME -> null to null
      TimestampPreset.LAST_24_HOURS -> (nowMillis - dayMs) to null
      TimestampPreset.LAST_7_DAYS -> (nowMillis - 7L * dayMs) to null
      TimestampPreset.LAST_30_DAYS -> (nowMillis - 30L * dayMs) to null
      TimestampPreset.CUSTOM -> {
        // End date should include the full selected UTC day (+ 23h 59m 59s)
        val adjustedEnd = filterState.customEndMillis?.let { it + dayMs - 1L }
        filterState.customStartMillis to adjustedEnd
      }
    }
  }

  private fun scoreEntityForQuery(
    entity: ScreenshotEntity,
    tokens: List<String>,
    scope: SearchScope
  ): SearchMatchResult? {
    val handles = entity.extractedHandles.lowercase()
    val mutuals = entity.mutualFriends.lowercase()
    val people = entity.peopleSummary.lowercase()
    val landmarks = entity.landmarksAndBackground.lowercase()
    val platform = entity.platform.lowercase()
    val postType = entity.postType.lowercase()
    val caption = entity.captionText.lowercase()
    val timestamp = entity.citationTimestamp.lowercase()
    val keywordsText = extractImageKeywords(entity).joinToString(" ").lowercase()

    val matchedFields = linkedSetOf<String>()
    var totalScore = 0.0
    var matchedTokenCount = 0

    for (token in tokens) {
      var tokenMatchedInScope = false

      val matchHandle = handles.contains(token)
      val matchMutual = mutuals.contains(token)
      val matchPeople = people.contains(token)
      val matchLandmark = landmarks.contains(token)
      val matchPlatform = platform.contains(token) || postType.contains(token)
      val matchKeyword = caption.contains(token) || keywordsText.contains(token)
      val matchTimestamp = timestamp.contains(token)

      when (scope) {
        SearchScope.ALL -> {
          if (matchHandle || matchMutual || matchPeople || matchLandmark || matchPlatform || matchKeyword || matchTimestamp) {
            tokenMatchedInScope = true
          }
        }
        SearchScope.NAME -> {
          if (matchHandle || matchMutual || matchPeople) {
            tokenMatchedInScope = true
          }
        }
        SearchScope.LANDMARKS -> {
          if (matchLandmark) {
            tokenMatchedInScope = true
          }
        }
        SearchScope.KEYWORDS -> {
          if (matchKeyword || matchPeople || matchLandmark || postType.contains(token)) {
            tokenMatchedInScope = true
          }
        }
        SearchScope.PLATFORM -> {
          if (matchPlatform) {
            tokenMatchedInScope = true
          }
        }
      }

      if (!tokenMatchedInScope) {
        return null // Every search token must match within the chosen scope
      }

      matchedTokenCount++

      if (matchHandle) {
        matchedFields.add("Handle")
        totalScore += if (handles.split(",").any { it.trim().removePrefix("@") == token.removePrefix("@") }) 100.0 else 65.0
      }
      if (matchMutual) {
        matchedFields.add("Mutuals")
        totalScore += 50.0
      }
      if (matchPeople) {
        matchedFields.add("People")
        totalScore += 40.0
      }
      if (matchLandmark) {
        matchedFields.add("Landmark")
        totalScore += 55.0
      }
      if (matchPlatform) {
        matchedFields.add("Platform")
        totalScore += 45.0
      }
      if (matchKeyword) {
        matchedFields.add("Keywords")
        totalScore += 35.0
      }
      if (matchTimestamp) {
        matchedFields.add("Timestamp")
        totalScore += 25.0
      }
    }

    if (matchedTokenCount == 0) return null

    // Add accuracy bonus as tie-breaker
    totalScore += entity.confidenceScore * 15.0

    return SearchMatchResult(
      entity = entity,
      relevanceScore = totalScore,
      matchedFields = matchedFields.toList()
    )
  }
}
