//# sourceURL=analysisInit.js

"use strict";

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

window.smartR = window.smartR || {};

window.smartR.initAnalysis = function initAnalysis(name) {
    /* wiring of components */
        name = name.capitalize();
    var ajaxServices = smartR.ajaxServices(window.pageInfo.basePath, name);
    var model = smartR[name + 'Model']();
    var controller = smartR[name + 'Controller'](model, ajaxServices);
    smartR[name + 'View'](controller, model);
};
