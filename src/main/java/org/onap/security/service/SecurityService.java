package org.onap.security.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.sf.json.JSONObject;
import org.onap.security.common.CommonConstants;
import org.onap.security.common.SystemException;
import org.onap.security.domain.Audio;
import org.onap.security.domain.Media;
import org.onap.security.domain.Metadata;
import org.onap.security.domain.Video;
import org.onap.security.util.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
                RestClient.sendBandWidthEvent(RestClient.ABNORMAL,null);
                logger.info("Push messages to the client："+audioJson.toString());
            } catch (IOException | SystemException e) {
                e.printStackTrace();
            }
        }
    }

    public LinkedList<JsonObject> getAudioList(Media media){
        Audio audio = media.getAudio();
        LinkedList<JsonObject> audioList = audio.getAudioList();
        LinkedList<JsonObject> lastAudioList = (LinkedList<JsonObject>) CommonConstants.map.get(CommonConstants.LASTAUDIOLIST);
        if(lastAudioList.size() > 0){
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<LinkedList<JsonObject>> future = executorService.submit(getAudioListTask(lastAudioList, media));
            try {
                audioList = future.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                executorService.shutdown();
            }
        }
        lastAudioList.clear();
        lastAudioList.addAll(audioList);
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

    public String fromJsonMetadata(){
        String json = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(CommonConstants.JSON_PATH));
            StringBuilder sb = new StringBuilder();
            String value;
            while ((value = br.readLine()) != null){
                sb.append(value);
            }
            br.close();
            json = sb.toString();
        } catch (IOException e) {
            logger.error("character stream read failed！filePath = " + CommonConstants.JSON_PATH);
            e.printStackTrace();
        }
        return json ;
    }

    private void writeJson(Object o){
        BufferedWriter out = null;
        try {
            FileWriter fileWriter = new FileWriter(CommonConstants.JSON_PATH, false);
            out = new BufferedWriter(fileWriter);
            out.append(new Gson().toJson(o));
            out.flush();
        } catch (IOException e) {
            logger.error("character stream write failed！   " + o.toString());
            e.printStackTrace();
        }finally {
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteNodeByAudioIp(String audioIp){
        List<Metadata> metadataList = (List<Metadata>) CommonConstants.map.get(CommonConstants.DEVICE_LIST);
        Optional<Metadata> optionalMetadata = metadataList.stream().filter(o -> o.getAudioIp().equals(audioIp)).findFirst();
        if(optionalMetadata.isPresent()){
            metadataList.remove(optionalMetadata.get());
            writeJson(metadataList);
            logger.info("delete node audioIp = "+audioIp);
        }
    }

    public void addMonitoringNode(Metadata metadata){
        if(metadata != null){
            List<Metadata> metadataList = (List<Metadata>) CommonConstants.map.get(CommonConstants.DEVICE_LIST);
            metadataList.add(metadata);
            writeJson(metadataList);
            initNodeData(metadata);
        }
    }

    public void initNodeData(Metadata metadata){
        Media media = new Media();
        Audio audio = new Audio();
        Video video = new Video();
        media.setAudio(audio);
        media.setVideo(video);
        media.setMetadata(metadata);
        CommonConstants.map.put(CommonConstants.LASTAUDIOLIST,new LinkedList<JSONObject>());
        CommonConstants.map.put(metadata.getAudioIp(),media);
        logger.info("initNodeData metadata is:"+metadata);
    }

}
