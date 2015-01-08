/*
 *  Tcp Bulbs (Connect)
 *
 *  Author: todd@wackford.net
 *
 ******************************************************************************
 *                        Setup Namespace & OAuth
 ******************************************************************************
 *
 * Namespace:			"wackford"
 *
 * OAuth:				"Enabled"
 *
 ******************************************************************************
 *                                 Changes
 ******************************************************************************
 *
 *  Change 1:	2014-03-06
 *					Initial Release
 *
 *  Change 2:	2014-03-15
 *					a. Documented Header
 *					b. Fixed on()/off() during level changes			
 *					c. Minor code cleanup and UI changes
 *
 *  Change 3:   2014-04-02 (lieberman) 	
 *                  a. Added RoomGetCarousel to poll()
 * 					b. Added checks in locationHandler() to sync ST status with TCP status
 *
 *  Change 4:   2014-05-02 (twackford) 	
 *                  a. Added current power usage functionality
 *
 *  Change 5:	2014-10-02 (twackford)
 *					a. Fixed on/off tile update
 *					b. Fixed var type issues with power calculations
 *					c. Added IP checker for DHCP environments
 *					d. Added delete device that is not selected in bulb picker
 *
 *  Change 6:	2014-10-17 (twackford)
 *					a. added uninstallFromChildDevice to handle removing from settings                   
 *
 *  Change 7:	2015-01-08 (nohr)
 *					a. use new runEvery5Minutes schedule to spread out load of all TCP Bulbs (Connect) apps
 *
 *
 ******************************************************************************
 *                                   Code
 ******************************************************************************
 */


// Automatically generated. Make future change here.
definition(
    name: "Tcp Bulbs (Connect)",
    namespace: "wackford",
    author: "SmartThings",
    description: "Connect your TCP bulbs to SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/tcp.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tcp@2x.png"
)

preferences {
	page(name:"Gateway", title: "Begin Connected by TCP Device Discovery", content: "discoverGateway", nextPage: "bulbDiscovery", install: false)
	page(name:"bulbDiscovery", title:"TCP Device Setup", content:"bulbDiscovery", refreshTimeout:5)
}

def updateGatewayIP() {
	log.debug "Checking to see if IP changed"
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:greenwavereality-com:service:gop:1", physicalgraph.device.Protocol.LAN))
}

def discoverGateway() {
	log.debug "In discoverGateway"
	if(canInstallLabs()) {
		state.subscribe = false
		state.gateway = []

		if(!state.subscribe) {
			subscribe(location, null, locationHandler, [filterEvents:false])
			state.subscribe = true
		}

		//send out the search for the gateway, we'll pick up response in locationEvt
		sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:greenwavereality-com:service:gop:1", physicalgraph.device.Protocol.LAN))

		bulbDiscovery()
	}
	else
	{
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""


		return dynamicPage(name:"Gateway", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section {
				paragraph "$upgradeNeeded"
			}
		}

	}
}

private bulbDiscovery() {
	log.debug "In bulbDiscovery"

	def data = "<gwrcmds><gwrcmd><gcmd>RoomGetCarousel</gcmd><gdata><gip><fields>name,power,control,status</fields></gip></gdata></gwrcmd></gwrcmds>"

	def qParams = [
		cmd: "GWRBatch",
		data: "${data}",
		fmt: "xml"
	]

	def cmd = "/gwr/gop.php?" + toQueryString(qParams)

	// Check for bulbs 
	state.bulbs = state.bulbs ?: [:]


	if (state.bulbs.size() == 0) {
		sendCommand(cmd)
	}

	def options = bulbsDiscovered() ?: []
	def numFound = options.size() ?: 0

	return dynamicPage(name:"bulbDiscovery", title:"Discovering TCP Bulbs!", nextPage:"", refreshInterval: 3, install:true, uninstall: true) {
		section("Please wait while we look for TCP Bulbs: ${numFound} discovered") {
			input "selectedBulbs", "enum", required:false, title:"Tap here to select bulbs to setup", multiple:true, options:options
		}
	}
}

def installed() {
	initialize();
}

def updated() {
	unschedule()
	initialize();
}

def initialize() {
	if (selectedBulbs) {
		addBulbs()
	}
	runEvery5Minutes("updateGatewayIP")
}

def uninstalled()
{
    unschedule()
}

