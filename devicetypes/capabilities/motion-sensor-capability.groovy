metadata {
	// Automatically generated. Make future change here.
	definition (name: "Motion Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Motion Sensor"
	}

	simulator {
		status "active": "motion:active"
		status "inactive": "motion:inactive"
	}

	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
		}
		main "motion"
		details "motion"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
