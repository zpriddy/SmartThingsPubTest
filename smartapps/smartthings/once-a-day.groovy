/**
 *  Once a Day
 *
 *  Author: SmartThings
 *
 *  Turn on one or more switches at a specified time and turn them off at a later time.
 */

preferences {
	section("Select switches to control...") {
		input name: "switches", type: "capability.switch", multiple: true
	}
	section("Turn them all on at...") {
		input name: "startTime", title: "Turn On Time?", type: "time"
	}
	section("And turn them off at...") {
		input name: "stopTime", title: "Turn Off Time?", type: "time"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")

}

def updated(settings) {
	unschedule()
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")
}

def startTimerCallback() {
	log.debug "Turning on switches"
	switches.on()

}

def stopTimerCallback() {
	log.debug "Turning off switches"
	switches.off()
}