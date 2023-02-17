package plugin.httpclient.openproxylist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import scraper.net.CoroutinesHttpClient
import scraper.plugin.Plugin
import scraper.plugin.PluginFactory
import scraper.plugin.hook.ProxyData
import scraper.plugin.hook.ProxyWebsite
import scraper.scripts.Utils
import java.net.http.HttpResponse

/**
 * OpenProxyList - 17/02/2023
 * @author Kai
 *
 * Source: https://openproxylist.xyz/
 * Endpoint (HTTP): https://openproxylist.xyz/http.txt
 * Endpoint (SOCKS4): https://openproxylist.xyz/socks4.txt
 * Endpoint (SOCKS5): https://openproxylist.xyz/socks5.txt
 * Endpoint (ALL): https://openproxylist.xyz/all.txt
 * Method: GET
 *
 * ContentType: Plain-Text
 * Format:
 * 127.0.0.1:80
 * 127.0.0.2:8080
 */
class OpenProxyList : Plugin, ProxyWebsite {

    private val endpointUrls = mapOf(
        "HTTP" to "https://openproxylist.xyz/http.txt",
        "SOCKS4" to "https://openproxylist.xyz/socks4.txt",
        "SOCKS5" to "https://openproxylist.xyz/socks5.txt"
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
                val proxyIpPortArray = (entry.value as HttpResponse<String>).body().split("\n")
                for (proxyIpPort in proxyIpPortArray) {
                    if(!Utils.isValidIpAndPort(proxyIpPort)) {
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