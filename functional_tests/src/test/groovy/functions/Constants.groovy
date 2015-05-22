package functions

import geb.Page
import pages.BrowsePage

class Constants {

    public static final String BAD_USERNAME = 'bad username'
    public static final String BAD_PASSWORD = 'bad password'
    public static final String GOOD_USERNAME = 'admin'
    public static final String GOOD_PASSWORD = 'admin'
    public static final String ADMIN_USERNAME = 'admin'
    public static final String ADMIN_PASSWORD = 'admin'

    public static final boolean AUTO_LOGIN_ENABLED = false // locally set to true
    public static final Page LANDING_PAGE = new BrowsePage() // local configuration

    public static final String GSE8581_KEY = '\\\\Public Studies\\Public Studies\\GSE8581\\'
}