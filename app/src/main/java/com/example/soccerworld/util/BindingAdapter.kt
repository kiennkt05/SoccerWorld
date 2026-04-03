package com.example.soccerworld.util

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.soccerworld.R
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("android:backgroundLeagueState")
fun setBackgroundLeagueState(textView: TextView, url:String?){
    val context = textView.context
    when (url) {
        "Promotion - Champions League (Group Stage)" -> {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChampionsLeagueGroup))
        }
        "Promotion - Champions League (Qualification)" -> {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChampionsLeagueQualification))
        }
        "Promotion - Europa League (Group Stage)" -> {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEuropaLeagueGroup))
        }
        "Promotion - Europa League (Qualification)" -> {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEuropaLeagueQualification))
        }
        "Promotion - Eredivisie (Europa League - Play Offs)" -> {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEredivisie))
        }
        null -> {
            textView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
    }
}

@BindingAdapter("android:dayConverter")
fun convertToDay(view: TextView, value:Long){
    val converter = SimpleDateFormat("EEE, d MMM yyyy hh:mm a", Locale.getDefault())
    val convertedDay = converter.format(Date(value*1000))
    view.text = convertedDay
}