def addBulbs() {
	log.debug " in addBulbs"

	def bulbs = getBulbs()
	def name  = "Dimmer Switch"
	def deviceFile = "TCP Bulb"

	selectedBulbs.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newBulb = bulbs.find { (it.value.id) == dni }

			d = addChildDevice("wackford", deviceFile, dni, null, [name: "${name}", label: "${newBulb?.value.name}", completedSetup: true])

			if (newBulb?.value.state == "1")   //set ST device state as we find the TCP state to be
				d.on()
			else
				d.off()

			d.setLevel(newBulb?.value.level)

		} else {
			log.debug "We already added this device"
		}
	}

	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !selectedBulbs?.contains(it.deviceNetworkId) }
	removeChildDevices(delete)
}

private removeChildDevices(delete)
{
	log.debug "deleting ${delete.size()} bulbs"
    log.debug "deleting ${delete}"
	delete.each {
		deleteChildDevice(it.device.deviceNetworkId)
	}
}

def uninstallFromChildDevice(childDevice) //called from child and will remove from settings
{
	log.debug "in uninstallFromChildDevice"

    //now remove the child from settings. Unselects from list of devices, not delete
    log.debug "Settings size = ${settings['selectedBulbs']}"
    
    if (!settings['selectedBulbs']) //empty list, bail
    	return
    
    def newDeviceList = settings['selectedBulbs'] - childDevice.device.deviceNetworkId
    app.updateSetting("selectedBulbs", newDeviceList)
}

def getBulbs()
{
	state.bulbs = state.bulbs ?: [:]
}

Map bulbsDiscovered() {
	def bulbs =  getBulbs()
	log.debug bulbs
	def map = [:]
	if (bulbs instanceof java.util.Map) {
		bulbs.each {
			def value = "${it?.value?.name}"
			def key = it?.value?.id
			map["${key}"] = value
		}
	} else { //backwards compatable
		bulbs.each {
			def value = "${it?.name}"
			def key = it?.id
			map["${key}"] = value
		}
	}
	map
}

def calculateCurrentPowerUse(deviceCapability, usePercentage) {
	log.debug "In calculateCurrentPowerUse()"
    
    log.debug "deviceCapability: ${deviceCapability}"
    log.debug "usePercentage: ${usePercentage}"
    
    def calcPower = usePercentage * 1000
    def reportPower = calcPower.round(1) as String
    reportPower += " Watt(s)"
    
    log.debug "report power = ${reportPower}"
    
    return reportPower
}

def locationHandler(evt) {
	log.debug "In locationHandler()"

	def description = evt.description
	def hub = evt?.hubId

	def parsedEvent = parseEventMessage(description)
	parsedEvent << ["hub":hub]

	if (parsedEvent.ssdpTerm?.contains("urn:greenwavereality-com:service:gop:1"))
	{
		//stuff the gateway data
		state.gateway = []
		state.gateway = ([ 	'ip' 		: parsedEvent.ip,
                            'port'    	: "0050", //tcp returns zeros for some reason, dunno
							'type'   	: 'gateway',
							'dni'     	: parsedEvent.ssdpUSN,
							'path'		: parsedEvent.ssdpPath
		])
	}

	if (parsedEvent.headers && parsedEvent.body)
	{
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())

		def type = (headerString =~ /Content-Type:.*/) ? (headerString =~ /Content-Type:.*/)[0] : null

		def body = new XmlSlurper().parseText(bodyString)

		def devices = []
		def bulbIndex = 1
		def lastRoomName = null

		body.gwrcmd.gdata.gip.'*'.each({

			if (it.name() == 'room')
			{
				def roomName = it.name.text()
				it.'*'.each({
					if ( roomName != lastRoomName ) {
						bulbIndex = 1 //reset counter for names
					}
					if (it.name() == 'device')
					{
						if (state.bulbs == null) {
							state.bulbs = [:]
						}
                        
                        def bulbDid =             it.did.text() as String
		         		def bulbState =           it.state.text()
                		def bulbLevel =           it.level.text()
                        def bulbPowerCapability = it.other.bulbpower.text()// as Integer                       
                        def bulbPowerPercentage = it.power.text() as Double
                        def bulbPower = calculateCurrentPowerUse(bulbPowerCapability, bulbPowerPercentage)
                        
                        def theBulb = getChildDevice( bulbDid )

						if ( theBulb ) {
							log.debug( "bulb exists in state and the bulb's state is ${bulbState}" )
							def currentBulbState = theBulb.currentValue("switch")
							def currentBulbLevel = theBulb.currentValue( "level" ) as Integer
                            sendEvent( bulbDid, [name: "power", value: bulbPower] )
                            
							if ( currentBulbState == "on" && bulbState == "0" ) {
								log.debug( "ST thinks the bulb is on, but TCP says the bulb is off" )
								sendEvent( bulbDid, [name: "switch",value:"off"] )
                                sendEvent( bulbDid, [name: "power", value: "0 Watt(s)"] )
							}
                            
							if ( currentBulbState == "off" && bulbState == "1" ) {
								log.debug( "ST thinks the bulb is off, but TCP says the bulb is on" )
								sendEvent( bulbDid, [name: "switch",value:"on"] )
                                sendEvent( bulbDid, [name: "power", value: bulbPower] )
							}
                            
							if ( currentBulbLevel != bulbLevel ) {
								log.debug( "ST thinks the bulb level is ${currentBulbLevel} but TCP says the level is ${bulbLevel}" )
								sendEvent( bulbDid, [name: "level", value: bulbLevel] )
								sendEvent( bulbDid, [name: "switch.setLevel", value:bulbLevel] )
								sendEvent( bulbDid, [name: "power", value: bulbPower] )
							}
						} else {
							state.bulbs[it.did.text()] = [id 	: it.did.text(),
								                          name 	: "${roomName} ${it.name.text()} ${bulbIndex}",
								                          state	: bulbState,
								                          level : bulbLevel,
                                                          power : bulbPower ]
							lastRoomName = roomName
							bulbIndex++
                        }
					}
				});
			}
		})
	}
}

