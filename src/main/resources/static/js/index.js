//2019.07.11 add
$(document).ready(function() {
    var trigger = $('.hamburger'),
        // overlay = $('.overlay'),
        isClosed = false;
    trigger.click(function() {
        hamburger_cross();
    });
    function hamburger_cross() {
        if (isClosed == true) {
            // overlay.hide();
            trigger.removeClass('is-open');
            trigger.addClass('is-closed');
            isClosed = false;
        } else {
            // overlay.show();
            trigger.removeClass('is-closed');
            trigger.addClass('is-open');
            isClosed = true;
        }
    }
    $('[data-toggle="offcanvas"]').click(function() {
        $('#wrapper').toggleClass('toggled');
    });
});

var httpUrl = "http://172.20.10.2:8080/api";
var httpsUrl = "https://172.20.10.2:8080/api";
// var httpUrl = "http://192.168.199.157:8888";
// var httpsUrl = "https://192.168.199.157:8884";
//var socketUrl = "192.168.17.8";
//var socketUrl = window.location.host;

//用来接收用户信息接口请求过来的data
var sessionUserInfo = {
	"ipAddress":""
};

// 用于存放 MediaRecorder 对象和音频Track，关闭录制和关闭媒体设备需要用到
var recorder, mediaStream;

// 保存音频文件的索引
var savecount = 1;

//用于存放录音的音频文件
var chunks = [];

// 用于存放录制后的音频文件对象和录制结束回调
var recorderFile, stopRecordCallback;

// 用于存放是否开启了视频录制
// var videoEnabled = false;
// var audioCtx = new (window.AudioContext || window.webkitAudioContext)();
//
// var analyser = audioCtx.createAnalyser();
// analyser.minDecibels = -90;
// analyser.maxDecibels = -10;
// analyser.smoothingTimeConstant = 0.85;
//
// var distortion = audioCtx.createWaveShaper();
// var gainNode = audioCtx.createGain();
// var biquadFilter = audioCtx.createBiquadFilter();
// var convolver = audioCtx.createConvolver();

var sendWav;

//用于存放请求接口后存放拼接的路径
var urlList = [];
var num = 0;//每次播放的数

var flag = false;
var ajax_num = 0;
//创建一个waveurfer实例，用于播放音频的音波
var wavesurfer;

//初始化视频接口参数
var oPlugin = {
    iWidth: "100%",             // plugin width
    iHeight: "100%"             // plugin height
};

var oLiveView = {
    iProtocol: 1,            // protocol 1：http, 2:https
    szIP: "",    // protocol ip
    szPort: "80",            // protocol port
    szUsername: "",     // device username
    szPassword: "", // device password
    audioIp:"",
    iStreamType: 1,          // stream 1：main stream  2：sub-stream  3：third stream  4：transcode stream
    iChannelID: 1,           // channel no
    bZeroChannel: false      // zero channel
};

//调用 获取分支的接口
branchList();

//调用用户信息接口
// getMedia();

//-----------------------------------方法---------------------------------------------
//2019.07.12 add

