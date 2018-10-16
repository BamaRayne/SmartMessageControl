/* 
* Smart Message Control Zones
*
*
* 
*  Copyright 2018 Jason Headley
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
/**********************************************************************************************************************************************/
import org.apache.log4j.Logger
import org.apache.log4j.Level
definition(
	name			: "Smart Message Control Zone",
    namespace		: "Echo",
    author			: "bamarayne",
	description		: "Smart Message Controls - Only send messages where they will be heard",
	category		: "My Apps",
    parent			: "Echo:Smart Message Control",
	iconUrl			: "https://raw.githubusercontent.com/jasonrwise77/My-SmartThings/master/LogicRulz%20Icons/LogicRulz.png",
	iconX2Url		: "https://raw.githubusercontent.com/jasonrwise77/My-SmartThings/master/LogicRulz%20Icons/LogicRulz2x.png",
	iconX3Url		: "https://raw.githubusercontent.com/jasonrwise77/My-SmartThings/master/LogicRulz%20Icons/LogicRulz2x.png")
/**********************************************************************************************************************************************/
private def version() { 
    	def text = "Smart Message Control Ver 1.0 / R.0.0.1, Release date: in development, Initial App Release Date: not released" 
	}

preferences {

    page name: "mainProfilePage"
    page name: "speakers"
    page name: "conditions"
    page name: "messages"
    page name: "restrictions"
    page name: "certainTime"
   
}

/******************************************************************************
	MAIN PROFILE PAGE
******************************************************************************/
def mainProfilePage() {	
    dynamicPage(name: "mainProfilePage", title:"", install: true, uninstall: installed) {
        section ("Details and Status") {
            label title: "Name this Room", required:true
            }
        section ("Pause Announcements") {
        	input "rPause", "bool", title: "Enable Announcements to this room", required: false, submitOnChange: true
            }
        if (rPause == null) {
        	section ("") {
            paragraph "This Room has not been configured, please activate to continue."
           	}
        }    
        if (rPause == false) {
        section(""){
        	paragraph "This Rooms activity has been paused and will not receive any messages."
            }
        }    
        else if(rPause == true) {    
    	    section ("${app.label}" + "'s Configuration") {
                href "speakers", title: "Select speaker in this room"//, description: pTriggerComplete(), state: pTriggerSettings()
                href "conditions", title: "Configure activity conditions"//, description: pConditionComplete(), state: pConditionSettings()
                href "messages", title: "Configure messages delivery"//, description: pMessageComplete(), state: pMessageSettings()
                href "restrictions", title: "Set restrictions for this room"//, description: pMessageComplete(), state: pMessageSettings()
            }
        }    
    }
}
 
/******************************************************************************
	SPEAKERS SELECTIONS
******************************************************************************/
page name: "speakers"
def speakers() {
	dynamicPage(name: "speakers", title: "Select the speakers for this zone",install: false, uninstall: false) {
        section ("Audio Output Devices"){
        	input "synthDevice", "capability.speechSynthesis", title: "Speech Synthesis Devices", multiple: true, required: false
        	}
        section ("") {
        	input "echoDevice", "capability.notification", title: "Amazon Alexa Devices", multiple: true, required: false
            }
        section ("") {
            input "sonosDevice", "capability.musicPlayer", title: "Music Player Devices", required: false, multiple: true, submitOnChange: true    
            if (sonosDevice) {
                input "volume", "number", title: "Temporarily change volume", description: "0-100% (default value = 30%)", required: false
            	}
            }
        section ("Text Messages" ) {
            input "sendText", "bool", title: "Enable Text Notifications", required: false, submitOnChange: true     
            if (sendText){      
                paragraph "You may enter multiple phone numbers separated by comma to deliver the Alexa message. E.g. +18045551122,+18046663344"
                input name: "sms", title: "Send text notification to (optional):", type: "phone", required: false
            	}
            }
        section ("Push Messages") {
            input "push", "bool", title: "Send Push Notification (optional)", required: false, defaultValue: false
        	}
        }
	}    


