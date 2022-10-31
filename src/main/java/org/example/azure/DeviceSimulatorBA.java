package org.example.azure;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static java.lang.StrictMath.random;

public class DeviceSimulatorBA {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSimulatorBA.class);

    private final String iotHubName;
    double temperature = 20.0d;
    private final RegistryClient registryClient;
    public DeviceSimulatorBA(String iotHubConnectionString, String iotHubName) {
        this.registryClient = new RegistryClient(iotHubConnectionString);
        this.iotHubName = iotHubConnectionString;
    }

    // Plug and play features are available over MQTT, MQTT_WS, AMQPS, and AMQPS_WS.
    private static final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    /**
     * Initialize the device client instance over Mqtt protocol, setting the ModelId into ClientOptions.
     * This method also sets a connection status change callback, that will get triggered any time the device's connection status changes.
     */
    public void startDeviceSimulator(String deviceId) throws IotHubClientException, IOException, IotHubException {
        String primaryKey = registryClient.getDevice(deviceId).getPrimaryKey();
        String hostName = iotHubName + ".azure-devices.net";
        String deviceConnectionString = String.format("HostName=%s;DeviceId=%s;SharedAccessKey=%s",hostName,deviceId,primaryKey);
        LOGGER.info("DeviceConnectionString:{}", deviceConnectionString);
        DeviceClient deviceClient = new DeviceClient(deviceConnectionString, protocol);

        deviceClient.setConnectionStatusChangeCallback((context) -> {
            LOGGER.debug("Connection status change registered: status={}, reason={}", context.getNewStatus(), context.getNewStatusReason());
            Throwable throwable = context.getCause();
            if (throwable != null) {
                LOGGER.debug("The connection status change was caused by the following Throwable: {}", throwable.getMessage());
                throwable.printStackTrace();
            }
        }, deviceClient);

        deviceClient.open(false);
        sendTemperatureTelemetryContinuously(deviceClient);
    }


    private void sendTemperatureTelemetryContinuously(DeviceClient deviceClient) {
        new Thread(new Runnable() {
            @SneakyThrows({InterruptedException.class, IOException.class})
            @Override
            public void run() {
                while (true) {
                    sendTemperatureTelemetry(deviceClient);
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    private void sendTemperatureTelemetry(DeviceClient deviceClient) {
        String telemetryName = "temperature";

        String telemetryPayload = String.format("{\"%s\": %f}", telemetryName, temperature);

        Message message = new Message(telemetryPayload);
        message.setContentEncoding(StandardCharsets.UTF_8.name());
        message.setContentType("application/json");
        message.setConnectionDeviceId(deviceClient.getConfig().getDeviceId());
        message.setProperty("eben", "anan");

        deviceClient.sendEventAsync(message, new MessageSentCallback(), message);
        MessageReceivedCallback callback = new MessageReceivedCallback();
        deviceClient.setMessageCallback(callback, null);

        LOGGER.info("Telemetry: Sent - {\"{}\": {} C} with message Id {}.", telemetryName, temperature, message.getMessageId());
        double randomNum = random();
        temperature = temperature + randomNum;
    }

    /**
     * The callback to be invoked when a telemetry response is received from IoT Hub.
     */
    private static class MessageSentCallback implements com.microsoft.azure.sdk.iot.device.MessageSentCallback {
        @Override
        public void onMessageSent(Message sentMessage, IotHubClientException exception, Object callbackContext) {
            Message msg = (Message) callbackContext;
            LOGGER.info("Telemetry - Response from IoT Hub: message Id={}, status={}", msg.getMessageId(), exception == null ? OK : exception.getStatusCode());
        }
    }


    /**
     * The callback to be invoked when a telemetry response is received from IoT Hub.
     */
    private static class MessageReceivedCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback {

        @Override
        public IotHubMessageResult onCloudToDeviceMessageReceived(Message message, Object callbackContext) {
            Message msg = (Message) callbackContext;
            LOGGER.info("Message recevied from cloud: message Id={}", msg.getMessageId());
            System.out.println("Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            return IotHubMessageResult.COMPLETE;
        }
    }


}
