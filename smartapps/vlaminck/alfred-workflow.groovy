/**
 *  Alfred Workflow
 *
 *  Author: SmartThings
 */

definition(
    name: "Alfred Workflow",
    namespace: "vlaminck",
    author: "SmartThings",
    description: "This SmartApp allows you to interact with the things in your physical graph through Alfred.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/alfred-app.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/alfred-app@2x.png",
    oauth: [displayName: "SmartThings Alfred Workflow", displayLink: ""]
)

preferences {
	section("Allow Alfred to Control These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
	}
}

mappings {
	path("/switches") {
		action: [
			GET: "listSwitches",
			PUT: "updateSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch",
			PUT: "updateSwitch"
		]
	}
    path("/locks") {
		action: [
			GET: "listLocks",
			PUT: "updateLocks"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock",
			PUT: "updateLock"
		]
	}
}

def installed() {}

def updated() {}

def listSwitches() {
	switches.collect { device(it,"switch") }
}
void updateSwitches() {
	updateAll(switches)
}
def showSwitch() {
	show(switches, "switch")
}
void updateSwitch() {
	update(switches)
}

def listLocks() {
	locks.collect { device(it, "lock") }
}
void updateLocks() {
	updateAll(locks)
}
def showLock() {
	show(locks, "lock")
}
void updateLock() {
	update(locks)
}

private void updateAll(devices) {
	def command = request.JSON?.command
	if (command) {
		devices."$command"()
	}
}

private void update(devices) {
	log.debug "update, request: ${request.JSON}, params: ${params}, devices: $devices.id"
	def command = request.JSON?.command
	if (command) {
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
			device."$command"()
		}
	}
}

private show(devices, name) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def s = device.currentState(name)
		[id: device.id, label: device.displayName, name: device.displayName, state: s]
	}
}

private device(it, name) {
	if (it) {
		def s = it.currentState(name)
		[id: it.id, label: it.displayName, name: it.displayName, state: s]
    }
}
