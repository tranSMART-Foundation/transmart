package org.transmart.plugin.custom

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CustomizationTagLib {

	static namespace = 'transmart'
	static returnObjectForTags = ['setting', 'settingValue', 'userLevel',
	                              'userSetting', 'userSettingValue']

	CustomizationService customizationService
	CustomizationConfig customizationConfig

	/**
	 * Renders the body if the current user has level 0.
	 */
	def ifLevelZero = { Map attrs, Closure body ->
		ifLevel UserLevel.ZERO, body
	}

	/**
	 * Renders the body if the current user has level 1.
	 */
	def ifLevelOne = { Map attrs, Closure body ->
		ifLevel UserLevel.ONE, body
	}

	/**
	 * Renders the body if the current user has level 2.
	 */
	def ifLevelTwo = { Map attrs, Closure body ->
		ifLevel UserLevel.TWO, body
	}

	/**
	 * Renders the body if the current user has level admin.
	 */
	def ifLevelAdmin = { Map attrs, Closure body ->
		ifLevel UserLevel.ADMIN, body
	}

	/**
	 * Renders the body if the current user is unregistered.
	 */
	def ifLevelUnregistered = { Map attrs, Closure body ->
		ifLevel UserLevel.UNREGISTERED, body
	}

	/**
	 * @return the <code>UserLevel</code> for the currently authenticated user.
	 */
	def userLevel = {
		customizationService.currentUserLevel()
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the <code>Settings</code> instance for the currently authenticated user.
	 */
	def userSetting = { Map attrs ->
		lookupSettings attrs, 'userSetting', true
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the <code>Settings</code> instance <code>fieldvalue</code> (or an
	 *         empty String if not found) for the currently authenticated user.
	 */
	def userSettingValue = { Map attrs ->
		lookupSettings(attrs, 'userSettingValue', true)?.fieldvalue ?: ''
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the shared <code>Settings</code> instance.
	 */
	def setting = { Map attrs ->
		lookupSettings attrs, 'setting', false
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the shared <code>Settings</code> instance <code>fieldvalue</code>
	 *         (or an empty String if not found).
	 */
	def settingValue = { Map attrs ->
		lookupSettings(attrs, 'settingValue', false)?.fieldvalue ?: ''
	}

	// UICustomizationTagLib start

	def useAccessLevelDescription = {
		out << (customizationConfig.instanceType == 'grdr' ? 'Open Data Access' : 'Level 1 Access')
	}

	def useAccessLevelMemo = {
		if (customizationConfig.instanceType == 'grdr') {
			out << '<div class="description"><p>Although <i>Open Data</i> access does not require prior permission, authorization or review, you are required to <br />register and provide the minimal information as requested in the form below.</p></div>'
		}
	}

	def useRegisterButton = {
		if (customizationConfig.instanceType == 'pmsdn') {
			out << "I agree to the terms and register"
		}
		else {
			out << "Register"
		}
	}

	def useUserGuide = {
		if (customizationConfig.instanceType == 'grdr') {
			out << '''<b>NIH NCATS User's Guide</b> is available here: <br /><br /><a href="''' + customizationConfig.userGuideUrl + '">' +
					customizationConfig.userGuideUrl + '</a><br />'
		}
		else {
			out << 'To learn more about the application, please visit <a href="' + customizationConfig.userGuideUrl + '">' +
					customizationConfig.userGuideUrl + '</a>.'
		}
	}

	// UICustomizationTagLib end

	protected void ifLevel(UserLevel level, Closure body) {
		if (customizationService.currentUserLevel() == level) {
			out << (String) body()
		}
	}

	protected assertAttribute(String name, Map attrs, String tag) {
		if (!attrs.containsKey(name)) {
			throwTagError "Tag [$tag] is missing required attribute [$name]"
		}
		attrs.remove name
	}

	protected Settings lookupSettings(Map attrs, String tagName, boolean forUser) {
		String name = assertAttribute('name', attrs, tagName)
		forUser ? customizationService.userSetting(name) : customizationService.setting(name)
	}
}
