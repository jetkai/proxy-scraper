package scraper.plugin.hook

import scraper.util.data.ProxyData

interface ProxyWebsite {

    val proxies : MutableList<ProxyData>

    fun initialize() : Boolean

    fun thenConnect()

    fun thenHandleData(data : MutableMap<String, *>)

    fun finallyComplete() : Boolean = false

}