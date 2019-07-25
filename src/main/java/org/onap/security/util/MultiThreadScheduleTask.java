package org.onap.security.util;

import com.google.gson.JsonObject;
import org.onap.security.common.CommonConstants;
import org.onap.security.common.SystemException;
import org.onap.security.domain.Audio;
import org.onap.security.domain.Media;
import org.onap.security.service.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@EnableScheduling   // 1.开启定时任务
@EnableAsync        // 2.开启多线程
public class MultiThreadScheduleTask {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadScheduleTask.class);

    /*@Async
    @Scheduled(fixedDelay = 3000)  //间隔3秒
    public void first() throws InterruptedException {

        Thread.sleep(1000 * 10);
    }*/

    @Async
    @Scheduled(fixedDelay = 2000)
    public void second() {
        CommonConstants.map.forEach((key, media) -> {
            if(media instanceof Media){
                Audio audio = ((Media) media).getAudio();
                if(audio.getStatus().equalsIgnoreCase(CommonConstants.WARNING)){
                    long timeMillis = System.currentTimeMillis();
                    int currentTime = (int)(timeMillis - audio.getStatusTimestamp()) / 1000;
                    if(currentTime > CommonConstants.WARNINGMAXTIME){
                        audio.setStatus(CommonConstants.NORMAL);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("status",audio.getStatus());
                        try {
                            WebSocketServer.sendInfo(jsonObject.toString(),((Media) media).getMetadata().getAudioIp());
                            RestClient.sendBandWidthEvent(RestClient.NORMAL,null);
                        } catch (IOException | SystemException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

}
