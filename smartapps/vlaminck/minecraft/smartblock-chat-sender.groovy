/**
 *  SmartClockChatSender
 *
 *  Author: Steve Vlaminck
 *
 *  Date: 2014-02-03
 */

definition(
    name: "SmartBlock Chat Sender",
    namespace: "vlaminck/Minecraft",
    author: "SmartThings",
    description: "Send chat messages into Minecraft via the SmartBlock mod",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page(name: "chatPage", title: "Send Notifications Into Minecraft", install: true, uninstall: true) {
		section {
			input(name: "chatsEnabled", type: "bool", title: "Enable This Notification?", defaultValue: "true")
			input(name: "modes", type: "mode", title: "Notify SmartBlock users when the mode changes to:", description: "(optional)", multiple: true, required: false)
			input(name: "username", type: "string", title: "Only send to this username", required: false, description: "(optional)")
			input(name: "customMessage", type: "string", title: "Custom message?", required: false, description: "(optional)")
		}
		section(hidden: true, hideable: true, title: "Other Options") {
			label(title: "Label this Notification", required: false)
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(location, modeChangeHandler)
}

def modeChangeHandler(evt) {
	def newMode = evt.value
	log.debug "evt: ${newMode}"
	if (modes && modes.contains(newMode))
	{
		def message = customMessage ?: "SmartThings mode has changed to: \"${newMode}\""
		chatMessageToMC(message)
	}
}

def chatMessageToMC(message) {

	def parent = app.getParent()

	def url = "${parent.getServerURL()}/chat?message=${message.encodeAsURL()}"

	if (username)
	{
		url += "&username=${username.encodeAsURL()}"
	}
	log.debug "POST to ${url}"

	httpPost(url, "foo=bar") { response ->
		content = response.data
		log.debug "response: ${content}"
	}

}
