metadata {
	// Automatically generated. Make future change here.
	definition (name: "Lock Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Lock"
	}

	simulator {
		status "locked": "lock:locked"
		status "unlocked": "lock:unlocked"

		reply "lock": "lock:locked"
		reply "unlock": "lock:unlocked"
	}

	tiles {
		standardTile("toggle", "device.lock", width: 2, height: 2) {
			state "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
			state "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821"
		}
		standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat") {
			state "default", label:'lock', action:"lock.lock", icon:"st.locks.lock.locked"
		}
		standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat") {
			state "default", label:'unlock', action:"lock.unlock", icon:"st.locks.lock.unlocked"
		}

		main "toggle"
		details(["toggle", "lock", "unlock", "refresh"])
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def lock() {
	"lock"
}

def unlock() {
	"unlock"
}
