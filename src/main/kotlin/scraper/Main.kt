package scraper

import com.fasterxml.jackson.databind.ObjectMapper
import scraper.plugin.PluginFactory
import scraper.plugin.hook.ProxyOutputData
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class Main {

    private val windowsOutputPath = "proxies.json"
    //TODO - Update this path
    private val linuxOutputPath = "/home/proxybuilder/IntelliJProjects/proxy-builder-2/proxies/proxies.json"

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
            Paths.get(windowsOutputPath)
        } else {
            Paths.get(linuxOutputPath)
        }

        val file = File(path.toUri())
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

        mapper.writeValue(file, proxyList)
    }

}