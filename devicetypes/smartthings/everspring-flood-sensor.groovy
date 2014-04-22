metadata {
	// Automatically generated. Make future change here.
	definition (name: "Everspring Flood Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Water Sensor"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"

		fingerprint deviceId: "0xA102", inClusters: "0x86,0x72,0x85,0x84,0x80,0x70,0x9C,0x20,0x71"
	}

	simulator {
		status "dry": "command: 9C02, payload: 00 05 00 00 00"
		status "wet": "command: 9C02, payload: 00 05 FF 00 00"
		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i).incomingMessage()
		}
	}
	tiles {
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""/*, backgroundColors:[
				[value: 5, color: "#BC2323"],
				[value: 10, color: "#D04E00"],
				[value: 15, color: "#F1D801"],
				[value: 16, color: "#FFFFFF"]
			]*/
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main "water"
		details(["water", "battery", "configure"])
	}
}

def parse(String description) {

	def parsedZwEvent = zwave.parse(description, [0x9C: 1, 0x71: 1, 0x84: 2, 0x30: 1])
	def zwEvent = zwaveEvent(parsedZwEvent)
	def result = []

	result << createEvent( zwEvent )

	if( parsedZwEvent.CMD == "8407" ) {
		def lastStatus = device.currentState("battery")
		def ageInMinutes = lastStatus ? (new Date().time - lastStatus.date.time)/60000 : 600
		log.debug "Battery status was last checked ${ageInMinutes} minutes ago"

		if (ageInMinutes >= 600) {
			log.debug "Battery status is outdated, requesting battery report"
			result << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
		}
		result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	}

	log.debug "Parse returned ${result}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange:  false]
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	def map = [:]
	if (cmd.sensorType == 0x05) {
		map.name = "water"
		map.value = cmd.sensorState ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	def map = [:]
	map.name = "water"
	map.value = cmd.sensorValue ? "wet" : "dry"
	map.descriptionText = "${device.displayName} is ${map.value}"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
	def map = [:]
	if (cmd.alarmType == 1 && cmd.alarmLevel == 0xFF) {
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
		map
	} else if (cmd.alarmType == 2 && cmd.alarmLevel == 1) {
		map.descriptionText = "${device.displayName} powered up"
		map.displayed = false
		map
	} else {
		log.debug cmd
	}
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	if (cmd.batteryLevel == 0xFF) {
		map.name = "battery"
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
	} else {
		map.name = "battery"
		map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
		map.unit = "%"
		map.displayed = false
	}
	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd)
{
	log.debug "COMMAND CLASS: $cmd"
}

def configure()
{
	zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[1]).format()  // TODO: this nodeId is the hub's node id which isn't necessarily 1
}
