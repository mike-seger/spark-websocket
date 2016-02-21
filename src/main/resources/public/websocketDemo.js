//Establish the WebSocket connection and set up event handlers

var webSocket=null;
function disconnectWebSocket() {
	if(this.webSocket != null) {
    	this.webSocket.close()	
	}
}
function releaseWebSocket(automatic) {
	this.webSocket = null;
	updateConnectionStatus(false, automatic);
}
function hasWebSocket() {
	return this.webSocket != null;
}
function setupWebSocket(userTriggered) {
	if(this.webSocket == null) {
		this.webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat/");
		this.webSocket.onmessage = function (msg) { updateChat(msg); };
		//this.webSocket.onclose = function () { releaseWebSocket(); };
		this.webSocket.onclose = function () { 
			releaseWebSocket(false);
			//setTimeout(setupWebSocket, 1000);
		};
		updateConnectionStatus(true, userTriggered);
	}
}

id("connected").addEventListener("change", function () {
	if(!hasWebSocket()) setupWebSocket(true);
	else disconnectWebSocket(false); });
id("clear").addEventListener("click", function () {
    clear("chat"); });
id("send").addEventListener("click", function () {
    sendMessage(id("message").value); });
id("message").addEventListener("keypress", function (e) {
    if (e.keyCode === 13) { sendMessage(e.target.value); } });

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "" && hasWebSocket()) {
        webSocket.send(message);
        id("message").value = "";
    }
}

//Update the chat-panel, and the list of connected users
function updateChat(msg) {
    var data = JSON.parse(msg.data);
    insert("chat", data.userMessage);
    id("userName").innerHTML = data.userName;
    id("userlist").innerHTML = "";
    data.userlist.forEach(function (user) {
        insert("userlist", "<li>" + user + "</li>");
    });
}

function updateConnectionStatus(connected, userTriggered) {
    if( connected ) {
		console.log("New Connection");
    	insert("chat", localDate(new Date()) + ": connected<br/>");
    	if(!userTriggered) {
    	    check("connected", true);
    	}
    } else {
    	insert("chat", localDate(new Date()) + ": disconnected<br/>");
    	check("connected", false);
	}

}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for clearing the inner HTML of an element
function clear(targetId) {
    id(targetId).innerHTML="";
}

//Helper function for checking a check box
function check(targetId, checkFlag) {
	if(checkFlag) id(targetId).checked = true;
	else id(targetId).checked = false;
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}

function localDate(date) {
	var local = new Date(date);
	local.setMinutes(date.getMinutes() - date.getTimezoneOffset());
	return local.toJSON().slice(0, 18).replace("T"," ");
}

setupWebSocket(false);

