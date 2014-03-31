package pages

import geb.Page

/**
 * Created by glopes on 06-02-2014.
 */
class Constants {

    public static final String BAD_USERNAME = 'bad username'
    public static final String BAD_PASSWORD = 'bad password'
    public static final String GOOD_USERNAME = 'admin'
    public static final String GOOD_PASSWORD = 'admin'

	public static final boolean AUTO_LOGIN_ENABLED = true
	public static final Page LANDING_PAGE = new DatasetExplorerPage()
}
