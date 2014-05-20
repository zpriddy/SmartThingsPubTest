/**
 *  Jawbone Service Manager
 *
 *  Author: Juan Risso
 *  Date: 2013-12-19
 */
definition(
	name: "Jawbone UP24 (Connect)",
	namespace: "juano2310",
	author: "Juan Pablo Risso",
	description: "Connect your Jawbone UP to SmartThings",
	category: "Reviewers",
	iconUrl: "http://thinkmakelab.com/images/upsmall.png",
	iconX2Url: "http://thinkmakelab.com/images/upbig.png",
	oauth: true
)

preferences {
    page(name: "Credentials", title: "Jawbone UP", content: "authPage", install: false)
}

mappings {
        path("/receivedToken") { action: [ POST: "receivedToken", GET: "receivedToken"] }
        path("/receiveToken") { action: [ POST: "receiveToken", GET: "receiveToken"] }
        path("/hookCallback") { action: [ POST: "hookEventHandler", GET: "hookEventHandler"] }
}

def getSmartThingsClientId() {
   return "txql_lgXcLA"
}

def getSmartThingsClientSecret() {
   return "df52c46689595959b35fd9cee3ea9133369e58ca"
}

def authPage() {
    // log.debug "authPage"
    def description = null          
    if (state.JawboneAccessToken == null) {   
        log.debug "About to create access token."                
        createAccessToken()       
        description = "Click to enter Jawbone Credentials."
        def redirectUrl = oauthInitUrl()
        // log.debug "RedirectURL = ${redirectUrl}"
        return dynamicPage(name: "Credentials", title: "Jawbone UP", nextPage: null, uninstall: true, install:false) {
               section { href url:redirectUrl, style:"embedded", required:true, title:"Jawbone UP", description:description }
        }
    } else {
        description = "Jawbone Credentials Already Entered." 
        return dynamicPage(name: "Credentials", title: "Jawbone UP", uninstall: true, install:true) {
               section { href url: buildRedirectUrl("receivedToken"), style:"embedded", state: "complete", title:"Jawbone UP", description:description }
        }
    }
}

def oauthInitUrl()
{
    // log.debug "oauthInitUrl"
    def stcid = getSmartThingsClientId();
    state.oauthInitState = UUID.randomUUID().toString()
    def oauthParams = [ response_type: "code", client_id: stcid, scope: "move_read sleep_read", redirect_uri: buildRedirectUrl("receiveToken") ]
        return "https://jawbone.com/auth/oauth2/auth?" + toQueryString(oauthParams)
}

