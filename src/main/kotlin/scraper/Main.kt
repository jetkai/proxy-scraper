package scraper

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import scraper.plugin.PluginFactory
import scraper.util.ScraperExecutor
import scraper.util.data.ProxyOutputData
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

/**
 * Main - 12/02/2023
 * @author Kai
 *
 * Description: Main class, runs the application
 **/
class Main {

    private val logger = KotlinLogging.logger { }

    companion object {
        var outputPath = Path.of("proxies")
        @JvmStatic
        fun main(args : Array<String>) {
            if(args.contentToString().contains("proxybuilder")) {
                outputPath = Path.of("/home/proxybuilder/IntelliJProjects/proxy-builder-2/proxies")
            }
            Main().init()
        }
    }

    fun init() {
        PluginFactory.init()
        for(proxy in PluginFactory.proxyWebsites) {
            ScraperExecutor.submitTask(proxy::initialize)
        }
        val websitesLength = PluginFactory.proxyWebsites.size
        while(PluginFactory.proxyWebsites.count { it.finallyComplete() } != websitesLength) {
            val sites = PluginFactory.proxyWebsites.count { it.finallyComplete() }
            logger.info { "Scraped [$sites/${PluginFactory.proxyWebsites.size}] sites, waiting on remaining sites..." }
            Thread.sleep(1000L)
        }
        output()
        exitProcess(0)
    }

    private fun output() {
        val mapper = ObjectMapper()

        val proxyList = ProxyOutputData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

        for(website in PluginFactory.proxyWebsites) {
            val websiteProxies = website.proxies.distinctBy { it.values() }.toMutableList()
            for(proxy in websiteProxies) {
                when (proxy.protocol) {
                    "HTTP" -> { proxyList.http.add(proxy.ip + ":" + proxy.port) }
                    "HTTPS" -> { proxyList.https.add(proxy.ip + ":" + proxy.port) }
                    "SOCKS4" -> { proxyList.socks4.add(proxy.ip + ":" + proxy.port) }
                    "SOCKS5" -> { proxyList.socks5.add(proxy.ip + ":" + proxy.port) }
                }
            }
        }

        val allProxies = mutableListOf<String>()
        allProxies.addAll(proxyList.http)
        allProxies.addAll(proxyList.https)
        allProxies.addAll(proxyList.socks4)
        allProxies.addAll(proxyList.socks5)

        if(!outputPath.exists()) {
            Files.createDirectory(outputPath)
        }

        val entries = mapOf(
            proxyList.http to Path.of("$outputPath/http.txt"),
            proxyList.https to Path.of("$outputPath/https.txt"),
            proxyList.socks4 to Path.of("$outputPath/socks4.txt"),
            proxyList.socks5 to Path.of("$outputPath/socks5.txt"),
            allProxies to Path.of("$outputPath/proxies.txt"),
        )

        entries.iterator().forEach { entry ->
            Files.write(entry.value, entry.key)
        }

        val jsonFile = Path.of("$outputPath/proxies.json").toFile()
        mapper.writeValue(jsonFile, proxyList)

        logger.info { "Total unique proxies collected: ${allProxies.size}" }
        logger.info { "HTTP:[${proxyList.http.size}] | HTTPS:[${proxyList.https.size}] " +
                "| SOCKS4:[${proxyList.socks4.size}] | SOCKS5:[${proxyList.socks5.size}]" }
    }

}