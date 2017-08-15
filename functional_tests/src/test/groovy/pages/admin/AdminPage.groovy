package pages.admin

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.ScrollingModule
import pages.modules.UtilityModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class AdminPage extends Page {

    public static final String HEADER_TAB_NAME = 'Admin'

    static url = 'accessLog/index'

    static at = {
        commonHeader.currentMenuItem?.text() == HEADER_TAB_NAME
    }

    static content = {
        admin(wait: true) { $() }

        commonHeader { module CommonHeaderModule }
        scrolling { module ScrollingModule }
        utility { module UtilityModule }

        logOptions  { $("div.navContainer1").has("h1.panelheader", text: "Access Log") }
        logOptionsView  { logOptions.find("a.list", text: "View Access Log") }

        groupsOptions  { $("div.navContainer1").has("h1.panelheader", text: "Groups") }
        groupsOptionsList  { groupsOptions.find("a.list", text: "Group List") }
        groupsOptionsCreate  { groupsOptions.find("a.list", text: "Create Group") }
        groupsOptionsMembers  { groupsOptions.find("a.list", text: "Group Membership") }

        usersOptions  { $("div.navContainer1").has("h1.panelheader", text: "...") }
        usersOptionsList  { usersOptions.find("a.list", text: "User List") }
        usersOptionsCreate  { usersOptions.find("a.list", text: "Create User") }

        galaxyOptions  { $("div.navContainer1").has("h1.panelheader", text: "Galaxy Users") }
        galaxyOptions  { galaxyOptions.find("a.list", text: "User List") }
        galaxyOptions  { galaxyOptions.find("a.list", text: "Create User") }

        accessOptions  { $("div.navContainer1").has("h1.panelheader", text: "Access Control") }
        accessOptionsGroup  { accessOptions.find("a.list", text: "Access Control by Group") }
        accessOptionsStudy  { accessOptions.find("a.list", text: "Access Control by Study") }

        studyOptions  { $("div.navContainer1").has("h1.panelheader", text: "Study") }
        studyOptionsList  { studyOptions.find("a.list", text: "Study List") }
        studyOptionsAdd  { studyOptions.find("a.list", text: "Add Study") }

        secureOptions  { $("div.navContainer1").has("h1.panelheader", text: "Secure Object Paths") }
        secureOptions  { secureOptions.find("a.list", text: "SecureObjectPath List") }
        secureOptions  { secureOptions.find("a.list", text: "Add SecureObjectPath") }

        roleOptions  { $("div.navContainer1").has("h1.panelheader", text: "Roles") }
        roleOptionsList  { roleOptions.find("a.list", text: "Role List") }
        roleOptionsCreate  { roleOptions.find("a.list", text: "Create Role") }

        requestmapOptions  { $("div.navContainer1").has("h1.panelheader", text: "RequestMap Setup") }
        requestmapOptions  { requestmapOptions.find("a.list", text: "Requestmap List") }
        requestmapOptions  { requestmapOptions.find("a.list", text: "Requestmap Create") }

        xnatOptions  { $("div.navContainer1").has("h1.panelheader", text: "Import XNAT clinical data") }
        xnatOptionsList  { xnatOptions.find("a.list", text: "Configuration List") }
        xnatOptionsCreate  { xnatOptions.find("a.list", text: "Create Configuration") }

        packageOptions  { $("div.navContainer1").has("h1.panelheader", text: "Package and Configuration") }
        packageOptionsBuild  { packageOptions.find("a.list", text: "Build Information") }
        packageOptionsSupport  { packageOptions.find("a.list", text: "Status of Support Connections") }
    }
}