/******************************************************************************
	CONDITIONS SELECTION PAGE
******************************************************************************/
page name: "conditions"
def conditions() {
    dynamicPage(name: "conditions", title: "Execute this routine when...",install: false, uninstall: false) {
        section ("Location Settings Conditions") {
            input "cMode", "mode", title: "Location Mode is...", multiple: true, required: false, submitOnChange: true
        	input "cSHM", "enum", title: "Smart Home Monitor is...", options:["away":"Armed (Away)","stay":"Armed (Home)","off":"Disarmed"], multiple: false, required: false, submitOnChange: true
            input "cDays", title: "Days of the week", multiple: true, required: false, submitOnChange: true,
                "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            href "certainTime", title: "Time Schedule", description: pTimeComplete(), state: pTimeSettings()
        }         
        section ("Switch and Dimmer Conditions") {
            input "cSwitch", "capability.switch", title: "Switches", multiple: true, submitOnChange: true, required:false
            if (cSwitch) {
                input "cSwitchCmd", "enum", title: "are...", options:["on":"On","off":"Off"], multiple: false, required: true, submitOnChange: true
                if (cSwitch?.size() > 1) {
                    input "cSwitchAll", "bool", title: "Activate this toggle if you want ALL of the switches to be $tSwitchCmd as a condition.", required: false, defaultValue: false, submitOnChange: true
                	}
            }
            input "cDim", "capability.switchLevel", title: "Dimmers", multiple: true, submitOnChange: true, required: false
            if (cDim) {
                input "cDimCmd", "enum", title: "is...", options:["greater":"greater than","lessThan":"less than","equal":"equal to"], multiple: false, required: false, submitOnChange: true
                if (cDimCmd == "greater" ||cDimCmd == "lessThan" || cDimCmd == "equal") {
                    input "cDimLvl", "number", title: "...this level", range: "0..100", multiple: false, required: false, submitOnChange: true
                if (cDim.size() > 1) {
                    input "cDimAll", "bool", title: "Activate this toggle if you want ALL of the dimmers for this condition.", required: false, defaultValue: false, submitOnChange: true
                	}
                }
            }
        }
        section ("Motion and Presence Conditions") {
            input "cMotion", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false, submitOnChange: true
            if (cMotion) {
                input "cMotionCmd", "enum", title: "are...", options: ["active":"active", "inactive":"inactive"], multiple: false, required: true, submitOnChange: true
            	if (cMotion?.size() > 1) {
                	input "cMotionAll", "bool", title: "Activate this toggle if you want ALL of the Motion Sensors to be $cMotionCmd as a condition."
                    }
                }
        	input "cPresence", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false, submitOnChange: true
            if (cPresence) {
                input "cPresenceCmd", "enum", title: "are...", options: ["present":"Present","not present":"Not Present"], multiple: false, required: true, submitOnChange: true
                if (cPresence?.size() > 1) {
                    input "cPresenceAll", "bool", title: "Activate this toggle if you want ALL of the Presence Sensors to be $cPresenceCmd as a condition.", required: false, defaultValue: false, submitOnChange: true
                	}
                }
            }
        section ("Door, Window, and other Contact Sensor Conditions") {
            input "cContactDoor", "capability.contactSensor", title: "Contact Sensors only on Doors", multiple: true, required: false, submitOnChange: true
            	if (cContactDoor) {
                input "cContactDoorCmd", "enum", title: "that are...", options: ["open":"open", "closed":"closed"], multiple: false, required: true, submitOnChange: true
            	if (cContactDoor?.size() > 1) {
                	input "cContactDoorAll", "bool", title: "Activate this toggle if you want ALL of the Doors to be $cContactDoorCmd as a condition.", required: false, defaultValue: false, submitOnChange: true
                    }
            	}
            input "cContactWindow", "capability.contactSensor", title: "Contact Sensors only on Windows", multiple: true, required: false, submitOnChange: true
            	if (cContactWindow) {
                input "cContactWindowCmd", "enum", title: "that are...", options: ["open":"open", "closed":"closed"], multiple: false, required: true, submitOnChange: true
            	if (cContactWindow?.size() > 1) {
                	input "cContactWindowAll", "bool", title: "Activate this toggle if you want ALL of the Doors to be $cContactWindowCmd as a condition.", required: false, defaultValue: false, submitOnChange: true
                    }
            	}
            input "cContact", "capability.contactSensor", title: "All Other Contact Sensors", multiple: true, required: false, submitOnChange: true
            	if (cContact) {
                input "cContactCmd", "enum", title: "that are...", options: ["open":"open", "closed":"closed"], multiple: false, required: true, submitOnChange: true
            	if (cContact?.size() > 1) {
                	input "cContactAll", "bool", title: "Activate this toggle if you want ALL of the Doors to be $cContactCmd as a condition.", required: false, defaultValue: false, submitOnChange: true
                    }
            	}
            }
		section ("Garage Door and Lock Conditions"){
            input "cLocks", "capability.lock", title: "Smart Locks", multiple: true, required: false, submitOnChange: true
            if (cLocks) {
                input "cLocksCmd", "enum", title: "are...", options:["locked":"locked", "unlocked":"unlocked"], multiple: false, required: true, submitOnChange:true
            }
            input "cGarage", "capability.garageDoorControl", title: "Garage Doors", multiple: true, required: false, submitOnChange: true
            if (cGarage) {
                input "cGarageCmd", "enum", title: "are...", options:["open":"open", "closed":"closed"], multiple: false, required: true, submitOnChange: true
        	}
        }
        section ("Environmental Conditions") {
        	input "cHumidity", "capability.relativeHumidityMeasurement", title: "Relative Humidity", required: false, submitOnChange: true
            	if (cHumidity) input "cHumidityLevel", "enum", title: "Only when the Humidity is...", options: ["above", "below"], required: false, submitOnChange: true            
            	if (cHumidityLevel) input "cHumidityPercent", "number", title: "this level...", required: true, description: "percent", submitOnChange: true            
                if (cHumidityPercent) input "cHumidityStop", "number", title: "...but not ${cHumidityLevel} this percentage", required: false, description: "humidity"
            input "cTemperature", "capability.temperatureMeasurement", title: "Temperature", required: false, multiple: true, submitOnChange: true
				if (cTemperature) input "cTemperatureLevel", "enum", title: "When the temperature is...", options: ["above", "below"], required: false, submitOnChange: true
                if (cTemperatureLevel) input "cTemperatureDegrees", "number", title: "Temperature...", required: true, description: "degrees", submitOnChange: true
                if (cTemperatureDegrees) input "cTemperatureStop", "number", title: "...but not ${cTemperatureLevel} this temperature", required: false, description: "degrees"
		}
    }
} 


