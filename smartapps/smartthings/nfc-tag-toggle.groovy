/**
 *  NFC Tag Toggle
 *
 *  Copyright 2014 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "NFC Tag Toggle",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Allows toggling of a switch, lock, or garage door based on an NFC Tag touch event",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor@2x.png")


preferences {
	section("Select an NFC tag") {
		input "tag", "device.nfcTag", title: "NFC Tag"
	}
    section("Select a device to control. The state of this device will be toggled each time the tag is activated, " + 
    	    "e.g. a light that's on will be turned off and one that's off will be turned on") {
            
        input "switch1", "capability.switch", title: "Light or switch", required: false
        input "lock", "capability.lock", title: "Lock", required: false
        input "garageDoor", "capability.momentary", title: "Garage door opener (or other pushbutton)", required: false
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
	subscribe tag, "nfcTouch", touchHandler
    subscribe app, touchHandler
}

def touchHandler(evt) {
	log.trace "touchHandler($evt.descriptionText)"
    if (switch1) {
    	if (switch1.currentValue("switch") == "on") {
        	switch1.off()
        }
        else {
        	switch1.on()
        }
    }
    
    if (lock) {
    	if (lock.currentValue("lock") == "locked") {
        	lock.unlock()
        }
        else {
        	lock.lock()
        }
    }
    
    if (garageDoor) {
    	garageDoor.push()
    }
}