function branchList() {
	console.log("branchList（） start");
    $.ajax({
        type: "get",
        url: httpUrl + '/getMetadata',
		// data:{},
        async: false,
        success: function (data) {
            console.log(data,"branchList");
			var str="<li class='sidebar-brand'>"
                +"<a href='javascript:void(0);'>智能监控</a>"
                +"</li>";
			for(var i=0;i<data.length;i++){
                str = str
					+"<li class='dropdown nav-li'>"
                    +"<a href='javascript:void(0);' class='dropdown-toggle branch-title' data-toggle='dropdown' data-username='"+data[i].username+"' data-password='"+data[i].password+"' data-deviceip='"+data[i].deviceIp+"' data-audioip='"+data[i].audioIp+"'><i class='fa fa-fw fa-plus'></i> |&nbsp;&nbsp;"
					+data[i].deviceSite
					+"<span class='caret'></span></a>"
					+"<ul class=' nav-li-ul' role='menu'>"
					+"<li><a href='javascript:void(0);' class='audiobranch' data-audioip='"+data[i].audioIp+"'> |&#45;&#45;音频</a></li>"
					+ "<li><a href='javascript:void(0);' class='videobranch' data-deviceip='"+data[i].deviceIp+"'> |&nbsp;&nbsp;视频</a></li>"
					+ "</ul>"
					+ "</li>"
			};
			$(".nav-list").html(str);
            $(".nav-list").find(".nav-li-ul").eq(0).show();
            oLiveView.szIP = data[1].deviceIp;
            oLiveView.szPort = 8888;
            oLiveView.szUsername =  data[1].username;
            oLiveView.szPassword =  data[1].password;
            oLiveView.audioIp =  data[1].audioIp;
            sessionUserInfo.ipAddress =  data[1].audioIp;
            getMediaAudio();
            audioPlay();
            // Login(oLiveView);
        },
		error:function(err){
			console.log(err);
		}
    });
}

//音频状态 初始化接口
function getMediaAudio() {
	$.ajax({
		type: "get",
		url: httpUrl + '/initIndex/'+oLiveView.audioIp,
		async: false,
		success: function (data) {
			//console.log(data);
			//初始化状态值：
			if (data == null || data == "" || data == undefined) {
				console.log(data)
			} else {
				//WarningAudio(data.audio.status);
				if (data.audio.status.toLocaleLowerCase() == "warning") {
					WarningAudio(data.audio.status);
				} else if (data.audio.status.toLocaleLowerCase() == "normal") {
					NormalAudio(data.audio.status);
				} else {
					console.log(data.audio.status,"data.audio.status");
				}
				// if(data.audio.status == "Warning" && data.video.status == "Warning"){
				// 	//WarningVideo(data.video.status);
				// 	WarningAudio(data.audio.status);
				// }else if(data.audio.status == "Normal" && data.video.status == "Normal"){
				// 	console.log(data.audio.status,"====audiostatus");
				// 	NormalAudio(data.audio.status);
				// 	//NormalVideo(data.video.status);
				// }else if(data.audio.status == "Warning" && data.video.status == "Normal"){
				// 	console.log(data.audio.status);
				// 	WarningAudio(data.audio.status);
				// 	//NormalVideo(data.video.status);
				// }else if(data.audio.status == "Normal" && data.video.status == "Warning"){
				// 	NormalAudio(data.audio.status);
				// 	//WarningVideo(data.video.status);
				// };
			}
		},
		//			error:function(err){
		//				console.log(err);
		//				window.location.href="../video";
		//			}
	});
}

// 2019.07.08 add   Login()  clickStartRealPlay()

function Login(oLiveView) {

    WebVideoCtrl.I_Login(
    	oLiveView.szIP,
		oLiveView.iProtocol,
		oLiveView.szPort,
		oLiveView.szUsername,
		oLiveView.szPassword,{
        success: function (xmlDoc) {
            // 开始预览
            var szDeviceIdentify = oLiveView.szIP + "_" + oLiveView.szPort;
            setTimeout(function () {
                WebVideoCtrl.I_StartRealPlay(
                	szDeviceIdentify, {// 开始预览
                    iStreamType: oLiveView.iStreamType,
                    iChannelID: oLiveView.iChannelID,
                    bZeroChannel: oLiveView.bZeroChannel
                });
            }, 1000);
        },
        error: function (status, xmlDoc) {
            console.log(szDeviceIdentify + " 登录失败！", status, xmlDoc)
            // showOPInfo(szDeviceIdentify + " 登录失败！", status, xmlDoc);
        }
    });

}

