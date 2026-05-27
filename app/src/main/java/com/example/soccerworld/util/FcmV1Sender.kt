package com.example.soccerworld.util

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Gửi FCM message tới topic thông qua FCM HTTP v1 API.
 *
 * Luồng:
 *   1. Đọc service-account.json từ assets/
 *   2. Build JWT và trao đổi lấy OAuth2 access token
 *   3. Gọi FCM v1 REST API gửi data message tới topic
 *
 * Thiết bị nhận sẽ xử lý trong SoccerWorldMessagingService.onMessageReceived()
 */
object FcmV1Sender {

    private const val TAG           = "FcmV1Sender"
    private const val TOKEN_URL     = "https://oauth2.googleapis.com/token"
    private const val FCM_SCOPE     = "https://www.googleapis.com/auth/firebase.messaging"
    private const val ASSETS_FILE   = "service-account.json"

    private val httpClient = OkHttpClient()

    // Cache token để tránh gọi OAuth2 quá nhiều lần
    @Volatile private var cachedToken: String? = null
    @Volatile private var tokenExpiry: Long    = 0L

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Gửi Match Reminder FCM message tới topic "match_reminder".
     */
    suspend fun sendMatchReminder(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        minutesLeft: Int
    ) = sendToTopic(
        context = context,
        topic   = FcmTopicManager.TOPIC_MATCH_REMINDER,
        title   = "⏰ Trận đấu sắp bắt đầu!",
        body    = "$homeTeam vs $awayTeam – còn $minutesLeft phút nữa",
        data    = mapOf(
            "type"        to "MATCH_REMINDER",
            "matchId"     to matchId,
            "homeTeam"    to homeTeam,
            "awayTeam"    to awayTeam,
            "minutesLeft" to minutesLeft.toString()
        )
    )

    /**
     * Gửi Live Score FCM message tới topic "live_score".
     */
    suspend fun sendLiveScore(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int
    ) = sendToTopic(
        context = context,
        topic   = FcmTopicManager.TOPIC_LIVE_SCORE,
        title   = "⚽ Bàn thắng!",
        body    = "$homeTeam $homeScore - $awayScore $awayTeam",
        data    = mapOf(
            "type"      to "LIVE_SCORE",
            "matchId"   to matchId,
            "homeTeam"  to homeTeam,
            "awayTeam"  to awayTeam,
            "homeScore" to homeScore.toString(),
            "awayScore" to awayScore.toString()
        )
    )

    /**
     * Gửi Match Result FCM message tới topic "match_result".
     */
    suspend fun sendMatchResult(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        winner: String?
    ) = sendToTopic(
        context = context,
        topic   = FcmTopicManager.TOPIC_MATCH_RESULT,
        title   = "🏁 Kết quả trận đấu",
        body    = "$homeTeam $homeScore - $awayScore $awayTeam",
        data    = mapOf(
            "type"      to "MATCH_RESULT",
            "matchId"   to matchId,
            "homeTeam"  to homeTeam,
            "awayTeam"  to awayTeam,
            "homeScore" to homeScore.toString(),
            "awayScore" to awayScore.toString(),
            "winner"    to (winner ?: "")
        )
    )

    // ── Core send logic ──────────────────────────────────────────────────────

