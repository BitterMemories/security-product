package org.onap.security.util;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.onap.security.common.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;


public class RestClient {

    public final static String NORMAL = "normal";
    public final static String ABNORMAL = "abnormal";
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
    /**
     * 带宽调整URL,192.168.10.251   119.3.20.102/api/ves-collector/v5
     */
    private final static String BANDWIDTHEVENTURL = "http://192.168.10.251:8080/eventListener/v5";
    private final static String FIVEGEVENTURL = "http://192.168.10.212/api/ves-collector/v5";
    private final static int ERRORCODE = 1301;

    /**
     * 发送带宽调整事件
     */
    public static void sendBandWidthEvent(String exceptionType, String sourceIP) throws SystemException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        String body = getBodyByExceptionType(exceptionType.toLowerCase(), sourceIP);
        HttpEntity entity = new HttpEntity(body, headers);

        RestTemplate client = new RestTemplate();
        ResponseEntity<String> response = client.postForEntity(FIVEGEVENTURL, entity, String.class);

        int status = response.getStatusCodeValue();
        if (status < HttpStatus.OK.value() || status > HttpStatus.NON_AUTHORITATIVE_INFORMATION.value()) {
            throw new SystemException(String.join("bandWidth modify failed, statusCode:",String.valueOf(status)), ERRORCODE);
        }
        String responseStr = response.getBody();
        logger.info("response:"+responseStr);

    }

    /**
     * 获取Body体
     *
     * @param exceptionType 下发调整类型
     * @param sourceIP      源地址
     * @return
     * @throws SystemException
     */
    private static String getBodyByExceptionType(String exceptionType, String sourceIP) throws SystemException {
        String typeValue = (NORMAL.equals(exceptionType) || ABNORMAL.equals(exceptionType)) ? exceptionType : null;

//        if (StringUtils.isBlank(typeValue) || StringUtils.isBlank(sourceIP)) {
//            throw new SystemException("Body is null");
//        }

        if (StringUtils.isBlank(typeValue)) {
            throw new SystemException("Body is null");
        }

        Map<String, Object> commonEventHeaderMap = new HashMap<>();
        //必填
        commonEventHeaderMap.put("sourceId", "5G-site-25");
        commonEventHeaderMap.put("startEpochMicrosec", System.currentTimeMillis());
        //必填，must be changed for every event
        commonEventHeaderMap.put("eventId", UUID.randomUUID().toString().replace("-", "").toLowerCase());
        commonEventHeaderMap.put("nfcNamingCode", "");
        commonEventHeaderMap.put("reportingEntityId", "");
        commonEventHeaderMap.put("internalHeaderFields", new HashMap<String, String>());
        commonEventHeaderMap.put("eventType", "bandwidth_notification");
        commonEventHeaderMap.put("priority", "High");
        commonEventHeaderMap.put("version", 3);
        commonEventHeaderMap.put("reportingEntityName", "china mobile booth");
        commonEventHeaderMap.put("sequence", 960);
        commonEventHeaderMap.put("domain", "fault");
        commonEventHeaderMap.put("lastEpochMicrosec", System.currentTimeMillis());
        //必填，固定，必須是bandwidth
        commonEventHeaderMap.put("eventName", "quality");
        commonEventHeaderMap.put("sourceName", "bandwidth_device");
        commonEventHeaderMap.put("nfNamingCode", "");

        List<Map<String, String>> alarmInfoList = new ArrayList<>();
        //必填，normal/abnormal
        alarmInfoList.add(getAlarmInfo("exceptionType", exceptionType));

        Calendar cal = Calendar.getInstance();
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
        String dateString = formatter.format(cal.getTime());

//        Date currentTime = new Date();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
//        String dateString = formatter.format(currentTime);

        //必填，时间戳
        alarmInfoList.add(getAlarmInfo("timestamp", dateString));

        Map<String, Object> faultFieldsMap = new HashMap<>();
        faultFieldsMap.put("eventSeverity", "CRITICAL");
        faultFieldsMap.put("alarmCondition", "The bandwidth is too low or high");
        faultFieldsMap.put("faultFieldsVersion", 2);
        faultFieldsMap.put("eventCategory", "bandwidth");
        faultFieldsMap.put("specificProblem", "The bandwidth is too low or high");
        faultFieldsMap.put("alarmInterfaceA", "VNF_194.15.13.138");
        faultFieldsMap.put("alarmAdditionalInformation", alarmInfoList);
        faultFieldsMap.put("eventSourceType", "CMCCBandwidth");
        faultFieldsMap.put("vfStatus", "Active");


        //JsonObject eventObject = new JsonObject();
        Map<String, Map<String, Object>> eventMap = new HashMap<>();
        eventMap.put("commonEventHeader", commonEventHeaderMap);
        eventMap.put("faultFields", faultFieldsMap);

        Map<String, Object> map = new HashMap<>();
        map.put("event", eventMap);

        JSONObject jsonObj = JSONObject.fromObject(map);

        logger.info(jsonObj.toString());
        return jsonObj.toString();

    }

    private static Map<String, String> getAlarmInfo(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put("name", key);
        map.put("value", value);

        return map;
    }

    public static void main(String[] args) {
        try {                                           //beijing:192.168.13.3 hkg:192.168.11.120
            sendBandWidthEvent("normal", null);
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

}
