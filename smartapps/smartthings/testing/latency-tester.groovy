/**
 *  Latency Test
 *  Toggles a switch every minute to track roundtrip time
 *
 *  Author: SmartThings
 */
 
preferences {
	page (name: "mainPage", title: "Latency Tester", install: true, uninstall:true)
}

def mainPage() {
	dynamicPage (name: "mainPage", title: "Select CentraLite Switch and Input Location", install: true) {
		section {
			input "switch1", "capability.switch", multiple:false
		}

		section {
			input(name:"locationName", type:"text", title:"Give your location a name")
		}
	}
}

def installed() {
	subscribe(switch1, "switch", switchUpdate)
    schedule("0 */1 * * * ?", "runWhen") //Run every minute
}
    

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
    
	unsubscribe()
    unschedule()
    
	subscribe(switch1, "switch", switchUpdate)
    schedule("0 */1 * * * ?", "runWhen") //Run every minute
}

def switchUpdate(evt) {
	
    log.debug "here {$evt}"
    log.debug "here ${evt.dateCreated}"
    
    if (state.sentAt){
    	def time = new Date().time - state.sentAt
        log.debug "took: ${time}"
        saveRoundTripTimeToCopperEgg("SA $locationName","smartapp_timings", time)

		def dhTimeAt = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", evt.dateCreated).time
        def dhTime = dhTimeAt - state.sentAt
        
        log.debug "DH took: ${dhTime}"
		saveRoundTripTimeToCopperEgg("DH $locationName","device_timings", dhTime)
    }
}

def runWhen(){
	
    if (state.last == "on"){
	    log.debug "turning off"
		switch1.off()
	    state.last = "off"
    }
    
    else {
    	log.debug "turning on"
        switch1.on()
        state.last = "on"
    }
    
    state.sentAt = new Date().time
}



def saveRoundTripTimeToCopperEgg(String id, String metricGroupId, Long roundTripMillis)
{
	// curl -isk -u SRpxsLgRP0DTDyMW:U http://api.copperegg.com/v2/revealmetrics/samples/smartapp_timings.json -H "Content-Type: application/json" -d "{\"timestamp\":1382558798, \"identifier\":\"my_identifier\",\"values\":{\"round_trip\":7994}}"
	
    //Name def id by location
	
    log.debug "sending to copperegg id:$id, metricGroupId:$metricGroupId"
	def metricName = "round_trip"
	//def metricGroupId = "smartapp_timings"
	def apiKeyBase64 = "U1JweHNMZ1JQMERURHlNVzpV"
	def time = (new Date().time / 1000).toInteger()

	def postParams = [
		uri: "https://api.copperegg.com", 
		path: "/v2/revealmetrics/samples/${metricGroupId}.json", 
		body: [identifier: id, timestamp: time, values: [round_trip: roundTripMillis]], 
		requestContentType: "application/json", 
		headers: ["Authorization":"Basic ${apiKeyBase64}", "Content-Type": "application/json"]
	]

    try {
		httpPost(postParams)
    } catch(Exception e) { }
}



// curl -su <APIKEY>:U -H "Content-type: application/json" -XPOST https://api.copperegg.com/v2/revealmetrics/samples/<METRICGROUP_ID>.json -d '{"identifier": <YOURIDENTIFIER>,"timestamp": <UNIXTIMESTAMP>, "values": { <METRICNAME>: <METRICVALUE>}'
// 
// curl 
// 	-s 
// 	-u <APIKEY>:U 
// 	-H "Content-type: application/json" 
// 	-XPOST 
// 	https://api.copperegg.com/v2/revealmetrics/samples/<METRICGROUP_ID>.json 
// 	-d '{"identifier": <YOURIDENTIFIER>,"timestamp": <UNIXTIMESTAMP>, "values": { <METRICNAME>: <METRICVALUE>}'
// 


