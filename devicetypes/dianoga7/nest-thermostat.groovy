/**
 *  Nest Thermostat
 *
 *  Author: Brian Steere
 *
 *  Date: 2014-02-15
 */
metadata {
	definition (name: "Nest Thermostat", namespace: "dianoga7", author: "Brian Steere") {
		capability "Actuator"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Sensor"
	}

	simulator {
// TODO: define status and reply messages here
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: true) {
			state("temperature", label: '${currentValue}°', unit:"F", backgroundColors: [
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
		standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false) {
			state "heat", label:'${name}', action:"thermostat.off", icon: "st.Weather.weather14", backgroundColor: '#E7672A'
			state "off", label:'${name}', action:"thermostat.cool", icon: "st.Outdoor.outdoor19"
			state "cool", label:'${name}', action:"thermostat.heat", icon: "st.Weather.weather7", backgroundColor: '#003CEF'
		}
		standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "auto", label:'${name}', action:"thermostat.fanOn", icon: "st.Appliances.appliances11"
			state "on", label:'${name}', action:"thermostat.fanCirculate", icon: "st.Appliances.appliances11"
			state "circulate", label:'${name}', action:"thermostat.fanAuto", icon: "st.Appliances.appliances11"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 2, width: 1, inactiveLabel: false) {
			state "setCoolingSetpoint", label:'Set temperarure to', action:"thermostat.setCoolingSetpoint",
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "default", label:'${currentValue}°', unit:"F"
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "default", label:'${currentValue}% Humidity', unit:"Humidity"
		}
		standardTile("presence", "device.presence", inactiveLabel: false, decoration: "flat") {
			state "present", label:'${name}', action:"away", icon: "st.Home.home2"
			state "away", label:'${name}', action:"present", icon: "st.Transportation.transportation5"
		}
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}
		main "temperature"
		details(["temperature", "thermostatMode", "humidity", "coolSliderControl", "coolingSetpoint", "thermostatFanMode", "presence", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'humidity' attribute
	// TODO: handle 'presence' attribute

}

// handle commands
def setHeatingSetpoint(temp) {
	log.debug "Executing 'setHeatingSetpoint'"
	parent.setHeatingSetpoint(this, temp)
}

def setCoolingSetpoint(temp) {
	log.debug "Executing 'setCoolingSetpoint'"
	parent.setCoolingSetpoint(this, temp)
}

def off() {
	log.debug "Executing 'off'"
	parent.off(this)
}

def heat() {
	log.debug "Executing 'heat'"
	parent.heat(this)
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	parent.emergencyHeat(this)
}

def cool() {
	log.debug "Executing 'cool'"
	parent.cool(this)
}

def setThermostatMode(mode) {
	log.debug "Executing 'setThermostatMode'"
	parent.setThermostatMode(this, mode)
}

def fanOn() {
	log.debug "Executing 'fanOn'"
	parent.fanOn(this)
}

def fanAuto() {
	log.debug "Executing 'fanAuto'"
	parent.fanAuto(this)
}

def fanCirculate() {
	log.debug "Executing 'fanCirculate'"
	parent.fanCirculate(this)
}

def setThermostatFanMode(mode) {
	log.debug "Executing 'setThermostatFanMode'"
	parent.setThermostatFanMode(this, mode)
}

def auto() {
	log.debug "Executing 'auto': Does nothing"

}

def away() {
	log.debug "Executing 'away'"
	parent.away(this)
}

def present() {
	log.debug "Executing 'present'"
	parent.present(this)
}

def setPresence(status) {
	log.debug "Executing 'setPresence'"
	parent.setPresence(this, status)
}

