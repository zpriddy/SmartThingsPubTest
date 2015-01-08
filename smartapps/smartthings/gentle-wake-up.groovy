/**
 *  Gentle Wake Up
 *
 *  Author: Steve Vlaminck
 *  Date: 2013-03-11
 *
 * 	https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime.png
 * 	https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime%402x.png
 * 	Gentle Wake Up turns on your lights slowly, allowing you to wake up more
 * 	naturally. Once your lights have reached full brightness, optionally turn on
 * 	more things, or send yourself a text for a more gentle nudge into the waking
 * 	world (you may want to set your normal alarm as a backup plan).
 *
 */
definition(
	name: "Gentle Wake Up",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Gentle Wake Up dims your lights slowly, allowing you to wake up more naturally. Once your lights have finished dimming, optionally turn on more things or send yourself a text for a more gentle nudge into the waking world (you may want to set your normal alarm as a backup plan).",
	category: "Health & Wellness",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime@2x.png"
)

preferences {
	page(name: "rootPage")
	page(name: "schedulingPage")
	page(name: "completionPage")
	page(name: "numbersPage")
}

def rootPage() {
	dynamicPage(name: "rootPage", title: "", install: true, uninstall: true) {

		section {
			input(name: "dimmers", type: "capability.switchLevel", title: "Dimmers", description: null, multiple: true, required: true, refreshAfterSelection: true)
		}

		if (dimmers) {

			section {
				href(name: "toNumbersPage", page: "numbersPage", title: "Duration & Direction", description: numbersPageHrefDescription(), state: "complete")
			}

			section {
				href(name: "toSchedulingPage", page: "schedulingPage", title: "Rules For Automatically Dimming Your Lights", description: schedulingHrefDescription(), state: schedulingHrefDescription() ? "complete" : "")
			}

			section {
				href(name: "toCompletionPage", title: "Completion Actions (Optional)", page: "completionPage", state: completionHrefDescription() ? "complete" : "", description: completionHrefDescription())
			}

			section {
				// TODO: fancy label
				label(title: "Label this SmartApp", required: false, defaultValue: "")
			}
		}
	}
}

def numbersPage() {
	dynamicPage(name:"numbersPage", title:"") {

		section {
			paragraph(name: "pGraph", title: "These lights will dim", fancyDeviceString(dimmers))
		}

		section {
			input(name: "duration", type: "number", title: "For this many minutes", description: "30", required: false, defaultValue: 30)
		}

		section {
			input(name: "startLevel", type: "number", range: "0..99", title: "From this level", defaultValue: defaultStart(), description: "Current Level", required: false, multiple: false)
			input(name: "endLevel", type: "number", range: "0..99", title: "To this level", defaultValue: defaultEnd(), description: "Between 0 and 99", required: true, multiple: false)
		}

		def colorDimmers = dimmersWithSetColorCommand()
		if (colorDimmers) {
			section {
				input(name: "colorize", type: "bool", title: "Gradually change the color of ${fancyDeviceString(colorDimmers)}", description: null, required: false, defaultValue: "true")
			}
		}
	}
}

def defaultStart() {
	if (usesOldSettings() && direction && direction == "Down") {
		return 99
	}
	return 0
}

def defaultEnd() {
	if (usesOldSettings() && direction && direction == "Down") {
		return 0
	}
	return 99
}

def startLevelLabel() {
	if (usesOldSettings()) { // using old settings
		if (direction && direction == "Down") { // 99 -> 1
			return "99%"
		}
		return "0%"
	}
	return hasStartLevel() ? "${startLevel}%" : "Current Level"
}

def endLevelLabel() {
	if (usesOldSettings()) {
		if (direction && direction == "Down") { // 99 -> 1
			return "0%"
		}
		return "99%"
	}
	return "${endLevel}%"
}

