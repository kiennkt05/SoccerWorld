package com.example.soccerworld.util

import com.example.soccerworld.model.topscorer.TopScorerEntity
import com.example.soccerworld.model.topscorer.TopScorerResponse


// Hàm này sẽ hô biến 1 cục ScorerItem (từ API) thành 1 cục TopScorerEntity (để lưu ổ cứng)
fun TopScorerResponse.toEntityList(): List<TopScorerEntity> {
    // 1. Dùng let để đảm bảo scorers không bị null
    // 2. Dùng map để biến List<ScorerItem> thành List<TopScorerEntity>
    return this.scorers?.map { item ->
        TopScorerEntity(
            playerId = item?.player?.id ?: 0,
            playerName = item?.player?.name ?: "Unknown Player",
            teamName = item?.team?.name ?: "Unknown Team",
            goals = item?.goals ?: 0
        )
    } ?: emptyList() // 3. Nếu toàn bộ list scorers bị null, trả về một list rỗng an toàn
}
