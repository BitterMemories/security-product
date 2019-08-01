/**
 * Created by liujie on 2018/4/18.
 */

var socketClient;
var lockReconnect = false;
var reconnectTime;

// 监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function () {
    socketClient.close();
}

//建立连接
function createWebSocket(host,ip) {
    console.log(sessionUserInfo, '===sessionUserInfo');
    //	var userInfoData = userInfo;
    //  userInfoData = JSON.stringify(userInfoData);
    //  console.log(userInfoData);
    if ("WebSocket" in window) {
        var socketUrl = 'ws://'+ host +'/websocket/'+ip;
        console.log(socketUrl,host,'socketUrl');
        socketClient = new WebSocket(socketUrl);
        socketClient.binaryType = 'arraybuffer';

        socketClient.onopen = function (data) {// 打开连接
            lockReconnect = true;
            //          sendRegister();
            console.log("onopen打开连接");
            console.log(data);
            reconnect();
            clearTimeout(reconnectTime);
        };

        socketClient.onclose = function (data) { // 关闭连接
            console.log("onopen关闭连接")
            console.log(data)
            console.info("close websocket");
            lockReconnect = false;
            reconnect();
        };

        //连接发生错误的回调方法
        	    socketClient.onerror = function (data) {
        	    	console.log("onerror 连接发生错误");
        	    	console.log(data);
        	    	lockReconnect = false;
                    //setMessageInnerHTML("WebSocket连接发生错误");
        	    };

        socketClient.onmessage = function (event) { // 弹消息
            console.log("onmessage 弹消息");
            console.log(event);
            onMessageShow(event.data);
        };
    } else {
        var winService = CommonService();
        winService.alertWinMessage("当前浏览器不支持 websocket\n 会导致无法下载视频及无法收到告警信息\n 请升级浏览器");
    }
}

//跟后台webSocket建立的连接
function reconnect() {
    if (lockReconnect)
        return;
    reconnectTime = setTimeout(function () { //没连接上会一直重连，设置延迟避免请求过多
        createWebSocket();
        lockReconnect = false;
    }, 2000);
}

//后台返回的消息，前端改变视频状态值
function onMessageShow(event) {
    var currentStatus = JSON.parse(event);
    console.log(currentStatus.status,'-----socket event');
    if (currentStatus.status == "Warning" || currentStatus.status == "warning") {
        WarningAudio(currentStatus.status);
    } else if (currentStatus.status == "Normal" || currentStatus.status == "normal") {
        NormalAudio(currentStatus.status);
    }
};