    private suspend fun sendToTopic(
        context: Context,
        topic: String,
        title: String,
        body: String,
        data: Map<String, String>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val serviceAccount = readServiceAccount(context)
            if (serviceAccount == null) {
                Log.w(TAG, "service-account.json not found – falling back to local notification")
                showLocalFallback(context, data)
                return@withContext false
            }

            val projectId = serviceAccount.getString("project_id")
            val token = getAccessToken(serviceAccount)
            if (token == null) {
                Log.e(TAG, "Cannot get OAuth2 token – falling back to local notification")
                showLocalFallback(context, data)
                return@withContext false
            }

            // Build FCM v1 message (data-only, handled in onMessageReceived)
            val dataObj = JSONObject().apply { data.forEach { (k, v) -> put(k, v) } }
            val message = JSONObject().apply {
                put("topic", topic)
                // Dùng data-only message để kiểm soát hoàn toàn hiển thị
                put("data", dataObj)
                // Android-specific config
                put("android", JSONObject().apply {
                    put("priority", "high")
                })
            }
            val payload = JSONObject().apply { put("message", message) }

            val url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val success  = response.isSuccessful
            if (success) {
                Log.d(TAG, "FCM sent OK → topic=$topic")
            } else {
                Log.e(TAG, "FCM error ${response.code}: ${response.body?.string()}")
                showLocalFallback(context, data)
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "FCM send exception", e)
            showLocalFallback(context, data)
            false
        }
    }

    // ── Fallback: hiển thị local notification nếu FCM không gửi được ────────

    private fun showLocalFallback(context: Context, data: Map<String, String>) {
        when (data["type"]) {
            "MATCH_REMINDER" -> NotificationHelper.sendMatchReminderNotification(
                context     = context,
                matchId     = data["matchId"] ?: "0",
                homeTeam    = data["homeTeam"] ?: "Home",
                awayTeam    = data["awayTeam"] ?: "Away",
                minutesLeft = data["minutesLeft"]?.toIntOrNull() ?: 15
            )
            "LIVE_SCORE" -> NotificationHelper.sendLiveScoreNotification(
                context   = context,
                matchId   = data["matchId"] ?: "0",
                homeTeam  = data["homeTeam"] ?: "Home",
                awayTeam  = data["awayTeam"] ?: "Away",
                homeScore = data["homeScore"]?.toIntOrNull() ?: 0,
                awayScore = data["awayScore"]?.toIntOrNull() ?: 0
            )
            "MATCH_RESULT" -> NotificationHelper.sendMatchResultNotification(
                context   = context,
                matchId   = data["matchId"] ?: "0",
                homeTeam  = data["homeTeam"] ?: "Home",
                awayTeam  = data["awayTeam"] ?: "Away",
                homeScore = data["homeScore"]?.toIntOrNull() ?: 0,
                awayScore = data["awayScore"]?.toIntOrNull() ?: 0,
                winner    = data["winner"]
            )
        }
    }

    // ── Service Account & Auth ───────────────────────────────────────────────

    private fun readServiceAccount(context: Context): JSONObject? {
        return try {
            val json = context.assets.open(ASSETS_FILE).bufferedReader().use { it.readText() }
            JSONObject(json)
        } catch (e: Exception) {
            null // File chưa được đặt → dùng fallback
        }
    }

    /**
     * Lấy OAuth2 access token từ Google bằng JWT assertion.
     * Token được cache trong bộ nhớ để tránh gọi lại liên tục.
     */
    private fun getAccessToken(serviceAccount: JSONObject): String? {
        // Trả về token còn hạn trong cache
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiry) return cachedToken

        return try {
            val clientEmail = serviceAccount.getString("client_email")
            val privateKey  = serviceAccount.getString("private_key")
            val nowSec      = now / 1000

            val jwt = buildJwt(clientEmail, nowSec, privateKey) ?: return null

            val formBody = "grant_type=${
                "urn:ietf:params:oauth:grant-type:jwt-bearer".encode()
            }&assertion=$jwt"

            val request = Request.Builder()
                .url(TOKEN_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()

            val response   = httpClient.newCall(request).execute()
            val bodyStr    = response.body?.string() ?: return null
            val json       = JSONObject(bodyStr)
            val token      = json.optString("access_token").ifEmpty { return null }
            val expiresIn  = json.optLong("expires_in", 3600)

            // Cache: hết hạn trước 60 giây để có buffer
            cachedToken  = token
            tokenExpiry  = now + (expiresIn - 60) * 1000

            Log.d(TAG, "OAuth2 token obtained, expires in ${expiresIn}s")
            token
        } catch (e: Exception) {
            Log.e(TAG, "getAccessToken error", e)
            null
        }
    }

    /**
     * Build JWT dạng RS256 để trao đổi lấy OAuth2 token.
     */
    private fun buildJwt(clientEmail: String, nowSec: Long, privateKeyPem: String): String? {
        return try {
            fun encode(bytes: ByteArray) =
                Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

            val header  = encode("""{"alg":"RS256","typ":"JWT"}""".toByteArray())
            val claims  = encode(JSONObject().apply {
                put("iss",   clientEmail)
                put("scope", FCM_SCOPE)
                put("aud",   TOKEN_URL)
                put("iat",   nowSec)
                put("exp",   nowSec + 3600)
            }.toString().toByteArray())

            val sigInput = "$header.$claims"

            // Parse PKCS#8 private key
            val cleanPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "").trim()
            val keyBytes = Base64.decode(cleanPem, Base64.DEFAULT)
            val privKey  = KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(keyBytes))

            // Sign
            val sig = Signature.getInstance("SHA256withRSA").run {
                initSign(privKey)
                update(sigInput.toByteArray())
                encode(sign())
            }

            "$sigInput.$sig"
        } catch (e: Exception) {
            Log.e(TAG, "buildJwt error", e)
            null
        }
    }

    private fun String.encode() = java.net.URLEncoder.encode(this, "UTF-8")
}
