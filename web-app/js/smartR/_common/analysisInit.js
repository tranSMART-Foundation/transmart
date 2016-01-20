//# sourceURL=analysisInit.js

"use strict";

window.smartR = window.smartR || {};

window.smartR.initAnalysis = function initAnalysis(name) {
    var model = smartR[name + '_model']();
    var controller = smartR[name + '_controller'](model);
    smartR[name + '_view'](controller, model);
};
