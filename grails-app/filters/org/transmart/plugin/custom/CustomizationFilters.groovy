package org.transmart.plugin.custom

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CustomizationFilters {

	CustomizationService customizationService

	def filters = {

		/**
		 * Intercepts all requests to enforce minimum <code>UserLevel</code> for controller
		 * actions annotated with <code>@RequiresLevel</code>.
		 */
		checkUserLevelAccess(controller: '*', action: '*') {
			before = {
				customizationService.checkUserLevelAccess controllerName, actionName
			}
		}
	}
}