/******************************************************************************************************
	MESSAGES CONFIGURATION
******************************************************************************************************/
page name: "messages"
def messages(){
    dynamicPage(name: "messages", title: "Audio and Text Message Settings", uninstall: false){
		section ("Message Variables Devices", hideable: true, hidden: false) {
            input "vTemperature", "capability.temperatureMeasurement", title: "Temperature", required: false, multiple: true, submitOnChange: true
        	input "vFeelsLike", "capability.relativeHumidityMeasurement", title: "How it feels", required: false, multiple: true, submitOnChange: true
            input "vWind", "capability.sensor", title: "Wind Speed, Direction, Gusts", multiple: true, required: false, submitOnChange: true
            input "vRain", "capability.sensor", title: "Rain Accumulation", multiple: true, required: false, submitOnChange: true
            input "vHumidity", "capability.relativeHumidityMeasurement", title: "Relative Humidity", required: false, submitOnChange: true
            input "vLux", "capability.illuminanceMeasurement", title: "Lux Level", required: false, submitOnChange: true
           	input "vWater", "capability.waterSensor", title: "Water/Moisture Sensors", required: false, multiple: true, submitOnChange: true
            input "vSmoke", "capability.smokeDetector", title: "Smoke Detectors", required: false, multiple: true, submitOnChange: true
            input "vCO2", "capability.carbonDioxideMeasurement", title: "Carbon Dioxide (CO2)", required: false, multiple: true, submitOnChange: true
			}        	
		section ("Custom Message"){
        	input "message", "text", title: "Send this message when this Logic Block is executed", required: false, submitOnChange: true //, defaultValue: "Attention, the Logic Block, $app.label, has executed"
            }
            if(message) {
				def report
               	section ("Preview Message", hideable: true, hidden: true) {
                	paragraph report = runProfile(message, evt) 
				}
            }
        section ("Tap here to see available &variables", hideable: true, hidden: true) {    
                    paragraph 	"CUSTOM MESSAGES: \n"+
                    				"Devices: \n"+
                                    "&device, &action \n"+
                                    "\n"+
                                    "LOCATION: \n"+
                                    "&time, &date, &stRoutAction, &stRoutEvent \n"+ 
                                    "\n"+
                                    "Temperature Variables: \n"+
                                    "&temperature, &high, &low, &tempTrend, &feelsLike \n"+
                                    "\n"+
                                    "Sensors Variables: \n"+
                                    "&smoke, &CO2, &water \n"+
                                    "\n"+
                                    "Climate Variables: \n"+
                                    "&windSpeed, &windDir, &rain, &humidity \n"+
                                    "\n"
					                }
    
        section ("Messaging Restrictions"){
            if (speakDisable==true) {
            href "smsCertainTime", title: "SMS/Push/Audio Message only during this time schedule", description: pmTimeComplete(), state: pmTimeSettings()
            input "smsDays", title: "...or, only on these days of the week", multiple: true, required: false, submitOnChange: true,
                "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        	}
        }   
    }                 
}

/******************************************************************************************************
	RESTRICTIONS TIME SELECTION PAGE FOR MESSAGING SECTION ONLY
******************************************************************************************************/
page name: "smsCertainTime"
def smsCertainTime() {
    dynamicPage(name:"smsCertainTime",title: "Only during this time schedule...", uninstall: false) {
        section("Beginning at....") {
            input "startingXm", "enum", title: "Starting at...", options: ["A specific time", "Sunrise", "Sunset"], required: false , submitOnChange: true
            if(startingXm in [null, "A specific time"]) input "startingm", "time", title: "Start time", required: false, submitOnChange: true
            else {
                if(startingXm == "Sunrise") input "startSunriseOffsetm", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                else if(startingXm == "Sunset") input "startSunsetOffsetm", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
            }
        }
        section("Ending at....") {
            input "endingXm", "enum", title: "Ending at...", options: ["A specific time", "Sunrise", "Sunset"], required: false, submitOnChange: true
            if(endingXm in [null, "A specific time"]) input "endingm", "time", title: "End time", required: false, submitOnChange: true
            else {
                if(endingXm == "Sunrise") input "endSunriseOffsetm", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                else if(endingXm == "Sunset") input "endSunsetOffsetm", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
        	}
        }
    }
}

/******************************************************************************************************
	RESTRICTIONS - MAIN APP TIME 
******************************************************************************************************/
page name: "certainTime"
def certainTime() {
    dynamicPage(name:"certainTime",title: "", uninstall: false) {
        section("") {
            input "startingX", "enum", title: "Starting at...", options: ["A specific time", "Sunrise", "Sunset"], required: false , submitOnChange: true
            if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false, submitOnChange: true
            else {
                if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                    }
        }
        section("") {
            input "endingX", "enum", title: "Ending at...", options: ["A specific time", "Sunrise", "Sunset"], required: false, submitOnChange: true
            if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false, submitOnChange: true
            else {
                if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                    }
        }
    }
}

    
/************************************************************************************************************
	Base Process
************************************************************************************************************/
def installed() {
	log.debug "Installed with settings: ${settings}"
    state?.isInstalled = true
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
	log.info "The parent pause toggle is $parent.aPause and this routines pause toggle is $rPause"
    if (rPause == false) { unschedule() }
    if (rPause == true) {
    
    // Misc Variables
    state.tSelfHandlerEvt = null
    state.speakers = false

	//ONCE A DAY CHECK VARIABLES
    state.cycleTh = false //temperature
    state.cycleTl = false
    state.cycleHh = false //humidity
    state.cycleHl = false    
    state.cycleWh = false //wind
    state.cycleWl = false    
    state.cycleLl = false //lux
    state.cycleLh = false
    state.cycleCO2h = false //CO2
    state.cycleCO2l = false    
	state.rainStart = true //rain
    state.rainStop = false

    //LOCATION & SCHEDULING
	
    subscribe(location, "alarmSystemStatus",shmModeChange)
    subscribe(location, "House Fan Controller", ttsActions)
    if (tMode) 									subscribe (location, processModeChange) 
    if (tRoutine)								subscribe(location, "routineExecuted", routineHandler)
    if (frequency) 								cronHandler(frequency)
    if (txFutureTime) 							oneTimeHandler()
    if (mySunState == "Sunset") {
    subscribe(location, "sunsetTime", sunsetTimeHandler)
	sunsetTimeHandler(location.currentValue("sunsetTime"))
    }
    if (mySunState == "Sunrise") {
	subscribe(location, "sunriseTime", sunriseTimeHandler)
	sunriseTimeHandler(location.currentValue("sunriseTime"))
    }
}

}
/***********************************************************************************************************
   CONDITIONS HANDLER
************************************************************************************************************/
def conditionHandler(evt) {
    log.info "Checking that all conditions are ok."
    def result
    def cSwitchOk = false
    def cDimOk = false
    def cHumOk = false
    def cTempOk = false
    def cSHMOk = false
    def cModeOk = false
    def cMotionOk = false
    def cPresenceOk = false
    def cDoorOk = false
    def cWindowOk = false
    def cContactOk = false
    def cDaysOk = false
    def cPendAll = false
    def timeOk = false
    def cGarageOk = false
    def cLocksOk = false
    def devList = []

	// COMMUNICATIONS PAUSED
    if (rPause == false || rPause == null) {
    	log.warn "The communications to the room, $app.label, have been paused by the user."
        state.speakers = false
        return
        }
    if (rPause == true) {

	// SWITCHES
    if (cSwitch == null) { cSwitchOk = true }
    if (cSwitch) {
    log.trace "Conditions: Switches events method activated"
        def cSwitchSize = cSwitch?.size()
        cSwitch.each { deviceName ->
            def status = deviceName.currentValue("switch")
            if (status == "${cSwitchCmd}"){ 
                String device  = (String) deviceName
                devList += device
            }
        }
        def devListSize = devList?.size()
        if(!cSwitchAll) {
            if (devList?.size() > 0) { 
                cSwitchOk = true  
            }
        }        
        if(cSwitchAll) {
            if (devListSize == cSwitchSize) { 
                cSwitchOk = true 
            }
        }
        if (cSwitchOk == false) log.warn "Switches Conditions Handler failed"
    }

    // HUMIDITY
    if (cHumidity == null) {cHumOk = true }
    if (cHumidity) {
    log.trace "Conditions: Humidity events method activated"
        int cHumidityStopVal = cHumidityStop == null ? 0 : cHumidityStop as int
            cHumidity.each { deviceName ->
                def status = deviceName.currentValue("humidity")
                if (cHumidityLevel == "above") {
                    cHumidityStopVal = cHumidityStopVal == 0 ? 999 :  cHumidityStopVal as int
                        if (status >= cHumidityPercent && status <= cHumidityStopVal) {
                            cHumOk = true
                        }
                }
                if (cHumidityLevel == "below") {
                    if (status <= cHumidityPercent && status >= cHumidityStopVal) {
                        cHumOk = true
                    }
                }    
            }
            if (cHumOk == false) log.warn "Humidity Conditions Handler failed"
    }

    // TEMPERATURE
    if (cTemperature == null) {cTempOk = true }
    if (cTemperature) {
    log.trace "Conditions: Temperature events method activated"
        int cTemperatureStopVal = cTemperatureStop == null ? 0 : cTemperatureStop as int
            cTemperature.each { deviceName ->
                def status = deviceName.currentValue("temperature")
                if (cTemperatureLevel == "above") {
                    cTemperatureStopVal = cTemperatureStopVal == 0 ? 999 :  cTemperatureStopVal as int
                        if (status >= cTemperatureDegrees && status <= cTemperatureStopVal) {
                            cTempOk = true
                        }
                }
                if (cTemperatureLevel == "below") {
                    if (status <= cTemperatureDegrees && status >= cTemperatureStopVal) {
                        cTempOk = true
                    }
                }    
            }
            if (cTempOk == false) log.warn "Temperature Conditions Handler failed"
    }	

    // DIMMERS
    if (cDim == null) { cDimOk = true }
    if (cDim) {
    log.trace "Conditions: Dimmers events method activated"
        cDim.each {deviceD ->
            def currLevel = deviceD.latestValue("level")
            if (cDimCmd == "greater") {
                if ("${currLevel}" > "${cDimLvl}") { 
                    def cDimSize = cDim?.size()
                    cDim.each { deviceName ->
                        def status = deviceName.currentValue("level")
                        if (status > cDimLvl){ 
                            String device  = (String) deviceName
                            devList += device
                        }
                    }
                }        
            }
            if (cDimCmd == "lessThan") {
                if ("${currLevel}" < "${cDimLvl}") { 
                    def cDimSize = cDim?.size()
                    cDim.each { deviceName ->
                        def status = deviceName.currentValue("level")
                        if (status < cDimLvl){ 
                            String device  = (String) deviceName
                            devList += device
                        }
                    }
                }        
            }
            if (cDimCmd == "equal") {
                if ("${currLevel}" == "${cDimLvl}") { 
                    def cDimSize = cDim?.size()
                    cDim.each { deviceName ->
                        def status = deviceName.currentValue("level")
                        if (status == cDimLvl){ 
                            String device  = (String) deviceName
                            devList += device
                        }
                    }
                }        
            }
            def devListSize = devList?.size()
            if(!cDimAll) {
                if (devList?.size() > 0) { 
                    cDimOk = true  
                }
            }        
            if(cDimAll) {
                if (devListSize == cDimSize) { 
                    cDimOk = true 
                }
            }
        }
        if (cDimOk == false) log.warn "Dimmers Conditions Handler failed"
    }

    // DAYS OF THE WEEK
    if (cDays == null) { cDaysOk = true }
    if (cDays) {
    	log.trace "Conditions: Days of the Week events method activated"
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        }
        def day = df.format(new Date())
        if (cDaysOk == false) log.warn "Days Conditions Handler failed"
        result = cDays.contains(day)
    }

    // SMART HOME MONITOR
    if (cSHM == null) { cSHMOk = true }
    if (cSHM) {
    	log.trace "Conditions: SHM events method activated"
        def currentSHM = location.currentState("alarmSystemStatus")?.value
        if (cSHM == currentSHM) {
            cSHMOk = true
        }
        if (cSHMOk == false) log.warn "SHM Conditions Handler failed"
    }    

    // LOCATION MODE
    if (cMode == null) { cModeOk = true }
    if (cMode) {
    log.trace "Conditions: Mode events method activated"
        cModeOk = !cMode || cMode?.contains(location.mode)
    	if (cModeOk == false) log.warn "Mode Conditions Handler failed"
    }

    // MOTION
    if (cMotion == null) { cMotionOk = true }
    if (cMotion) {
    log.trace "Conditions: Motion events method activated"
        def cMotionSize = cMotion?.size()
        cMotion.each { deviceName ->
            def status = deviceName.currentValue("motion")
            if (status == "${cMotionCmd}"){ 
                String device  = (String) deviceName
                devList += device
             }   
        }
        def devListSize = devList?.size()
        if(!cMotionAll) {
            if (devList?.size() > 0) { 
                cMotionOk = true  
            }
        }        
        if(cMotionAll) {
            if (devListSize == cMotionSize) { 
                cMotionOk = true 
            }
        }
        if (cMotionOk == false) log.warn "Motion Conditions Handler failed"
    }

    // PRESENCE
    if (cPresence == null) { cPresenceOk = true }
    if (cPresence) {
    log.trace "Conditions: Presence events method activated"
        def cPresenceSize = cPresence.size()
        cPresence.each { deviceName ->
            def status = deviceName.currentValue("presence")
            if (status == cPresenceCmd){ 
                String device  = (String) deviceName
                devList += device
            }
        }
        def devListSize = devList?.size()
        if(!cPresenceAll) {
            if (devList?.size() > 0) { 
                cPresenceOk = true  
            }
        }        
        if(cPresenceAll) {
            if (devListSize == cPresenceSize) { 
                cPresenceOk = true 
            }
        }
        if (cPresenceOk == false) log.warn "Presence Conditions Handler failed"
    }

    // CONTACT SENSORS
    if (cContact == null) { cContactOk = true }
    if (cContact) {
    log.trace "Conditions: Contacts events method activated"
        def cContactSize = cContact?.size()
        cContact.each { deviceName ->
            def status = deviceName.currentValue("contact")
            if (status == "${cContactCmd}"){ 
                String device  = (String) deviceName
                devList += device
            }
        }
        def devListSize = devList?.size()
        if(!cContactAll) {
            if (devList?.size() > 0) { 
                cContactOk = true  
            }
        }        
        if(cContactAll) {
            if (devListSize == cContactSize) { 
                cContactOk = true 
            }
        }
        if (cContactOk == false) log.warn "Contacts Conditions Handler failed"
    }

    // DOOR CONTACT SENSORS
    if (cContactDoor == null) { cDoorOk = true }
    if (cContactDoor) {
    log.trace "Conditions: Door Contacts events method activated"
        def cContactDoorSize = cContactDoor?.size()
        cContactDoor.each { deviceName ->
            def status = deviceName.currentValue("contact")
            if (status == "${cContactDoorCmd}"){ 
                String device  = (String) deviceName
                devList += device
            }
        }
        def devListSize = devList?.size()
        if(!cContactDoorAll) {
            if (devList?.size() > 0) { 
                cDoorOk = true  
            }
        }        
        if(cContactDoorAll) {
            if (devListSize == cContactDoorSize) { 
                cDoorOk = true 
            }
        }
        if (cDoorOk == false) log.warn "Door Contacts Conditions Handler failed"
    }

    // WINDOW CONTACT SENSORS
    if (cContactWindow == null) { cWindowOk = true }
    if (cContactWindow) {
    log.trace "Conditions: Window Contacts events method activated"
        def cContactWindowSize = cContactWindow?.size()
        cContactWindow.each { deviceName ->
            def status = deviceName.currentValue("contact")
            if (status == cContactWindowCmd){ 
                String device  = (String) deviceName
                devList += device
            }
        }
        def devListSize = devList?.size()
        if(!cContactWindowAll) {
            if (devList?.size() > 0) { 
                cWindowOk = true  
            }
        }        
        if(cContactWindowAll) {
            if (devListSize == cContactWindowSize) { 
                cWindowOk = true 
            }
        }
        if (cWindowOk == false) log.warn "Window Contacts Conditions Handler failed"
    }

    // GARAGE DOORS
    if (cGarage == null) { cGarageOk = true }
    if (cGarage) {
    log.trace "Conditions: Garage Doors events method activated"
        cGarage.each { deviceName ->
            def status = deviceName.currentValue("door")
            if (status == "${cGarageCmd}"){
            cGarageOk = true
            }
            if (cGarageOk == false) log.warn "Garage Conditions Handler failed"
        }
    }    
    // LOCKS
    if (cLocks == null) { cLocksOk = true }
    if (cLocks) {
    log.trace "Conditions: Locks events method activated"
        cLocks.each { deviceName ->
            def status = deviceName.currentValue("lock")
            if (status == "${cLocksCmd}"){
            cLocksOk = true
            }
            if (cLocksOk == false) log.warn "Locks Conditions Handler failed"
        }
    }    


    if (cLocksOk==true && cGarageOk==true && cTempOk==true && cHumOk==true && cSHMOk==true && cDimOk==true && cSwitchOk==true && cModeOk==true && 
    	cMotionOk==true && cPresenceOk==true && cDoorOk==true && cWindowOk==true && cContactOk==true && cDaysOk==true) { // && getTimeOk(evt)==true)
        result = true
    }
    if (result == true) {
    	state.speakers = true
        log.warn "Conditions Handler ==> All Conditions have been met"
   		return ["result":result]
        }
     if (result == false) { state.speakers = false  
        log.warn "Conditions Handler ==>  \n" +
        "*************************************************************************** \n" +
        "**** cLocksOk=$cLocksOk, cGarageOk=$cGarageOk, cTempOk=$cTempOk 		 \n" +
        "**** cHumOk=$cHumOk, SHM=$cSHMOk, cDim=$cDimOk, cSwitchOk=$cSwitchOk 	 \n" + 
        "**** cModeOk=$cModeOk, cMotionOk=$cMotionOk, cPresenceOk=$cPresenceOk 	 \n" +
        "**** cDoorOk=$cDoorOk,	cWindowOk=$cWindowOk, cContactOk=$cContactOk 	 \n" +
        "**** cDaysOk=$cDaysOk, getTimeOk=" + getTimeOk(evt) +					 "\n" +
        "***************************************************************************"
    return ["result":result]
	}
}    
}

