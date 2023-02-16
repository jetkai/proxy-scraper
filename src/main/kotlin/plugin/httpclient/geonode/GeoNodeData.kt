package plugin.httpclient.geonode

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
data class GeoNodeData(@JsonProperty("data") val data : List<GeoNodeDataArray>)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
data class GeoNodeDataArray(
    @JsonProperty("ip")
    val host : String,
    @JsonProperty("port")
    val port : Int,
    @JsonProperty("protocols")
    val protocols : List<String>,
)

