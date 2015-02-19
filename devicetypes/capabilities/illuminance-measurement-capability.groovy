metadata {
	// Automatically generated. Make future change here.
	definition (name: "Illuminance Measurement Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Illuminance Measurement"
	}

	simulator {
		for (i in [0,5,10,15,20,30,40,50,100,200,300,400,600,800,1000]) {
			status "${i} lux": "illuminance:${i}"
		}
	}

	tiles {
		valueTile("illuminance", "device.illuminance", width: 2, height: 2) {
			state "luminosity", label:'${currentValue} ${unit}', unit:"lux"
		}
		main(["illuminance"])
		details(["illuminance"])
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
