package org.example.azure;

public class MainApplication {

    public static String STORAGE_ACCOUNT_CONNECTION_STRING;
    public static String IOTHUB_CONNECTION_STRING;
    public static String IOTHUB_NAME;

    public static void main(String[] args) throws Exception {
        STORAGE_ACCOUNT_CONNECTION_STRING = System.getenv("STORAGE_ACCOUNT_CONNECTION_STRING");
        IOTHUB_CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
        IOTHUB_NAME = System.getenv("IOTHUB_NAME");

        // Create Business Components
        DeviceSimulatorBA deviceSimulatorBA = new DeviceSimulatorBA(IOTHUB_CONNECTION_STRING, IOTHUB_NAME);

        String deviceId = "dishwasher_1";
        deviceSimulatorBA.startDeviceSimulator(deviceId);
    }


}
