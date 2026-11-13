package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.ScreenshotEntity
import org.json.JSONArray
import org.json.JSONObject

enum class ExportFormat {
  JSON, CSV
}

object CitationExporter {

  fun generateJson(records: List<ScreenshotEntity>): String {
    val root = JSONObject()
    root.put("appName", "ReConnect")
    root.put("version", "1.0")
    root.put("exportedAt", System.currentTimeMillis())
    root.put("totalRecords", records.size)

    val avgAccuracy = if (records.isNotEmpty()) {
      (records.map { it.confidenceScore }.average() * 100).toInt() / 100.0
    } else 0.0
    root.put("averageAccuracyRate", avgAccuracy)
    root.put("accuracyBenchmarkMet", avgAccuracy >= 0.85)

    val array = JSONArray()
    for (item in records) {
      val obj = JSONObject()
      obj.put("id", item.id)
      obj.put("platform", item.platform)
      obj.put("postType", item.postType)
      obj.put("citationTimestamp", item.citationTimestamp)
      obj.put("citationSourceUrl", item.citationSourceUrl)
      obj.put("confidenceScore", item.confidenceScore)
      obj.put("accuracyPercent", item.accuracyPercent)
      obj.put("peopleSummary", item.peopleSummary)
      obj.put("landmarksAndBackground", item.landmarksAndBackground)
      obj.put("extractedHandles", item.extractedHandles)
      obj.put("mutualFriends", item.mutualFriends)
      obj.put("captionText", item.captionText)
      obj.put("reconnectionQueries", item.reconnectionQueries)
      obj.put("createdAtMillis", item.createdAtMillis)

      // Search engine / DB optimization keywords
      val keywords = JSONArray()
      keywords.put(item.platform.lowercase())
      item.handlesList.forEach { keywords.put(it) }
      item.mutualFriendsList.forEach { keywords.put(it) }
      obj.put("searchOptimizationKeywords", keywords)

      array.put(obj)
    }
    root.put("indexedScreenshots", array)

    return root.toString(2)
  }

  fun generateCsv(records: List<ScreenshotEntity>): String {
    val sb = StringBuilder()
    // Headers
    sb.append("ID,Platform,PostType,AccuracyPercent,ConfidenceScore,CitationTimestamp,CitationSourceUrl,Handles,MutualFriends,LandmarksAndBackground,PeopleSummary,ReconnectionLink\n")

    for (item in records) {
      sb.append(item.id).append(",")
      sb.append(escapeCsv(item.platform)).append(",")
      sb.append(escapeCsv(item.postType)).append(",")
      sb.append(item.accuracyPercent).append("%,")
      sb.append(item.confidenceScore).append(",")
      sb.append(escapeCsv(item.citationTimestamp)).append(",")
      sb.append(escapeCsv(item.citationSourceUrl)).append(",")
      sb.append(escapeCsv(item.extractedHandles)).append(",")
      sb.append(escapeCsv(item.mutualFriends)).append(",")
      sb.append(escapeCsv(item.landmarksAndBackground)).append(",")
      sb.append(escapeCsv(item.peopleSummary)).append(",")
      sb.append(escapeCsv(item.reconnectionQueries)).append("\n")
    }

    return sb.toString()
  }

  private fun escapeCsv(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
      "\"$escaped\""
    } else {
      escaped
    }
  }

  fun shareExportData(context: Context, format: ExportFormat, records: List<ScreenshotEntity>) {
    val textData = when (format) {
      ExportFormat.JSON -> generateJson(records)
      ExportFormat.CSV -> generateCsv(records)
    }

    val mimeType = when (format) {
      ExportFormat.JSON -> "application/json"
      ExportFormat.CSV -> "text/csv"
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, "ReConnect_Analytics_Export_${format.name}")
      putExtra(Intent.EXTRA_TEXT, textData)
    }
    context.startActivity(Intent.createChooser(intent, "Export ${format.name} Analytics & Citations"))
  }
}
