package plugin.httpclient.proxyscrape

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
 * ProxyScrape - 12/02/2023
 * @author Kai
 *
 * Source: https://proxyscrape.com/
 * Endpoint (HTTP): https://api.proxyscrape.com/v2/?request=displayproxies&protocol=http&timeout=10000
 * Endpoint (HTTPS): https://api.proxyscrape.com/v2/?request=displayproxies&protocol=https&timeout=10000
 * Endpoint (SOCKS4): https://api.proxyscrape.com/v2/?request=displayproxies&protocol=socks4&timeout=10000
 * Endpoint (SOCKS5): https://api.proxyscrape.com/v2/?request=displayproxies&protocol=socks5&timeout=10000
 * Endpoint (ALL): https://api.proxyscrape.com/v2/?request=displayproxies&protocol=http,https,socks4,socks5&timeout=10000
 * Method: GET
 *
 * ContentType: Plain-Text
 * Format:
 * 127.0.0.1:80
 * 127.0.0.2:8080
 */
class ProxyScrape : Plugin, ProxyWebsite {

    private val endpointUrls = mapOf(
        "HTTP" to "https://api.proxyscrape.com/v2/?request=displayproxies&protocol=http&timeout=10000",
        "HTTPS" to "https://api.proxyscrape.com/v2/?request=displayproxies&protocol=https&timeout=10000",
        "SOCKS4" to "https://api.proxyscrape.com/v2/?request=displayproxies&protocol=socks4&timeout=10000",
        "SOCKS5" to "https://api.proxyscrape.com/v2/?request=displayproxies&protocol=socks5&timeout=10000"
    )

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

        val responses = mutableMapOf<String, HttpResponse<String>?>()
        for (endpointEntry in endpointUrls.entries.iterator()) {
            runBlocking {
                val result = async(Dispatchers.IO) {
                    val data = CoroutinesHttpClient().fetch(endpointEntry.value, null)
                    data
                }
                responses[endpointEntry.key] = result.await()
            }
        }

        this.thenHandleData(responses)
    }

    override fun thenHandleData(data : MutableMap<String, *>) {
        logger.info { "Handling Data" }

        for(entry in data.entries.iterator()) {
            val type = entry.key
            if(entry.value is HttpResponse<*>) {
                val proxyIpPortArray = (entry.value as HttpResponse<String>).body().split("\r")
                for (proxyIpPort in proxyIpPortArray) {
                    if (!proxyIpPort.contains(":")) {
                        continue
                    }
                    val ip = proxyIpPort.split(":")[0]
                    val port = proxyIpPort.split(":")[1]
                    val proxy = ProxyData(ip, port.toInt(), type)
                    proxies.add(proxy)
                }
            }

        }

        //Go Next
        this.finallyComplete()
    }

    override fun finallyComplete() {
        logger.info { "Finalizing" }
        logger.info { "Collected ${proxies.size} proxies" }
    }

}