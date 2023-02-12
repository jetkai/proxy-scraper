package plugin.httpclient.freeproxyapi

import com.fasterxml.jackson.annotation.JsonProperty

data class FreeProxyApiData(
    @JsonProperty("Host")
    val host : String,
    @JsonProperty("Port")
    val port : Int,
    @JsonProperty("Type")
    val type : Int,
    @JsonProperty("ProxyLevel")
    val proxyLevel : Int
)
