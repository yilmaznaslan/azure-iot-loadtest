package org.example.azure.services.iotHub.loadTest.business;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.twin.Twin;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTestBA {

    private static Logger LOGGER = LoggerFactory.getLogger(LoadTestBA.class);

    private final String iotHubConnectionString;

    public LoadTestBA(String iotHubConnectionString) {
        this.iotHubConnectionString = iotHubConnectionString;
    }

    public void connectDeviceById(String deviceId) throws IotHubClientException, InterruptedException {
        String deviceConnectionString = getConnectionStringByDeviceId(deviceId);
        DeviceClient client = new DeviceClient(deviceConnectionString, IotHubClientProtocol.MQTT);
        LOGGER.info("Successfully created an IoT Hub client.");

        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        try {
            client.open(false);
            System.out.println("Opened connection to IoT Hub.");

        } catch (Exception e) {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" + e.getMessage());
            client.close();
            System.out.println("Shutting down...");
        }


        int i = 1;
        Twin twin = client.getTwin();
        TwinCollection reportedProperties =  twin.getReportedProperties();
        while(true){
            reportedProperties.replace("temp", String.valueOf(i) );
            client.updateReportedProperties(reportedProperties);
            Thread.sleep(1000);
            i = i+1;
        }

    }

    private String getConnectionStringByDeviceId(String deviceId) {
        return "HostName=smartHomeDemoIotHub.azure-devices.net;DeviceId=dishwasher_0;SharedAccessKey=HXWAisoBDuJA/k56hg57wQ==";
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback {
        @Override
        public void onStatusChanged(ConnectionStatusChangeContext connectionStatusChangeContext) {
            IotHubConnectionStatus status = connectionStatusChangeContext.getNewStatus();
            IotHubConnectionStatusChangeReason statusChangeReason = connectionStatusChangeContext.getNewStatusReason();
            Throwable throwable = connectionStatusChangeContext.getCause();

            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null) {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED) {
                System.out.println("The connection was lost, and is not being re-established." +
                        " Look at provided exception for how to resolve this issue." +
                        " Cannot send messages until this issue is resolved, and you manually re-open the device client");
            } else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING) {
                System.out.println("The connection was lost, but is being re-established." +
                        " Can still send messages, but they won't be sent until the connection is re-established");
            } else if (status == IotHubConnectionStatus.CONNECTED) {
                System.out.println("The connection was successfully established. Can send messages.");
            }
        }
    }

}
