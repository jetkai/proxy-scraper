package scraper.plugin.hook

import scraper.util.data.ProxyData

/**
 * ProxyWebsite - 12/02/2023
 * @author Kai
 *
 * Description: Interface for the Website Plugins within /src/main/kotlin/plugin/
 * Plugins will follow this interface format
 **/
interface ProxyWebsite {

    val proxies : MutableList<ProxyData>

    fun initialize() : Boolean

    fun thenConnect()

    fun thenHandleData(data : MutableMap<String, *>)

    fun finallyComplete() : Boolean = false

}