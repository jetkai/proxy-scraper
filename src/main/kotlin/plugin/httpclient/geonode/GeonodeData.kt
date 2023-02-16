package plugin.httpclient.geonode

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
data class GeonodeData(@JsonProperty("data") val data : List<GeonodeDataArray>)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
data class GeonodeDataArray(
    @JsonProperty("ip")
    val host : String,
    @JsonProperty("port")
    val port : Int,
    @JsonProperty("protocols")
    val protocols : List<String>,
)

