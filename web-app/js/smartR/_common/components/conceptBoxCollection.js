//# sourceURL=conceptBoxCollection.js

"use strict";

window.smartR.components.conceptBoxCollection = function conceptBoxCollection(dict) {
    function prefixObjectKeys(targetObject, prefix, object) {
        Object.keys(object).forEach(function(key) {
            var newKey = prefix + key;
            targetObject[newKey] = object[key];
        });
    }

    return {
        forModel: {
            getAllConcepts: function BoxPlotCollections_getAllConcepts() {
                var res = {};

                Object.keys(dict).forEach(function(key) {
                    prefixObjectKeys(res, key + '_', dict[key].getLabelledConcepts());
                });

                return res;
            }
        }
    };
};