//录音插件的方法
var MediaUtils = {
	/**
	 * 获取用户媒体设备(处理兼容的问题)
	 * @param videoEnable {boolean} - 是否启用摄像头
	 * @param audioEnable {boolean} - 是否启用麦克风
	 * @param callback {Function} - 处理回调
	 */
	getUserMedia: function (videoEnable, audioEnable, callback) {
		navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia
			|| navigator.msGetUserMedia || window.getUserMedia;
		//var constraints = {video: videoEnable, audio: audioEnable};
		var constraints = { audio: audioEnable };
		if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
			navigator.mediaDevices.getUserMedia(constraints).then(function (stream) {
				callback(false, stream);
			}).catch(function (err) {
				callback(err);
			});
		} else if (navigator.getUserMedia) {
			navigator.getUserMedia(constraints, function (stream) {
				callback(false, stream);
			}, function (err) {
				callback(err);
			});
		} else {
			callback(new Error('Not support userMedia'));
		}


		if (navigator.mediaDevices === undefined) {
			navigator.mediaDevices = {};
		}

		if (navigator.mediaDevices.getUserMedia === undefined) {
			navigator.mediaDevices.getUserMedia = promisifiedOldGUM;
		}
	},

	/**
	 * 关闭媒体流
	 * @param stream {MediaStream} - 需要关闭的流
	 */
	closeStream: function (stream) {
		if (typeof stream.stop === 'function') {
			stream.stop();
		} else {
			let trackList = [stream.getAudioTracks(), stream.getVideoTracks()];

			for (let i = 0; i < trackList.length; i++) {
				let tracks = trackList[i];
				if (tracks && tracks.length > 0) {
					for (let j = 0; j < tracks.length; j++) {
						let track = tracks[j];
						if (typeof track.stop === 'function') {
							track.stop();
						}
					}
				}
			}
		}
	}
};

//录音开始
function start() {
	tabStatus = "tabNative";
	startRecord(true);   //调用录制短语音
	sendWav = setInterval("save5secData()", 3000);  // in (ms)   //5s 保存一次文件
}

//录制结束
function stop() {
	clearInterval(sendWav);
	stopRecord();
}

// 录制短语音      开始录制
function startRecord(enableVideo) {
	videoEnabled = enableVideo;     //enableVideo  形参  true  /false
	MediaUtils.getUserMedia(enableVideo, true, function (err, stream) {
		if (err) {
			throw err;
		} else {
			source = audioCtx.createMediaStreamSource(stream);
			source.connect(analyser);
			analyser.connect(distortion);
			distortion.connect(biquadFilter);
			biquadFilter.connect(convolver);
			convolver.connect(gainNode);
			gainNode.connect(audioCtx.destination);

			visualizeNew();  //调用新的音频动画
			// 通过 MediaRecorder 记录获取到的媒体流
			recorder = new MediaRecorder(stream);  //每次录音时新建的媒体流
			mediaStream = stream;
			// onstop and ondataavailable change position
			recorder.onstop = function (e) {  //onstop方法是？？
				chunks = [];
				if (null != stopRecordCallback) {
					stopRecordCallback();
				}
			};
			recorder.ondataavailable = function (e) {  //ondataavailable方法是？？？
				chunks.push(e.data);
				recorderFile = new Blob(chunks, { 'type': recorder.mimeType });   //新文件

				var data = new FormData(); //ajax请求文件上传，需要创建一个FormData()对象
				data.append("file", recorderFile);  //调java用file接收

				//jquery写法
				$.ajax({
					type: "POST",
					url: httpsUrl + "/restful/jersey/recording",
					data: data,
					dataType: "json",
					cache: false,//上传文件无需缓存
					processData: false, //processData设置为false，必须。因为data值是FormData对象，不需要对数据做处理。用于对data参数进行序列化处理 这里必须false
					contentType: false,  //contentType设置为false，必须。不设置contentType值，因为是由<form>表单构造的FormData对象，且已经声明了属性enctype="multipart/form-data"，所以这里设置为false。
					success: function (data) {
						//			        		console.log(data);
						if (data.status.toLocaleLowerCase() == "warning" && data.refresh == 1) {
							console.log("status==1 --->" + data.refresh);
							WarningAudio(data.status);
						} else if (data.status.toLocaleLowerCase() == "normal" && data.refresh == 1) {
							console.log("status==0 --->" + data.refresh);
							NormalAudio(data.status);
						}
					},
					error: function (error) {
						console.log(error);
					}
				});
			};
			recorder.start();
		}
	});
}

