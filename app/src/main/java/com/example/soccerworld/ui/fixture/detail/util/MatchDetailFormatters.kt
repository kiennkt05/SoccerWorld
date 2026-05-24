package com.example.soccerworld.ui.fixture.detail.util

import com.example.soccerworld.model.matchdetail.MatchEvent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Helper function to format match ISO-8601 UTC date string to local "dd/MM/yyyy • HH:mm"
fun formatMatchDateTime(utcDateStr: String?): String {
    if (utcDateStr.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcDateStr) ?: return ""
        val outputFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        outputFormat.format(date)
    } catch (e: Exception) {
        try {
            val altFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = altFormat.parse(utcDateStr) ?: return ""
            val outputFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            outputFormat.format(date)
        } catch (e2: Exception) {
            utcDateStr
        }
    }
}

// Helper function to group goals by scorer name and aggregate minutes
fun groupGoals(goalEvents: List<MatchEvent>): List<Pair<String, String>> {
    val grouped = mutableMapOf<String, MutableList<String>>()
    goalEvents.forEach { event ->
        val name = event.description.split(" |")[0].trim()
        val min = event.minute.trim()
        if (name.isNotBlank() && name != "Event") {
            grouped.getOrPut(name) { mutableListOf() }.add(min)
        }
    }
    return grouped.map { (name, minutes) ->
        name to minutes.joinToString(", ")
    }
}

fun formatPublishedTime(publishedSeconds: Long?): String {
    publishedSeconds ?: return ""
    val now = System.currentTimeMillis()
    val diffMs = now - (publishedSeconds * 1000L)
    if (diffMs < 0) return "Just now"
    
    val diffMinutes = diffMs / 60000L
    if (diffMinutes < 60) return "${diffMinutes.coerceAtLeast(1)}m ago"
    
    val diffHours = diffMinutes / 60
    if (diffHours < 24) return "${diffHours}h ago"
    
    val diffDays = diffHours / 24
    if (diffDays == 1L) return "Yesterday"
    if (diffDays < 7) return "${diffDays}d ago"
    
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(java.util.Date(publishedSeconds * 1000L))
}
