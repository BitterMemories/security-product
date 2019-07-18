window.AudioContext = window.AudioContext || window.webkitAudioContext || window.mozAudioContext;

function visualizeNew() {
    var audio = document.getElementById('audio');
    var ctx = new AudioContext();
    var analyser = ctx.createAnalyser();
    var audioSrc = ctx.createMediaElementSource(audio);
    // we have to connect the MediaElementSource with the analyser 
    audioSrc.connect(analyser);
    analyser.connect(ctx.destination);
    // we could configure the analyser: e.g. analyser.fftSize (for further infos read the spec)
    // analyser.fftSize = 64;
    // frequencyBinCount tells you how many values you'll receive from the analyser
    var frequencyData = new Uint8Array(analyser.frequencyBinCount);

    // we're ready to receive some data!
    var canvas = document.getElementById('canvas'),
        cwidth = canvas.width,
        cheight = canvas.height -2,
        meterWidth = 5, //width of the meters in the spectrum
        gap = 2, //gap between meters
        capHeight = 2,
//      capStyle = '#fff', //原来的顶部跳跃的配色
//         capStyle = '#B366FF',
        capStyle = '#A403FF',
        meterNum = 800 / (10 + 2), //count of the meters
        capYPositionArray = []; ////store the vertical position of hte caps for the preivous frame
    ctx = canvas.getContext('2d'),
    gradient = ctx.createLinearGradient(0, 0, 0, 300);
//  gradient.addColorStop(1, '#0f0');  //底部绿色
//  gradient.addColorStop(0.5, '#ff0');  //中间黄色
//  gradient.addColorStop(0, '#f00'); //顶部红色
//      gradient.addColorStop(0.5, '#0000FF');
//     gradient.addColorStop(1, '#3D44E0');
//     gradient.addColorStop(0.5, '#B366FF');
    gradient.addColorStop(0.5, '#7F07FF');
    gradient.addColorStop(1, '#A304FF');
    gradient.addColorStop(0.5, '#BA02FF');
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
            ctx.fillRect(i * 12 /*meterWidth+gap*/ , cheight - value + capHeight, meterWidth, cheight); //the meter
        }
        requestAnimationFrame(renderFrame);
    }
    renderFrame();
    audio.play();
};