//5s请求一次
function save5secData() {
	recorder.stop();  //5s先停止，然后直接再开始录制；
	//console.log("save");
	recorder.start();
}

// 停止录制
function stopRecord(callback) {
	stopRecordCallback = callback;
	// 终止录制器
	recorder.stop();
	// 关闭媒体流
	MediaUtils.closeStream(mediaStream);
}

var times=setInterval(audioPlay, 5000);
//调用音频文件的接口，播放
function audioPlay() {
	$.ajax({
		type: "get",
		url: httpUrl + '/getAudioList/'+sessionUserInfo.ipAddress,
		async: false,
		// data: {
		// 	"client": sessionUserInfo.name
		// },
		success: function (data) {
			if(data.length ==0 ){
				return false;
			}else {
			clearInterval(times);
			ajax_num++;
			console.log("这是第" + ajax_num + "次请求");
            // visualizeNew();
            $.each(data, function (i, obj) {
				urlList.push("http://"  + obj.path);
				if (this) {
					if (this.status.toLocaleLowerCase() == "warning" && this.refresh == 1) {
						console.log("status==1 --->" + this.refresh + this.refresh);
						WarningAudio(this.status);
					} else if (this.status.toLocaleLowerCase() == "normal" && this.refresh == 1) {
						console.log("status==0 --->" + this.refresh)
						NormalAudio(this.status);
					}
				}
			});

			console.log("urlList---------->");
			console.log(urlList);
			if (flag == false) {
				console.log("我只会出现一次哦！！！！！！！！！！！！")
				flag = true;
				if (ajax_num == 1) {
					//每次都重新创建WaveSurfer实例
					wavesurfer = WaveSurfer.create({
						container: '#waveform', //容器
						height: 250,
						waveColor: '#7C358F', //光标下面底部波形的填充颜色。
						progressColor: 'rgba(0, 255, 255, 0.2)',   //光标后面进度的波形部分的填充颜色。
						cursorColor: "#00FFFF", //光标的填充颜色表示播放头位置。
						responsive: 'ture',
						autoCenter: "true",
						overflow: "hidden",
					});

					//调用音频插件数组
					wavesurferPlay(urlList);//插件方法自动播放下一条音频
				}
			} else {

			}
		}
		}
	});
}

//插件自动播放下一条音频
function wavesurferPlay(arr) {
	//加载路径
	wavesurfer.load(arr[num]);
	// $("#audio").attr("src",(arr[num]));
	//播放
	wavesurfer.on('ready', function () {
		wavesurfer.play(); //播放
		console.log("313行-播放第" + num + "条" + arr[num]);
	});

	//监听播放完成的时
	wavesurfer.on('finish', function () {
		console.log("第" + num + "条end");
		num++;
		wavesurfer.load(arr[num]);//加载路径
		//播到最后一条音频时，再次请求ajax
		if (num == arr.length - 1) {
			audioPlay();//再次请求ajax
		};
		//num为6时，清空前6条
		if (num == 6) {
			num = 0;
			urlList.splice(0, 6);
			console.log(arr);
			wavesurfer.load(arr[num]);
		}
	});

	//监听播放错误的情况下，未找到路径，直接调接口
	wavesurfer.on('error', function () {
		console.log("第" + num + "条报错了，未找到路径")
		wavesurfer.load(urlList[num]); //加载当前num
		console.log("345行-播放第几条" + urlList[num]);
		audioPlay();  //调用音频路径的接口
		wavesurfer.play(); //播放
	});
}

