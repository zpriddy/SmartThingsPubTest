/**
 *  Mail Arrived
 *
 *  Author: SmartThings
 */
definition(
    name: "Mail Arrived",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Send a text when mail arrives in your mailbox using SmartSense Multi on your mailbox door.  Note:  battery life may be impacted in cold climates.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/mail_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/mail_contact@2x.png"
)

preferences {
	section("When mail arrives...") {
		input "accelerationSensor", "capability.accelerationSensor", title: "Where?"
	}
	section("Text me at...") {
		input "phone1", "phone", title: "Phone number?"
	}
}

def installed() {
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def updated() {
	unsubscribe()
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def accelerationActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"

	// Don't send a continuous stream of text messages
	def deltaSeconds = 5
	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = accelerationSensor.eventsSince(timeAgo)
	log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
	def alreadySentSms = recentEvents.count { it.value && it.value == "active" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent to $phone1 within the last $deltaSeconds seconds"
	} else {
		log.debug "$accelerationSensor has moved, texting $phone1"
		sendSms(phone1, "Mail has arrived!")
	}
}