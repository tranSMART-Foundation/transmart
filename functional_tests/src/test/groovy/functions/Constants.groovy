package functions

import geb.Browser
import geb.Page
import pages.BrowsePage

class Constants {

    public static String HOST_SERVER = "localhost"

    public static final String BAD_USERNAME = 'bad username'
    public static final String BAD_PASSWORD = 'bad password'
    public static String GOOD_USERNAME = 'guest'
    public static String GOOD_PASSWORD = 'transmart2016'
    public static String GUEST_USERNAME = 'guest'
    public static String GUEST_PASSWORD = 'transmart2016'
    public static String ADMIN_USERNAME = 'admin'
    public static String ADMIN_PASSWORD = 'admin'

    public static boolean AUTO_LOGIN_ENABLED = true // locally set to true
    public static boolean GALAXY_ENABLED = true
    public static boolean GWAS_PLINK_ENABLED = true
    public static boolean METACORE_ENABLED = true
    public static boolean SMARTR_ENABLED = true
    public static boolean XNAT_IMPORT_ENABLED = true
    public static boolean XNAT_VIEW_ENABLED = true

    public static Page LANDING_PAGE = new BrowsePage()

    public static final String GSE8581_KEY = '\\\\Public Studies\\Public Studies\\COPD_Bhattacharya_GSE8581\\'
    public static final String GSE13168_KEY = '\\\\Public Studies\\Public Studies\\Asthma_Misior_GSE13168\\'
    public static final String GSE15258_KEY = '\\\\eTRIKS Rheumatoid Arthritis\\eTRIKS Rheumatoid Arthritis\\Bienkowska(2009) GSE15258\\'
    public static final String GSE35643_KEY = '\\\\eTRIKS Asthma\\eTRIKS Asthma\\Gounni(2012) GSE35643\\'

    /*
    def Constants(Browser browser) {

        String hasBaseUrl = browser.config.rawConfig.baseUrl
        if(hasBaseUrl == 'http://localhost/transmart/') {
            HOST_SERVER = 'localhost'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            GALAXY_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

        } else if(hasBaseUrl == 'http://postgres-ci.transmartfoundation.org/transmart/') {
            HOST_SERVER = 'TranSMART Oracle test server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//             AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

       } else if(hasBaseUrl == 'http://postgres-test.transmartfoundation.org/transmart/') {
            HOST_SERVER = 'TranSMART Postgres test server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

        } else if(hasBaseUrl == 'http://transmartci.etriks.org/') {
            HOST_SERVER = 'eTRIKS test server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

        } else {                // default values
            HOST_SERVER = 'default server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

        }
        if(LANDING_PAGE == null)
            LANDING_PAGE = new BrowsePage()
    }
    */
    }
