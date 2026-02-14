package my.noveldokusha.text_to_speech

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object EdgeTts {
    private const val TRUSTED_CLIENT_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4"
    private const val WSS_URL = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=$TRUSTED_CLIENT_TOKEN"
    
    private val client = OkHttpClient()

    suspend fun synthesize(text: String, voice: String, rate: String = "+0%", pitch: String = "+0Hz"): ByteArray = withContext(Dispatchers.IO) {
        val audioData = Channel<ByteArray>(Channel.UNLIMITED)
        val errorChannel = Channel<Throwable>(1)
        
        val requestId = UUID.randomUUID().toString().replace("-", "")
        val timestamp = System.currentTimeMillis().toString()

        val request = Request.Builder().url(WSS_URL).build()
        
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 1. Send Config
                val configMsg = """
                    X-Timestamp:$timestamp
                    Content-Type:application/json; charset=utf-8
                    Path:speech.config
                    
                    {"context":{"synthesis":{"audio":{"metadataoptions":{"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},"outputFormat":"audio-24khz-48kbitrate-mono-mp3"}}}}
                """.trimIndent()
                webSocket.send(configMsg)

                // 2. Send SSML
                val ssml = """
                    <speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>
                      <voice name='$voice'>
                        <prosody pitch='$pitch' rate='$rate'>$text</prosody>
                      </voice>
                    </speak>
                """.trimIndent()
                
                val ssmlMsg = """
                    X-RequestId:$requestId
                    Content-Type:application/ssml+xml
                    X-Timestamp:$timestamp
                    Path:ssml
                    
                    $ssml
                """.trimIndent()
                webSocket.send(ssmlMsg)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val data = bytes.toByteArray()
                // Check if it's binary audio data
                // Header length is 2 bytes (big endian)
                if (data.size > 2) {
                    val headerLen = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
                    val header = String(data, 2, headerLen)
                    
                    if (header.contains("Path:audio")) {
                        // Extract audio payload
                        val audioStart = 2 + headerLen
                        if (audioStart < data.size) {
                            val audioPayload = data.copyOfRange(audioStart, data.size)
                            audioData.trySend(audioPayload)
                        }
                    } else if (header.contains("Path:turn.end")) {
                        audioData.close()
                        webSocket.close(1000, "Done")
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                errorChannel.trySend(t)
                audioData.close(t)
            }
        }

        client.newWebSocket(request, listener)

        // Collect all audio chunks
        val fullAudio = java.io.ByteArrayOutputStream()
        try {
            for (chunk in audioData) {
                fullAudio.write(chunk)
            }
        } catch (e: Exception) {
            throw errorChannel.tryReceive().getOrNull() ?: e
        }
        
        fullAudio.toByteArray()
    }
    
    fun getVoices(): List<VoiceData> {
        // Expanded list of popular Edge TTS voices
        return listOf(
            VoiceData("en-US-GuyNeural", "English (US) - Guy", true, 100),
            VoiceData("en-US-JennyNeural", "English (US) - Jenny", true, 100),
            VoiceData("en-US-AnaNeural", "English (US) - Ana", true, 100),
            VoiceData("en-US-AriaNeural", "English (US) - Aria", true, 100),
            VoiceData("en-US-ChristopherNeural", "English (US) - Christopher", true, 100),
            VoiceData("en-US-EricNeural", "English (US) - Eric", true, 100),
            VoiceData("en-US-MichelleNeural", "English (US) - Michelle", true, 100),
            VoiceData("en-US-RogerNeural", "English (US) - Roger", true, 100),
            
            VoiceData("en-GB-RyanNeural", "English (UK) - Ryan", true, 100),
            VoiceData("en-GB-SoniaNeural", "English (UK) - Sonia", true, 100),
            VoiceData("en-GB-LibbyNeural", "English (UK) - Libby", true, 100),
            
            VoiceData("en-AU-NatashaNeural", "English (AU) - Natasha", true, 100),
            VoiceData("en-AU-WilliamNeural", "English (AU) - William", true, 100),
            
            VoiceData("ja-JP-KeitaNeural", "Japanese - Keita", true, 100),
            VoiceData("ja-JP-NanamiNeural", "Japanese - Nanami", true, 100),
            
            VoiceData("ko-KR-KeitaNeural", "Korean - Keita", true, 100), // Placeholder if exists
            VoiceData("zh-CN-XiaoxiaoNeural", "Chinese - Xiaoxiao", true, 100)
        )
    }
}