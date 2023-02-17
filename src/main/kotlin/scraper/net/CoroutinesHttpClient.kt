package scraper.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration

/**
 * CoroutinesHttpClient - 12/02/2023
 * @author Kai
 *
 * Description: Used for simple web calls, such as sending GET/POST data for text content (JSON etc)
 **/
class CoroutinesHttpClient {

    companion object {

        val timeout : Duration = Duration.ofSeconds(15)

        private val httpClient : HttpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(timeout)
            .build()

    }

    var userAgent = arrayOf("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0")
    var accept = arrayOf("accept", "*/*")
    var acceptLanguage = arrayOf("accept-language", "en-US,en;q=0.9")
    var acceptEncoding = arrayOf("accept-encoding", "text/plain; charset=UTF-8")
    var contentType = arrayOf("content-type", "text/plain; charset=UTF-8")

    suspend fun fetch(url : String, postData : String?) : HttpResponse<String>? {
        val request : HttpRequest = if(postData == null) {
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .headers(*userAgent)
                .headers(*accept)
                .headers(*acceptLanguage)
                .headers(*acceptEncoding)
                .headers(*contentType)
                .GET()
                .build()
        } else {
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .headers(*userAgent)
                .headers(*accept)
                .headers(*acceptLanguage)
                .headers(*acceptEncoding)
                .headers(*contentType)
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build()
        }
        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        return withContext(Dispatchers.IO) {
            try {
                response.get()
            } catch (timeout : HttpTimeoutException) {
                null
            }
        }
    }

}