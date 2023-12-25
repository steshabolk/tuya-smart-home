package ru.handh.project.dto.tuya

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class TuyaDevice (
    @JsonProperty("id")
    val id: String,
    @JsonProperty("active_time")
    val activeTime: Long,
    val category: String,
    @JsonProperty("create_time")
    val createTime: Long,
    @JsonProperty("update_time")
    val updateTime: Long,
    @JsonProperty("custom_name")
    val customName: String,
    val icon: String,
    val ip: String,
    @JsonProperty("is_online")
    val isOnline: Boolean,
    val lat: String,
    @JsonProperty("local_key")
    val localKey: String,
    val lon: String,
    val name: String,
    @JsonProperty("product_id")
    val productId: String,
    @JsonProperty("product_name")
    val productName: String,
    val sub: Boolean,
    @JsonProperty("time_zone")
    val timeZone: String,
    val uuid: String
)
