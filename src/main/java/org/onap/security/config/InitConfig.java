package org.onap.security.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sf.json.JSONObject;
import org.onap.security.common.CommonConstants;
import org.onap.security.domain.Audio;
import org.onap.security.domain.Media;
import org.onap.security.domain.Metadata;
import org.onap.security.domain.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

@Component
public class InitConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitConfig.class);



    //@Value("classpath:json/device.json")
    //private Resource resource;

    @Override
    public void run(String... args){
        String json = null;
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("json/device.json");
            //File resourceFile = resource.getFile();
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(resourceFile)));
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String value;
            while ((value = br.readLine()) != null){
                sb.append(value);
            }
            br.close();
            json = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.fromObject(json);
        if(!jsonObject.isEmpty()){
            String deviceList = jsonObject.getString("deviceList");
            List<Metadata> metadataList = new Gson().fromJson(deviceList,new TypeToken<List<Metadata>>(){}.getType());
            metadataList.forEach(metadata -> {
                Media media = new Media();
                Audio audio = new Audio();
                Video video = new Video();
                media.setAudio(audio);
                media.setVideo(video);
                media.setMetadata(metadata);
                CommonConstants.map.put(CommonConstants.LASTAUDIOLIST,new LinkedList<JSONObject>());
                CommonConstants.map.put(metadata.getAudioIp(),media);
                logger.debug("metadata is:"+metadata);
            });
            CommonConstants.map.put(CommonConstants.DEVICE_LIST,metadataList);
        }
    }
}