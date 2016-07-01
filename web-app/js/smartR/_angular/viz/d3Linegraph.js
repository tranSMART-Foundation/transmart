//# sourceURL=d3Linegraph.js

'use strict';

window.smartRApp.directive('lineGraph', [
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
                    if (! $.isEmptyObject(scope.data)) {
                        smartRUtils.prepareWindowSize(scope.width, scope.height);
                        createLinegraph(scope, element[0]);
                    }
                });
            }
        };

        function createLinegraph(scope, root) {
            var data = crossfilter(scope.data.data_matrix);
            var byPatientID = data.dimension(function(d) { return d.patientID; });
            var byValue = data.dimension(function(d) { return d.value; });
            var byTime = data.dimension(function(d) { return d.time; });
            // We don't use name as a dimension because it is not alwats unique, whereas fullName is
            var byFullName = data.dimension(function(d) { return d.fullName; });
            var byType = data.dimension(function(d) { return d.type; });
            var bySubset = data.dimension(function(d) { return d.subset; });
            
            var groupByTime = byTime.group();
            var timepoints = groupByTime.all().map(function(d) { return d.key; });

            byType.filterExact('categoric');
            var catFullNames = byFullName.group().all().filter(function(d) { return d.value > 0; }).map(function(d) { return d.key; });
            byType.filterExact('numeric');
            var numFullNames = byFullName.group().all().filter(function(d) { return d.value > 0; }).map(function(d) { return d.key; });
            byType.filterAll();

            console.log(catFullNames);
            console.log(numFullNames);
        }

    }
]);
