/**
 *  Unlock It When I Arrive
 *
 *  Author: SmartThings
 *  Date: 2013-02-11
 */
preferences {
	section("When I arrive..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Unlock the lock..."){
		input "lock1", "capability.lock", multiple: true
	}
}

def installed()
{
	subscribe(presence1, "presence.present", presence)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence.present", presence)
}

def presence(evt)
{
	def anyLocked = lock1.count{it.currentLock == "unlocked"} != lock1.size()
	if (anyLocked) {
		sendPush "Unlocked door due to arrival of $evt.displayName"
		lock1.unlock()
	}
}