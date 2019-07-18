package org.onap.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.JsonObject;
import org.onap.security.common.CommonConstants;

import java.util.LinkedList;

public class Audio {

    private String status;      //Current audio status

    private String path;        //Current audio access path

    private long statusTimestamp;       //Current state timestamp

    private LinkedList<JsonObject> audioList;

    public Audio() {
        this.statusTimestamp = System.currentTimeMillis();
        this.status = CommonConstants.NORMAL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(long statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    @JsonIgnore
    public LinkedList<JsonObject> getAudioList() {
        if(audioList == null){
            audioList = new LinkedList<>();
        }
        return audioList;
    }

    public void setAudioList(LinkedList<JsonObject> audioList) {
        this.audioList = audioList;
    }

}
