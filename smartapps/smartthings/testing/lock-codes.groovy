/**
 *  Lock Codes
 *
 *  Author: SmartThings
 *  Date: 2014-01-22
 */
preferences {
	page(name:"dynamicPage", title:"Lock Codes", content:"getPage")
}

def getPage()
{
	if (!lock) {
		dynamicPage(name:"dynamicPage", title:"Lock Codes", nextPage:"dynamicPage", uninstall:true) {
    		section("Select a lock") {
    			input "lock","capability.lockCodes", required: true
  			}
    	}
    } else {
	    def codes = lock.getAllCodes()
    	state.numCodes = (codes.codes instanceof Integer) ? codes.codes : 8
		dynamicPage(name:"dynamicPage", title:"Manage codes", uninstall:true, install:true) {
 	   		section("Codes") {
				1.upto(8) { n ->
					input "code$n", "text", title: "Code $n", defaultValue:codes["code$n"], description: "Set user code $n", required: false
     			}
    		}
       	 	if (state.numCodes > 8) {
    			section("More codes", hideable: true, hidden: true) {
    				9.upto(codes.codes) { n ->
       	 				input "code$n", "text", title: "Code $n", defaultValue:codes["code$n"], description: "Set user code $n", required: false
                    }
        		}
            }
            section {
            	label title: "Assign a name", defaultValue: "$lock.displayName Codes", required: false
            }
    	}
    }
}

def installed() {
	subscribe(lock, "codeReport", gotCodeReport)
    log.debug "sending updateCodes ${settings.findAll { it.key =~ /code\d/ && it.value != "?" }}"
	lock.updateCodes(settings.findAll { it.key =~ /code\d/ && it.value != "?" })
    log.debug "lock state: ${lock.getAllCodes()}"
}

def updated() {
	unsubscribe()
    installed()
}

def gotCodeReport(event) {
    app.updateSetting("code$event.value", parseJson(event.data).code ?: "")
}