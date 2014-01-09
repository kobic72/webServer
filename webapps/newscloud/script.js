//로딩이 완료되면 실행
window.addEventListener("load", function load(event){
    window.removeEventListener("load", load, false); //remove listener, no longer needed
    loadingPage();  
},false);

var loadingPage = function(){
	console.log("loading complete");
	//슬라이드 애니메이션 등록
	controlMenu.addEventListener('click', function(e){linkEvent(e)}, false);
	
	//신문사 랜덤 인덱스 생성
	newspaperRandomIndex();
	
	//태그 생성하고 표시
	canvasInit();
}


//일단 상수로 담아두자 - 나중에 동적으로 받아올 것
var canvasWidth = 1180;
var canvasHeight = 580;

/*	
	슬라이드 애니메이션 
*/
//슬라이드 방향
var scrollDirection = {
    'PREVIOUS' : 0,
    'NEXT' : 1,
    'AUTO' : 2
}

//스크롤되는 태그 페이지 수(태그 클라우드가 생성된 신문사의 수)
var tagPageNumber = document.querySelectorAll("#tag_cloud > ul > li").length;

//스크롤되는 단위 크기 = 태그 클라우드 표시 영역의 가로 길이
var tagElement = document.getElementById("tag_cloud");
var TagCloudWidth = parseInt(window.getComputedStyle(tagElement,null).getPropertyValue("width"), 10);
//var TagCloudWidth = canvasWidth;

//animation 관련 변수
var scrollTime = 700; //500ms
var animationInterval = 50; //10ms
var animationFlag = false; //애니메이션 재생 도중 입력에 의한 오류 제어

var controlMenu = document.getElementById("control_menu");

//이동되는 방향에 따른 이벤트 콜백 함수 생성
//조심해! - 나중에 자동 스크롤 기능 추가할 것
var scroll = function(e, direction){
	//console.log("scroll()");
	
	//현재 애니메이션이 진행 중이라면 이번 입력은 처리하지 않는다.
	//사용자가 불편하다면 나중에 버퍼에 담아두고 재생이 완료된 후에 재생할 수도...?
	if(animationFlag)
		return;
	
	//애니메이션을 적용할 타겟 설정
	var tags = document.getElementById("tags");
	var startPosition = parseInt(window.getComputedStyle(tags,null).getPropertyValue("left"), 10);
	var animationDirection; //스크롤 방향 : 다음 페이지인 경우 타겟이 왼쪽으로 이동하므로 -1, 이전 페이지이면 반대
	
	switch(direction){
	case scrollDirection['NEXT']:
        if (startPosition <= -TagCloudWidth * (tagPageNumber - 1) )
        	return;
        	
    	animationDirection = -1;
        break;
    case scrollDirection['PREVIOUS']:
		if (startPosition >= 0)
        	return;
        
        animationDirection = 1;
        break;
    default:
    	//현재 이벤트가 준비된 버튼이 아닌 경우 linkEvent()에서 필터링 되지만, 만약 진입하더라도 여기서 중지
        return;  	
    }
    
    //tag cloud도 교체한다.
    currentCanvas.change(direction);
    
    animationFlag = true;
    var animationKeyFrame = 0;
    //console.log(startPosition);

	//애니메이션 재생 시작으로부터 현재까지의 시간 값을 인자로 받아서 타겟을 적절한 위치로 이동 시킴
	var move = function(frame){
		//console.log("move()");
		
		var leftValue = startPosition + (animationDirection * TagCloudWidth * Math.sin( (Math.PI / 2) * (frame / scrollTime) ) );
	    
	    //console.log(startPosition);		    
		//console.log(leftValue);
	    tags.style.left = leftValue + "px";	
	}
	
    //주기적으로 불리어져서 현재 타겟이 위치할 위치값(전체 이동해야 할 거리에 대한 비율)을 계산해서 이동시키는 함수 호출
    //목표지점에 도달하면 setInterval 종료
    var scrollAnimation = function(){
    	//console.log("scrollAnimation()");
	
	    animationKeyFrame += animationInterval;
	    
	    if (animationKeyFrame >= scrollTime){
	    	animationKeyFrame = scrollTime;
	    	animationFlag = false;
	    	clearInterval(animationIntervalID);
	    }
	    
	    //console.log(animationKeyFrame);
	    move(animationKeyFrame);
	    
    }
    
    var animationIntervalID = setInterval(scrollAnimation, animationInterval);
}

