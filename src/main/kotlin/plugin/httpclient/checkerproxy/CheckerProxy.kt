package plugin.httpclient.checkerproxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import scraper.net.CoroutinesHttpClient
import scraper.plugin.Plugin
import scraper.plugin.PluginFactory
import scraper.plugin.hook.ProxyWebsite
import scraper.util.NetworkUtils
import scraper.util.data.ProxyData
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * CheckerProxy - 16/02/2023
 * @author Kai
 *
 * Source: https://checkerproxy.net/
 * Endpoint: https://checkerproxy.net/api/archive/yyyy-MM-dd
 * Method: GET
 *
 * ContentType: JSON
 */
@Suppress("unused")
class CheckerProxy : Plugin, ProxyWebsite {

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val currentDate = LocalDateTime.now().format(dateFormat)

    private val endpointUrl = "https://checkerproxy.net/api/archive/$currentDate"

    private val logger = KotlinLogging.logger { }

    override val proxies : MutableList<ProxyData> = mutableListOf()

    private var completed : Boolean = false

    override fun register() {
        PluginFactory.register(this)
    }

    override fun initialize() : Boolean {
        logger.info { "Initializing" }

        try {
            this.thenConnect()
        } catch (ex : Exception) {
            completed = true
            logger.error { ex.message }
        }

        return true
    }

    override fun thenConnect() {
        logger.info { "Connecting" }

        val response = mutableMapOf<String, HttpResponse<String>?>()
        runBlocking {
            val result = async(Dispatchers.IO) {
                val client = CoroutinesHttpClient()
                client.contentType = arrayOf("content-type",  "application/json")
                val data = client.fetch(endpointUrl, null)
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
        val proxyList = mapper.readValue<List<CheckerProxyData>>((data.getValue("json") as HttpResponse<String>).body())
        for(proxy in proxyList) {
            if(!NetworkUtils.isValidIpAndPort(proxy.host)) {
                continue
            }
            val ip = proxy.host.split(":")[0]
            val port = proxy.host.split(":")[1].toInt()
            val protocol : String = when (proxy.protocolAsInt) {
                1 -> { "HTTP" }
                2 -> { "HTTPS" }
                4 -> { "SOCKS5" }
                else -> { null }
            } ?: continue
            proxies.add(ProxyData(ip, port, protocol))
        }

        logger.info { "Collected ${proxies.size} proxies" }
        completed = true
        this.finallyComplete()
    }

    override fun finallyComplete() : Boolean {
        return completed
    }

}