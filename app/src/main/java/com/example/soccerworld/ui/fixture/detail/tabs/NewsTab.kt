package com.example.soccerworld.ui.fixture.detail.tabs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.model.matchdetail.MatchNews
import com.example.soccerworld.ui.fixture.detail.util.formatPublishedTime
import com.example.soccerworld.ui.theme.TextDark
import com.example.soccerworld.ui.theme.TextSecondary

@Composable
fun NewsTab(newsList: List<MatchNews>) {
    if (newsList.isEmpty()) {
        EmptyState(message = "Không có tin tức liên quan")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = newsList,
            key = { "${it.title}_${it.published}" },
            contentType = { "news_card" }
        ) { item ->
            NewsCard(item = item)
        }
    }
}

@Composable
fun NewsCard(item: MatchNews) {
    val context = LocalContext.current
    val imageUrl = remember(item.imageUrl) { item.imageUrl }
    val formattedTime = remember(item.published) { formatPublishedTime(item.published) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!item.link.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                    context.startActivity(intent)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // Provider badge & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.providerName ?: "News",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                
                // News Title
                Text(
                    text = item.title ?: "",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            
            // Thumbnail image
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(width = 96.dp, height = 72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_ball)
                )
            }
        }
    }
}
