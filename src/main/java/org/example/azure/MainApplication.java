package org.example.azure;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.example.azure.config.DefaultConfiguration;
import org.example.azure.loadTest.business.LoadTestBA;
import org.example.azure.loadTest.service.LoadTestResource;
import org.example.azure.simulator.DeviceBA;
import org.example.azure.simulator.DeviceSimulatorResource;

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

        // Create Business Components
        DeviceBA deviceBA = new DeviceBA();
        LoadTestBA loadTestBA = new LoadTestBA(IOTHUB_CONNECTION_STRING);

        // Create resources
        DeviceSimulatorResource deviceSimulatorResource = new DeviceSimulatorResource(deviceBA);
        LoadTestResource loadTestResource = new LoadTestResource(loadTestBA);

        // Register resources
        environment.jersey().register(deviceSimulatorResource);
        environment.jersey().register(loadTestResource);
    }


}