def schedulingPage() {
	dynamicPage(name: "schedulingPage", title: "Rules For Automatically Dimming Your Lights") {

		section {
			input(name: "days", type: "enum", title: "Allow Automatic Dimming On These Days", description: "Every day", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
		}

		section {
			input(name: "modeStart", title: "Start when entering this mode", type: "mode", required: false, mutliple: false, refreshAfterSelection: true)
			if (modeStart) {
				input(name: "modeStop", title: "Stop when leaving '${modeStart}' mode", type: "bool", required: false)
			}
		}

		section {
			input(name: "startTime", type: "time", title: "Start Dimming At This Time", description: null, required: false)
		}

	}
}

def completionPage() {
	dynamicPage(name: "completionPage", title: "Completion Rules") {

		section("Switches") {
			input(name: "completionSwitches", type: "capability.switch", title: "Set these switches", description: null, required: false, multiple: true, refreshAfterSelection: true)
			if (completionSwitches || androidClient()) {
				input(name: "completionSwitchesState", type: "enum", title: "To", description: null, required: false, multiple: false, options: ["on", "off"], style: "segmented", defaultValue: "on")
				input(name: "completionSwitchesLevel", type: "number", title: "Optionally, Set Dimmer Levels To", description: null, required: false, multiple: false, range: "(0..99)")
			}
		}

		section("Notifications") {
			input("recipients", "contact", title: "Send notifications to") {
				input(name: "completionPhoneNumber", type: "phone", title: "Text This Number", description: "Phone number", required: false)
				input(name: "completionPush", type: "bool", title: "Send A Push Notification", description: "Phone number", required: false)
			}
			input(name: "completionMusicPlayer", type: "capability.musicPlayer", title: "Speak Using This Music Player", required: false)
			input(name: "completionMessage", type: "text", title: "With This Message", description: null, required: false)
		}

		section("Modes and Phrases") {
			input(name: "completionMode", type: "mode", title: "Change ${location.name} Mode To", description: null, required: false)
			input(name: "completionPhrase", type: "enum", title: "Execute The Phrase", description: null, required: false, multiple: false, options: location.helloHome.getPhrases().label)
		}

		section("Delay") {
			input(name: "completionDelay", type: "number", title: "Delay This Many Minutes Before Executing These Actions", description: "0", required: false)
		}
	}
}

// ========================================================
// Handlers
// ========================================================

def installed() {
	log.debug "Installing 'Gentle Wake Up' with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updating 'Gentle Wake Up' with settings: ${settings}"
	unschedule()

	initialize()
}

private initialize() {
	stop()

	if (startTime) {
		log.debug "scheduling dimming routine to run at $startTime"
		schedule(startTime, "scheduledStart")
	}

	// TODO: make this an option
	subscribe(app, appHandler)

	subscribe(location, locationHandler)
}

def appHandler(evt) {
	log.debug "appHandler evt: ${evt.value}"
	if (evt.value == "touch") {
		if (atomicState.running) {
			stop()
		} else {
			start()
		}
	}
}

def locationHandler(evt) {
	log.debug "locationHandler evt: ${evt.value}"

	if (!modeStart) {
		return
	}

	def isSpecifiedMode = (evt.value == modeStart)
	def modeStopIsTrue = (modeStop && modeStop != "false")

	if (isSpecifiedMode && canStartAutomatically()) {
		start()
	} else if (!isSpecifiedMode && modeStopIsTrue) {
		stop()
	}

}

// ========================================================
// Scheduling
// ========================================================

def scheduledStart() {
	if (canStartAutomatically()) {
		start()
	}
}

def start() {
	log.trace "START"

	setLevelsInState()

	atomicState.running = true

	atomicState.start = new Date().getTime()

	schedule("0 * * * * ?", "healthCheck")
	increment()
}

def stop() {
	log.trace "STOP"

	atomicState.running = false
	atomicState.start = 0

	unschedule("healthCheck")
}

private healthCheck() {
	log.trace "'Gentle Wake Up' healthCheck"

	if (!atomicState.running) {
		return
	}

	increment()
}

// ========================================================
// Setting levels
// ========================================================


private increment() {

	if (!atomicState.running) {
		return
	}

	def percentComplete = completionPercentage()

	if (percentComplete > 99) {
		percentComplete = 99
	}

	updateDimmers(percentComplete)

	if (percentComplete < 99) {

		def runAgain = stepDuration()
		log.debug "Rescheduling to run again in ${runAgain} seconds"

		runIn(runAgain, 'increment', [overwrite: true])

	} else {

		int completionDelay = completionDelaySeconds()
		if (completionDelay) {
			log.debug "Finished with steps. Scheduling completion for ${completionDelay} second(s) from now"
			runIn(completionDelay, 'completion', [overwrite: true])
			unschedule("healthCheck")
			// don't let the health check start incrementing again while we wait for the delayed execution of completion
		} else {
			log.debug "Finished with steps. Execution completion"
			completion()
		}

	}
}


def updateDimmers(percentComplete) {
	dimmers.each { dimmer ->

		def nextLevel = dynamicLevel(dimmer, percentComplete)

		if (nextLevel == 0) {

			dimmer.off()

		} else {

			def shouldChangeColors = (colorize && colorize != "false")
			def canChangeColors = hasSetColorCommand(dimmer)

			log.debug "Setting ${deviceLabel(dimmer)} to ${nextLevel}"

			if (shouldChangeColors && canChangeColors) {
				dimmer.setColor([hue: getHue(dimmer, nextLevel), saturation: 100, level: nextLevel])
			} else {
				dimmer.setLevel(nextLevel)
			}

		}
	}
}

int dynamicLevel(dimmer, percentComplete) {
	def start = atomicState.startLevels[dimmer.id]
	def end = dynamicEndLevel()

	if (!percentComplete) {
		return start
	}

	def totalDiff = end - start
	def actualPercentage = percentComplete / 100
	def percentOfTotalDiff = totalDiff * actualPercentage

	(start + percentOfTotalDiff) as int
}

// ========================================================
// Completion
// ========================================================

private completion() {
	log.trace "Starting completion block"

	if (!atomicState.running) {
		return
	}

	stop()

	handleCompletionSwitches()

	handleCompletionMessaging()

	handleCompletionModesAndPhrases()

}

private handleCompletionSwitches() {
	completionSwitches.each { completionSwitch ->

		def isDimmer = hasSetLevelCommand(completionSwitch)

		if (completionSwitchesLevel && isDimmer) {
			completionSwitch.setLevel(completionSwitchesLevel)
		} else {
			def command = completionSwitchesState ?: "on"
			completionSwitch."${command}"()
		}
	}
}

private handleCompletionMessaging() {
	if (completionMessage) {
		if (location.contactBookEnabled) {
			sendNotificationToContacts(completionMessage, recipients)
		} else {
			if (completionPhoneNumber) {
				sendSms(completionPhoneNumber, completionMessage)
			}
			if (completionPush) {
				sendPush(completionMessage)
			}
		}
		if (completionMusicPlayer) {
			speak(completionMessage)
		}
	}
}

private handleCompletionModesAndPhrases() {

	if (completionMode) {
		setLocationMode(completionMode)
	}

	if (completionPhrase) {
		location.helloHome.execute(completionPhrase)
	}

}

def speak(message) {
	def sound = textToSpeech(message)
	def soundDuration = (sound.duration as Integer) + 2
	log.debug "Playing $sound.uri"
	completionMusicPlayer.playTrack(sound.uri)
	log.debug "Scheduled resume in $soundDuration sec"
	runIn(soundDuration, resumePlaying, [overwrite: true])
}

def resumePlaying() {
	log.trace "resumePlaying()"
	def sonos = completionMusicPlayer
	if (sonos) {
		def currentTrack = sonos.currentState("trackData").jsonValue
		if (currentTrack.status == "playing") {
			sonos.playTrack(currentTrack)
		} else {
			sonos.setTrack(currentTrack)
		}
	}
}

// ========================================================
// Helpers
// ========================================================

def setLevelsInState() {
	def startLevels = [:]
	dimmers.each { dimmer ->
		if (usesOldSettings()) {
			startLevels[dimmer.id] = defaultStart()
		} else if (hasStartLevel()) {
			startLevels[dimmer.id] = startLevel
		} else {
			def dimmerIsOff = dimmer.currentValue("switch") == "off"
			startLevels[dimmer.id] = dimmerIsOff ? 0 : dimmer.currentValue("level")
		}
	}

	atomicState.startLevels = startLevels
}

def canStartAutomatically() {

	def today = new Date().format("EEEE")
	log.debug "today: ${today}, days: ${days}"

	if (!days || days.contains(today)) {// if no days, assume every day
		return true
	}

	log.trace "should not run"
	return false
}

def completionPercentage() {
	log.trace "checkingTime"

	if (!atomicState.running) {
		return
	}

	int now = new Date().getTime()
	int diff = now - atomicState.start
	int totalRunTime = totalRunTimeMillis()
	int percentOfRunTime = (diff / totalRunTime) * 100
	log.debug "percentOfRunTime: ${percentOfRunTime}"

	percentOfRunTime
}

int totalRunTimeMillis() {
	int minutes = sanitizeInt(duration, 30)
	def seconds = minutes * 60
	def millis = seconds * 1000
	return millis as int
}

int dynamicEndLevel() {
	if (usesOldSettings()) {
		if (direction && direction == "Down") {
			return 0
		}
		return 99
	}
	return endLevel as int
}

def getHue(dimmer, level) {
	def start = atomicState.startLevels[dimmer.id] as int
	def end = dynamicEndLevel()
	if (start > end) {
		return getDownHue(level)
	} else {
		return getUpHue(level)
	}
}

def getUpHue(level) {
	getBlueHue(level)
}

def getDownHue(level) {
	getRedHue(level)
}

private getBlueHue(level) {
	if (level < 5) return 72
	if (level < 10) return 71
	if (level < 15) return 70
	if (level < 20) return 69
	if (level < 25) return 68
	if (level < 30) return 67
	if (level < 35) return 66
	if (level < 40) return 65
	if (level < 45) return 64
	if (level < 50) return 63
	if (level < 55) return 62
	if (level < 60) return 61
	if (level < 65) return 60
	if (level < 70) return 59
	if (level < 75) return 58
	if (level < 80) return 57
	if (level < 85) return 56
	if (level < 90) return 55
	if (level < 95) return 54
	if (level >= 95) return 53
}

private getRedHue(level) {
	if (level < 6) return 1
	if (level < 12) return 2
	if (level < 18) return 3
	if (level < 24) return 4
	if (level < 30) return 5
	if (level < 36) return 6
	if (level < 42) return 7
	if (level < 48) return 8
	if (level < 54) return 9
	if (level < 60) return 10
	if (level < 66) return 11
	if (level < 72) return 12
	if (level < 78) return 13
	if (level < 84) return 14
	if (level < 90) return 15
	if (level < 96) return 16
	if (level >= 96) return 17
}

private hasSetLevelCommand(device) {
	def isDimmer = false
	device.supportedCommands.each {
		if (it.name.contains("setLevel")) {
			isDimmer = true
		}
	}
	return isDimmer
}

private hasSetColorCommand(device) {
	def hasColor = false
	device.supportedCommands.each {
		if (it.name.contains("setColor")) {
			hasColor = true
		}
	}
	return hasColor
}

private dimmersWithSetColorCommand() {
	def colorDimmers = []
	dimmers.each { dimmer ->
		if (hasSetColorCommand(dimmer)) {
			colorDimmers << dimmer
		}
	}
	return colorDimmers
}

private int sanitizeInt(i, int defaultValue = 0) {
	try {
		if (!i) {
			return defaultValue
		} else {
			return i as int
		}
	}
	catch (Exception e) {
		log.debug e
		return defaultValue
	}
}

private completionDelaySeconds() {
	int completionDelayMinutes = sanitizeInt(completionDelay)
	int completionDelaySeconds = (completionDelayMinutes * 60)
	return completionDelaySeconds ?: 0
}

private stepDuration() {
	int minutes = sanitizeInt(duration, 30)
	int stepDuration = (minutes * 60) / 100
	return stepDuration ?: 1
}

private debug(message) {
	log.debug "${message}\nstate: ${state}"
}

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public humanReadableStartDate() {
	new Date().parse(smartThingsDateFormat(), startTime).format("h:mm a", timeZone(startTime))
}

def fancyString(listOfStrings) {

	def fancify = { list ->
		return list.collect {
			def label = it
			if (list.size() > 1 && it == list[-1]) {
				label = "and ${label}"
			}
			label
		}.join(", ")
	}

	return fancify(listOfStrings)
}

def fancyDeviceString(devices = []) {
	fancyString(devices.collect { deviceLabel(it) })
}

def deviceLabel(device) {
	return device.label ?: device.name
}

def schedulingHrefDescription() {

	def descriptionParts = []
	if (days) {
		descriptionParts << "On ${fancyString(days)},"
	}

	descriptionParts << "${fancyDeviceString(dimmers)} will start dimming"

	if (startTime) {
		descriptionParts << "at ${humanReadableStartDate()}"
	}

	if (modeStart) {
		if (startTime) {
			descriptionParts << "or"
		}
		descriptionParts << "when ${location.name} enters '${modeStart}' mode"
	}

	if (descriptionParts.size() <= 1) {
		// dimmers will be in the list no matter what. No rules are set if only dimmers are in the list
		return null
	}

	return descriptionParts.join(" ")
}

def completionHrefDescription() {

	def descriptionParts = []
	def example = "Switch1 will be turned on. Switch2, Switch3, and Switch4 will be dimmed to 50%. The message '<message>' will be spoken, sent as a text, and sent as a push notification. The mode will be changed to '<mode>'. The phrase '<phrase>' will be executed"

	if (completionSwitches) {
		def switchesList = []
		def dimmersList = []


		completionSwitches.each {
			def isDimmer = completionSwitchesLevel ? hasSetLevelCommand(it) : false

			if (isDimmer) {
				dimmersList << deviceLabel(it)
			}

			if (!isDimmer) {
				switchesList << deviceLabel(it)
			}
		}


		if (switchesList) {
			descriptionParts << "${fancyString(switchesList)} will be turned ${completionSwitchesState ?: 'on'}."
		}

		if (dimmersList) {
			descriptionParts << "${fancyString(dimmersList)} will be dimmed to ${completionSwitchesLevel}%."
		}

	}

	if (completionMessage && (completionPhoneNumber || completionPush || completionMusicPlayer)) {
		def messageParts = []

		if (completionMusicPlayer) {
			messageParts << "spoken"
		}
		if (completionPhoneNumber) {
			messageParts << "sent as a text"
		}
		if (completionPush) {
			messageParts << "sent as a push notification"
		}

		descriptionParts << "The message '${completionMessage}' will be ${fancyString(messageParts)}."
	}

	if (completionMode) {
		descriptionParts << "The mode will be changed to '${completionMode}'."
	}

	if (completionPhrase) {
		descriptionParts << "The phrase '${completionPhrase}' will be executed."
	}

	return descriptionParts.join(" ")
}

def numbersPageHrefDescription() {
	def title = "All dimmers will dim for ${duration ?: '30'} minutes from ${startLevelLabel()} to ${endLevelLabel()}"
    if (colorize) {
			def colorDimmers = dimmersWithSetColorCommand()
			if (colorDimmers == dimmers) {
				title += " and will gradually change color."
			} else {
				title += ".\n${fancyDeviceString(colorDimmers)} will gradually change color."
			}
    }
    return title
}

def hueSatToHex(h, s) {
	def convertedRGB = hslToRgb(h, s, 0.5)
	return rgbToHex(convertedRGB)
}

def hslToRgb(h, s, l) {
	def r, g, b;

	if (s == 0) {
		r = g = b = l; // achromatic
	} else {
		def hue2rgb = { p, q, t ->
			if (t < 0) t += 1;
			if (t > 1) t -= 1;
			if (t < 1 / 6) return p + (q - p) * 6 * t;
			if (t < 1 / 2) return q;
			if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
			return p;
		}

		def q = l < 0.5 ? l * (1 + s) : l + s - l * s;
		def p = 2 * l - q;

		r = hue2rgb(p, q, h + 1 / 3);
		g = hue2rgb(p, q, h);
		b = hue2rgb(p, q, h - 1 / 3);
	}

	return [r * 255, g * 255, b * 255];
}

def rgbToHex(red, green, blue) {
	def toHex = {
		int n = it as int;
		n = Math.max(0, Math.min(n, 255));
		def hexOptions = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"]

		def firstDecimal = ((n - n % 16) / 16) as int
		def secondDecimal = (n % 16) as int

		return "${hexOptions[firstDecimal]}${hexOptions[secondDecimal]}"
	}

	def rgbToHex = { r, g, b ->
		return toHex(r) + toHex(g) + toHex(b)
	}

	return rgbToHex(red, green, blue)
}

def usesOldSettings() {
	!hasEndLevel()
}

def hasStartLevel() {
	return (startLevel != null && startLevel != "")
}

def hasEndLevel() {
	return (endLevel != null && endLevel != "")
}