/******************************************************************************************************
SPEECH AND TEXT ACTION
******************************************************************************************************/
def ttsActions(evt) {
	log.info "TTS Actions Handler activated with this message --> $evt.descriptionText"
    conditionHandler(text) 
    if (state.speakers == true) {
        def tts = evt.descriptionText
            if (echoDevice) {
				settings?.echoDevice?.each { spk->
     					spk?.speak(tts)
					}            	
            	}
            if (synthDevice) {
                synthDevice?.speak(tts) 
            }
            if (sonosDevice){ 
                def currVolLevel = sonosDevice.latestValue("level")
                def currMuteOn = sonosDevice.latestValue("mute").contains("muted")
                if (currMuteOn) { 
                    sonosDevice.unmute()
                }
                def sVolume = settings.volume ?: 20
                sonosDevice?.playTrackAndResume(state.sound.uri, state.sound.duration, sVolume)
            }
        if(recipients || sms){				
            sendtxt(tts)
        }
        if (push) {
            sendPushMessage(tts)
        }	
        state.lastMessage = tts
        return
    }
    state.speakers = false
}

/***********************************************************************************************************************
	SMS HANDLER
***********************************************************************************************************************/
private void sendtxt(tts) {
    if (parent.debug) log.info "Send Text method activated."
    if (sendContactText) { 
        sendNotificationToContacts(tts, recipients)
        if (push || shmNotification) { 
            sendPushMessage
        }
    } 
    if (notify) {
        sendNotificationEvent(tts)
    }
    if (sms) {
        sendText(sms, tts)
    }
    if (psms) {
        processpsms(psms, tts)
    }
}

