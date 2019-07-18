package org.onap.security.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.onap.security.common.CommonConstants;
import org.onap.security.common.ResponseBuilder;
import org.onap.security.domain.Media;
import org.onap.security.domain.Metadata;
import org.onap.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
@Path("/")
public class SecurityController {

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private Gson gson;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RestTemplate restTemplate;

    @POST
    @Path("/audioProcessing/{audioIp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response audioProcessing(String requestJson,@PathParam("audioIp") String audioIp){
        JsonObject jsonObject = gson.fromJson(requestJson, JsonObject.class);
        Media media = (Media) CommonConstants.map.get(audioIp);
        if(media == null){
            return builder.buildResponse(200,"AudioIp does not exist");
        }
        securityService.addAudioList(media,jsonObject);
        return builder.buildResponse(200,null);
    }

    @GET
    @Path("/getMetadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetadata(){
        List<Metadata> metadataList = (ArrayList<Metadata>) CommonConstants.map.get(CommonConstants.DEVICE_LIST);
        return builder.buildResponse(200,metadataList);
    }

    @GET
    @Path("/initIndex/{audioIp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response initIndex(@PathParam("audioIp")String audioIp){
        Media media = (Media) CommonConstants.map.get(audioIp);
        if(media == null){
            return builder.buildResponse(200,"AudioIp does not exist");
        }
        return builder.buildResponse(200,media);
    }

    @GET
    @Path("/getAudioList/{audioIp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAudioList(@PathParam("audioIp")String audioIp){
        Media media = (Media) CommonConstants.map.get(audioIp);
        if(media == null){
            return builder.buildResponse(200,"AudioIp does not exist");
        }
        LinkedList<JsonObject> audioList = securityService.getAudioList(media);
        return builder.buildResponse(200,audioList.toString());
    }

    @POST
    @Path("startRecording")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startRecording(){
        ResponseEntity<String> entity = restTemplate.postForEntity("http://172.20.10.3:8080/audio/recordingStart", null, String.class);
        return builder.buildResponse(200,entity);
    }


}
