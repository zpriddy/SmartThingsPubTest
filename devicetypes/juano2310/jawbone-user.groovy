/**
 *  Jawbone-User
 *
 *  Author: juano23@gmail.com
 *  Date: 2013-08-15
 */
 // for the UI

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Jawbone User", namespace: "juano2310", author: "Juan Pablo Risso") {
		capability "Refresh"
		capability "Polling"
        capability "Button"
        
        attribute "sleeping", "string"
        attribute "steps", "string"
        attribute "goal", "string"        
	}

        simulator {
                status "sleeping": "sleeping: 1"
                status "not sleeping": "sleeping: 0"
        }

        tiles {
                standardTile("sleeping", "device.sleeping", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
                        state("sleeping", label: "Sleeping", icon:"st.Bedroom.bedroom12", backgroundColor:"#ffffff")
                        state("not sleeping", label: "Awake", icon:"st.Health & Wellness.health12", backgroundColor:"#79b821")
                }
                standardTile("steps", "device.steps", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false) {
                        state("steps", label: '${currentValue} Steps', icon:"st.Health & Wellness.health11", backgroundColor:"#ffffff")                     
				}
                standardTile("goal", "device.goal", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false,  decoration: "flat") {
                        state("goal", label: '${currentValue} Steps', icon:"st.Health & Wellness.health5", backgroundColor:"#ffffff")
                }                
                standardTile("refresh", "device.sleeping", inactiveLabel: false, decoration: "flat") {
						state "default", action:"polling.poll", icon:"st.secondary.refresh"
				}
                main "steps"
				details(["steps", "goal", "sleeping", "refresh"])
        }
}

def generateSleepingEvent(boolean sleeping) {
        log.debug "Here in generateSleepingEvent!"
        def value = formatValue(sleeping)
        def linkText = getLinkText(device)
        def descriptionText = formatDescriptionText(linkText, sleeping)
        def handlerName = getState(sleeping)

        def results = [
                name: "sleeping",
                value: value,
                unit: null,
                linkText: linkText,
                descriptionText: descriptionText,
                handlerName: handlerName
        ]

        sendEvent (results)
        
        log.debug "Generating Sleep Event: ${results}"        
        
        
        def results2 = [
                name: "button",
                value: "held",
                unit: null,
                linkText: linkText,
                descriptionText: "${linkText} button was pressed",
                handlerName: "buttonHandler",
                data: [buttonNumber: 1],
                isStateChange: true
        ] 
        
       
        log.debug "Generating Button Event: ${results2}"
        
        sendEvent (results2)
}


def poll() {
	log.debug "Executing 'poll'"
	def results = parent.pollChild(this)
	return null
}

def setMemberId (String memberId) {
   log.debug "MemberId = ${memberId}"
   state.jawboneMemberId = memberId
}

def getMemberId () {
    log.debug "MemberId = ${state.jawboneMemberId}"
    return(state.jawboneMemberId)
}

private String formatValue(boolean sleeping) {
        if (sleeping)
            return "sleeping"
        else
            return "not sleeping"
}

private formatDescriptionText(String linkText, boolean sleeping) {
        if (sleeping)
            return "$linkText is sleeping"
        else
            return "$linkText is not sleeping"
}

private getState(boolean sleeping) {
        if (sleeping)
            return "sleeping"
        else
            return "not sleeping"
}