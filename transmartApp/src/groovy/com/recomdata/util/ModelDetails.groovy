/**
 * This class serves as a base class for representing model details information
 * which is useful to pass back and forth to views as a single model instance
 */
package com.recomdata.util

import groovy.transform.CompileStatic

/**
 * @author jspencer
 */
@CompileStatic
class ModelDetails {
    def loggedInUser
}
