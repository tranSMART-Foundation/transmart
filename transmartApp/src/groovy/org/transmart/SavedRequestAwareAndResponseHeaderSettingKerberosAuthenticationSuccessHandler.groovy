package org.transmart

import groovy.transform.CompileStatic
import org.springframework.security.core.Authentication
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken
import org.springframework.security.kerberos.web.authentication.ResponseHeaderSettingKerberosAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handler used to avoid ClassCastException (due to https://github.com/grails-plugins/grails-spring-security-kerberos/issues/3 )
 * if both kerberosServiceAuthenticationProvider and ldap provider are active
 */
@CompileStatic
class SavedRequestAwareAndResponseHeaderSettingKerberosAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private ResponseHeaderSettingKerberosAuthenticationSuccessHandler kerberosAuthenticationSuccessHandler =
	new ResponseHeaderSettingKerberosAuthenticationSuccessHandler()

    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
	                         Authentication auth) throws ServletException, IOException {
	if (auth instanceof KerberosServiceRequestToken) {
	    // TODO remove Authentication cast when Groovy no longer complains without it
	    kerberosAuthenticationSuccessHandler.onAuthenticationSuccess request, response, (auth as Authentication)
	}
	else {
	    super.onAuthenticationSuccess request, response, auth
        }
    }
}