//이벤트가 발생한 타겟에 적절한 이벤트를 연결    
var linkEvent = function(e){
	var target = e.target.parentNode;
	
	if (target.id === "next_page")
	{
		scroll(e, scrollDirection['NEXT']);
	}
	if (target.id === "previous_page")
	{
		scroll(e, scrollDirection['PREVIOUS']);
	}
	if (target.id === "refresh")
	{
		currentCanvas.drawingStart();
	}
}


/*	
	신문사 랜덤 인덱스 생성
*/
var newsPaperList = [
						['경향신문', 'http://www.khan.co.kr/', 0],
						['오마이뉴스', 'http://www.ohmynews.com/', 0],
						['ZDNet Korea', 'http://www.zdnet.co.kr/', 0],
						['블로터닷넷', 'http://www.bloter.net/', 0],
						['한겨례', 'http://www.hani.co.kr/', 0],
						['중앙일보', 'http://joongang.joins.com/', 0],
						['조선일보', 'http://www.chosun.com/', 0],
						['동아일보', 'http://www.donga.com/', 0],
						['과학동아', 'http://www.dongascience.com/', 0],
						['시사IN Live', 'http://www.sisainlive.com/', 0],
						['THE KOREA TIMES', 'http://www.khan.co.kr/', 0],
						['THE WALL STREET JOURNAL', 'http://kr.wsj.com/home-page', 0]
					];

var newspaperRandomIndex = function(){
	//console.log("newspaperRandomIndex");
	newsPaperList.forEach(
		function(item){
	    	item[2] = Math.floor(Math.random() * 20);
	    }
	);
	
	function compare(value1, value2){
		if (value1[2] < value2[2]) {
			return 1;
		} else if (value1[2] > value2[2]){
			return -1;
		} else {
			return 0;
		}
	}
	
	newsPaperList.sort(compare);
	
	var newsPaperArray = [];
	var newsPaperArray = document.querySelectorAll("#newspapers > li");
	
	//console.log(newsPaperArray);
	
	var htmlLength = newsPaperArray.length;
	
	for(var i = 0; i < htmlLength; ++i){
		newsPaperArray[i].insertAdjacentHTML(
		'afterbegin', "<div><a href=" + newsPaperList[i][1] + ">" + newsPaperList[i][0] + "</a></div>"
		);
	}
}


/*	
	tag cloud 생성
*/
var colorTable = [
	"rgb(7, 104, 172)",
	"rgb(8, 161, 229)",
	"rgb(30, 153, 197)",
	"rgb(6, 134, 191)",
	"rgb(4, 89, 127)",
	"rgb(2, 45, 64)"
]

var minTextSize = 14;

//단어들의 변경된 정보를 5분마다 서버에서 새로 받아와서 receivedTagWords의 내용을 업데이트한다.
var refreshTime = 5 * 60 * 1000; //5min
var moveFrameInterval = 20;

var canvasList = [];
var buttonList = [];
var currentCanvas = null;

var newspaperTable = [
	"kh",
	"joins",
	"cb",
	"bloter",
	"mediatoday"
]

var canvasInit = function(){
	for (var idx = 0, count = tagPageNumber; idx < count; ++idx){
		//var path = "sampleTags" + idx + ".json";
		var path = newspaperTable[idx] + "_newsTag.json";
		
	    canvasList[idx] = createCanvas(idx, path, canvasList);
	    canvasList[idx].init();
	    //console.log(canvasList[idx].context);
	}
	
	currentCanvas = canvasList[0];
	currentCanvas.drawingStart();
}

