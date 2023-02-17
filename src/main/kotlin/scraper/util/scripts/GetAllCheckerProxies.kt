package scraper.util.scripts

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import plugin.httpclient.checkerproxy.CheckerProxyData
import scraper.net.CoroutinesHttpClient
import scraper.util.data.ProxyData
import scraper.util.data.ProxyOutputData
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.exists
import kotlin.system.exitProcess

/**
 * GetAllCheckerProxies - 17/02/2023
 * @author Kai
 *
 * Description: Quick and easy script for getting all the proxies from CheckerProxy's API
 **/
private const val endpoint = "https://checkerproxy.net/api/archive/"

private val proxies : MutableList<ProxyData> = mutableListOf()

fun main() {
    loopThroughDates()
}

private fun loopThroughDates() {
    val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fromDate = LocalDate.parse("2023-01-18", format)
    val toDate = LocalDate.parse("2023-02-17", format)
    fromDate.datesUntil(toDate).forEach {
        println("Scraping day: $it")
        scrape(it.toString())
        Thread.sleep(5000L)
    }
    output()
    exitProcess(0)
}

private fun scrape(date : String) {
    val client = CoroutinesHttpClient()
    val response = mutableMapOf<String, HttpResponse<String>?>()
    runBlocking {
        val result = async(Dispatchers.IO) {
            val data = client.fetch(endpoint + date, null)
            data
        }
        response["json"] = result.await()
    }
    parseJson(response)
}

private fun parseJson(response : MutableMap<String, HttpResponse<String>?>) {
    val mapper = ObjectMapper()
    val json = response.getValue("json")?.body() ?: return
    var proxyList : List<CheckerProxyData>? = null
    try {
        proxyList = mapper.readValue<List<CheckerProxyData>>(json)
    } catch (ex : JsonMappingException) {
        println(ex.message)
    }
    if(proxyList == null) {
        return
    }
    for(proxy in proxyList) {
        if(proxy.host.isEmpty() || !proxy.host.contains(":")) {
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
}
private fun output() {
    val mapper = ObjectMapper()

    val proxyList = ProxyOutputData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

    val distinctProxies = proxies.distinctBy { it.values() }.toMutableList()
    for(proxy in distinctProxies) {
        when (proxy.protocol) {
            "HTTP" -> { proxyList.http.add(proxy.ip + ":" + proxy.port) }
            "HTTPS" -> { proxyList.https.add(proxy.ip + ":" + proxy.port) }
            "SOCKS4" -> { proxyList.socks4.add(proxy.ip + ":" + proxy.port) }
            "SOCKS5" -> { proxyList.socks5.add(proxy.ip + ":" + proxy.port) }
        }
    }

    val allProxies = mutableListOf<String>()
    allProxies.addAll(proxyList.http)
    allProxies.addAll(proxyList.https)
    allProxies.addAll(proxyList.socks4)
    allProxies.addAll(proxyList.socks5)

    val outputDir = Path.of("proxies/")
    if(!outputDir.exists()) {
        Files.createDirectory(outputDir)
    }
    val entries = mapOf(
        proxyList.http to Path.of(outputDir.toUri().toString(), "http.txt"),
        proxyList.https to Path.of(outputDir.toUri().toString(), "https.txt"),
        proxyList.socks4 to Path.of(outputDir.toUri().toString(), "socks4.txt"),
        proxyList.socks5 to Path.of(outputDir.toUri().toString(), "socks5.txt"),
        allProxies to Path.of(outputDir.toUri().toString(), "proxies.txt"),
    )
    entries.iterator().forEach { entry ->
        Files.write(entry.value, entry.key)
    }
    val jsonFile = Path.of(outputDir.toUri().toString(), "proxies.json").toFile()
    mapper.writeValue(jsonFile, proxyList)
}