def receiveToken() {
    def stcid = getSmartThingsClientId();
    def oauthClientSecret = getSmartThingsClientSecret();
    def oauthParams = [ client_id: stcid, client_secret: oauthClientSecret, grant_type: "authorization_code",code: params.code ]
        def tokentUrl = "https://jawbone.com/auth/oauth2/token?" + toQueryString(oauthParams)
        def params = [
          uri: tokentUrl,
        ]
        httpGet(params) { response -> 
        	log.debug "${response?.data}"
        	state.JawboneAccessToken = response.data.access_token
        }
        
    def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=640">
        <title>Withings Connection</title>
        <style type="text/css">
            @font-face {
                font-family: 'Swiss 721 W01 Thin';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            @font-face {
                font-family: 'Swiss 721 W01 Light';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            .container {
                width: 560px;
                padding: 40px;
                /*background: #eee;*/
                text-align: center;
            }
            img {
                vertical-align: middle;
            }
            img:nth-child(2) {
                margin: 0 30px;
            }
            p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                padding: 0 40px;
                margin-bottom: 0;
            }
        /*
            p:last-child {
                margin-top: 0px;
            }
        */
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
        </head>
        <body>
            <div class="container">
                <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSMuoEIQ7gQhFtc02vXkybwmH0o7L1cs5mtbcJye0mgNqop_LOZbg" alt="Jawbone UP icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>Your Jawbone Account is now connected to SmartThings!</p>
                <p>Click 'Done' to finish setup.</p>
                        </div>
        </body>
        </html>
        """
        render contentType: 'text/html', data: html
}

def receivedToken() {
    def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=640">
        <title>Withings Connection</title>
        <style type="text/css">
            @font-face {
                font-family: 'Swiss 721 W01 Thin';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            @font-face {
                font-family: 'Swiss 721 W01 Light';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            .container {
                width: 560px;
                padding: 40px;
                /*background: #eee;*/
                text-align: center;
            }
            img {
                vertical-align: middle;
            }
            img:nth-child(2) {
                margin: 0 30px;
            }
            p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                padding: 0 40px;
                margin-bottom: 0;
            }
        /*
            p:last-child {
                margin-top: 0px;
            }
        */
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
        </head>
        <body>
            <div class="container">
                <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSMuoEIQ7gQhFtc02vXkybwmH0o7L1cs5mtbcJye0mgNqop_LOZbg" alt="Jawbone UP icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>Your Jawbone Account is already connected to SmartThings!</p>
                <p>Click 'Done' to finish setup.</p>
                        </div>
        </body>
        </html>
        """
        render contentType: 'text/html', data: html
}

String toQueryString(Map m) {
        return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def getServerUrl() { return "https://graph.api.smartthings.com" }

def buildRedirectUrl(page) {
    // log.debug "buildRedirectUrl"
    // /api/token/:st_token/smartapps/installations/:id/something
    return serverUrl + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/${page}"
}

def validateCurrentToken() {
    def url = "https://jawbone.com/nudge/api/v.1.1/users/@me/refreshToken"
    def requestBody = "secret="+getSmartThingsClientSecret()
        
	httpPost(uri: url, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ],  body: requestBody) {response ->
    	if (response.status == 200) {
     		state.refreshToken = response.data.refresh_token   
        }
        else if (response.status == 401) { // token is expired
        
        	if (state.refreshToken) { // if we have this we are okay
            
            	def stcid = getSmartThingsClientId();
    			def oauthClientSecret = getSmartThingsClientSecret();
    			def oauthParams = [ client_id: stcid, client_secret: oauthClientSecret, grant_type: "refresh_token",refresh_token: state.refreshToken]
        		def tokentUrl = "https://jawbone.com/auth/oauth2/token?" + toQueryString(oauthParams)
        		def params = [
          			uri: tokentUrl,
        		]
        		httpGet(params) { refreshResponse -> state.JawboneAccessToken = response.data.access_token }
            
            }
        }
    }
}

def initialize() {

	def hookUrl = "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/hookCallback"
    def webhook = "https://jawbone.com/nudge/api/v.1.1/users/@me/pubsub?webhook=$hookUrl"
    log.debug "Callback URL: $webhook"        
	httpPost(uri: webhook, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ])
    
}

def installed() {

	// make sure this is going to work
    validateCurrentToken()
    
    // log.debug "In installed() method."
    def urlmember = "https://jawbone.com/nudge/api/users/@me/"
        def member = null
    
    httpGet(uri: urlmember, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        member = response.data.data
    }    
    // log.debug "member=${member.first} ${member.last}"  
    
    // create the device
    if (member) {
    	state.member = member
        def childDevice = addChildDevice("juano2310","Jawbone User", "${app.id}.${member.xid}",null,[name:"Jawbone UP - " + member.first, completedSetup: true])
        if (childDevice) {
        	log.debug "Child Device Successfully Created"  
            generateInitialEvent (member, childDevice)
        }
    }
    
    initialize()
    
}

def updated() {

	// make sure this is going to work
    validateCurrentToken()
    
    // log.debug "App Updated"
    def urlmember = "https://jawbone.com/nudge/api/users/@me/"
    def member = null    
    httpGet(uri: urlmember, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        member = response.data.data
        state.member = member
    }
    def externalId = "${app.id}.${member.xid}"
    
    // find the appropriate child device based on my app id and the device network id 
    def deviceWrapper = getChildDevice("${externalId}")
    
    // invoke the generatePresenceEvent method on the child device
    log.debug "Device $externalId: $deviceWrapper"
    if (!deviceWrapper) {
      	def childDevice = addChildDevice('jawbone', "jawbone-user", "${app.id}.${member.xid}",null,[name:"Jawbone UP - " + member.first, completedSetup: true])
        if (childDevice) {
           	log.debug "Child Device Successfully Created"
            generateInitialEvent (member, childDevice)
        }
    }  
    
    initialize()
    
}

def pollChild (childDevice) {
    def member = state.member 
    generatePollingEvents (member, childDevice)   
}