//tag instance 생성
var createTag = function(idx, keyword, frequency, maxFrequency, tagList){
	var obj = new Object();
	
	//기본값 설정 - 빈도에 비례해서 텍스트의 크기가 정해지고 텍스트 크기와 텍스트 길이에 비례해서 반경이 정해짐
	obj.idx = idx;
	obj.keyword = keyword;
	obj.frequency = frequency;
	obj.otherTags = tagList;
	obj.textSize = minTextSize + (frequency * minTextSize * 3 / maxFrequency);
	obj.radius = obj.textSize * keyword.length / 3;
	
	//가운데 몰려서 생성되게 설정
	obj.xPos = (canvasWidth / 2) + ( (Math.random() > 0.5) ? 1 : -1) * Math.floor(Math.random() * (maxFrequency - frequency) * 5);
	obj.yPos = (canvasHeight / 2) + ( (Math.random() > 0.5) ? 1 : -1) * Math.floor(Math.random() * (maxFrequency - frequency) * 2);
	
	//아래 움직임에 관련된 상수들 나중에 따로 변수에 담아둘 것
	//obj.xVelocity = 0 //Math.floor(Math.random() * 1);
	//obj.yVelocity = 0 //Math.floor(Math.random() * 1);
	//obj.xAcceleration = 0 //Math.floor(Math.random() * 1);
	//obj.yAcceleration = 0 //Math.floor(Math.random() * 1);
	
	obj.color = colorTable[idx % colorTable.length]; //colorTable[Math.floor(Math.random() * 5)];
	
	obj.tagDetailSrc = "newspaper" /* + newspaperIdx */ + "_tagDetail_" + idx + ".json"; //newpaper_tagDetail_0.json

	// 태그들이 매 프레임마다 하는 일은
	// 일단 다른 태그들과 겹치는지 체크해서 겹치면 서로 반대방향으로 속도 증가 시킴(거리에 비례해서)
	// 그 다음은 지금 설정된 속도대로 움직임 - 좌표 변경
	// 이 작업의 반복
	obj.collide = function(){
		//console.log("collide start");
	
		for (var i = 0; i < this.idx; ++i){
	    	//check the collision with each tags
	    	var dx = this.otherTags[i].xPos - this.xPos;
	    	var dy = this.otherTags[i].yPos - this.yPos;
	    	var distance = Math.sqrt( (dx * dx) + (dy * dy) );
	    	
	    	var radiusSum = this.radius + this.otherTags[i].radius;
	    	
	    	var weight = (radiusSum - distance) / radiusSum;
	    	
	    	if (weight > 0.05){ //너무 작은 이동 - 미세한 떨림 보정을 위해 0.05과 비교
	    		//값이 너무 크면 dx, dy를 0 ~ 1 사이의 값으로 변경할 것
	    		xWeight = weight * dx * 0.03;
	    		yWeight = weight * dy * 0.03;
	    		
	    		//var textSizeSum = otherTags[i].textSize + this.textSize;
	    		
	    		this.xPos -= xWeight; // * (this.textSize / textSizeSum);
	    		this.yPos -= yWeight; // * (this.textSize / textSizeSum);
	    		
	    		this.otherTags[i].xPos += xWeight * 0.1; // * (otherTags[i].textSize / textSizeSum);
	    		this.otherTags[i].yPos += yWeight * 0.1; // * (otherTags[i].textSize / textSizeSum);
	    	}
		}
		
		//console.log("collide end");
	}
	
	obj.move = function(){
		//console.log("move start");
		
		//마찰력을 넣으면 여기서 가속도와 속도 모두 변경해야 하려나?
		//현재 속도의 반대 방향으로 가속도를 주자!
		
		//속도 업데이트
		//this.xVelocity += this.xAcceleration;
		//this.yVelocity += this.yAcceleration;
		
		//위치 이동
		//this.xPos += this.xVelocity;
		//this.yPos += this.yVelocity;
		
		//화면 밖으로 나가는 경우 고정시켜버림
		if (this.xPos < (this.radius / 1) ){
			this.xPos = this.radius / 1;
			//xVeolocity *= -1;
		} else if (this.xPos > (canvasWidth - (this.radius / 1) ) ){
			this.xPos = canvasWidth - (this.radius / 1);
			//xVelocity *= -1;
		}
		
		if (this.yPos < (this.radius / 2) ){
			this.yPos = this.radius / 2;
			//yVelocity *= -1;
		} else if (this.yPos > (canvasHeight - this.radius / 2) ){
			this.yPos = canvasHeight - (this.radius / 2);
			//yVelocity *= -1;
		}
		
		//console.log("move end");
	}
	
	obj.openPopup = function(){
		//test this function's scoop
		console.log(this.keyword);
		
		var receivedDetail = null;
	    var detailRequest = new XMLHttpRequest();
	    detailRequest.open("GET", this.tagDetailSrc, false);
		detailRequest.send(null);
		
		//받은 데이터를 리스트 형태로 변환해서 저장 
	    receivedDetail = JSON.parse(detailRequest.responseText);
		
		var detailArea = document.getElementById("tagDetail");
	    var detailContent = "";
	    
	    //contents
	    detailContent += "<ul>"
	    
	    
	    for (var i = 0, count = receivedDetail.length; i < count; ++i){
		    detailContent += "<li>";
		    detailContent += ("<a href=" + receivedDetail[i].link + ">" + receivedDetail[i].title + "</a>");
		    detailContent += ("<p>" + receivedDetail[i].summary + "</p>");
		    detailContent += "</li>";
		    
	    }
	    
	    detailContent += "</ul>";
	    
	    detailArea.innerHTML = detailContent;
	    
	    var detailBox = document.querySelector("#tagDetail > ul");
	    
	    var yPos = window.event.pageY;
	    if (yPos > 200){
		    yPos = 200;
	    }
	    
	    detailBox.style.left = window.event.pageX + "px";
	    detailBox.style.top = yPos + "px";
	}
	
	return obj;
}

