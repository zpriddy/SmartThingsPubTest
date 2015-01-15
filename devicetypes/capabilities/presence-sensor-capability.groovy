metadata {
	// Automatically generated. Make future change here.
	definition (name: "Presence Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Presence Sensor"
	}

	simulator {
		status "present": "presence: present"
		status "not present": "presence: not present"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2) {
			state("not present", label:'not present', icon:"st.presence.tile.not-present", backgroundColor:"#ffffff")
			state("present", label:'present', icon:"st.presence.tile.present", backgroundColor:"#53a7c0")
		}
		main "presence"
		details "presence"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
