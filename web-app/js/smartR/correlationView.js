//# sourceURL=correlationView.js

'use strict';

window.CorrelationView = (function() {

    var smartRService;

    var CorrelationView = function CorrelationView() {
        SmartRView.call(this);
        smartRService = new SmartRService();
        // create session
        smartRService.create('correlation');
    };

    // subclass extends superclass
    CorrelationView.prototype = Object.create(SmartRView.prototype);
    CorrelationView.prototype.constructor = CorrelationView;

    return CorrelationView;
})();

var correlationView = new CorrelationView();

