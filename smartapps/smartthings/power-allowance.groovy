/**
 *  Power Allowance
 *
 *  Author: SmartThings
 */
preferences {
	section("When a switch turns on...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it off how many minutes later?") {
		input "minutesLater", "number", title: "When?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: false])
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: false])
}

def switchOnHandler(evt) {
	log.debug "Switch ${theSwitch} turned: ${evt.value}"
	def delay = minutesLater * 60
	log.debug "Turning off in ${minutesLater} minutes (${delay}seconds)"
	runIn(delay, turnOffSwitch)
}

def turnOffSwitch() {
	theSwitch.off()
}
