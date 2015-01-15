/**
 *  Ecobee Thermostat
 *
 *  Author: SmartThings
 *  Date: 2013-06-13
 */
metadata {
	
    definition (name: "Ecobee", namespace: "smartthings-deprecated", author: "SmartThings") {
		capability "Thermostat"
		capability "Polling"
	}

	simulator { }

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"F",
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° heat', unit:"F", backgroundColor:"#ffffff"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}° cool', unit:"F", backgroundColor:"#ffffff"
		}
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "off", label:'${name}', action:"thermostat.heat"
			state "heat", label:'${name}', action:"thermostat.emergencyHeat"
			state "emergencyHeat", label:'${name}', action:"thermostat.cool"
			state "cool", label:'${name}', action:"thermostat.auto"
			state "auto", label:'${name}', action:"thermostat.off"
		}
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "fanAuto", label:'${name}', action:"thermostat.fanOn"
			state "fanOn", label:'${name}', action:"thermostat.fanCirculate"
			state "fanCirculate", label:'${name}', action:"thermostat.fanAuto"
		}
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}
		main "temperature"
		details(["temperature", "heatingSetpoint", "coolingSetpoint", "mode", "fanMode","refresh"])
	}
}

def parse(Map description) {

	def results = []
	if (description?.status != 200) {

		if (description.status == 500 && description.data.status.code == 14) {
			log.debug "Storing the failed action to try later"
			data.action = description.action

			log.debug "Refreshing your auth_token!"
			results << refreshAuthToken()
		} else {
			log.error "Authentication error, invalid authentication method, lack of credentials, etc."
		}

	} else if (description?.data?.refresh_token) {

		log.debug "Token refreshed...calling saved RestAction now!"

		data.refreshToken = description?.data?.refresh_token
		data.authToken = description?.data?.access_token

		if (data?.action && data?.action != "") {
			log.debug data.action

			def replayAction = data.action
			replayAction.headers."Authorization" = "Bearer ${data.authToken}"

			log.debug replayAction

			results << rest(replayAction)

			//remove saved action
			data.action = ""
		} else {
			results << getUpdate()
		}
	} else {
		if (description.data.page) {
			results << getTempResult(description)
			results << getHeatingSetpoint(description)
			results << getCoolingSetpoint(description)
		} else {
			log.debug "Command was successful"
		}
	}
	results
}

// handle commands
def poll() {

	/*data.authToken = "IQetV8gPR120D3qInbpJewmYbAynoqOA"
	data.refreshToken = "oyDb2FGtTlVroobBhNXVeyksPevpMoko"
	data.clientId = "qqwy6qo0c2lhTZGytelkQ5o8vlHgRsrO"
	data.action = ""*/
	log.debug "Executing 'poll'"
	getUpdate()
}


private getUpdate() {
	rest(
		method: 'GET',
		endpoint: "https://api.ecobee.com",
		path: "/1/thermostat",
		headers: ["Content-Type": "text/json", "Authorization": "Bearer ${data?.authToken}"],
		query: [format: 'json', body: '{"selection":{"selectionType":"thermostats","selectionMatch":"' + preferences.selectionMatch + '","includeRuntime":true}}'],
		synchronous: true
	)
}

private refreshAuthToken() {
	rest(
		method: 'POST',
		endpoint: "https://api.ecobee.com",
		path: "/token",
		query: [grant_type:'refresh_token', code:"${data?.refreshToken}", client_id:"${data?.clientId}"],
		synchronous: true
	)
}

private getTempResult(description) {
	def name = "temperature"
	def value = (description.data.thermostatList[0].runtime.actualTemperature / 10).toString()
	def linkText = getLinkText(device)
	def descriptionText = "$linkText was $value°F"
	def isStateChange = isTemperatureStateChange(device, name, value)
	log.debug descriptionText
	[
		name: name,
		value: value,
		unit: "F",
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getHeatingSetpoint(description) {
	def name = "heatingSetpoint"
	def value = (description.data.thermostatList[0].runtime.desiredHeat / 10).toString()
	def linkText = getLinkText(device)
	def descriptionText = "latest heating setpoint was $value°F"
	def isStateChange = isTemperatureStateChange(device, name, value)
	log.debug descriptionText
	[
		name: name,
		value: value,
		unit: "F",
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getCoolingSetpoint(description) {
	def name = "coolingSetpoint"
	def value = (description.data.thermostatList[0].runtime.desiredCool / 10).toString()
	def linkText = getLinkText(device)
	def descriptionText = "latest cooling setpoint was $value°F"
	def isStateChange = isTemperatureStateChange(device, name, value)
	log.debug descriptionText
	[
		name: name,
		value: value,
		unit: "F",
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

def setHeatingSetpoint(degreesF) {
	setHeatingSetpoint(degreesF.toDouble())
}

def setHeatingSetpoint(BigDecimal degreesF) {
	log.debug "Executing 'setHeatingSetpoint' to ${degreesF}"

		def coolSetPoint = data?.lastCoolSetPoint ?: device.currentValue("coolingSetpoint") * 10

		def heatSetPoint = degreesF * 10
		data.lastHeatSetPoint = heatSetPoint

	rest(
		method: 'POST',
		endpoint: "https://api.ecobee.com",
		path: "/1/thermostat",
		headers: ["Content-Type": "application/json;charset=UTF-8" , "Authorization": "Bearer ${data?.authToken}"],
		query: [format: 'json'],
		body: [selection:[selectionType:"thermostats", selectionMatch:preferences.selectionMatch],functions:[[type:"setHold", params:[coolHoldTemp:coolSetPoint, heatHoldTemp:heatSetPoint, holdType:"nextTransition"]]]],
				requestContentType: "application/json",
				synchronous: true
		)
}

def setCoolingSetpoint(degreesF) {
	setCoolingSetpoint(degreesF.toDouble())
}

def setCoolingSetpoint(BigDecimal degreesF) {
	log.debug "Executing 'setCoolingSetpoint' to ${degreesF}"

		def coolSetPoint = degreesF * 10
		data.lastCoolSetPoint = coolSetPoint

		def heatSetPoint = data?.lastHeatSetPoint ?: device.currentValue("heatingSetpoint") * 10

	rest(
		method: 'POST',
		endpoint: "https://api.ecobee.com",
		path: "/1/thermostat",
		headers: ["Content-Type": "application/json;charset=UTF-8" , "Authorization": "Bearer ${data?.authToken}"],
		query: [format: 'json'],
		body: [selection:[selectionType:"thermostats", selectionMatch:preferences.selectionMatch],functions:[[type:"setHold", params:[coolHoldTemp:coolSetPoint, heatHoldTemp:heatSetPoint, holdType:"nextTransition"]]]],
				requestContentType: "application/json",
				synchronous: true
		)
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// TODO: handle 'emergencyHeat' command
}

def cool() {
	log.debug "Executing 'cool'"
	// TODO: handle 'cool' command
}

def setThermostatMode() {
	log.debug "Executing 'setThermostatMode'"
	// TODO: handle 'setThermostatMode' command
}

def fanOn() {
	log.debug "Executing 'fanOn'"
	// TODO: handle 'fanOn' command
}

def fanAuto() {
	log.debug "Executing 'fanAuto'"
	// TODO: handle 'fanAuto' command
}

def fanCirculate() {
	log.debug "Executing 'fanCirculate'"
	// TODO: handle 'fanCirculate' command
}

def setThermostatFanMode() {
	log.debug "Executing 'setThermostatFanMode'"
	// TODO: handle 'setThermostatFanMode' command
}
