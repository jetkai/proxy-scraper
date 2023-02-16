package plugin.httpclient.geonode

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
 * Geonode - 16/02/2023
 * @author Kai
 *
 * Description: Grabs 2000 proxies (HTTP, HTTPS, SOCKS4 & SOCKS5)
 *
 * Source: https://proxylist.geonode.com/
 * Endpoint (HTTP): https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=http
 * Endpoint (HTTPS): https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=http
 * Endpoint (SOCKS4): https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=http
 * Endpoint (SOCKS5): https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=http
 * Endpoint (ALL): https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=http,https,socks4,socks5
 * Method: GET
 *
 * ContentType: JSON
 * Format:
 * {
 *   "data": [
 *     {
 *       "_id": "60d65a0cce5b3bb0e93f237b",
 *       "ip": "136.243.174.243",
 *       "port": "1080",
 *       "anonymityLevel": "elite",
 *       "asn": "AS24940",
 *       "city": "Falkenstein",
 *       "country": "DE",
 *       "created_at": "2021-06-25T22:34:52.034Z",
 *       "google": false,
 *       "hostName": null,
 *       "isp": "Hetzner Online GmbH",
 *       "lastChecked": 1676567264,
 *       "latency": 15.8,
 *       "org": "Hetzner",
 *       "protocols": [
 *         "socks4"
 *       ],
 *       "region": null,
 *       "responseTime": 1435,
 *       "speed": 1,
 *       "updated_at": "2023-02-16T17:07:44.778Z",
 *       "workingPercent": null,
 *       "upTime": 99.94736842105263,
 *       "upTimeSuccessCount": 7596,
 *       "upTimeTryCount": 7600
 *     }
 *   ]
 * }
 */
class Geonode : Plugin, ProxyWebsite {

    private val endpointUrls = mapOf(
        "HTTP" to "https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=http",
        "HTTPS" to "https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=https",
        "SOCKS4" to "https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=socks4",
        "SOCKS5" to "https://proxylist.geonode.com/api/proxy-list?limit=500&sort_by=lastChecked&sort_type=desc&filterUpTime=90&protocols=socks5"
    )

    private val logger = KotlinLogging.logger { }

    override val proxies : MutableList<ProxyData> = mutableListOf()

    override fun init() {
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

        val responses = mutableMapOf<String, HttpResponse<String>>()
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

    override fun thenHandleData(data : MutableMap<String, HttpResponse<String>>) {
        logger.info { "Handling Data" }

        for(entry in data.entries.iterator()) {
            val json = entry.value.body()
            val mapper = ObjectMapper()
            val geonodeArray = mapper.readValue<GeonodeData>(json)
            geonodeArray.data
                .filterNot { it.protocols.isEmpty() }
                .mapTo(proxies) { ProxyData(it.host, it.port, it.protocols[0]) }
        }

        //Go Next
        this.finallyComplete()
    }

    override fun finallyComplete() {
        logger.info { "Finalizing" }
        logger.info { "Collected ${proxies.size} proxies" }
    }

}