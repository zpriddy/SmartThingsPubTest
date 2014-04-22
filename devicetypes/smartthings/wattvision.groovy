/**
 *  Wattvision
 *
 *  Author: steve
 *  Date: 2014-02-13
 */
// for the UI
metadata {
	simulator {
		// TODO: define status and reply messages here
	}

	tiles {

		valueTile("power", "device.power") { // TODO: add colors to state
			state "power", label: '${currentValue} W'
		}

		chartTile(name: "powerChart", attribute: "power")

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

	}

	main "power"
	details(["powerChart", "power"])//, "refresh"])

	preferences {
		input(type: "enum", name: "chartGranularity", title: "Chart Granularity", options: granularityOptions(), defaultValue: "Daily", style: "segmented")
	}

}

def refresh() {
	// TODO: handle this
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
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
			date: new Date().parse(wattvisionDateFormat, it.t),
			value: it.v,
			name: "power",
			displayed: true,
			isStateChange: true,
			description: "${it.v} ${units}",
			descriptionText: "${it.v} ${units}"
		]

		sendEvent(eventData)

		granularityOptions().each { gOption ->
			storeData("power${gOption}", it.v, "getFromDate${gOption}"())
		}
	}
}

def getVisualizationData(attribute) {
	log.debug "getChartData for $attribute"
	def keyBase = "measure.${attribute}${getGranularity()}"
	log.debug "getChartData state = $state"

	def dateBuckets = state[keyBase]

	//convert to the right format
	def results = dateBuckets?.sort { it.key }.collect {
		[
			date: Date.parse("yyyy-MM-dd", it.key),
			average: it.value.average,
			min: it.value.min,
			max: it.value.max
		]
	}

	log.debug "getChartData results = $results"
	results
}

private storeData(attribute, value, dateString = getKeyFromDateDaily()) {
	log.debug "storeData initial state: $state"
	def keyBase = "measure.${attribute}"
	def numberValue = value.toBigDecimal()

	// create bucket if it doesn't exist
	if (!state[keyBase]) {
		state[keyBase] = [:]
		log.debug "storeData - attribute not found. New state: $state"
	}

	if (!state[keyBase][dateString]) {
		//no date bucket yet, fill with initial values
		state[keyBase][dateString] = [:]
		state[keyBase][dateString].average = numberValue
		state[keyBase][dateString].runningSum = numberValue
		state[keyBase][dateString].runningCount = 1
		state[keyBase][dateString].min = numberValue
		state[keyBase][dateString].max = numberValue

		log.debug "storeData date bucket not found. New state: $state"

		// remove old buckets
		def old = getKeyFromDateDaily(new Date() - 10)
		state[keyBase].findAll { it.key < old }.collect { it.key }.each { state[keyBase].remove(it) }
	} else {
		//re-calculate average/min/max for this bucket
		state[keyBase][dateString].runningSum = (state[keyBase][dateString].runningSum.toBigDecimal()) + numberValue
		state[keyBase][dateString].runningCount = state[keyBase][dateString].runningCount.toInteger() + 1
		state[keyBase][dateString].average = state[keyBase][dateString].runningSum.toBigDecimal() / state[keyBase][dateString].runningCount.toInteger()

		log.debug "storeData after average calculations. New state: $state"

		if (state[keyBase][dateString].min == null) {
			state[keyBase][dateString].min = numberValue
		} else if (numberValue < state[keyBase][dateString].min.toBigDecimal()) {
			state[keyBase][dateString].min = numberValue
		}
		if (state[keyBase][dateString].max == null) {
			state[keyBase][dateString].max = numberValue
		} else if (numberValue > state[keyBase][dateString].max.toBigDecimal()) {
			state[keyBase][dateString].max = numberValue
		}
	}
	log.debug "storeData after min/max calculations. New state: $state"
}

// Keep everything below this in sync

def getGranularity() {
	chartGranularity ?: "Daily"
}

def granularityOptions() { ["Daily", "Hourly"] }

private getKeyFromDateDaily(date = new Date()) {
	date.format("yyyy-MM-dd")
}

private getKeyFromDateHourly(date = new Date()) {
	date.format("yyyy-MM-dd:HH")
}
