package com.example.soccerworld.model.transfer


import com.google.gson.annotations.SerializedName

data class TransferResponse(
    @SerializedName("api")
    var api: Api
)