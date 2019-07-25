package org.onap.security.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.onap.security.controller.SecurityController;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    /**
     * Scan the org.onap.security.controller package to recognize JAX-RS annotations
     */
    public JerseyConfig() {
        //packages("org.onap.security.controller");
        register(SecurityController.class);
    }

}
