package ru.handh.project.connector;

import com.tuya.connector.api.annotations.Body;
import com.tuya.connector.api.annotations.GET;
import com.tuya.connector.api.annotations.POST;
import com.tuya.connector.api.annotations.Path;
import org.springframework.stereotype.Component;
import ru.handh.project.dto.tuya.TuyaCommand;
import ru.handh.project.dto.tuya.TuyaDevice;
import ru.handh.project.dto.tuya.TuyaSendCommandsRequest;

import java.util.List;

@Component
public interface DeviceConnector {

    @GET("/v2.0/cloud/thing/{deviceId}")
    TuyaDevice getDeviceDetails(@Path("deviceId") String deviceId);

    @GET("/v1.0/iot-03/devices/{deviceId}/status")
    List<TuyaCommand> getDeviceStatus(@Path("deviceId") String deviceId);

    @POST("/v1.0/iot-03/devices/{deviceId}/commands")
    Boolean sendCommands(@Path("deviceId") String deviceId, @Body TuyaSendCommandsRequest body);

}