//监听窗口放大缩小时canvas的变动
// $(document).ready(function () {
// 	$(window).resize(function () {
// 		var canvas = document.getElementById('canvas');
// 		canvas.setAttribute('width', 250);
// 		canvas.setAttribute('height', 157);
// 	});
// });

//跳动音频的canvas
function visualizeNew() {
	// we're ready to receive some data!
	var canvas = document.getElementById('canvas'),
		cwidth = canvas.width,
		cheight = canvas.height - 2,  //底部高度固定2，看着细
		meterWidth = 5, //每一条的宽度
		gap = 2, //每一条竖线之间的距离
		capHeight = 2, //顶部跳跃的高度
		capStyle = '#B366FF', //顶部跳跃的配色
		meterNum = 600 / (10 + 2), //count of the meters
		capYPositionArray = []; //store the vertical position of hte caps for the preivous frame
	ctx = canvas.getContext('2d'),
		gradient = ctx.createLinearGradient(0, 0, 0, 300),
		//  gradient.addColorStop(1, '#0f0');  //底部绿色
		//  gradient.addColorStop(0.5, '#ff0');  //中间黄色
		//  gradient.addColorStop(0, '#f00'); //顶部红色
		gradient.addColorStop(0, '#0000FF');
	gradient.addColorStop(0.5, '#3D44E0');
	gradient.addColorStop(1, '#B366FF');

	//		canvas.setAttribute('width',divCanvas.offsetWidth);

	// loop
	function renderFrame() {
		var array = new Uint8Array(analyser.frequencyBinCount);
		analyser.getByteFrequencyData(array);
		var step = Math.round(array.length / meterNum); //sample limited data from the total array
		ctx.clearRect(0, 0, cwidth, cheight);
		for (var i = 0; i < meterNum; i++) {
			var value = array[i * step];
			if (capYPositionArray.length < Math.round(meterNum)) {
				capYPositionArray.push(value);
			};
			ctx.fillStyle = capStyle;
			//draw the cap, with transition effect
			if (value < capYPositionArray[i]) {
				ctx.fillRect(i * 12, cheight - (--capYPositionArray[i]), meterWidth, capHeight);
			} else {
				ctx.fillRect(i * 12, cheight - value, meterWidth, capHeight);
				capYPositionArray[i] = value;
			};
			ctx.fillStyle = gradient; //set the filllStyle to gradient for a better look
			ctx.fillRect(i * 12 /*meterWidth+gap*/, cheight - value + capHeight, meterWidth, cheight); //the meter
		}
		requestAnimationFrame(renderFrame);
	}
	renderFrame();
};
//-----------------------------------------------------------------------------------
//点击关闭，调接口后关闭窗口
function close() {
	//
	window.close(htmlobj.responseText);
}
//音频显示warning
function WarningAudio(data) {
	console.log(data, 'warning audio data======------')
	//		$("#statusAudio").html("Warning");//音频状态标识
	$("#statusAudio").html(data);
	$("#statusAudio").css("color", "#FF0048").addClass("warningText");
	$(".status .lds-css2").addClass("disBlock").removeClass("disNone");
	$(".status .audio_img1").removeClass("disNone").attr("src", '/image/Warning31.png');
}
//音频显示normal
function NormalAudio(data) {
	console.log(data, 'normal audio data======------')
	//		$("#statusAudio").html("Normal"); //音频状态标识
	$("#statusAudio").html(data);
	$("#statusAudio").css("color", "#00FFFF").removeClass("warningText");
	$(".status .lds-css2").addClass("disNone").removeClass("disBlock")
	$(".status .audio_img1").removeClass("disNone").attr("src", '/image/Normal46.png');;
}
//视频显示warning
function WarningVideo(data) {
	$("#statusVideo").html(data);//音频状态标识
	//		$("#statusVideo").html("Warning");//音频状态标识
	$("#statusVideo").css("color", "#FF0048");
	$(".status .lds-css2").addClass("disBlock").removeClass("disNone");
	$(".status .audio_img2").addClass("disNone");
}
//视频显示normal
function NormalVideo(data) {
	//		$("#statusVideo").html("Normal"); //音频状态标识
	$("#statusVideo").html(data); //音频状态标识
	$("#statusVideo").css("color", "#00FFFF");
	$(".status .lds-css2").addClass("disNone").removeClass("disBlock")
	$(".status .audio_img2").removeClass("disNone");
}

