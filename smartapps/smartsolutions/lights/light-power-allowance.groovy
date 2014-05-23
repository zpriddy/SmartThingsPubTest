/**
*  Light Power Allowance
*
*  Author: SmartThings
*
*  Date: 2014-05-09
*/
definition(
	name: "Light Power Allowance",
	namespace: "SmartSolutions/Lights",
	parent: "SmartSolutions/Lights:New Light/Switch",
	author: "SmartThings",
	description: "Turn off after a period of time",
	category: "SmartSolutions",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Solution/switches.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Solution/switches@2x.png"
)

preferences {
	page(name: "lightPowerAllowance")

	page(name: "easingCards", title: "Follow these 3 tips to install your things", install: true) {
		section(title: "Place outlets where you intend to use them. Distance is important!") {
			image "http://cdn.easing-screens.smartthings.com/lights-switches/turn-on-motion/outlet-overlay-plugged-a.png", width: 600, height: 350
		}
		section(title: "Plug your lamp or electronics into the outlet.") {
			image "http://cdn.easing-screens.smartthings.com/lights-switches/turn-on-motion/outlet-overlay-plugged-lamp.png", width: 600, height: 350
		}
		section(title: "For questions about in-wall switches, see their instructions.") {
			image "http://cdn.easing-screens.smartthings.com/lights-switches/turn-on-motion/in-wall-switch-manual.png", width: 600, height: 350
		}
	}

	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def lightPowerAllowance() {
	def needEasingCards = (app.getInstallationState() != 'COMPLETE')
	def dynamicPageParams = [
		name: "lightPowerAllowance",
		title: "Turn off after a period of time",
		install: !needEasingCards
	]
	if(needEasingCards){
		dynamicPageParams.nextPage = "easingCards"
	}

	dynamicPage(dynamicPageParams) {
		if (!parent.switches) {
			section {
				input "switches", "capability.switch", title: "Choose devices for ${parent.app.label}", multiple: true, required: true, pairedDeviceName: nextPairedDeviceName("$parent.app.label", switches)
				icon title: "Choose an icon for ${parent.app.label}", required: true, defaultValue: "st.Lighting.light13-icn"
			}
		}
		section {
			input "turnOffAfter", "number", title: "Any time the lights are turned on, wait this many minutes and then turn them off", required: true
		}

		def timeLabel = timeIntervalLabel()
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"

			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
		}

		if(!needEasingCards) {
			section(title: "Setup Help", hidden: true, hideable: true) {
				href "easingCards", title: "Tap for more information", description: ""
			}
		}
	}
}

def installed()
{
	log.trace "$app.label installed with settings: ${settings}"
	subscribeToDevices()
}

def updated()
{
	log.trace "$app.label updated with settings: ${settings}"

	unsubscribe()
	subscribeToDevices()
}

def subscribeToDevices() {
	log.trace "subscribeToDevices"
	if (switches && !parent.switches) {
		log.trace "parent.setSwitchDevices"
		parent.setSwitchDevices(switches)
	}
	
	log.debug "switches: ${switches}"
	log.debug "parent.switches: ${parent.switches}"
	
	subscribe(parent.switches, "switch.on", switchHandler)
}

def switchHandler(evt) {
	log.trace "switchHandler($evt.name: $evt.value) turnOffAfter: $turnOffAfter min"
	def delay = turnOffAfter * 60
	runIn(delay, turnOffAfterTimer, [overwrite: false])
	log.trace "scheduled turnOffAfterTimer in $delay seconds"
}

def turnOffAfterTimer() {
	def anyOn = parent.switches?.find { it.currentValue("switch") != "off" }
	if (allOk && anyOn) {
		def switchState = "off"
		parent.switches?."$switchState"()
		def text = "$parent.app.label turned $switchState after $turnOffAfter minute${turnOffAfter > 1 ? 's' : ''}"

		parent.sendEvent(
			linkText: parent.app.label,
			descriptionText: text,
			eventType: "SOLUTION_EVENT",
			displayed: false,
			value: switchState,
			data: parent.getSolutionEventData(switchState)
		)
	}
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

// TODO - centralize somehow
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
