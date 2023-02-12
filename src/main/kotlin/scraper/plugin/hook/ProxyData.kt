package scraper.plugin.hook

data class ProxyData(var ip : String, var port : Int, var type : String) {
    fun values() : String {
        return ip + port + type
    }
}
