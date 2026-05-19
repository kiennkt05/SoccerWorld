package com.example.soccerworld.model.comment

data class Comment(
    val id: String = "",
    val fixtureId: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
