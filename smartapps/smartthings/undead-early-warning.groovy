/**
 *  The simplest Undead Early Warning system that could possibly work. ;)
 *
 *  Author: SmartThings
 */
preferences {
	section("When the door opens...") {
		input "contacts", "capability.contactSensor", multiple: true, title: "Where could they come from?"
	}
	section("Turn on the lights!") {
		input "switches", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(contacts, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contacts, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
	log.trace "The Undead are coming! Turning on the lights: $switches"
	switches.on()
}