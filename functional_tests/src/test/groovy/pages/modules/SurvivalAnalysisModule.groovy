package pages.modules

import geb.Module

/**
 * Created by weymouth on 5/6/15.
 */

class SurvivalAnalysisModule extends Module {
    static content = {
        layoutContainer = {$('div#three-layout-container')}
        fieldset = layoutContainer.{$('div.displaydivCategoryVariable').b()}

    }
}