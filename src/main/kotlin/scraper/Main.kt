package scraper

import com.fasterxml.jackson.databind.ObjectMapper
import scraper.plugin.PluginFactory
import scraper.plugin.hook.ProxyOutputData
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

class Main {

    private val windowsOutputPath = Path.of("proxies")
    //TODO - Update this path
    private val linuxOutputPath = Path.of("/home/proxybuilder/IntelliJProjects/proxy-builder-2/proxies")

    private val isWindows = System.getProperty("os.name").startsWith("Windows")

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            Main().init()
        }
    }

    fun init() {
        PluginFactory.init()
        for(proxy in PluginFactory.proxyWebsites) {
            proxy.initialize()
        }
        output()
        exitProcess(0)
    }

    private fun output() {
        val mapper = ObjectMapper()

        val path : Path = if(isWindows) {
            windowsOutputPath
        } else {
            linuxOutputPath
        }

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

        if(!path.exists()) {
            Files.createDirectory(path)
        }
        val entries = mapOf(
            proxyList.http to Path.of("$path/http.txt"),
            proxyList.https to Path.of("$path/https.txt"),
            proxyList.socks4 to Path.of("$path/socks4.txt"),
            proxyList.socks5 to Path.of("$path/socks5.txt"),
            allProxies to Path.of("$path/proxies.txt"),
        )
        entries.iterator().forEach { entry ->
            Files.write(entry.value, entry.key)
        }
        val jsonFile = Path.of("$path/proxies.json").toFile()
        mapper.writeValue(jsonFile, proxyList)
    }

}