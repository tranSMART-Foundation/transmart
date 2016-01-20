//# sourceURL=heatmapValidator.js

'use strict';

/**
 * Heatmap Validator
 */
window.HeatmapValidator = (function() {

    var validator = {
        NO_SUBSET_ERR : 'No subsets are selected',
        NO_HD_ERR : 'No high dimension data is selected.',
        NUMERICAL_ERR : 'Input must be numeric'
    };

    validator.isEmptySubset = function (subsets) {
        return subsets.length < 1;
    };

    validator.isEmptyHighDimensionalData = function (hd) {
        return hd === '';
    };

    validator.isNumeric = function (val) {
        return !isNaN(val);
    };


    return validator;
})();
