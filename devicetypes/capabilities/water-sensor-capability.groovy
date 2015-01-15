metadata {
	// Automatically generated. Make future change here.
	definition (name: "Water Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Water Sensor"
	}

	simulator {
		status "wet": "water:wet"
		status "dry": "water:dry"
	}

	tiles {
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}

		main "water"
		details "water"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