private void sendText(number, tts) {
    if (sms) {
        def phones = sms.split("\\,")
        for (phone in phones) {
            sendSms(phone, tts)
        }
    }
}
private void processpsms(psms, tts) {
    if (psms) {
        def phones = psms.split("\\,")
        for (phone in phones) {
            sendSms(phone, tts)
        }
    }
}

/******************************************************************************************************
MODE CHANGE HANDLER FOR TRIGGER 
******************************************************************************************************/
def processModeChange(evt){
    if (parent.debug) log.info "Mode Change Execution Event method is activated"
    def M = "${[evt.value]}"
    def S = "${tMode}"
    if (M == S) {
        processActions(evt)
    }
}  

/***********************************************************************************************************
   SMART HOME MONITOR SETTINGS HANDLER - good
************************************************************************************************************/
def shmModeChange(evt){
    if (parent.debug) log.info "shmModeChange method activated."
    if ("$evt.value" == tSHM) {
        processActions(evt) 
    }
}


/***********************************************************************************************************************
    CRON HANDLER - good
***********************************************************************************************************************/
def cronHandler(var) {
    if (parent.debug) log.info "cron handler method activated"
    def result
    if(var == "Minutes") {
        //	0 0/3 * 1/1 * ? *
        if(xMinutes) { result = "0 0/${xMinutes} * 1/1 * ? *"
                      schedule(result, "processActions")
                     }
        else log.error " unable to schedule your reminder due to missing required variables"
    }
    if(var == "Hourly") {
        //	0 0 0/6 1/1 * ? *
        if(xHours) { 
            result = "0 0 0/${xHours} 1/1 * ? *"
            schedule(result, "processActions")
        }
        else log.error " unable to schedule your reminder due to missing required variables"
    }
    if(var == "Daily") {
        // 0 0 1 1/7 * ? *
        def hrmn = hhmm(xDaysStarting, "HH:mm")
        def hr = hrmn[0..1] 
        def mn = hrmn[3..4]
        if(xDays && xDaysStarting) {
            result = "0 $mn $hr 1/${xDays} * ? *"
            schedule(result, "processActions")
        }
        else if(xDaysWeekDay && xDaysStarting) {
            //0 13 2 ? * MON-FRI *
            result = "0 $mn $hr ? * MON-FRI *"
            schedule(result, "processActions")
        }
        else log.error " unable to schedule your reminder due to missing required variables"
    }
    if(var == "Weekly") {
        // 	0 0 2 ? * TUE,SUN *
        def hrmn = hhmm(xWeeksStarting, "HH:mm")
        def hr = hrmn[0..1]
        def mn = hrmn[3..4]
        def weekDaysList = [] 
        xWeeks?.each {weekDaysList << it }
        def weekDays = weekDaysList.join(",")
        if(xWeeks && xWeeksStarting) { result = "0 $mn $hr ? * ${weekDays} *" }
        else log.error " unable to schedule your reminder due to missing required variables"
        schedule(result, "processActions")
    }
    if(var == "Monthly") { 
        // 0 30 5 6 1/2 ? *
        def hrmn = hhmm(xMonthsStarting, "HH:mm")
        def hr = hrmn[0..1]
        def mn = hrmn[3..4]
        if(xMonths && xMonthsDay) { result = "0 $mn $hr ${xMonthsDay} 1/${xMonths} ? *"}
        else log.error "unable to schedule your reminder due to missing required variables"
        schedule(result, "processActions")
    }
    if(var == "Yearly") {
        //0 0 4 1 4 ? *
        def hrmn = hhmm(xYearsStarting, "HH:mm")
        def hr = hrmn[0..1]
        def mn = hrmn[3..4]           
        if(xYears) {result = "0 $mn $hr ${xYearsDay} ${xYears} ? *"}
        else log.error "unable to schedule your reminder due to missing required variables"
        schedule(result, "processActions")
    }
    log.info "scheduled $var recurring event" 
}

