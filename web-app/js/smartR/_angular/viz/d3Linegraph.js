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
            var margin = {top: 50, right: 50, bottom: 50, left: 50};
            var width = parseInt(scope.width) - margin.left - margin.right;
            var height = parseInt(scope.height) - margin.top - margin.bottom;
            var data_matrix = scope.data.data_matrix;

            var cf = crossfilter(data_matrix);
            var byPatientID = cf.dimension(function(d) { return d.patientID; });
            var byValue = cf.dimension(function(d) { return d.value; });
            var byTime = cf.dimension(function(d) { return d.time; });
            var byFullName = cf.dimension(function(d) { return d.fullName; });
            var byType = cf.dimension(function(d) { return d.type; });

            var groupByPatientID = byPatientID.group();
            var groupByFullName = byFullName.group();
            var groupByTime = byTime.group();

            var shownPatients = groupByPatientID.all().map(function(d) { return d.key; }).slice(0, 5); // TODO
            (function resetPatientIDDimension() {
                byPatientID.filterExact(function(d) { return shownPatients.indexOf(d.patientID) !== -1; });
            })();

            function getNonZeroGroupKeys(group) {
                return group.all()
                    .filter(function(d) { return d.value > 0; })
                    .map(function(d) { return d.key; });
            }

            var x = d3.scale.linear();
            // recomputes x scale for current filters
            (function calculateXScale() {
                var times = getNonZeroGroupKeys(groupByTime);
                var xTicks = times.map(function(time, i) {
                    if (i === 0) { return 0; }
                    return times[times.length - 1] / time * width;
                });
                x.domain(times).range(xTicks);
            })();

            byType.filterExact('categoric');
            var catFullNames = getNonZeroGroupKeys(groupByFullName);

            byType.filterExact('numeric');
            var numFullNames = getNonZeroGroupKeys(groupByFullName);

            byType.filterAll();

            var svg = d3.select(root).append('svg')
                .attr('width', width + margin.left + margin.right)
                .attr('height', height + margin.top + margin.bottom)
                .append('g')
                .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

            var xAxis = d3.svg.axis()
                .scale(x)
                .tickValues(getNonZeroGroupKeys(groupByTime));

            svg.append('g')
                .attr('class', 'sr-linegraph-x-axis')
                .call(xAxis);

            // returns size of highest stack of categories for a certain patient at all times
            function getCategoricalDensity(patientID) {
                var tempByPatientID = cf.dimension(function(d) { return d.patientID; });
                var tempByTime = cf.dimension(function(d) { return d.time; });

                tempByPatientID.filterExact(patientID);
                var maxCount = 0;
				getNonZeroGroupKeys(groupByTime).forEach(function(time) {
					tempByTime.filterExact(time);
                    var count = byValue.top(Infinity).length;
                    maxCount = count > maxCount ? count : maxCount;
                });

                tempByPatientID.dispose();
                tempByTime.dispose();
                return maxCount;
            }

            byType.filterExact('categoric');
            var categoricalDensities = getNonZeroGroupKeys(groupByPatientID).map(function(patientID) {
                return getCategoricalDensity(patientID);
            });
            byType.filterAll();

            console.log(categoricalDensities);
        }

    }
]);
