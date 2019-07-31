package org.onap.security.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.onap.security.common.CommonConstants;
import org.onap.security.domain.Metadata;
import org.onap.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitConfig implements CommandLineRunner {

    @Value("${JSON_PATH}")
    private String JSON_PATH;

    @Autowired
    private SecurityService securityService;

    //@Value("classpath:json/device.json")
    //private Resource resource;

    @Override
    public void run(String... args){
        CommonConstants.JSON_PATH = this.JSON_PATH;
        String json = securityService.fromJsonMetadata();
        if(!json.isEmpty()){
            List<Metadata> metadataList = new Gson().fromJson(json,new TypeToken<List<Metadata>>(){}.getType());
            metadataList.forEach(metadata -> securityService.initNodeData(metadata));
            CommonConstants.map.put(CommonConstants.DEVICE_LIST,metadataList);
        }
    }
}