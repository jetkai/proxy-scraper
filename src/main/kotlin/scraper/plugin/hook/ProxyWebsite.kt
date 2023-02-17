package scraper.plugin.hook

interface ProxyWebsite {

    val proxies : MutableList<ProxyData>

    fun initialize() : Boolean

    fun thenConnect()

    fun thenHandleData(data : MutableMap<String, *>)

    fun finallyComplete() : Boolean = false

}