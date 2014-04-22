/**
 *  Lock It When I Leave
 *
 *  Author: SmartThings
 *  Date: 2013-02-11
 */
preferences {
	section("When I leave...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Lock the lock...") {
		input "lock1","capability.lock", multiple: true
		input "unlock", "enum", title: "Unlock when presence is detected?", metadata: [values: ["Yes","No"]]
		input "spam", "enum", title: "Send Me Notifications?", metadata: [values: ["Yes","No"]]
	}
}

def installed()
{
	subscribe(presence1, "presence", presence)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presence)
}

def presence(evt)
{
	if (evt.value == "present") {
		if (unlock == "Yes") {
			def anyLocked = lock1.count{it.currentLock == "unlocked"} != lock1.size()
			if (anyLocked) {
				sendMessage("Doors unlocked at arrival of $evt.linkText")
			}
			lock1.unlock()
		}
	}
	else {
		def nobodyHome = presence1.find{it.currentPresence == "present"} == null
		if (nobodyHome) {
			def anyUnlocked = lock1.count{it.currentLock == "locked"} != lock1.size()
			if (anyUnlocked) {
				sendMessage("Doors locked after everyone departed")
			}
			lock1.lock()
		}
	}
}

def sendMessage(msg) {
	if (spam == "Yes") {
		sendPush msg
	}
}