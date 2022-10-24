package org.example.azure.services.iotHub.loadTest.service;

import com.codahale.metrics.annotation.Timed;
import org.example.azure.services.iotHub.loadTest.business.LoadTestBA;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("loadtest/devices/{deviceId}")
@Produces(MediaType.APPLICATION_JSON)
public class LoadTestResource {

    private final LoadTestBA loadTestBA;

    public LoadTestResource(LoadTestBA loadTestBA) {
        this.loadTestBA = loadTestBA;
    }


    @POST
    @Timed
    public void connectDevice(@PathParam("deviceId") String deviceId) throws Exception {
        loadTestBA.connectDeviceById(deviceId);
    }

}