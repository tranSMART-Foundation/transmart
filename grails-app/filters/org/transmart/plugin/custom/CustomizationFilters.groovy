package org.transmart.plugin.custom

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CustomizationFilters {

	CmsService cmsService
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

		/**
		 * Calls CmsService to store the current GSP model in a thread-local for
		 * use by renderers.
		 */
		cmsModelAccess(controller: '*', action: '*') {
			after = { Map model ->
				cmsService.setModel model ?: [:]
			}
			afterView = { Exception e ->
				cmsService.clearModel()
			}
		}
	}
}
