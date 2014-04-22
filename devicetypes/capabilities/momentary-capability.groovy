metadata {
	// Automatically generated. Make future change here.
	definition (name: "Momentary Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Momentary"
	}

	// simulator metadata
	simulator {
		// status messages
		// none

		// reply messages
		reply "'on','delay 2000','off'": "switch:off"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		main "switch"
		details "switch"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def push() {
	['on','delay 2000','off']
}

def off() {
	'off'
}