def generatePollingEvents (member, childDevice) {
    // lets figure out if the member is currently "home" (At the place)
    def urlgoals = "https://jawbone.com/nudge/api/users/@me/goals" 
    def urlmoves = "https://jawbone.com/nudge/api/users/@me/moves"  
    def urlsleeps = "https://jawbone.com/nudge/api/users/@me/sleeps"     
    def goals = null
    def moves = null
 	def sleeps = null   
    httpGet(uri: urlgoals, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        goals = response.data.data
    }   
    httpGet(uri: urlmoves, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        moves = response.data.data.items[0]
    }    
   
    try { // we are going to just ignore any errors
        log.debug "Member = ${member.first}"
        log.debug "Moves Goal = ${goals.move_steps} Steps"
        log.debug "Moves = ${moves.details.steps} Steps" 

        childDevice?.sendEvent(name:"steps", value: moves.details.steps)
        childDevice?.sendEvent(name:"goal", value: goals.move_steps)
    	//setColor(moves.details.steps,goals.move_steps,childDevice)
    }
    catch (e) {
            // eat it
    }       
}

def generateInitialEvent (member, childDevice) {
    // lets figure out if the member is currently "home" (At the place)
    def urlgoals = "https://jawbone.com/nudge/api/users/@me/goals" 
    def urlmoves = "https://jawbone.com/nudge/api/users/@me/moves"  
    def urlsleeps = "https://jawbone.com/nudge/api/users/@me/sleeps"     
    def goals = null
    def moves = null
 	def sleeps = null   
    httpGet(uri: urlgoals, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        goals = response.data.data
    }   
    httpGet(uri: urlmoves, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        moves = response.data.data.items[0]
    }    
    
    try { // we are going to just ignore any errors
        log.debug "Member = ${member.first}"
        log.debug "Moves Goal = ${goals.move_steps} Steps"
        log.debug "Moves = ${moves.details.steps} Steps"
        log.debug "Sleeping state = false"   
        childDevice?.generateSleepingEvent(false)
        childDevice?.sendEvent(name:"steps", value: moves.details.steps)
        childDevice?.sendEvent(name:"goal", value: goals.move_steps)
    	//setColor(moves.details.steps,goals.move_steps,childDevice)
    }
    catch (e) {
            // eat it
    }       
}

def setColor (steps,goal,childDevice) {
    
    def result = steps * 100 / goal
    if (result < 25) 
    	childDevice?.sendEvent(name:"steps", value: "steps", label: steps)
    else if ((result >= 25) && (result < 50)) 
        childDevice?.sendEvent(name:"steps", value: "steps1", label: steps)
    else if ((result >= 50) && (result < 75)) 
        childDevice?.sendEvent(name:"steps", value: "steps1", label: steps)
    else if (result >= 75)     
        childDevice?.sendEvent(name:"steps", value: "stepsgoal", label: steps)   
}

def hookEventHandler() {
    // log.debug "In hookEventHandler method."
    log.debug "request = ${request}"
    
    def json = request.JSON 
    
    // get some stuff we need
    def userId = json.events.user_xid[0]
    def	json_type = json.events.type[0]
	def json_action = json.events.action[0]

    //log.debug json
    log.debug "Userid = ${userId}"
    log.debug "Notification Type: " + json_type
    log.debug "Notification Action: " + json_action 
    
    // find the appropriate child device based on my app id and the device network id
    def externalId = "${app.id}.${userId}"
    def childDevice = getChildDevice("${externalId}")
            
    if (childDevice) {
    	switch (json_action) {   
	        case "enter_sleep_mode":      
            	childDevice?.generateSleepingEvent(true)           
                break           
            case "exit_sleep_mode":       
            	childDevice?.generateSleepingEvent(false) 
                break 
            case "creation": 
                childDevice?.generateSleepingEvent(false)  
                childDevice?.sendEvent(name:"steps", value: 0)
          		break
            case "updation":
                def urlgoals = "https://jawbone.com/nudge/api/users/@me/goals"     
                def urlmoves = "https://jawbone.com/nudge/api/users/@me/moves"       
                def goals = null
                def moves = null    
                httpGet(uri: urlgoals, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
 	               goals = response.data.data
                }       
                httpGet(uri: urlmoves, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
                   moves = response.data.data.items[0]
                }        
                log.debug "Goal = ${goals.move_steps} Steps"
        		log.debug "Steps = ${moves.details.steps} Steps"
                childDevice?.sendEvent(name:"steps", value: moves.details.steps)
                childDevice?.sendEvent(name:"goal", value: goals.move_steps)       
                //setColor(moves.details.steps,goals.move_steps,childDevice)   
                break               
            }
    }
    else {
            log.debug "Couldn't find child device associated with Jawbone."
    }

    def html = """{"code":200,"message":"OK"}"""
        render contentType: 'application/json', data: html
        
}