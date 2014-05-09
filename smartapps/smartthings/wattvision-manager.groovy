/**
 *  Wattvision Manager
 *
 *  Author: steve
 *  Date: 2014-02-13
 */

// Automatically generated. Make future change here.
definition(
	name: "Wattvision Manager",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Wattvision integration",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision%402x.png",
	oauth: [displayName: "Wattvision", displayLink: "https://www.wattvision.com/"]
)

preferences {
	page(name: "rootPage", title: "Wattvision", install: true, uninstall: true) {
		section {
			input(name: "wattvisionDataType", type: "enum", required: false, multiple: false, defaultValue: "rate", options: ["rate", "consumption"])
			label(title: "Assign a name")
		}
	}
}

mappings {
	path("/access") {
		actions:
		[
			POST  : "setApiAccess",
			DELETE: "revokeApiAccess"
		]
	}
	path("/devices") {
		actions:
		[
			GET: "listDevices"
		]
	}
	path("/device/:sensorId") {
		actions:
		[
			GET   : "getDevice",
			PUT   : "updateDevice",
			POST  : "createDevice",
			DELETE: "deleteDevice"
		]
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	getDataFromWattvision()
}

def getDataFromWattvision() {

	log.trace "Getting data from Wattvision"

	def children = getChildDevices()
	if (!children) {
		log.warn "No children. Not collecting data from Wattviwion"
		// currently only support one child
		return
	}

	def endDate = new Date()
	def startDate

	if (!state.lastUpdated) {
		log.debug "no state.lastUpdated"
		startDate = new Date(hours: endDate.hours - 3)
	} else {
		log.debug "parsing state.lastUpdated"
		startDate = new Date().parse(smartThingsDateFormat(), state.lastUpdated)
	}

	state.lastUpdated = endDate.format(smartThingsDateFormat())

	children.each { child ->
		getDataForChild(child, startDate, endDate)
	}

}

def getDataForChild(child, startDate, endDate) {
	if (!child) {
		return
	}

	def wattvisionURL = wattvisionURL(child.deviceNetworkId, startDate, endDate)
	if (wattvisionURL) {
		httpGet(uri: wattvisionURL) { response ->
			def json = new org.codehaus.groovy.grails.web.json.JSONObject(response.data.toString())
			child.addWattvisionData(json)
			return "success"
		}
	}
}

def wattvisionURL(senorId, startDate, endDate) {

	log.trace "getting wattvisionURL"

	def wattvisionApiAccess = state.wattvisionApiAccess
	if (!wattvisionApiAccess.id || !wattvisionApiAccess.key || !wattvisionApiAccess.url) {
		return null
	}

	if (!endDate) {
		endDate = new Date()
	}
	if (!startDate) {
		startDate = new Date(hours: endDate.hours - 3)
	}

	def params = [
		"sensor_id" : senorId,
		"api_id"    : wattvisionApiAccess.id,
		"api_key"   : wattvisionApiAccess.key,
		"type"      : wattvisionDataType ?: "rate",
		"start_time": startDate.format(wattvisionDateFormat()),
		"end_time"  : endDate.format(wattvisionDateFormat())
	]

	def parameterString = params.collect { key, value -> "${key.encodeAsURL()}=${value.encodeAsURL()}" }.join("&")
	def url = "${wattvisionApiAccess.url}?${parameterString}"

//	log.debug "wattvisionURL: ${url}"
	return url
}

def getData() {
	state.lastUpdated = new Date().format(smartThingsDateFormat())
}

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public wattvisionDateFormat() { "yyyy-MM-dd'T'HH:mm:ss" }

def childMarshaller(child) {
	return [
		name     : child.name,
		label    : child.label,
		sensor_id: child.deviceNetworkId,
		location : child.location.name
	]
}

/*
			ENDPOINTS
*/

def listDevices() {
	getChildDevices().collect { childMarshaller(it) }
}

def getDevice() {

	log.trace "Getting device"

	def child = getChildDevice(params.sensorId)

	if (!child) {
		httpError(404, "Device not found")
	}

	return childMarshaller(child)
}

def updateDevice() {

	log.trace "Updating Device with data from Wattvision"

	def body = request.JSON

	def child = getChildDevice(params.sensorId)

	if (!child) {
		httpError(404, "Device not found")
	}

	child.addWattvisionData(body)

	render([status: 204, data: " "])
}

def createDevice() {

	log.trace "Creating Wattvision device"

	if (getChildDevice(params.sensorId)) {
		httpError(403, "Device already exists")
	}

	def child = addChildDevice("smartthings", "Wattvision", params.sensorId, null, [name: "Wattvision", label: request.JSON.label])

	child.setGraphUrl(getGraphUrl(params.sensorId));

	getDataForChild(child, null, null)

	return childMarshaller(child)
}

def deleteDevice() {

	log.trace "Deleting Wattvision device"

	deleteChildDevice(params.sensorId)
	render([status: 204, data: " "])
}

def setApiAccess() {

	log.trace "Granting access to Wattvision API"

	def body = request.JSON

	state.wattvisionApiAccess = [
		url: body.url,
		id : body.id,
		key: body.key
	]

	schedule("* /5 * * * ?", "getDataFromWattvision") // every 5 minutes

	render([status: 204, data: " "])
}

def revokeApiAccess() {

	log.trace "Revoking access to Wattvision API"

	state.wattvisionApiAccess = [:]
	render([status: 204, data: " "])
}

public getGraphUrl(sensorId) {

	log.trace "Collecting URL for Wattvision graph"

	def apiId = state.wattvisionApiAccess.id
	def apiKey = state.wattvisionApiAccess.key

	// TODO: allow the changing of type?
	"http://www.wattvision.com/partners/smartthings/charts?s=${sensorId}&api_id=${apiId}&api_key=${apiKey}&type=w"
}
