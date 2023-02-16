package plugin.httpclient.checkerproxy

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
data class CheckerProxyData(
    @JsonProperty("addr") //ip:port
    val host : String,
    @JsonProperty("type") //0=http
    val protocolAsInt : Int,
    @JsonProperty("timeout")
    val timeout : Int,
    @JsonProperty("post")
    val post : Boolean
)