/***********************************************************************************************************************
    ONE TIME SCHEDULING HANDLER - good
***********************************************************************************************************************/
def oneTimeHandler(var) {
	if (parent.debug) log.info "One Time Scheduling Handler activated"
    def result
    def todayYear = new Date(now()).format("yyyy")
    def todayMonth = new Date(now()).format("MM")
    def todayDay = new Date(now()).format("dd")
    def yyyy = txFutureYear ?: todayYear
    def MM = txFutureMonth ?: todayMonth
    def dd = txFutureDay ?: todayDay

    if(!txFutureDay) {
        runOnce(txFutureTime, processActions)
    }
    else{
        def timeSchedule = hhmmssZ(txFutureTime)
        result = "${yyyy}-${MM}-${dd}T${timeSchedule}" 
        Date date = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", result)
        runOnce(date, processActions)
    }
}
private hhmmssZ(time, fmt = "HH:mm:ss.SSSZ") {
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}


/***********************************************************************************************************************
	TIME RESTRICTIONS FOR SMS/PUSH/AUDIO MESSAGES ONLY
***********************************************************************************************************************/
private getSMSDayOk(evt) {
	if (parent.debug) log.info "SMS custom restrictions Handler activated"
    def result = true
    if (smsDays) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        }
        def day = df.format(new Date())
        result = smsDays.contains(day)
    }
    if(parent.debug) log.warn "SMSDayOk = $result"
    return result
}
private getMTimeOk(evt) {
    def result = true
    if ((startingm && endingm) ||
        (startingm && endingXm in ["Sunrise", "Sunset"]) ||
        (startingXm in ["Sunrise", "Sunset"] && endingm) ||
        (startingXm in ["Sunrise", "Sunset"] && endingXm in ["Sunrise", "Sunset"])) {
        def currTimem = now()
        def startm = null
        def stopm = null
        def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffsetm: startSunriseOffsetm, sunsetOffsetm: startSunsetOffsetm)
        if(startingXm == "Sunrise") startm = s.sunrise.time
        else if(startingXm == "Sunset") startm = s.sunset.time
            else if(startingm) startm = timeToday(startingm,location.timeZone).time
                s = getSunriseAndSunset(zipCode: zipCode, sunriseOffsetm: endSunriseOffsetm, sunsetOffsetm: endSunsetOffsetm)
            if(endingXm == "Sunrise") stopm = s.sunrise.time
            else if(endingXm == "Sunset") stopm = s.sunset.time
                else if(endingm) stopm = timeToday(endingm,location.timeZone).time
                    result = startm < stopm ? currTimem >= startm && currTimem <= stopm : currTimem <= stopm || currTimem >= startm
            }
    if(parent.debug) log.warn "MTimeOk = $result"
    return result
}
private mhhmm(time, fmt = "h:mm a") {
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}
private offsetm(value) {
    def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}
