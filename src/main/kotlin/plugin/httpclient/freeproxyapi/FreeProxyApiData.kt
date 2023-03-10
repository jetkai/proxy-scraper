package plugin.httpclient.freeproxyapi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
data class FreeProxyApiData(
    @JsonProperty("Host")
    val host : String,
    @JsonProperty("Port")
    val port : Int,
    @JsonProperty("Type")
    val protocolAsInt : Int,
    @JsonProperty("ProxyLevel")
    val proxyLevel : Int
)
