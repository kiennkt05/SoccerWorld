package com.example.soccerworld.ui.fixture.detail.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soccerworld.model.matchdetail.MatchHighlight

@Composable
fun HighlightList(highlights: List<MatchHighlight>) {
    val context = LocalContext.current

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        items(highlights) { highlight ->
            HighlightCard(highlight) {
                // Open the link in a browser (or WebView)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(highlight.link))
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun HighlightCard(highlight: MatchHighlight, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small
    ) {
        Column {
            AsyncImage(
                model = highlight.images?.firstOrNull()?.url,
                contentDescription = highlight.title,
                modifier = Modifier.fillMaxWidth().height(112.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = highlight.title ?: "Highlight",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}