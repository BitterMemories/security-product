package org.onap.security.common;

import org.springframework.stereotype.Component;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
public class ResponseBuilder {

    public Response buildResponse(int status, Object jsonResponse){
        javax.ws.rs.core.Response.ResponseBuilder builder =
                Response.status(status).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return builder.entity(jsonResponse).build();
    }
}
