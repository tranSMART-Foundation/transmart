//# sourceURL=d3Linegraph.js

'use strict';

window.smartRApp.directive('lineGraph', [
    'smartRUtils',
    '$rootScope',
    function(smartRUtils, $rootScope) {

        return {
            restrict: 'E',
            scope: {
                data: '=',
                width: '@',
                height: '@'
            },
            templateUrl: $rootScope.smartRPath + '/js/smartR/_angular/templates/linegraph.html',
            link: function (scope, element) {
                var template_ctrl = element.children()[0],
                    template_viz = element.children()[1];

                scope.$watch('data', function() {
                    $(template_viz).empty();
                    if (! $.isEmptyObject(scope.data)) {
                        smartRUtils.prepareWindowSize(scope.width, scope.height);
                        createLinegraph(scope, template_viz, template_ctrl);
                    }
                });
            }
        };

        function createLinegraph(scope, vizDiv) {
            var MARGIN = {top: 50, right: 50, bottom: 50, left: 50};
            var LINEGRAPH_WIDTH = parseInt(scope.width) - MARGIN.left - MARGIN.right;
            var LINEGRAPH_HEIGHT = parseInt(scope.height) - MARGIN.top - MARGIN.bottom;

            var CAT_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2; // FIXME: make dynamic
            var NUM_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2; // FIXME: make dynamic
            var CAT_PLOTS_OFFSET_BOTTOM = 20;

            var data_matrix = scope.data.data_matrix;

            var cf = crossfilter(data_matrix);
            var byPatientID = cf.dimension(function(d) { return d.patientID; });
            var byValue = cf.dimension(function(d) { return d.value; });
            var byTime = cf.dimension(function(d) { return d.time; });
            var byFullName = cf.dimension(function(d) { return d.fullName; });
            var byType = cf.dimension(function(d) { return d.type; });
            var bySubset = cf.dimension(function(d) { return d.subset; });

            var groupByPatientID = byPatientID.group();
            var groupByFullName = byFullName.group();
            var groupByTime = byTime.group();
            var groupBySubset = bySubset.group();

            var patientRange = smartRUtils.getElementWithoutEventListeners('sr-linegraph-patient-range');
            patientRange.min = 0;
            patientRange.max = getNonZeroGroupKeys(groupByPatientID).length;
            patientRange.value = 5;
            patientRange.step = 1;
            patientRange.addEventListener('change', function() {
                numOfPatientsToShow(patientRange.value);
                renderCategoricPlots();
            });

            function numOfPatientsToShow(num) {
                var shownPatients = getNonZeroGroupKeys(groupByPatientID).slice(0, num); // show top 5 initially
                byPatientID.filterFunction(function(patient) { return shownPatients.indexOf(patient) !== -1; });
            }
            numOfPatientsToShow(5);

            function getNonZeroGroupKeys(group) {
                return group.all()
                    .filter(function(d) { return d.value > 0; })
                    .map(function(d) { return d.key; });
            }

            var x = d3.scale.linear();
            // recomputes x scale for current filters
            function calculateXScale() {
                var times = getNonZeroGroupKeys(groupByTime);
                var xTicks = times.map(function(time) {
                    return (time - times[0]) / (times[times.length - 1] - times[0]) * LINEGRAPH_WIDTH;
                });
                x.domain(times).range(xTicks);
            }
            calculateXScale();

            byType.filterExact('categoric');
            var catFullNames = getNonZeroGroupKeys(groupByFullName);
            byType.filterExact('numeric');
            var numFullNames = getNonZeroGroupKeys(groupByFullName);

            byType.filterAll();

            var svg = d3.select(vizDiv).append('svg')
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

            function renderCategoricPlots() {
                byType.filterExact('categoric');
                // temporary dimensions because we want to keep filters within this function scope
                var tmpByPatientID = cf.dimension(function(d) { return d.patientID; });
                var tmpByTime = cf.dimension(function(d) { return d.time; });

                var catBoxInfo = getNonZeroGroupKeys(groupByPatientID).map(function(patientID) {
                    tmpByPatientID.filterExact(patientID);
                    var maxCount = 0;
                    getNonZeroGroupKeys(groupByTime).forEach(function(time) {
                        tmpByTime.filterExact(time);
                        var count = byValue.top(Infinity).length;
                        maxCount = count > maxCount ? count : maxCount;
                        tmpByTime.filterAll();
                    });
                    tmpByPatientID.filterAll();
                    return {patientID: patientID, maxDensity: maxCount};
                });

                var totalDensity = catBoxInfo.reduce(function(prev, curr) { return curr.maxDensity + prev; }, 0);
                catBoxInfo.forEach(function(d) {
                    d.height = Math.floor(d.maxDensity / totalDensity * CAT_PLOTS_HEIGHT);
                    tmpByPatientID.filterExact(d.patientID);
                    d.subset = getNonZeroGroupKeys(groupBySubset);
                });

                byType.filterAll();
                tmpByPatientID.dispose();
                tmpByTime.dispose();

                var catBox = svg.selectAll('.sr-linegraph-cat-box')
                    .data(catBoxInfo);

                catBox.enter()
                    .append('rect')
                    .attr('class', function(d) {
                        return 'sr-linegraph-cat-box' + ' ' + smartRUtils.makeSafeForCSS('patientid-' + d.patientID);
                    })
                    .attr('x', 0)
                    .attr('width', LINEGRAPH_WIDTH)
                    .attr('fill', function(d) {
                        if (d.subset.length === 2) { return 'rgba(255, 255, 0, 0.5)'; }
                        if (d.subset[0] === 1) { return 'rgba(0, 255, 0, 0.5)'; }
                        return 'rgba(255, 0, 0, 0.5)';
                    });

                catBox
                    .attr('y', function(d, i) {
                        var previousHeight = 0;
                        for (var j = i - 1; j >= 0; j--) {
                            previousHeight += catBoxInfo[i].height;
                        }
                        return LINEGRAPH_HEIGHT - CAT_PLOTS_OFFSET_BOTTOM - previousHeight + d.height;
                    })
                    .attr('height', function(d) { return d.height; });
            }
            renderCategoricPlots();
        }
    }
]);
