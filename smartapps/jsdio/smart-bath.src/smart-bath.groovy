/**
 *  Smart Bath
 *
 *  Copyright 2020 Johnny Shen
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
    name: "Smart Bath",
    namespace: "jsdio",
    author: "Johnny Shen",
    description: "bathroom automation",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "lastBathroomMotion"
}


preferences {

    section("Sensors") {
		input "showerSensors", "capability.motionSensor", multiple: true, title: "Shower Sensor"
		input "outsideSensors", "capability.motionSensor", multiple: true, title: "Outside Sensor"
	}
    

    
	section("Lights") {
		input "initLights", "capability.switch", multiple: true, title: "Initial Lights"
	}
    
	section("Time to stay on with no motion in seconds (30s)") {
		input "noMotionThreshold", "number", title: "Number of seconds", required: false
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
    
    state.bathroom = [lastMotion: 0]
    
//    log.debug(findNoMotionThreshold())
    log.debug(findAllSensors())
 
 
    subscribe(findAllSensors(), "motion.active", motionDetectedHandler)
    subscribe(findAllSensors(), "motion.inactive", motionStoppedHandler)
    
    
    runIn(findNoMotionThreshold(), "checkMotion", [overwrite: false])
}

def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: $evt"
    state.bathroom.lastMotion = now()
    
    settings.lightsInit.on()
}

def motionStoppedHandler(evt) {
    log.debug "motionStoppedHandler called: $evt"
    
    settings.lightsInit.off()
    //runIn(settings.noMotionThreshold, checkMotion())
}



def checkMotion() {
    log.debug "In checkMotion scheduled method"

    def motionStates = findActiveStates(findAllSensors())
    
	log.debug(motionStates)
    
    
   
}


// get combined list of all sensors
private findActiveStates(args) {
	args.currentMotion.findAll { sensorVal ->
        sensorVal == "active" ? true : false
    }
//	[outsideSensors, showerSensors]
}



// get combined list of all sensors
private findAllSensors() {
	outsideSensors.plus(showerSensors)
//	[outsideSensors, showerSensors]
}

// gets the false alarm threshold, in minutes. Defaults to
// 10 minutes if the preference is not defined.
private findNoMotionThreshold() {
    // In Groovy, the return statement is implied, and not required.
    // We check to see if the variable we set in the preferences
    // is defined and non-empty, and if it is, return it.  Otherwise,
    // return our default value of 30
    (noMotionThreshold != null && noMotionThreshold != "") ? noMotionThreshold : 30
}