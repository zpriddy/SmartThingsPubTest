/**
 *  Wattvision
 *
 *  Author: steve
 *  Date: 2014-02-13
 */
// for the UI
metadata {

	definition(name: "Wattvision", namespace: "smartthings", author: "Steve Vlaminck") {
		capability "Power meter"
		capability "Refresh"
		attribute "powerContent", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {

		valueTile("power", "device.power") { // TODO: add colors to state
			state "power", label: '${currentValue} W'
		}

		tile(name: "powerChart", attribute: "powerContent", type: "HTML", content: '${currentValue}', width: 3, height: 2) {
			state "powerContent", label: ''
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

	}

	main "power"
	details(["powerChart", "power", "refresh"])

}

def refresh() {
    setGraphUrl(parent.getGraphUrl(device.deviceNetworkId))
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

public setGraphUrl(graphUrl) {
	sendEvent([
		date           : new Date(),
		value          : graphUrl,
		name           : "powerContent",
		displayed      : false,
		isStateChange  : true,
		description    : "Graph updated",
		descriptionText: "Graph updated"
	])
}

public addWattvisionData(json) {
	def data = json.data
	log.debug "adding wattvision data: ${json.data}"
	def units = json.units ?: "watts"
	log.debug "units: ${units}"

	def wattvisionDateFormat = parent.wattvisionDateFormat()
	log.debug "wattvisionDateFormat: ${wattvisionDateFormat}"

	data.each {
//		log.debug "sending event with data: ${it}"

		def eventData = [
			date           : new Date().parse(wattvisionDateFormat, it.t),
			value          : it.v,
			name           : "power",
			displayed      : true,
			isStateChange  : true,
			description    : "${it.v} ${units}",
			descriptionText: "${it.v} ${units}"
		]

		sendEvent(eventData)

	}
}
