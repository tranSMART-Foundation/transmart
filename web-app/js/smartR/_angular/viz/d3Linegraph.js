//# sourceURL=d3Linegraph.js

'use strict';

window.smartRApp.directive('linegraph', [
    'smartRUtils',
    function(smartRUtils) {

        return {
            restrict: 'E',
            scope: {
                data: '=',
                width: '@',
                height: '@'
            },
            link: function (scope, element) {
                scope.$watch('data', function() {
                    $(element[0]).empty();
                    console.log(scope.data);
                    if (! $.isEmptyObject(scope.data)) {
                        console.log(scope.data);
                        smartRUtils.prepareWindowSize(scope.width, scope.height);
                        createLinegraph(scope, element[0]);
                    }
                });
            }
        };

        function createLinegraph(scope, root) {
            console.log(scope.data);
            var cf = crossfilter(scope.data);
            console.log(cf.all);
        }

    }
]);
