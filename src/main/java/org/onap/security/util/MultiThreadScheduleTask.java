package org.onap.security.util;

import org.onap.security.common.CommonConstants;
import org.onap.security.domain.Audio;
import org.onap.security.domain.Media;
import org.onap.security.service.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
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
            Audio audio = ((Media) media).getAudio();
            if(audio.getStatus().equalsIgnoreCase(CommonConstants.WARNING)){
                long timeMillis = System.currentTimeMillis();
                int currentTime = (int)(timeMillis - audio.getStatusTimestamp()) / 1000;
                if(currentTime > CommonConstants.WARNINGMAXTIME){
                    audio.setStatus(CommonConstants.NORMAL);
                    try {
                        WebSocketServer.sendInfo(audio.toString(),((Media) media).getMetadata().getAudioIp());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
