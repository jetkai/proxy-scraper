package plugin.httpclient.vpnfail

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

/**
 * GeoNode - 17/02/2023
 * @author Kai
 *
 * Source: https://vpn.fail/
 * Endpoint: https://vpn.fail/free-proxy/json
 * Method: GET
 *
 * ContentType: JSON
 * Format:
 * [
 *   {
 *     "proxy": "39.104.62.128:8123",
 *     "type": "socks5",
 *   }
 * ]
 */
@Suppress("unused")
class VpnFail : Plugin, ProxyWebsite {

    private val endpointUrl = "https://vpn.fail/free-proxy/json"

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
        val values = mapper.readValue<List<VpnFailData>>((data.getValue("json") as HttpResponse<String>).body())
        val allowedProtocols = arrayOf("socks4", "socks5", "http", "https")

        for(value in values) {
            if(!NetworkUtils.isValidIpAndPort(value.host) || !allowedProtocols.contains(value.protocol)) {
                continue
            }
            val ip = value.host.split(":")[0]
            val port = value.host.split(":")[1]
            val proxy = ProxyData(ip, port.toInt(), value.protocol)
            proxies.add(proxy)
        }

        logger.info { "Collected ${proxies.size} proxies" }
        completed = true
        this.finallyComplete()
    }

    override fun finallyComplete() : Boolean {
        return completed
    }

}