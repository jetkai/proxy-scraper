package scraper.plugin.hook

data class ProxyOutputData(
    val http : MutableList<String>,
    val https : MutableList<String>,
    val socks4 : MutableList<String>,
    val socks5 : MutableList<String>
)