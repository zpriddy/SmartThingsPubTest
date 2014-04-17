/**
 *	Wemo Switch
 *
 *	Author: superuser
 *	Date: 2013-10-11
 */
 // for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Wemo Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "subscribe"
		command "resubscribe"
		command "unsubscribe"
	}

	// simulator metadata
	simulator {}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		/*standardTile("subscribe", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'[re]subscribe', action:"subscribe", icon:"st.unknown.thing.thing-circle"
		}*/

		main "switch"
		details (["switch", "refresh", "subscribe"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"


	def map = stringToMap(description)
	def headerString = new String(map.headers.decodeBase64())
	def result = []

	if (headerString.contains("SID: uuid:")) {
		def sid = (headerString =~ /SID: uuid:.*/) ? ( headerString =~ /SID: uuid:.*/)[0] : "0"
		sid -= "SID: uuid:".trim()

		updateDataValue("subscriptionId", sid)
	}

	if (map.body) {

		def bodyString = new String(map.body.decodeBase64())
		def body = new XmlSlurper().parseText(bodyString)

		if (body?.property?.TimeSyncRequest?.text())
		{
			log.trace "Got TimeSyncRequest"
			result << timeSyncResponse()
		}
		else if (body?.Body?.SetBinaryStateResponse?.BinaryState?.text())
		{
			log.trace "Got SetBinaryStateResponse = ${body?.Body?.SetBinaryStateResponse?.BinaryState?.text()}"
		}
		else if (body?.property?.BinaryState?.text())
		{
			def value = body?.property?.BinaryState?.text().toInteger() == 1 ? "on" : "off"
			log.trace "Notify: BinaryState = ${value}"
			result << createEvent(name: "switch", value: value)
		}
		else if (body?.property?.TimeZoneNotification?.text())
		{
			log.debug "Notify: TimeZoneNotification = ${body?.property?.TimeZoneNotification?.text()}"
		}
		else if (body?.Body?.GetBinaryStateResponse?.BinaryState?.text())
		{
			def value = body?.Body?.GetBinaryStateResponse?.BinaryState?.text().toInteger() == 1 ? "on" : "off"
			log.trace "GetBinaryResponse: BinaryState = ${value}"
			result << createEvent(name: "switch", value: value)
		}

	}

	result
}

////////////////////////////
private getTime() {
	// This is essentially System.currentTimeMillis()/1000, but System is disallowed by the sandbox.
	((new GregorianCalendar().time.time / 1000l).toInteger()).toString()
}

private getCallBackAddress() {
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

////////////////////////////
def on() {
	log.debug "Executing 'on'"
def turnOn = new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPAction: "urn:Belkin:service:basicevent:1#SetBinaryState"
Host: ${getHostAddress()}
Content-Type: text/xml
Content-Length: 333

<?xml version="1.0"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<SOAP-ENV:Body>
	<m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
<BinaryState>1</BinaryState>
	</m:SetBinaryState>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>""", physicalgraph.device.Protocol.LAN)
}
////////////////////////////
def off() {
	log.debug "Executing 'off'"

	def turnOff = new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPAction: "urn:Belkin:service:basicevent:1#SetBinaryState"
Host: ${getHostAddress()}
Content-Type: text/xml
Content-Length: 333

<?xml version="1.0"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<SOAP-ENV:Body>
	<m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
<BinaryState>0</BinaryState>
	</m:SetBinaryState>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>""", physicalgraph.device.Protocol.LAN)
}

/*def refresh() {
	log.debug "Executing 'refresh'"
new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPACTION: "urn:Belkin:service:basicevent:1#GetBinaryState"
Content-Length: 277
Content-Type: text/xml; charset="utf-8"
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1">
</u:GetBinaryState>
</s:Body>
</s:Envelope>""", physicalgraph.device.Protocol.LAN)
}*/

////////////////////////////
def refresh() {
	log.debug "Executing WeMo Switch 'subscribe', then 'timeSyncResponse', then 'poll'"
	[subscribe(), timeSyncResponse(), poll()]
}

////////////////////////////
def subscribe() {
log.debug "Executing 'subscribe()'"
def address = getCallBackAddress()
new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
CALLBACK: <http://${address}/>
NT: upnp:event
TIMEOUT: Second-5400
User-Agent: CyberGarage-HTTP/1.0


""", physicalgraph.device.Protocol.LAN)
}

////////////////////////////
def resubscribe() {
log.debug "Executing 'resubscribe()'"

def sid = getDeviceDataByName("subscriptionId")

new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}
TIMEOUT: Second-5400


""", physicalgraph.device.Protocol.LAN)

}

////////////////////////////
def unsubscribe() {
def sid = getDeviceDataByName("subscriptionId")
new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}


""", physicalgraph.device.Protocol.LAN)
}

////////////////////////////
//TODO: Use UTC Timezone
def timeSyncResponse() {
log.debug "Executing 'timeSyncResponse()'"
new physicalgraph.device.HubAction("""POST /upnp/control/timesync1 HTTP/1.1
Content-Type: text/xml; charset="utf-8"
SOAPACTION: "urn:Belkin:service:timesync:1#TimeSync"
Content-Length: 376
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
 <s:Body>
  <u:TimeSync xmlns:u="urn:Belkin:service:timesync:1">
   <UTC>${getTime()}</UTC>
   <TimeZone>-05.00</TimeZone>
   <dst>1</dst>
   <DstSupported>1</DstSupported>
  </u:TimeSync>
 </s:Body>
</s:Envelope>
""", physicalgraph.device.Protocol.LAN)
}


def poll() {
log.debug "Executing 'poll'"
new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPACTION: "urn:Belkin:service:basicevent:1#GetBinaryState"
Content-Length: 277
Content-Type: text/xml; charset="utf-8"
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1">
</u:GetBinaryState>
</s:Body>
</s:Envelope>""", physicalgraph.device.Protocol.LAN)
}