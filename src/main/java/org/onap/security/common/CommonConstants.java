package org.onap.security.common;

import java.util.concurrent.ConcurrentHashMap;

public final class CommonConstants {

    public static final String WARNING = "warning";

    public static final String NORMAL = "normal";

    public static final String DEVICE_LIST = "deviceList";

    public static final String LASTAUDIOLIST = "lastAudioList";

    public static final int WARNINGMAXTIME = 45;

    public static ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

}