//canvas instance 생성 
//조심해!
//나중에 업데이트된 키워드 중에서 이미 리스트에 있다면 그 자리에 업데이트 된 빈도수를 바꿔줘야 계속 같은 위치 유지
//그러니까 딕셔너리 타입으로 가야 될 수도 있겠네...
//태그 클라우드에 표시될 단어들의 내용과 빈도수를 담아두는 변수
//json data는 빈도순으로 정렬되어 있는 상태(가장 많이 나오는 키워드가 나중에 나오게)
//this 문제 : oCanvas내부에서 정의한 함수에서 this.idx나 context를 불러오면 undefined / 인자를 줘서 타이머를 설정하면 타이머가 작동 안 함
var createCanvas = function(canvasIdx, jsonPath, canvasList){
    var oCanvas = new Object();
    
    oCanvas.idx = canvasIdx;
    oCanvas.tagSrcURL = jsonPath;
    oCanvas.tagWords = [];
    oCanvas.otherCanvases = canvasList;
    oCanvas.drawing = null;
    oCanvas.drawContext = null;
    oCanvas.drawTimerId = null;
    oCanvas.refreshTimerId = null;
    	
    //자신의 대상 캔버스를 찾아서 등록
    oCanvas.init = function(){
		this.drawing = document.getElementById("canvas_" + canvasIdx);

	    if (this.drawing.getContext){
	    	this.drawContext = this.drawing.getContext("2d");
	    	//console.log(this.drawContext);
	    }
	}
    	
    oCanvas.drawingStart = function(){
    	//console.log(this.drawContext);
    	//태그 정보를 업데이트하고
    	//this.refreshTimerId = setInterval(refreshTags, refreshTime);
    	refreshTags();
    	
		//주기적으로 현재의 tag data를 기반으로 화면에 그린다.
		this.drawTimerId = setInterval(drawTags, moveFrameInterval);
	}
	
	/*
	oCanvas.drawTags = function(context){
    	console.log("Hi");
    	context.clearRect ( 0, 0, 1180, 580);
		
		for (var i = 0, count = this.tagWords.length; i < count; ++i){
			context.font = this.tagWords[i].textSize + "px Tahoma"; //"18px Tahoma"; //나중에 obj에서 정보 가져와서 넣을 것
			context.textAlign = "center";
			context.textBaseLine = "middle";
			
			context.fillStyle = this.tagWords[i].color;
			
			this.tagWords[i].collide();
			this.tagWords[i].move();
			
			context.fillText(this.tagWords[i].keyword, this.tagWords[i].xPos, this.tagWords[i].yPos);
		}		
		//setTimeout(drawTags, moveFrameInterval);
	}
	*/
	oCanvas.change = function(direction){
		clearInterval(this.refreshTimerId);
		clearInterval(this.drawTimerId);
	
		if (direction == scrollDirection['NEXT']){
			currentCanvas = this.otherCanvases[this.idx + 1];
		} else if (direction == scrollDirection['PREVIOUS']){
			currentCanvas = this.otherCanvases[this.idx - 1];
		}
		
		currentCanvas.drawingStart();
	}
	
	return oCanvas;
}

