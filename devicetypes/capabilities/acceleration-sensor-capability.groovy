metadata {
	// Automatically generated. Make future change here.
	definition (name: "Acceleration Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Acceleration Sensor"
	}

	simulator {
		status "active": "acceleration:active"
		status "inactive": "acceleration:inactive"
	}

	tiles {
		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
			state("active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
		}

		main "acceleration"
		details "acceleration"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