function showPage() {
	$("body").css("display", "block");
}

$(function () {
    // 检查插件是否已经安装过
    var iRet = WebVideoCtrl.I_CheckPluginInstall();
    if (-1 == iRet) {
        alert("您还未安装过插件，双击开发包目录里的WebComponentsKit.exe安装！");
        return;
    }

    // 初始化插件参数及插入插件
    WebVideoCtrl.I_InitPlugin(oPlugin.iWidth, oPlugin.iHeight, {
        bWndFull: true,//是否支持单窗口双击全屏，默认支持 true:支持 false:不支持
        iWndowType: 1,
        cbInitPluginComplete: function () {
            WebVideoCtrl.I_InsertOBJECTPlugin("divPlugin");

            // 检查插件是否最新
            if (-1 == WebVideoCtrl.I_CheckPluginVersion()) {
                alert("检测到新的插件版本，双击开发包目录里的WebComponentsKit.exe升级！");
                return;
            }

            // // 登录设备
            // WebVideoCtrl.I_Login(oLiveView.szIP, oLiveView.iProtocol, oLiveView.szPort, oLiveView.szUsername, oLiveView.szPassword, {
            //     success: function (xmlDoc) {
            //         // 开始预览
            //         var szDeviceIdentify = oLiveView.szIP + "_" + oLiveView.szPort;
            //         setTimeout(function () {
            //             WebVideoCtrl.I_StartRealPlay(szDeviceIdentify, {
            //                 iStreamType: oLiveView.iStreamType,
            //                 iChannelID: oLiveView.iChannelID,
            //                 bZeroChannel: oLiveView.bZeroChannel
            //             });
            //         }, 1000);
            //     }
            // });
        }
    });



    //让页面延迟加载，不出现小图
	setTimeout(showPage, 1);
	//一进页面开始调录音
	// start();
	//一进页面开始调用放音
    // audioPlay();
	//一进页面开始调视频地址
	// $(".video img").attr("src", "http://192.168.17.3:5001/video_feed");

	//调用创建websocket连接
	createWebSocket(sessionUserInfo.ipAddress);

	//WarningAudio('Warning')
	//setTimeout(NormalAudio('Normal'), 10000);

	/*点击图片弹出视频框*/
	$(".footer_img img").on("click", function () {
		$("#videoBig").css("display", "block");
		var video = document.getElementsByTagName("video")[0];
		video.preload();
		video.load();
	});
	$("#videoBig").on("click", function () {
		$("#videoBig").css("display", "none");

	});

	//刷新页面
	$(".logo_onapIcon").on("click", function () {
		window.location.reload();
	});

    // //左侧菜单栏点击
  	$(".nav-list").on('click','.branch-title',function(){
  		if($(this).siblings(".nav-li-ul").is(':hidden')){
            $(this).siblings(".nav-li-ul").show();
		}else {
            $(this).siblings(".nav-li-ul").hide();
		}

        oLiveView.szIP = $(this).attr("data-deviceip");
        oLiveView.szPort = 8888;
        oLiveView.szUsername =  $(this).attr("data-username");
        oLiveView.szPassword =  $(this).attr("data-password");
        oLiveView.audioIp =  $(this).attr("data-audioip");
        sessionUserInfo.ipAddress = $(this).attr("data-audioip");
  	});
    $(".nav-list").on('click','.audiobranch',function(){
        getMediaAudio();
        audioPlay();
    });
    $(".nav-list").on('click','.videobranch',function(){
        Login(oLiveView);
    });
});
