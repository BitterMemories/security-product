package org.onap.security.service;

import com.google.gson.JsonObject;
import org.onap.security.common.CommonConstants;
import org.onap.security.domain.Audio;
import org.onap.security.domain.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.*;

@Service
public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);


    public void addAudioList(Media media, JsonObject audioJson){
        Audio audio = media.getAudio();
        LinkedList<JsonObject> audioList = audio.getAudioList();
        audioList.add(audioJson);
        if(audioList.size() > 3){
            audioList.removeFirst();
        }
        String status = audioJson.get("status").getAsString();
        if(status.equalsIgnoreCase(CommonConstants.WARNING)&&audio.getStatus().equalsIgnoreCase(CommonConstants.NORMAL)){
            audio.setStatus(status);
            audio.setStatusTimestamp(System.currentTimeMillis());
            try {
                WebSocketServer.sendInfo(audioJson.toString(),media.getMetadata().getAudioIp());
                logger.debug("Push messages to the client："+audioJson.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public LinkedList<JsonObject> getAudioList(Media media){
        Audio audio = media.getAudio();
        LinkedList<JsonObject> audioList = audio.getAudioList();
        //JsonObject jsonObject = new JsonObject();
        //jsonObject.add("audioList", new Gson().toJsonTree(jsonObject));
        //jsonObject.addProperty("key",media.getMetadata().getAudioIp());

        LinkedList<JsonObject> lastAudioList = (LinkedList<JsonObject>) CommonConstants.map.get(CommonConstants.LASTAUDIOLIST);
        if(lastAudioList.size() > 0){
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<LinkedList<JsonObject>> future = executorService.submit(getAudioListTask(lastAudioList, media));
            try {
                audioList = future.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }finally {
                executorService.shutdown();
            }
        }
        lastAudioList.clear();
        lastAudioList.addAll(audioList);
        lastAudioList.forEach(System.out::println);
        CommonConstants.map.put(CommonConstants.LASTAUDIOLIST,lastAudioList);
        return audioList;
    }

    private Callable<LinkedList<JsonObject>> getAudioListTask(LinkedList<JsonObject> lastAudioList,Media media){
        return () -> {
            boolean result = true;
            LinkedList<JsonObject> audioList = null;
            while (result){
                audioList = media.getAudio().getAudioList();
                JsonObject lastAudio = lastAudioList.getLast();
                if(audioList.contains(lastAudio)){
                    Thread.sleep(3000);
                    continue;
                }
                result = false;
            }
            return audioList;
        };
    }

}
