metadata {
	// Automatically generated. Make future change here.
	definition (name: "Contact Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Contact Sensor"
	}

	simulator {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}

	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
		}
		main "contact"
		details "contact"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