private def parseEventMessage(Map event) {
	//handles  attribute events
	return event
}

private def parseEventMessage(String description) {
	def event = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()

		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpUSN = valueString
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString) {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			def valueString = part.trim()
			if (valueString) {
				event.body = valueString
			}
		}
	}
	event
}

private sendCommand(data)
{
	def deviceNetworkId = state.gateway.ip + ":" + state.gateway.port

	sendHubCommand(new physicalgraph.device.HubAction("""GET $data HTTP/1.1\r\nHOST: $deviceNetworkId\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

/**************************************************************************
 Child Device Call In Methods
 **************************************************************************/
def on(childDevice) {
	log.debug "Got On request from child device"

	def dni = childDevice.device.deviceNetworkId

	def data = "<gip><version>1</version><token>1234567890</token><did>${dni}</did><value>1</value></gip>"

	def qParams = [
		cmd: "DeviceSendCommand",
		data: "${data}",
		fmt: "xml"
	]

	def cmd = "/gwr/gop.php?" + toQueryString(qParams)

	sendCommand(cmd)
}

def off(childDevice) {
	log.debug "Got Off request from child device"

	def dni = childDevice.device.deviceNetworkId

	def data = "<gip><version>1</version><token>1234567890</token><did>${dni}</did><value>0</value></gip>"

	def qParams = [
		cmd: "DeviceSendCommand",
		data: "${data}",
		fmt: "xml"
	]
    
    sendEvent( dni, [name: "power", value: "0 Watt(s)"] )

	def cmd = "/gwr/gop.php?" + toQueryString(qParams)
	sendCommand(cmd)
}

def setLevel(childDevice, value) {
	log.debug "Got setLevel request from child device"

	def dni = childDevice.device.deviceNetworkId

	def data = "<gip><version>1</version><token>1234567890</token><did>${dni}</did><value>${value}</value><type>level</type></gip>"

	def qParams = [
		cmd: "DeviceSendCommand",
		data: "${data}",
		fmt: "xml"
	]

	def cmd = "/gwr/gop.php?" + toQueryString(qParams)

	sendCommand(cmd)
    
    poll()
}

def poll(childDevice) {
	log.debug "In poll()"
    def data = "<gwrcmds><gwrcmd><gcmd>RoomGetCarousel</gcmd><gdata><gip><fields>name,power,control,status</fields></gip></gdata></gwrcmd></gwrcmds>"

	def qParams = [
		cmd: "GWRBatch",
		data: "${data}",
		fmt: "xml"
	]

	def cmd = "/gwr/gop.php?" + toQueryString(qParams)
	sendCommand(cmd)
}

/******************************************************************************
 Helper Methods
 ******************************************************************************/
String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def debugEvent(message, displayEvent) {
	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}
