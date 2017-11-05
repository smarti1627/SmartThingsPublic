/*  Quirky-Wink-Powerstrip-Device.groovy
 *
 *  Author: todd@wackford.net
 *  Date: 2014-01-28
 *
 *****************************************************************
 *     Setup Namespace, acpabilities, attributes and commands
 *****************************************************************
 * Namespace:			"wackford"
 *
 * Capabilities:		"switch"
 *						"polling"
 *						"refresh"
 *
 * Custom Attributes:	"none"
 *
 * Custom Commands:		"none"
 *
 *****************************************************************
 *                       Changes
 *****************************************************************
 *
 *  Change 1:	2014-03-10
 *				Documented Header
 *
 *****************************************************************
 *     def powerstripEventHandler()
{
	log.debug "In Powerstrip Event Handler..."

	def json = request.JSON
	def outlets = json.outlets

	outlets.each() {
		def dni = getChildDevice(it.outlet_id)
		pollOutlet(dni)   //sometimes events are stale, poll for all latest states
	}

	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}

def pollOutlet(childDevice)
{
	log.debug "In pollOutlet"

	//login()

	log.debug "Polling powerstrip"
	apiGet("/outlets/" + childDevice.device.deviceNetworkId) { response ->
		def data = response.data.data
		data.powered ? childDevice?.sendEvent(name:"switch",value:"on") :
			childDevice?.sendEvent(name:"switch",value:"off")
	}
}

def on(childDevice)
{
	//login()

	apiPut("/outlets/" + childDevice.device.deviceNetworkId, [powered : true]) { response ->
		def data = response.data.data
		log.debug "Sending 'on' to device"
	}
}

def off(childDevice)
{
	//login()

	apiPut("/outlets/" + childDevice.device.deviceNetworkId, [powered : false]) { response ->
		def data = response.data.data
		log.debug "Sending 'off' to device"
	}
}

def createPowerstripChildren(deviceData)
{
	log.debug "In createPowerstripChildren"

	def powerstripName = deviceData.name
	def deviceFile = "Quirky Wink Powerstrip"

	deviceData.outlets.each {
		createChildDevice( deviceFile, it.outlet_id, it.name, "$powerstripName ${it.name}" )
	}
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}                  Code
 *****************************************************************
 */
// for the UI
metadata {

	definition(name:"Quirky Wink Powerstrip", namespace:"wackford", author:"Todd Wackford") {

		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Actuator"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}
	main(["switch"])
	details(["switch", "refresh" ])
}


// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	if (description?.name && description?.value)
	{
		results << sendEvent(name: "${description?.name}", value: "${description?.value}")
	}
}


// handle commands
def on() {
	log.debug "Executing 'on'"
	log.debug this
	parent.on(this)
}

def off() {
	log.debug "Executing 'off'"
	parent.off(this)
}

def poll() {
	log.debug "Executing 'poll'"
	parent.pollOutlet(this)
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}