private mtimeIntervalLabel() {
    def result = "true"
    if      (startingXm == "Sunrise" && endingXm == "Sunrise")  result = "Sunrise" + offsetm(startSunriseOffsetm) + " to Sunrise" + offsetm(endSunriseOffsetm)
    else if (startingXm == "Sunrise" && endingXm == "Sunset")  result = "Sunrise" + offsetm(startSunriseOffsetm) + " to Sunset" + offsetm(endSunsetOffsetm)
        else if (startingXm == "Sunset" && endingXm == "Sunrise")  result = "Sunset" + offsetm(startSunsetOffsetm) + " to Sunrise" + offsetm(endSunriseOffsetm)
            else if (startingXm == "Sunset" && endingXm == "Sunset")  result = "Sunset" + offsetm(startSunsetOffsetm) + " to Sunset" + offsetm(endSunsetOffsetm)
                else if (startingXm == "Sunrise" && endingm)  result = "Sunrise" + offsetm(startSunriseOffsetm) + " to " + mhhmm(ending, "h:mm a z")
                    else if (startingXm == "Sunset" && endingm)  result = "Sunset" + offsetm(startSunsetOffsetm) + " to " + mhhmm(ending, "h:mm a z")
                        else if (startingm && endingXm == "Sunrise")  result = mhhmm(startingm) + " to Sunrise" + offsetm(endSunriseOffsetm)
                            else if (startingm && endingXm == "Sunset")  result = mhhmm(startingm) + " to Sunset" + offsetm(endSunsetOffsetm)
                                else if (startingm && endingm)  result = mhhmm(startingm) + " to " + mhhmm(endingm, "h:mm a z")
                                    }



