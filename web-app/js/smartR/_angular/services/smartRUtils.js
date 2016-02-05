smartRApp.factory('smartRUtils', [function() {

    var service = {};

    service.getSubsetIds = function smartRUtil_getSubsetIds() {
        var defer = jQuery.Deferred();

        function resolveResult() {
            var res = window.GLOBAL.CurrentSubsetIDs.map(function (v) {
                return v || null;
            });
            if (res.some(function (el) {
                    return el !== null;
                })) {
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
    };

    return service;
}]);