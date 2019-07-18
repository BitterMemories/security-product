package org.onap.security.domain;

import org.onap.security.common.CommonConstants;

public class Video {

    private long statusTimestamp;       //Current state timestamp

    private String status;      //Current video status

    public Video() {
        this.statusTimestamp = System.currentTimeMillis();
        this.status = CommonConstants.NORMAL;
    }

    public long getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(long statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
