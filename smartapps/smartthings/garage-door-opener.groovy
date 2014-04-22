/**
 *  Garage Door Opener
 *
 *  Author: SmartThings
 */
preferences {
	section("When the garage door switch is turned on, open the garage door...") {
		input "switch1", "capability.switch"
	}
}

def installed() {
	subscribe(app, appTouchHandler)
	subscribeToCommand(switch1, "on", onCommand)
}

def updated() {
	unsubscribe()
	subscribe(app, appTouchHandler)
	subscribeToCommand(switch1, "on", onCommand)
}

def appTouch(evt) {
	log.debug "appTouch: $evt.value, $evt"
	switch1?.on()
}

def onCommand(evt) {
	log.debug "onCommand: $evt.value, $evt"
	switch1?.off(delay: 3000)
}