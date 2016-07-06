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
            var MARGIN = {top: 50, right: 50, bottom: 50, left: 50};
            var LINEGRAPH_WIDTH = parseInt(scope.width) - MARGIN.left - MARGIN.right;
            var LINEGRAPH_HEIGHT = parseInt(scope.height) - MARGIN.top - MARGIN.bottom;

            var CAT_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2; // FIXME: make dynamic
            var CAT_PLOTS_OFFSET_BOTTOM = 20;

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
                var xTicks = times.map(function(time) {
                    return (time - times[0]) / (times[times.length - 1] - times[0]) * LINEGRAPH_WIDTH;
                });
                x.domain(times).range(xTicks);
            })();

            byType.filterExact('categoric');
            var catFullNames = getNonZeroGroupKeys(groupByFullName);
            byType.filterExact('numeric');
            var numFullNames = getNonZeroGroupKeys(groupByFullName);

            byType.filterAll();

            var svg = d3.select(root).append('svg')
                .attr('width', LINEGRAPH_WIDTH + MARGIN.left + MARGIN.right)
                .attr('height', LINEGRAPH_HEIGHT + MARGIN.top + MARGIN.bottom)
                .append('g')
                .attr('transform', 'translate(' + MARGIN.left + ',' + MARGIN.top + ')');

            var xAxis = d3.svg.axis()
                .scale(x)
                .tickValues(getNonZeroGroupKeys(groupByTime)); // TODO: replace with node label, rather than time

            svg.append('g')
                .attr('class', 'sr-linegraph-x-axis')
                .attr('transform', 'translate(' + 0 + ',' + LINEGRAPH_HEIGHT + ')')
                .call(xAxis);

            // FIXME: I probably should use new dimensions rather than resetting possibly used ones
            (function renderCategoricPlots() {
                byType.filterExact('categoric');
                var catBoxInfo = getNonZeroGroupKeys(groupByPatientID).map(function(patientID) {
                    byPatientID.filterExact(patientID);
                    var maxCount = 0;
                    getNonZeroGroupKeys(groupByTime).forEach(function(time) {
                        byTime.filterExact(time);
                        var count = byValue.top(Infinity).length;
                        maxCount = count > maxCount ? count : maxCount;
                    });
                    return {patientID: patientID, maxDensity: maxCount};
                });
                byType.filterAll();
                byPatientID.filterAll();
                byTime.filterAll();

                var totalDensity = catBoxInfo.reduce(function(prev, curr) { return curr.maxDensity + prev; }, 0);
                catBoxInfo.forEach(function(d) {
                    d.height = Math.floor(d.maxDensity / totalDensity * CAT_PLOTS_HEIGHT);
                });

                var catBox = svg.selectAll('.sr-linegraph-cat-box')
                    .data(catBoxInfo);

                catBox.enter()
                    .append('rect')
                    .attr('class', function(d) {
                        return 'sr-linegraph-cat-box' + ' ' + smartRUtils.makeSafeForCSS('patientid-' + d.patientID);
                    })
                    .attr('x', 0)
                    .attr('width', LINEGRAPH_WIDTH);

                catBox
                    .attr('y', function(d, i) {
                        var previousHeight = 0;
                        for (var j = i - 1; j >= 0; j--) {
                            previousHeight += catBoxInfo[i].height;
                        }
                        return LINEGRAPH_HEIGHT - CAT_PLOTS_OFFSET_BOTTOM - previousHeight + d.height;
                    })
                    .attr('height', function(d) { return d.height; });
            })();
        }

    }
]);
