package com.example.soccerworld.util

import com.example.soccerworld.data.remote.flashlive.dto.Formation
import com.example.soccerworld.data.remote.flashlive.dto.LineupPlayer

/**
 * Translates a disposition string (e.g., "1-4-2-3-1") and a list of players
 * into a 2D list representing the spatial layers on the pitch.
 */
fun buildFormationLayers(formation: Formation, isHome: Boolean): List<List<LineupPlayer>> {
    val members = formation.members
    val disposition = formation.disposition 
    
    // 1. Parse the disposition. Reverse the layers for the Away team.
    val layerCounts = disposition.split("-").mapNotNull { it.toIntOrNull() }.let {
        if (isHome) it else it.reversed()
    }

    // 2. Sort players. Ascending for Home (GK -> FWD), Descending for Away (FWD -> GK)
    val sortedMembers = if (isHome) {
        members.sortedBy { it.position ?: 0 }
    } else {
        members.sortedByDescending { it.position ?: 0 }
    }

    // 3. Chunk the players into their respective rows
    val layers = mutableListOf<List<LineupPlayer>>()
    var currentIndex = 0
    
    for (count in layerCounts) {
        val layer = sortedMembers.drop(currentIndex).take(count)
        if (layer.isNotEmpty()) {
            layers.add(layer)
        }
        currentIndex += count
    }
    
    return layers
}
