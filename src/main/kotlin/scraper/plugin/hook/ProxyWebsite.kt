package scraper.plugin.hook

import java.net.http.HttpResponse

interface ProxyWebsite {

    val proxies : MutableList<ProxyData>

    fun initialize() : Boolean

    fun thenConnect()

    fun thenHandleData(data : MutableMap<String, HttpResponse<String>>)

    fun finallyComplete()

}