/**
 * http://stackoverflow.com/questions/19999388/check-if-user-is-using-ie-with-jquery
 *
 * detect IE
 * returns version of IE or false, if browser is not Internet Explorer
 */
function detectIE() {
	var ua = window.navigator.userAgent;

	var msie = ua.indexOf('MSIE ');
	if (msie > 0) {
		// IE 10 or older => return version number
		return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
	}

	var trident = ua.indexOf('Trident/');
	if (trident > 0) {
		// IE 11 => return version number
		var rv = ua.indexOf('rv:');
		return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
	}

	var edge = ua.indexOf('Edge/');
	if (edge > 0) {
		// Edge (IE 12+) => return version number
		return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
	}

	// other browser
	return false;
}

//If we detect IE, Hide the login form.
window.onload = function() {
	if (detectIE()) {
		document.getElementById("bannerRow").innerHTML = "<td style='border: 2px solid black;padding: 30px;'>" +
				"<font style='color:lightred;font-family:cambria;font-size:16px;font-weight:bold;'>" +
				"We've detected that you are attempting to use Internet Explorer to browse i2b2/tranSMART. <br />" +
				"Please use Safari, Firefox or Chrome instead.</font></td>"

		document.getElementById("loginWidgetRow").innerHTML =
				"If you are using Internet Explorer, the application will not function properly. Please use Safari, Firefox or Chrome instead."
	}
}
