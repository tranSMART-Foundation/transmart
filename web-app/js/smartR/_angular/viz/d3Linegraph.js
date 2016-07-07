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

            var patientRange = smartRUtils.getElementWithoutEventListeners('sr-linegraph-patient-range');
            patientRange.min = 0;
            patientRange.max = smartRUtils.unique(getValuesForDimension(byPatientID)).length;
            patientRange.value = 5;
            patientRange.step = 1;
            patientRange.addEventListener('change', function() {
                numOfPatientsToShow(patientRange.value);
                renderCategoricPlots();
            });

            function numOfPatientsToShow(num) {
                byPatientID.filterAll();
                var shownPatients = smartRUtils.unique(getValuesForDimension(byPatientID)).slice(0, num);
                byPatientID.filterFunction(function(patient) { return shownPatients.indexOf(patient) !== -1; });
            }
            numOfPatientsToShow(5);

            function getValuesForDimension(dimension) {
                return dimension.top(Infinity).map(function(record) { return dimension.accessor(record); });
            }

            var x = d3.scale.linear();
            // recomputes x scale for current filters
            function calculateXScale() {
                var times = smartRUtils.unique(getValuesForDimension(byTime)).sort();
                var xTicks = times.map(function(time) {
                    return (time - times[0]) / (times[times.length - 1] - times[0]) * LINEGRAPH_WIDTH;
                });
                x.domain(times).range(xTicks);
            }
            calculateXScale();

            byType.filterExact('categoric');
            var catFullNames = smartRUtils.unique(getValuesForDimension(byFullName));
            byType.filterExact('numeric');
            var numFullNames = smartRUtils.unique(getValuesForDimension(byFullName));
            byType.filterExact('highDimensional');
            var highFullNames = smartRUtils.unique(getValuesForDimension(byFullName));
            byType.filterAll();

            var svg = d3.select(vizDiv).append('svg')
                .attr('width', LINEGRAPH_WIDTH + MARGIN.left + MARGIN.right)
                .attr('height', LINEGRAPH_HEIGHT + MARGIN.top + MARGIN.bottom)
                .append('g')
                .attr('transform', 'translate(' + MARGIN.left + ',' + MARGIN.top + ')');

            var xAxis = d3.svg.axis()
                .scale(x);

            svg.append('g')
                .attr('class', 'sr-linegraph-x-axis')
                .attr('transform', 'translate(' + 0 + ',' + LINEGRAPH_HEIGHT + ')')
                .call(xAxis);

            function renderNumericPlots() {
                byType.filterExact('numeric');

            }

            function renderCategoricPlots() {
                byType.filterExact('categoric');
                // temporary dimensions because we want to keep filters within this function scope
                var tmpByPatientID = cf.dimension(function(d) { return d.patientID; });
                var tmpByTime = cf.dimension(function(d) { return d.time; });

                var catPlotInfo = smartRUtils.unique(getValuesForDimension(byPatientID)).map(function(patientID) {
                    tmpByPatientID.filterExact(patientID);
                    var maxCount = 0;
                    var times = smartRUtils.unique(getValuesForDimension(byTime));
                    times.forEach(function(time) {
                        tmpByTime.filterExact(time);
                        var count = byValue.top(Infinity).length;
                        maxCount = count > maxCount ? count : maxCount;
                        tmpByTime.filterAll();
                    });
                    tmpByPatientID.filterAll();
                    return {patientID: patientID, maxDensity: maxCount};
                });

                var totalDensity = catPlotInfo.reduce(function(prev, curr) { return curr.maxDensity + prev; }, 0);
                catPlotInfo.forEach(function(d) {
                    d.height = d3.round(d.maxDensity / totalDensity * CAT_PLOTS_HEIGHT);
                    tmpByPatientID.filterExact(d.patientID);
                    d.subset = smartRUtils.unique(getValuesForDimension(bySubset));
                });

                byType.filterAll();
                tmpByPatientID.dispose();
                tmpByTime.dispose();

                // DATA JOIN
                var catPlot = svg.selectAll('.sr-linegraph-cat-plot')
                    .data(catPlotInfo, function(d) { return d.patientID; });

                // ENTER g
                var catPlotEnter = catPlot.enter()
                    .append('g')
                    .attr('class', function(d) {
                        return 'sr-linegraph-cat-plot' + ' ' + 'patientid-' + smartRUtils.makeSafeForCSS(d.patientID);
                    });

                // ENTER rec
                catPlotEnter.append('rect')
                    .attr('width', LINEGRAPH_WIDTH)
                    .attr('fill', function(d) {
                        if (d.subset.length === 2) { return 'rgba(255, 255, 0, 0.5)'; }
                        if (d.subset[0] === 1) { return 'rgba(0, 255, 0, 0.5)'; }
                        return 'rgba(255, 0, 0, 0.5)';
                    });

                // ENTER text
                catPlotEnter.append('text')
                    .text(function(d) { return d.patientID; })
                    .attr('dy', '0.35em');

                // UPDATE g
                catPlot.attr('transform', function(d, i) {
                    var previousHeight = 0;
                    for (var j = i - 1; j >= 0; j--) {
                        previousHeight += catPlotInfo[i].height;
                    }
                    var y = LINEGRAPH_HEIGHT - CAT_PLOTS_OFFSET_BOTTOM - previousHeight - d.height;
                    return 'translate(' + 0 + ',' + y + ')';
                });

                // UPDATE rect
                catPlot.select('rect').attr('height', function(d) { return d.height; });

                // UPDATE text
                catPlot.select('text')
                    .attr('transform', function(d) {
                        return 'translate(' + (0) + ',' + (d.height / 2) + ')';
                    })
                    .style('font-size', function(d) { return d.height + 'px'; });

                // EXIT g
                catPlot.exit().remove();
            }
            renderCategoricPlots();
        }
    }
]);
