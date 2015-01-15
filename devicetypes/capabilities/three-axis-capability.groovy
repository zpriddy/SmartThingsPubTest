metadata {
	// Automatically generated. Make future change here.
	definition (name: "Three Axis Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Three Axis"
	}

	simulator {
		status "x,y,z: 0,0,0": "threeAxis:0,0,0"
		status "x,y,z: 1000,0,0": "threeAxis:1000,0,0"
		status "x,y,z: 0,1000,0": "threeAxis:0,1000,0"
		status "x,y,z: 0,0,1000": "xthreeAxis:0,0,1000"
		status "x,y,z: -1000,0,0": "threeAxis:-1000,0,0"
		status "x,y,z: 0,-1000,0": "threeAxis:0,-1000,0"
		status "x,y,z: 0,0,-1000": "xthreeAxis:0,0,-1000"
	}

	tiles {
		valueTile("3axis", "device.threeAxis", decoration: "flat") {
			state("threeAxis", label:'${currentValue}', unit:"", backgroundColor:"#ffffff")
		}

		main "3axis"
		details "3axis"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
