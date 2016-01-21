//# sourceURL=analysisInit.js

"use strict";

window.smartR = window.smartR || {};

window.smartR.initAnalysis = function initAnalysis(name) {
    /* wiring of components */
    var ajaxServices = smartR.ajaxServices(window.pageInfo.basePath, name);
    var model = smartR[name + '_model']();
    var controller = smartR[name + '_controller'](model, ajaxServices);
    smartR[name + '_view'](controller, model);
};
