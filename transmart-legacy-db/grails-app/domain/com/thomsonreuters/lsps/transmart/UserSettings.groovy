package com.thomsonreuters.lsps.transmart

class UserSettings {
    Long userId
    String name
    String value

    static mapping = {
        table 'searchapp.search_user_settings'
	id generator: 'sequence', params: [sequence: 'SEARCHAPP.HIBERNATE_SEQUENCE']
	version false

        name column: 'SETTING_NAME'
        value column: 'SETTING_VALUE'
    }

    static boolean isConfigured() {
        try {
	    count()
	    true
        }
	catch (ignored) {
	    false
        }
    }

    static String getSetting(Long userid, String name) {
        try {
	    findByUserIdAndName(userid, name)?.value
        }
	catch (ignored) {}
    }

    static void setSetting(Long userid, String name, String value) {
	UserSettings res = findByUserIdAndName(userid, name)
	if (res) {
            res.value = value
	}
	else {
            res = new UserSettings(userId: userid, name: name, value: value)
	}

        res.save()
    }
}

