//# sourceURL=analysisInit.js

"use strict";

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

window.smartR = window.smartR || {
        components: {},
        util: {},
    };

window.smartR.Observable = function Observable() {
    this.jquery = jQuery(this);
};
jQuery.extend(window.smartR.Observable.prototype, {
    on: function Observable_on() {
        this.jquery = this.jquery || jQuery(this); // so it works if constructor isn't called
        this.jquery.on.apply(this.jquery, arguments);
        return this;
    },
    trigger: function Observable_trigger() {
        this.jquery = this.jquery || jQuery(this);
        this.jquery.trigger.apply(this.jquery, arguments);
        return this;
    },
    unbind: function Observable_unbind() {
        this.jquery = this.jquery || jQuery(this);
        this.jquery.unbind.apply(this.jquery, arguments);
        return this;
    },
    forwardEvent: function Observable_forwardEvent(event, target) {
        this.jquery.on(event, function() {
            target.trigger.apply(target, arguments);
        });
    },
});

window.smartR.BaseController = function BaseController(ajaxServices) {
    this.ajaxServices = ajaxServices;
};
window.smartR.BaseController.prototype.init = function BaseController_init() {
    this.ajaxServices.startSession();
};

window.smartR.util.getSubsetIds = function smartRUtil_getSubsetIds() {
    var defer = jQuery.Deferred();

    function resolveResult() {
        var res = window.GLOBAL.CurrentSubsetIDs.map(function (v) { return v || null; });
        if (res.some(function(el) { return el !== null; })) {
            defer.resolve(res);
        } else {
            defer.reject();
        }
    }

    for (var i = 1; i <= window.GLOBAL.NumOfSubsets; i++) {
        if (!window.isSubsetEmpty(i) && !window.GLOBAL.CurrentSubsetIDs[i]) {
            window.runAllQueries(resolveResult);
            return defer.promise();
        }
    }

    resolveResult();

    return defer.promise();
}
