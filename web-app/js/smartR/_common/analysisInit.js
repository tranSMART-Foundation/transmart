//# sourceURL=analysisInit.js

"use strict";

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

window.smartR = window.smartR || {};

window.smartR.initAnalysis = function initAnalysis(name) {
    /* wiring of components */
    var ajaxServices = smartR.ajaxServices(window.pageInfo.basePath, name);
    ajaxServices.startSession();

    var model = smartR[name.capitalize() + 'Model']();
    var controller = smartR[name.capitalize() + 'Controller'](model, ajaxServices);
    smartR[name.capitalize() + 'View'](controller, model);
};