// TIME RESTRICTIONS - ENTIRE ROUTINE
private getTimeOk(evt) {
    if (parent.aPause == true) {
        def result = true
        if ((starting && ending) ||
            (starting && endingX in ["Sunrise", "Sunset"]) ||
            (startingX in ["Sunrise", "Sunset"] && ending) ||
            (startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
            def currTime = now()
            def start = null
            def stop = null
            def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
            if(startingX == "Sunrise") start = s.sunrise.time
            else if(startingX == "Sunset") start = s.sunset.time
                else if(starting) start = timeToday(starting,location.timeZone).time
                    s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
                if(endingX == "Sunrise") stop = s.sunrise.time
                else if(endingX == "Sunset") stop = s.sunset.time
                    else if(ending) stop = timeToday(ending,location.timeZone).time
                        result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
                }
        if(parent.trace) log.trace "timeOk = $result"
        return result
    }
}
private hhmm(time, fmt = "h:mm a") {
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}
private offset(value) {
    def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}
private timeIntervalLabel() {
    def result = "complete"
    if      (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
    else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
        else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
            else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
                else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
                    else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
                        else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
                            else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
                                else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")
                                    }

/************************************************************************************************************
   CUSTOM MESSAGE VARIABLES 
************************************************************************************************************/
def runProfile(message, evt) {
	if (parent.debug) log.info "Message Variables Handler activated"
    def result 
    if(message) {
        result = message ? "$message".replace("&date", "${getVar("date")}").replace("&time", "${getVar("time")}") : null
        result = result ? "$result".replace("&temperature", "${getVar("temperature")}").replace("&tempTrend", "${getVar("tempTrend")}").replace("&humidity", "${getVar("humidity")}") : null
        result = result ? "$result".replace("&windSpeed", "${getVar("windSpeed")}").replace("&windDir", "${getVar("windDir")}") : null
        result = result ? "$result".replace("&high", "${getVar("high")}").replace("&low", "${getVar("low")}").replace("&rain", "${getVar("rain")}") : null
        result = result ? "$result".replace("&smoke", "${getVar("smoke")}").replace("&CO2", "${getVar("CO2")}").replace("&water", "${getVar("water")}") : null
        result = result ? "$result".replace("&device", "${getVar("device")}").replace("&action", "${getVar("action")}").replace("&stRoutAction", "${getVar("stRoutAction")}") : null
        result = result ? "$result".replace("&stRoutEvent", "${getVar("stRoutEvent")}").replace("&feelsLike", "${getVar("feelsLike")}") : null
//        result = getWeatherVar(result) 
    }
    return stripBrackets(result ? " $result " : "")
}
/************************************************************************************************************
   REPORT VARIABLES   
************************************************************************************************************/
private getVar(var) {
    def devList = []
    def result
    
	if (var == "time"){
        result = new Date(now()).format("h:mm aa", location.timeZone) 
        return stripBrackets(result ? " $result " : "")
    }
    if (var == "date"){
        result = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
        return stripBrackets(result ? " $result " : "")    
    }
    if (var == "temperature"){    
        if(vTemperature){
            def total = 0
            vTemperature.each {total += it.currentValue("temperature")}
            int avgT = total as Integer
            result = Math.round(total/vTemperature?.size()) + " degrees"
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "high"){    
        if(vTemperature){
            result = vTemperature.latestValue("max_temp") + " degrees"             	
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "low"){    
        if(vTemperature){
            result = vTemperature.latestValue("min_temp") + " degrees"             	
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "tempTrend"){    
        if(vTemperature){
            result = "trending " + vTemperature.latestValue("temp_trend")             	
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "feelsLike"){
        if(vFeelsLike){
            result = "feel like " + vFeelsLike.latestValue("feelsLike") + " degrees"
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "rain"){    
        if(vRain){
            result = vRain.latestValue("rain") + " inches"             	
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "stRoutAction"){
        if(aRout1 || aRout2){
            result = "${state.stRoutAction}"   
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "stRoutEvent"){
        if(tRoutine){
            result = "${state.stRoutEvent}"
            return stripBrackets(result ? " $result " : "")
        }
    }            
    if (var == "device"){ 
        result = "${state.eDev}" 
        return stripBrackets(result ? " $result " : "")
    }
    if (var == "action"){ 
    log.info "the door is $state.eAct"
        result = "${state.eAct}"
        return stripBrackets(result ? " $result " : "")
    }
    if (var == "humidity"){    
        if(vHumidity){
            result = vHumidity.latestValue("humidity") + " percent"             	
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "windSpeed"){    
        if(vWind){
            result = vWind.latestValue("WindStrength") + " miles per hour"             	
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "windDir"){    
        if(vWind){
            result = " blowing " + vWind.latestValue("WindDirection")             	
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "CO2"){    
        if(vCO2){
            result = vCO2.latestValue("carbonDioxide")             	
            log.info "The CO2 level is at $result"
            return stripBrackets(result ? " $result " : "")
        }
    }
    if (var == "noise"){    
        if(vSound){
            result = vSound.latestValue("soundPressureLevel") + " decibles"            	
            return stripBrackets(result ? " $result " : "")
        }
    }    
    if (var == "smoke"){
        if(vSmoke){
            if (vSmoke.latestValue("smoke")?.contains("detected")) {
                vSmoke.each { deviceName ->
                    if (deviceName.currentValue("smoke")=="${"detected"}") {
                        String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " sensor detected smoke"
            else if (devList?.size() > 0) result = devList?.size() + " sensors detected smoke"
                else if (!devList) result = "no sensors detected smoke"
                    return stripBrackets(result ? " $result " : "")
                }
    }
    if (var == "water"){
        if(vWater){
            if (vWater.latestValue("water")?.contains("wet")) {
                vWater.each { deviceName ->
                    if (deviceName.currentValue("water")=="${"detected"}") {
                        String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " sensor detected water"
            else if (devList?.size() > 0) result = devList?.size() + " sensors detected water"
                else if (!devList) result = "no sensors detected water"
                    return stripBrackets(result ? " $result " : "")
                }
    }

}
private stripBrackets(str) {
    str = str.replace("[", "")
    return str.replace("]", "")
}

/******************************************************************************************************
PARENT STATUS CHECKS
******************************************************************************************************/
// TRIGGER ACTIONS
def pTriggerSettings() {
    if (myCO2||tTemperature||tWind||tRain||tWater||tHumidity||tKeypads||tMode||tSHM||tRoutine||mySunstate||tSchedule||tSwitch||tDim||
    	tMotion||tPresence||tContactDoor||tContactWindow||tContact||tLocks||tGarage) {
        return "complete"
    }
    return ""
}
def pTriggerComplete() {
    if (myCO2||tTemperature||tWind||tRain||tWater||tHumidity||tKeypads||tMode||tSHM||tRoutine||mySunstate||tSchedule||tSwitch||tDim||
    	tMotion||tPresence||tContactDoor||tContactWindow||tContact||tLocks||tGarage) {
        return "Events have been Configured!"
    }
    return "Tap here to Configure Events"
}  
// CONDITIONS
def pConditionSettings() {
    if (cTemperature||cHumidity||cDays||cMode||cSHM||cSwitch||cDim||cMotion||cPresence||cContactDoor||cContactWindow||cContact||cLocks||
    	cGarage||starting||ending) {
        return "complete"
    }
    return ""
}
def pConditionComplete() {
    if (cTemperature||cHumidity||cDays||cMode||cSHM||cSwitch||cDim||cMotion||cPresence||cContactDoor||cContactWindow||cContact||cLocks||
    	cGarage||starting||ending) {
        return "Conditions have been Configured!"
    }
    return "Tap here to Configure Conditions"
}  
// ACTIONS
def pDevicesSettings() {
    if(aCeilingFans||aPendSwitches||tSelf||aSwitches||aOtherSwitches||aDim||aOtherDim||aHues||aHuesOther||aSwitchesOn||aSwitchesOff||
    	aFans||aLocks||aDoor||aTstat1||aTstat2||aVents||aShades||aPresence||aMode||aSHM||modeDimmers) { 
        return "complete"
    }
    return ""
}
def pDevicesComplete() {
    if( aCeilingFans||aPendSwitches||tSelf||aSwitches||aOtherSwitches||aDim||aOtherDim||aHues||aHuesOther||aSwitchesOn||aSwitchesOff||
    	aFans||aLocks||aDoor||aTstat1||aTstat2||aVents||aShades||aPresence||aMode||aSHM||modeDimmers) {
        return "Actions have been Configured!"
    }
    return "Tap here to Configure Actions"
}
// SMS/PUSH/AUDIO    
def pMessageSettings() {
    if(synthDevice||sonosDevice||push||sendText||smsDays) { 
        return "complete"
    }
    return ""
}
def pMessageComplete() {
    if (synthDevice||sonosDevice||push||sendText||smsDays) {
        return "Messaging has been Configured!"
    }
    return "Tap here to Configure Messaging"
}
// RESTRICTIONS
def pRestrictSettings(){ def result = "" 
                        if (rSHM || rMode || rDays || startingX || endingX || rSwitch || days || rPresence) { 
                            result = "complete"
                        }
                        return ""
                       }
def pRestrictComplete() {
    if (rSHM || rMode || rDays || startingX || endingX || rSwitch || days || rPresence) {
        return "Restrictions have been configured!"
    }
    return "Tap here to Configure Restrictions"
}
def pTimeSettings(){ def result = "" 
                    if (startingX || endingX) { 
                        result = "complete"}
                    result}
def pTimeComplete() {def text = "Tap here to Configure" 
                     if (startingX || endingX) {
                         text = "Configured"}
                     else text = "Tap here to Configure"
                     text}
def pmTimeSettings(){ def result = "" 
                     if (startingXm || endingXm) { 
                         result = "complete"}
                     result}
def pmTimeComplete() {def text = "Tap here to configure" 
                      if (startingXm || endingXm) {
                          text = "Configured"}
                      else text = "Tap here to Configure"
                      text}
def strTimeSettings(){ def result = "" 
                      if (startingXR || endingXR) { 
                          result = "complete"}
                      result}
def strTimeComplete() {def text = "Tap here to configure" 
                       if (startingXR || endingXR) {
                           text = "Configured"}
                       else text = "Tap here to Configure"
                       text}
def strTimeSettings1(){ def result = "" 
                       if (startingXR1 || endingXR1) { 
                           result = "complete"}
                       result}
def strTimeComplete1() {def text = "Tap here to configure" 
                        if (startingXR1 || endingXR1) {
                            text = "Configured"}
                        else text = "Tap here to Configure"
                        text}

////////////////////////////////////////////////////////////////////////////////////////////////
// DEVELOPMENT SECTION - WORK HERE AND MOVE IT WHEN YOU'RE DONE
////////////////////////////////////////////////////////////////////////////////////////////////