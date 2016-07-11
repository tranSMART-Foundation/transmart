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
            var data_matrix = scope.data.data_matrix;

            var cf = crossfilter(data_matrix);
            var byPatientID = cf.dimension(function(d) { return d.patientID; });
            var byValue = cf.dimension(function(d) { return d.value; });
            var byTimeInteger = cf.dimension(function(d) { return d.timeInteger; });
            var byBioMarker = cf.dimension(function(d) { return d.bioMarker; });
            var byType = cf.dimension(function(d) { return d.type; });
            var bySubset = cf.dimension(function(d) { return d.subset; });

            var groupBioMarker = byBioMarker.group();

            var MARGIN = {top: 50, right: 50, bottom: 50, left: 50};
            var LINEGRAPH_WIDTH = parseInt(scope.width) - MARGIN.left - MARGIN.right;
            var LINEGRAPH_HEIGHT = parseInt(scope.height) - MARGIN.top - MARGIN.bottom;

            var CAT_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2; // FIXME: make dynamic
            var NUM_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2; // FIXME: make dynamic
            var CAT_PLOTS_OFFSET_BOTTOM = 20;

            var patientRange = smartRUtils.getElementWithoutEventListeners('sr-linegraph-patient-range');
            patientRange.min = 0;
            patientRange.max = smartRUtils.unique(getValuesForDimension(byPatientID)).length;
            patientRange.value = 5;
            patientRange.step = 1;
            patientRange.addEventListener('change', function() {
                numOfPatientsToShow(patientRange.value);
                renderCategoricPlots();
            });

            var x = d3.scale.linear();
            // recomputes x scale for current filters
            function calculateXScale() {
                var tmpByType = cf.dimension(function(d) { return d.type; });
                tmpByType.filterExact('categoric');
                var padding = 1 / byPatientID.top(Infinity).length * CAT_PLOTS_HEIGHT;
                tmpByType.dispose();
                var times = smartRUtils.unique(getValuesForDimension(byTimeInteger)).sort(function(a, b) {
                    return a - b;
                });
                x.domain(d3.extent(times)).range([padding, LINEGRAPH_WIDTH - padding]);
                updateXAxis(); // after changing the scale we need to update the x axis too
            }
            calculateXScale();

            function numOfPatientsToShow(num) {
                byPatientID.filterAll();
                var shownPatients = smartRUtils.unique(getValuesForDimension(byPatientID)).slice(0, num);
                byPatientID.filterFunction(function(patient) { return shownPatients.indexOf(patient) !== -1; });
                calculateXScale();
            }
            numOfPatientsToShow(5);

            function getValuesForDimension(dimension) {
                return dimension.top(Infinity).map(function(record) { return dimension.accessor(record); });
            }

            byType.filterExact('categoric');
            var catBioMarkers = smartRUtils.unique(getValuesForDimension(byBioMarker));
            byType.filterExact('numeric');
            var numBioMarkers = smartRUtils.unique(getValuesForDimension(byBioMarker));
            byType.filterExact('highDimensional');
            var highBioMarkers = smartRUtils.unique(getValuesForDimension(byBioMarker));
            byType.filterAll();

            var svg = d3.select(vizDiv).append('svg')
                .attr('width', LINEGRAPH_WIDTH + MARGIN.left + MARGIN.right)
                .attr('height', LINEGRAPH_HEIGHT + MARGIN.top + MARGIN.bottom)
                .append('g')
                .attr('transform', 'translate(' + MARGIN.left + ',' + MARGIN.top + ')');

            var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([-10, 0])
                .html(function(d) { return d; });

            svg.call(tip);

            svg.append('g')
                .attr('class', 'sr-linegraph-x-axis')
                .attr('transform', 'translate(' + 0 + ',' + LINEGRAPH_HEIGHT + ')')

            function updateXAxis() {
                // temporary dimension because we don't want to affect the time filter
                var tmpByTimeInteger = cf.dimension(function(d) { return d.timeInteger; });
                var tickFormat = {};
                smartRUtils.unique(getValuesForDimension(byTimeInteger)).forEach(function(timeInteger) {
                    tmpByTimeInteger.filterExact(timeInteger);
                    tickFormat[timeInteger] = byTimeInteger.top(1)[0].timeString;
                });
                tmpByTimeInteger.dispose();
                var xAxis = d3.svg.axis()
                    .scale(x)
                    .tickFormat(function(d) { return tickFormat[d]; });

                d3.select('.sr-linegraph-x-axis')
                    .call(xAxis);
            }

            function iconGenerator() {
                var square = function(size) { return 'M0,0H' + size + 'V' + size + 'H0Z'; };
                var triangle = function(size) { return 'M' + (size / 2) + ',0L' + size + ',' + size + 'H0Z'; };
                var diamond = function(size) {
                    return 'M' + (size / 2) + ',0' +
                        'L' + size + ',' + (size / 2) +
                        'L' + (size / 2) + ',' + size +
                        'L0,' + (size / 2) + 'Z';
                };
                var revTriangle = function(size) { return 'M0,0H' + size + 'L' + (size / 2) + ',' + size + 'Z'; };
                var hexagon = function(size) { return 'M' + (size / 2) + ',0' +
                        'L' + size + ',' + size / 4 +
                        'L' + size + ',' + (size * 3 / 4) +
                        'L' + (size / 2) + ',' + size +
                        'L0,' + (size * 3 / 4) +
                        'L0,' + (size / 4) + 'Z';
                };
                var fallback = function(size) { return 'M0,0L' + size + ',' + size + 'M' + size + ',0L0,' + size; };
                var iconTable = [
                    // square
                    {shape: square, fill: '#006980'},
                    {shape: square, fill: '#4B0080'},
                    {shape: square, fill: '#F6910E'},
                    {shape: square, fill: '#0EF611'},
                    {shape: square, fill: '#F60E1E'},
                    // triangle
                    {shape: triangle, fill: '#006980'},
                    {shape: triangle, fill: '#4B0080'},
                    {shape: triangle, fill: '#F6910E'},
                    {shape: triangle, fill: '#0EF611'},
                    {shape: triangle, fill: '#F60E1E'},
                    // triangle
                    {shape: diamond, fill: '#006980'},
                    {shape: diamond, fill: '#4B0080'},
                    {shape: diamond, fill: '#F6910E'},
                    {shape: diamond, fill: '#0EF611'},
                    {shape: diamond, fill: '#F60E1E'},
                    // diamond
                    {shape: revTriangle, fill: '#006980'},
                    {shape: revTriangle, fill: '#4B0080'},
                    {shape: revTriangle, fill: '#F6910E'},
                    {shape: revTriangle, fill: '#0EF611'},
                    {shape: revTriangle, fill: '#F60E1E'},
                    // revTriangle
                    {shape: hexagon, fill: '#006980'},
                    {shape: hexagon, fill: '#4B0080'},
                    {shape: hexagon, fill: '#F6910E'},
                    {shape: hexagon, fill: '#0EF611'},
                    {shape: hexagon, fill: '#F60E1E'},
                    // hexagon
                    {shape: fallback, fill: '#000000'} // fallback
                ];
                var cache = {};
                return function(bioMarker) {
                    var icon = cache[bioMarker];
                    if (typeof cache[bioMarker] === 'undefined') {
                        var itemsInCache = Object.keys(cache).length;
                        icon = iconTable[itemsInCache >= iconTable.length - 1 ?
                            iconTable[iconTable.length - 1] : itemsInCache];
                        cache[bioMarker] = icon;
                    }
                    // FIXME: for testing until we got real data
                    icon = iconTable[Math.floor(Math.random() * iconTable.length)];
                    return icon;
                };
            }
            var iconGen = iconGenerator();


            function renderNumericPlots() {
                byType.filterExact('numeric');
            }

            function renderCategoricPlots() {
                byType.filterExact('categoric');
                var iconSize = 1 / byPatientID.top(Infinity).length * CAT_PLOTS_HEIGHT;
                // temporary dimensions because we want to keep filters within this function scope
                var tmpByPatientID = cf.dimension(function(d) { return d.patientID; });
                var tmpByTimeInteger = cf.dimension(function(d) { return d.timeInteger; });

                var id = 0;
                var catPlotInfo = smartRUtils.unique(getValuesForDimension(byPatientID)).map(function(patientID) {
                    tmpByPatientID.filterExact(patientID);
                    var maxCount = 0;
                    var times = smartRUtils.unique(getValuesForDimension(byTimeInteger));
                    times.forEach(function(time) {
                        tmpByTimeInteger.filterExact(time);
                        var count = byValue.top(Infinity).length;
                        maxCount = count > maxCount ? count : maxCount;
                        // we need to disable this filter temporarily, otherwise it will affect the next iteration step
                        tmpByTimeInteger.filterAll();
                    });
                    return {id: id++, patientID: patientID, maxDensity: maxCount};
                });

                catPlotInfo.forEach(function(d) {
                    d.height = d.maxDensity * iconSize;
                    tmpByPatientID.filterExact(d.patientID);
                    d.subset = smartRUtils.unique(getValuesForDimension(bySubset));
                });

                // we don't dispose them because we need them again and dimension creation is expensive
                tmpByTimeInteger.filterAll();
                tmpByPatientID.filterAll();

                var catPlot = svg.selectAll('.sr-linegraph-cat-plot')
                    .data(catPlotInfo, function(d) { return d.id; });

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

                // UPDATE text
                catPlot.select('text')
                    .style('font-size', function(d) { return d.height + 'px'; })
                    .attr('x', 0)
                    .attr('y', function(d) { return d.height / 2; });

                // UPDATE rect
                catPlot.select('rect')
                    .attr('height', function(d) { return d.height; });

                // EXIT g
                catPlot.exit().remove();

                // start ENTER UPDATE EXIT cycle for each separate plot to render data points
                d3.selectAll('.sr-linegraph-cat-plot').each(function(d) {
                    tmpByPatientID.filterExact(d.patientID);
                    // a filtered & sorted list to determine the placement within a patient row
                    var bioMarkerToRender = groupBioMarker.all()
                        .filter(function(d) { return d.value > 0; })
                        .sort(function(a, b) {
                            var sortValue = a.value - b.value;
                            return sortValue === 0 ? a.key.localeCompare(b.key) : sortValue;
                        })
                        .map(function(d) { return d.key; });
                    // DATA JOIN
                    var icon = d3.select(this).selectAll('.sr-linegraph-cat-icon')
                        .data(byBioMarker.top(Infinity), function(d) { return d.id; });

                    // ENTER path
                    icon.enter()
                        .append('path')
                        .attr('class', function(d) {
                            return 'sr-linegraph-cat-icon' +
                                ' patientid-' + smartRUtils.makeSafeForCSS(d.patientID) +
                                ' time-' + smartRUtils.makeSafeForCSS(d.timeInteger) +
                                ' biomarker-' + smartRUtils.makeSafeForCSS(d.bioMarker) +
                                ' subset-' + d.subset;
                        })
                        .style('fill', function(d) { return iconGen(d.bioMarker).fill; })
                        .on('mouseover', function(d) {
                            tip.show(JSON.stringify(d)); // TODO
                        })
                        .on('mouseout', tip.hide);

                    // UPDATE path
                    icon.attr('d', function(d) { return iconGen(d.bioMarker).shape(iconSize); })
                        .attr('transform', function(d) {
                            return 'translate(' + (x(d.timeInteger) - iconSize / 2) + ',' + 0 + ')';
                        });
                });

                // drop temporary filters
                tmpByPatientID.dispose();
                tmpByTimeInteger.dispose();
                // reset other filters
                byType.filterAll();
            }
            renderCategoricPlots();
        }
    }
]);
