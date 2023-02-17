package plugin.httpclient.freeproxyapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import scraper.net.CoroutinesHttpClient
import scraper.plugin.Plugin
import scraper.plugin.PluginFactory
import scraper.plugin.hook.ProxyData
import scraper.plugin.hook.ProxyWebsite
import java.net.http.HttpResponse

/**
 * GeoNode - 11/02/2023
 * @author Kai
 *
 * Source: https://freeproxyapi.com/
 * Endpoint: https://public.freeproxyapi.com/api/Download/Json
 * Method: POST
 *
 * ContentType: JSON
 * Format:
 * [
 *   {
 *     "Host": "39.104.62.128",
 *     "Port": 8123,
 *     "Type": 2,
 *     "ProxyLevel": 1
 *   }
 * ]
 */
class FreeProxyApi : Plugin, ProxyWebsite {

    private val endpointUrl = "https://public.freeproxyapi.com/api/Download/Json"
    private val postData = "{\"types\":[],\"levels\":[],\"countries\":[],\"type\":\"json\",\"resultModel\":\"Mini\"}"

    private val logger = KotlinLogging.logger { }

    override val proxies : MutableList<ProxyData> = mutableListOf()

    override fun register() {
        PluginFactory.register(this)
    }

    override fun initialize() : Boolean {
        logger.info { "Initializing" }

        //Go Next
        this.thenConnect()

        return false
    }

    override fun thenConnect() {
        logger.info { "Connecting" }

        val response = mutableMapOf<String, HttpResponse<String>?>()
        runBlocking {
            val result = async(Dispatchers.IO) {
                val client = CoroutinesHttpClient()
                client.contentType = arrayOf("content-type",  "application/json")
                val data = client.fetch(endpointUrl, postData)
                data
            }
            response["json"] = result.await()
        }

        if (response.isNotEmpty()) {
            this.thenHandleData(response)
        } else {
            logger.error { "Failed to connect to $endpointUrl" }
        }
    }

    override fun thenHandleData(data : MutableMap<String, *>) {
        logger.info { "Handling Data" }

        val mapper = ObjectMapper()
        val values = mapper.readValue<List<FreeProxyApiData>>((data.getValue("json") as HttpResponse<String>).body())
        for(value in values) {
            val protocol : String = when (value.protocolAsInt) {
                1 -> "SOCKS4"
                2 -> "SOCKS5"
                3 -> "HTTP"
                4 -> "HTTPS"
                else -> null
            } ?: continue

            val proxy = ProxyData(value.host, value.port, protocol)
            proxies.add(proxy)
        }

        //Go Next
        this.finallyComplete()
    }

    override fun finallyComplete() {
        logger.info { "Collected ${proxies.size} proxies" }
    }

}