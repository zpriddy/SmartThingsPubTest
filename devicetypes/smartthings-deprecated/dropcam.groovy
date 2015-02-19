metadata {
	// Automatically generated. Make future change here.
	definition (name: "Dropcam", namespace: "smartthings-deprecated", author: "SmartThings", oauth: true) {
		capability "Image Capture"
	}

	simulator {
		status "image": "raw:C45F5708D89A4F3CB1A7EEEE2E0C73D900, image:C45F5708D89A4F3CB1A7EEEE2E0C73D9, result:00"

		reply "take C45F5708D89A4F3CB1A7EEEE2E0C73D9": "raw:C45F5708D89A4F3CB1A7EEEE2E0C73D900, image:C45F5708D89A4F3CB1A7EEEE2E0C73D9, result:00"
	}

	preferences {
		input "username", "text", title: "Username", description: "Your Drop Cam Username", required: true
		input "password", "password", title: "Password", description: "Your Drop Cam Password", required: true
		input "uuid", "text", title: "Device ID", description: "Your Drop Cam ID", required: true
	}
    
    tiles {
		standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
		}

		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.dropcam", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
		}

		main "camera"
		details(["cameraDetails", "take"])
	}
}

def parse(Map response) {
	def result = []
	if (response.status == 403) {
		// forbidden and either missing cookie or it's expired, get a new one
		result << login()
	} else if (response.status == 200) {
		if (response.headers.'Content-Type'.contains("image/jpeg")) {
			def imageBytes = response.data
			if (imageBytes) {
				storeImageInS3(getPictureName(), imageBytes)
			}
		} else if (response.headers.'Content-Type'.contains("application/json")) {
			def cookie = response.headers.'Set-Cookie'?.split(";")[0]
			if (cookie) {
				updateDataValue(device, "cookie", cookie)
				result << takePicture()
			}
		}
	}
	result
}

def take() {
	takePicture()
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	getCameraUuid() + "_$pictureUuid" + ".jpg"
}

private getCookieValue() {
	getDeviceDataByName(device, "cookie")
}

private getCameraUuid() {
	getDevicePreferenceByName(device, "uuid")
}

private getImageWidth() {
	getDevicePreferenceByName(device, "image_width") ?: 1280
}

private getUsername() {
	getDevicePreferenceByName(device, "username")
}

private getPassword() {
	getDevicePreferenceByName(device, "password")
}

private validUserAgent() {
	"curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8x zlib/1.2.5"
}

private takePicture() {
	rest(
			method: 'GET',
			endpoint: "https://nexusapi.dropcam.com",
			path: "/get_image",
			query: [width: getImageWidth(), uuid: getCameraUuid()],
			headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()],
			requestContentType: "application/x-www-form-urlencoded",
			synchronous: true
	)
}

private login() {
	rest(
			method: 'POST',
			endpoint: "https://www.dropcam.com",
			path: "/api/v1/login.login",
			body: [username: getUsername(), password: getPassword()],
			headers: ['User-Agent': validUserAgent()],
			requestContentType: "application/x-www-form-urlencoded",
			synchronous: true
	)
}
