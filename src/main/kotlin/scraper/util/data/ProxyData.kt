package scraper.util.data

data class ProxyData(var ip : String, var port : Int, var protocol : String) {
    fun values() : String {
        return ip + port + protocol
    }
}
