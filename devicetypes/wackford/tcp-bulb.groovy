/**
 *  TCP-Bulbs-Device.groovy
 *
 *  Author: todd@wackford.net
 *  Date: 2014-03-07
 *
 *****************************************************************
 *     Setup Namespace, capabilities, attributes and commands
 *****************************************************************
 * Namespace:			"wackford"
 *
 * Capabilities:		"polling"
 *						"refresh"
 *						"switch"
 *						"switch level"
 *
 * Custom Attributes:	"none"
 *
 * Custom Commands:		"levelUp"
 *						"levelDown"
 *
 *****************************************************************
 *                       Changes
 *****************************************************************
 *
 *  Change 1:	2014-03-10
 *				Documented Header
 *
 *  Change 2:	2014-03-15
 *				Fixed bug where we weren't coming on when changing 
 *				levels down.
 *
 *****************************************************************
 *                       Code
 *****************************************************************
 */


metadata {

	definition(name: "TCP Bulb", namespace: "wackford", author: "Todd Wackford") {
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"
		capability "Polling"

		command "levelUp"
		command "levelDown"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true ) {
			state "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light14", backgroundColor:"#79b821"//, nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light14", backgroundColor:"#ffffff"//, nextState:"turningOn"
			//state "turningOn", label:'${name}', icon:"st.Lighting.light14", backgroundColor:"#79b821"
			//state "turningOff", label:'${name}', icon:"st.Lighting.light14", backgroundColor:"#ffffff"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		standardTile("lUp", "device.switchLevel", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
			state "default", action:"levelUp", icon:"st.illuminance.illuminance.bright"
		}
		standardTile("lDown", "device.switchLevel", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
			state "default", action:"levelDown", icon:"st.illuminance.illuminance.light"
		}

		main(["switch"])
		details(["switch", "lUp", "lDown", "levelSliderControl", "level" , "refresh" ])
	}
}


// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	if (description?.name && description?.value)
	{
		results << sendEvent(name: "${description?.name}", value: "${description?.value}")
	}
}

// handle commands
def on() {
	log.debug "Executing 'on'"
	sendEvent(name:"switch",value:on)
	parent.on(this)
}

def off() {
	log.debug "Executing 'off'"
	sendEvent(name:"switch",value:off)
	parent.off(this)
}

def levelUp() {
	def level = device.latestValue("level") as Integer ?: 0

	level+= 10

	if ( level > 99 )
		level = 99

	setLevel(level)
}

def levelDown() {
	def level = device.latestValue("level") as Integer ?: 0

	level-= 10

	if ( level <  1 )
		level = 1

	setLevel(level)
}

def setLevel(value) {
	def level = value as Integer

	if (( level > 0 ) && ( level < 100 ))
		on()
	else
		off()

	sendEvent( name: "level", value: level )
	sendEvent( name: "switch.setLevel", value:level )
	parent.setLevel( this, level )
}

def poll() {
	log.debug "Executing poll()"
	parent.poll(this)
}

def refresh() {
	log.debug "Executing refresh()"
	parent.poll(this)
}
