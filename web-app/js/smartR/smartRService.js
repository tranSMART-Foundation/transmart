//# sourceURL=smartRService.js

'use strict';

/**
 * SmartR Service
 */
window.SmartRService = (function() {

    var SmartRService = function SmartRService () {

    };

    SmartRService.prototype.create = function (workflow) {
        // ajax call to session creation
        jQuery.ajax({
            url: pageInfo.basePath + '/RSession/create',
            type: 'POST',
            timeout: '30000',
            contentType: 'application/json',
            data : JSON.stringify( {
                workflow : workflow
            })
        }).done(function(response) {
            GLOBAL.HeimAnalyses = {
                type : workflow,
                sessionId :response.sessionId
            };
            return GLOBAL.HeimAnalyses;
        }).fail(function() {
            // TODO: error displayed in a placeholder somewhere in main heim-analysis page
            console.error('Cannot create r-session');
            return null;
        });
    };

    return SmartRService;
})();
