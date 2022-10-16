package org.example.azure;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.example.azure.config.DefaultConfiguration;
import org.example.azure.services.iotHub.devicemanagement.business.DeviceManagementBA;
import org.example.azure.services.iotHub.devicemanagement.service.DeviceManagementService;
import org.example.azure.services.iotHub.simulator.DeviceBA;
import org.example.azure.services.iotHub.simulator.DeviceSimulatorResource;
import org.example.azure.services.storage.business.StorageBA;

public class MainApplication extends Application<DefaultConfiguration> {

    public static String STORAGE_ACCOUNT_CONNECTION_STRING;
    public static String IOTHUB_CONNECTION_STRING;

    public static void main(String[] args) throws Exception {
        STORAGE_ACCOUNT_CONNECTION_STRING = System.getenv("STORAGE_ACCOUNT_CONNECTION_STRING");
        IOTHUB_CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
        new MainApplication().run(args);
    }


    @Override
    public void run(DefaultConfiguration configuration, Environment environment) {

        StorageBA storageBA = new StorageBA(STORAGE_ACCOUNT_CONNECTION_STRING);
        DeviceManagementBA deviceManagementBA = new DeviceManagementBA(storageBA, IOTHUB_CONNECTION_STRING);
        DeviceBA deviceBA = new DeviceBA();


        // Create resources
        DeviceSimulatorResource deviceSimulatorResource = new DeviceSimulatorResource(deviceBA);
        DeviceManagementService deviceManagementService = new DeviceManagementService(deviceManagementBA);

        // Register resources
        environment.jersey().register(deviceSimulatorResource);
        environment.jersey().register(deviceManagementService);

    }


}