var refreshTags = function(){
	//console.log(this.tagSrcURL);
	var receivedTagWords = null;
    var tagRequest = new XMLHttpRequest();
    tagRequest.open("GET", currentCanvas.tagSrcURL, false);
	tagRequest.send(null);
	
	console.log(tagRequest);
	
	//서버에서 업데이트 된 태그 정보를 받아 온 후에 데이터를 교환해야 하므로 동기적으로 작동되어야 한다.
    receivedTagWords = JSON.parse(tagRequest.responseText);
    
    var tagButton = document.getElementById("tagButton");
    var newButtons = "";
    
    for (var i = 0, count = receivedTagWords.length; i < count; ++i){
    	//최초 생성이 아니라 이미 생성된 리스트를 업데이트하는 경우에는 할당된 태그 오브젝트를 삭제
    	if (currentCanvas.tagWords[i] != undefined){
    		delete currentCanvas.tagWords[i];
    	}
    	
    	//새로 받아온 데이터를 기반으로 태그 오브젝트 생성
		currentCanvas.tagWords[i] = createTag(i, receivedTagWords[i][0], receivedTagWords[i][1], receivedTagWords[0][1], currentCanvas.tagWords);
		
		newButtons += "<div></div>";
    }
    
    tagButton.innerHTML = newButtons;
	buttonList = document.querySelectorAll("#tagButton > div");
	
	for (var i = 0, count = receivedTagWords.length; i < count; ++i){
		if (typeof buttonList[i] != 'undefined'){
			//기존 등록 이벤트 삭제
			buttonList[i].removeEventListener('mouseover', currentCanvas.tagWords[i].openPopup, false);
			buttonList[i].removeEventListener('mouseout', closePopup, false);
		}
		buttonList[i].style.width = (currentCanvas.tagWords[i].radius * 2) + "px";
		buttonList[i].style.height = currentCanvas.tagWords[i].textSize + "px";
		
		buttonList[i].addEventListener('mouseover', currentCanvas.tagWords[i].openPopup.bind(currentCanvas.tagWords[i]), false);
		buttonList[i].addEventListener('mouseout', closePopup, false);
	}
    
    //console.log(currentCanvas.tagWords);
    //맨처음에 한 번 태그 정보를 업데이트한 뒤에는 setInterval()로 주기적으로 업데이트
    //(시간 간격 동안 기다렸다가 자기 자신을 하나 더 호출하고 자신은 종료)
    //setTimeout(refreshTags, refreshTime); //5분마다 태그 데이터 업데이트
}

var drawTags = function(){
	currentCanvas.drawContext.clearRect ( 0, 0, 1180, 580);
	
	for (var i = 0, count = currentCanvas.tagWords.length; i < count; ++i){
		currentCanvas.drawContext.font = currentCanvas.tagWords[i].textSize + "px Tahoma"; //"18px Tahoma"; //나중에 obj에서 정보 가져와서 넣을 것
		currentCanvas.drawContext.textAlign = "center";
		currentCanvas.drawContext.textBaseLine = "middle";
		
		currentCanvas.drawContext.fillStyle = currentCanvas.tagWords[i].color;
		
		currentCanvas.tagWords[i].collide();
		currentCanvas.tagWords[i].move();
		
		currentCanvas.drawContext.fillText(currentCanvas.tagWords[i].keyword, currentCanvas.tagWords[i].xPos, currentCanvas.tagWords[i].yPos);
		
		buttonList[i].style.left = (currentCanvas.tagWords[i].xPos - currentCanvas.tagWords[i].radius) + "px";
		buttonList[i].style.top = (currentCanvas.tagWords[i].yPos - currentCanvas.tagWords[i].textSize) + "px";
	}
				
	//setTimeout(drawTags, moveFrameInterval);
}

function closePopup(){
	//fill this area
	//setTimeout(function(){
		var detailArea = document.getElementById("tagDetail");
		var detailContent = "";
		
		detailArea.innerHTML = detailContent;
	//}, 300);
}

/*
	color table
	light blue 	: 30, 153, 197
	deep blue 	: 7, 104, 172
	purple 		: 96, 2, 126
	magenta 	: 196, 15, 132
	red 		: 254, 0, 8
	orange 		: 255, 102, 0
	yellow 		: 255, 178, 0
	lime 		: 204, 232, 36
	green 		: 115, 191, 31
	jade 		: 78, 183, 153
*/