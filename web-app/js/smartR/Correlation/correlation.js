//# sourceURL=correlation.js

"use strict";

var ajaxServices = smartR.ajaxServices(window.pageInfo.basePath, 'correlation');
var initServices = smartR.initServices(ajaxServices);
ajaxServices.startSession();
initServices.initConceptBox($('#sr-conceptBox-data'));
initServices.initConceptBox($('#sr-conceptBox-annotations'));
initServices.initFetchDataButton($('#sr-btn-fetch-correlation'));
initServices.initRunButton($('#sr-btn-run-correlation'));