/**
 *  Weather Station Controller
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
preferences {
	section {
		input "weatherDevices", "device.smartweatherStationTile"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()
	initialize()
}

def initialize() {
    weatherDevices.poll()
    runIn(3600, scheduledEvent, [overwrite: false])
}

def scheduledEvent() {
	log.trace "scheduledEvent()"
    runIn(3600, scheduledEvent, [overwrite: false])
	weatherDevices.poll()
    state.lastRun = new Date().toSystemFormat()
}