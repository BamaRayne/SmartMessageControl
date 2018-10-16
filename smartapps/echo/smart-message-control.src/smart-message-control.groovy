/*  
 * Smart Message Control    
 *
 *	10/16/2018		ver 1.0 R.0.0.1		Initial Release
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

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 *  Supported by the following SmartThings Apps
 *	RemindR V2
 *	House Fan Controller
 *	Logic Rulz
 
 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
/**********************************************************************************************************************************************/
definition(
    name		: "Smart Message Control",
    namespace	: "Echo",
    author		: "bamarayne",
    description	: "The smart way to make and send messages in your home.",
    category	: "My Apps",
	iconUrl			: "https://raw.githubusercontent.com/jasonrwise77/My-SmartThings/master/LogicRulz%20Icons/LogicRulz.png",
	iconX2Url		: "https://raw.githubusercontent.com/jasonrwise77/My-SmartThings/master/LogicRulz%20Icons/LogicRulz2x.png",
	iconX3Url		: "https://raw.githubusercontent.com/jasonrwise77/My-SmartThings/master/LogicRulz%20Icons/LogicRulz2x.png")

/**********************************************************************************************************************************************/
private def version() { 
    	def text = "Smart Message Control Ver 1.0 / R.0.0.1, \n" +
        "Current Release date: in development \n" +
        "Initial Release Date: not released" 
	}

preferences {
    page(name: "main")
    page(name: "statusPage")
    page(name: "settingsPage")
    page(name: "weatherPage")
    page(name: "messages")
}
page name: "main"
def main() {
    dynamicPage (name: "main", title: "", install: true, uninstall: uninstalled) {  
        section("Create and Manage Announcement Zones") {
            href "messages", title: "Configure Your Communications System"//, description: mRoomsD(), state: mRoomsS() 
            }
        section("Zone Details",  uninstall: false){
            href "statusPage", title: "View the details of your Zones"//, description: mBlocksD(), state: mBlocksS()
            }
        section("Pause Communications") {
        	input "aPause", "bool", title: "Turn off this toggle to pause ALL Communication Zones", defaultValue: true, submitOnChange: true
        	}
        section("Settings") {
        	href "settingsPage", title: "Configure Settings", description: mSettingsD(), state: mSettingsS()
            }
        section("Uninstall") {
        	href "uninstallPage", title: "Click here to remove $app.label"
            }
    }
}

/******************************************************************************
	MESSAGING ROOMS
******************************************************************************/
def messages() {
    dynamicPage (name: "messages", title: "You have created (${childApps?.size()}) Communication Zones", install: true, uninstall: installed) {
        section(""){
            app(name: "Smart Message Control Zone", appName: "Smart Message Control Zone", title: "Create a new messaging Zone", namespace: "Echo", displayChildApps: true, multiple: true,  uninstall: false)
        }
    }
}

page name: "settingsPage"
	def settingsPage() {
    	dynamicPage (name: "settingsPage", title: "Tap here to configure settings") {
        section("") {
            input "debug", "bool", title: "Enable Debug Logging", default: true, submitOnChange: true
        	input "trace", "bool", title: "Enable Trace Logging", default: false, submitOnChange: true
            paragraph "Debug logging is for normal use, Trace logging is for when we have a problem"
			}
        section("App Details") {
            paragraph "${version()}"
     		}
        }    
	}
        
page name: "statusPage"
    def statusPage() {
    	dynamicPage (name: "statusPage", title: "You have created (${childApps?.size()}) Communication Zones", install: true, uninstall: installed) {
    		section("Paused Communication Zones"){
         //   paragraph runningAppsFalse()  
            }
            section("Active Communication Zones") {
         //   paragraph runningAppsTrue()
            }
        }
    }    
page name: "uninstallPage"
    def uninstallPage() {
    	dynamicPage (name: "uninstallPage", title: "Clicking on the BIG RED BUTTON below will completely remove $app.label and all Routines!", install: true, uninstall: true) {
    		section("Please ensure you are ready to take this step, there is no coming back from the brink!"){
            }
		}
    }    


/************************************************************************************************************
		Base Process
************************************************************************************************************/
def installed() {
	if (debug) log.debug "Installed with settings: ${settings}"
//    log.debug "Parent App Version: ${textVersion()} | Release: ${release()}"
    initialize()
}
def updated() { 
	if (debug) log.debug "Updated with settings: ${settings}"
//    log.debug "Parent App Version: ${textVersion()} | Release: ${release()}"
	unsubscribe()
    initialize()
}
def initialize() {
        //Other Apps Events	
        subscribe(location, "RemindRevent", RemindR)
        subscribe(location, "House Fan Controller", HouseFanController) 
        subscribe(location, "Logic Rulz", LogicRulz)
}


/** Configure Settings Pages **/
def mSettingsS(){
    def result = ""
    if (debug == true && trace == true) {
    	result = "complete"	
    }
    result
}
def mSettingsD() {
    def text = "Settings have not been configured. Tap here to begin"
    if ("$debug" == true) { debug = "Debug Logging is Active" }
    if ("$debug" == false) { debug = "Dubug Logging is not Active" }
    if ("$trace" == true) { trace = "Trace Logging is Active" }
    if ("$trace" == false) { trace = "Trace Logging is not Active" }
    	text = "Debug Logging is $debug \n" +
        	"Trace Logging is $trace "
}                     



// INTEGRATION WITH 3RD PARTY APPS

// HOUSE FAN CONTROLLER
def HouseFanController(evt) { 
    log.info "event received from House Fan Controller ==> $evt.value"
    def result
    childApps.each {child ->
        def ch = child.label.toLowerCase()
        if (ch) { 
            log.debug "Found a profile, $ch"
            result = child.ttsActions(evt) 
        }
        else {
            log.debug "Could not find a profile to run, $profile"
        }
    }
}

// REMINDR
def RemindR(evt) {  
    log.info "event received from RemindR ==> $evt.value"
    def result
    childApps.each {child ->
        def ch = child.label.toLowerCase()
        if (ch) { 
            log.debug "Found a profile, $ch"
            result = child.ttsActions(evt) 
        }
        else {
            log.debug "Could not find a profile to run, $profile"
        }
    }
}

// LOGIC RULZ
def LogicRulz(evt) { 
    log.info "event received from Logic Rulz ==> $message"
    def result
    childApps.each {child ->
        def ch = child.label.toLowerCase()
        if (ch) { 
            log.debug "Found a profile, $ch"
            result = child.ttsActions(evt) 
        }
        else {
            log.debug "Could not find a profile to run, $profile"
        }
    }
}