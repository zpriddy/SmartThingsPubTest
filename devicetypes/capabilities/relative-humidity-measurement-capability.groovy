metadata {
	// Automatically generated. Make future change here.
	definition (name: "Relative Humidity Measurement Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Relative Humidity Measurement"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 100; i += 10) {
			status "${i}%": "humidity: ${i}%"
		}
	}

	// UI tile definitions
	tiles {
		valueTile("humidity", "device.humidity", width: 2, height: 2) {
			state "humidity", label:'${currentValue}%', unit:""
		}
	}
}

// Parse incoming device messages to generate events
// Parse incoming device messages to generate events
def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim(), unit:"%")
}
