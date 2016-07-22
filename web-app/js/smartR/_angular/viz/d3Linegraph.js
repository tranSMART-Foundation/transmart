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

            var dataCF = crossfilter(data_matrix);

            // these dimensions are used globally, e.g. for filtering certain patients or bioMarker
            var byPatientID = dataCF.dimension(function(d) { return d.patientID; });
            var byTimeInteger = dataCF.dimension(function(d) { return d.timeInteger; });
            var byBioMarker = dataCF.dimension(function(d) { return d.bioMarker; });
            var bySubset = dataCF.dimension(function(d) { return d.subset; });

            // these dimensions are used temporarily, e.g. in function calls
            var tmpByType = dataCF.dimension(function(d) { return d.type; });
            var tmpByTimeInteger = dataCF.dimension(function(d) { return d.timeInteger; });
            var tmpByBioMarker = dataCF.dimension(function(d) { return d.bioMarker; });
            var tmpByPatientID = dataCF.dimension(function(d) { return d.patientID; });

            var MARGIN = {
                top: scope.height * 0.1,
                right: scope.width * 0.1,
                bottom: scope.height * 0.1,
                left: scope.width * 0.1
            };
            var LINEGRAPH_WIDTH = scope.width - MARGIN.left - MARGIN.right;
            var LINEGRAPH_HEIGHT = scope.height - MARGIN.top - MARGIN.bottom;

            var ERROR_BAR_WIDTH = 5;
            var MAX_XAXIS_ELEMENT_WIDTH = 40;
            var TICK_HEIGHT = 8;

            /**
             * In this section where we compute the plot sizes
             */

            tmpByType.filterExact('categoric');
            var catBoxes = smartRUtils.unique(getValuesForDimension(byPatientID)).length;
            var catDataDoExist = catBoxes > 0;
            tmpByType.filterExact('numeric');
            var numBoxes = smartRUtils.unique(getValuesForDimension(byBioMarker)).length;
            var numDataDoExist = numBoxes > 0;
            tmpByType.filterAll();

            var CAT_PLOTS_HEIGHT = 0;
            var NUM_PLOTS_HEIGHT = 0;
            var CAT_PLOTS_POS = 0;
            var NUM_PLOTS_POS = 0;
            var TIME_AXIS_POS = 0;

            var MAX_CAT_BOX_HEIGHT = 30;
            var MAX_NUM_BOX_HEIGHT = 200;
            if (catDataDoExist && !numDataDoExist) {
                if (LINEGRAPH_HEIGHT / catBoxes > MAX_CAT_BOX_HEIGHT) {
                    CAT_PLOTS_HEIGHT = MAX_CAT_BOX_HEIGHT * catBoxes;
                } else {
                    CAT_PLOTS_HEIGHT = LINEGRAPH_HEIGHT;
                }
                TIME_AXIS_POS = CAT_PLOTS_POS + CAT_PLOTS_HEIGHT;
            } else if (!catDataDoExist && numDataDoExist) {
                if (LINEGRAPH_HEIGHT / numBoxes > MAX_NUM_BOX_HEIGHT) {
                    NUM_PLOTS_HEIGHT = MAX_NUM_BOX_HEIGHT * numBoxes;
                } else {
                    NUM_PLOTS_HEIGHT = LINEGRAPH_HEIGHT;
                }
                TIME_AXIS_POS = NUM_PLOTS_POS + NUM_PLOTS_HEIGHT;
            } else {
                if (LINEGRAPH_HEIGHT / 2 / catBoxes > MAX_CAT_BOX_HEIGHT) {
                    CAT_PLOTS_HEIGHT = MAX_CAT_BOX_HEIGHT * catBoxes;
                } else {
                    CAT_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2;
                }

                if (LINEGRAPH_HEIGHT / 2 / numBoxes > MAX_NUM_BOX_HEIGHT) {
                    NUM_PLOTS_HEIGHT = MAX_NUM_BOX_HEIGHT * numBoxes;
                } else {
                    NUM_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2;
                }

                CAT_PLOTS_POS = NUM_PLOTS_POS + NUM_PLOTS_HEIGHT;
                TIME_AXIS_POS = CAT_PLOTS_POS + CAT_PLOTS_HEIGHT;
            }

            var LEGEND_OFFSET = 10;
            var LEGEND_ITEM_SIZE = 20;

            var plotTypeSelect = smartRUtils.getElementWithoutEventListeners('sr-linegraph-numplottype-select');
            plotTypeSelect.selectedIndex = 0;
            plotTypeSelect.addEventListener('change', function() {
                renderNumericPlots();
            });

            var patientRange = smartRUtils.getElementWithoutEventListeners('sr-linegraph-patient-range');
            patientRange.min = 0;
            patientRange.max = smartRUtils.unique(getValuesForDimension(byPatientID)).length;
            patientRange.value = 25;
            patientRange.step = 1;
            patientRange.addEventListener('input', function() {
                updateShownPatients();
                renderCategoricPlots();
                renderNumericPlots();
            });

            var x = d3.scale.linear();
            // recomputes x scale for current filters
            function calculateXScale() {
                tmpByType.filterExact('categoric');
                var padding = CAT_PLOTS_HEIGHT ? 1 / byPatientID.top(Infinity).length * CAT_PLOTS_HEIGHT : 10;
                tmpByType.filterAll();
                var times = smartRUtils.unique(getValuesForDimension(byTimeInteger)).sort(function(a, b) {
                    return a - b;
                });
                x.domain(d3.extent(times)).range([padding, LINEGRAPH_WIDTH - padding]);
                updateXAxis(); // after changing the scale we need to update the x axis too
            }
            calculateXScale();

            var firstPatientToShow = 0;
            function updateShownPatients() {
                byPatientID.filterAll();
                firstPatientToShow = firstPatientToShow + parseInt(patientRange.value) > parseInt(patientRange.max) ?
                    parseInt(patientRange.max) - parseInt(patientRange.value) : firstPatientToShow;
                var shownPatients = smartRUtils.unique(getValuesForDimension(byPatientID))
                    .slice(firstPatientToShow, firstPatientToShow + parseInt(patientRange.value));
                byPatientID.filterFunction(function(patient) { return shownPatients.indexOf(patient) !== -1; });
                calculateXScale();
            }
            updateShownPatients();

            function getValuesForDimension(dimension, ascendingOrder) {
                if (typeof ascendingOrder === 'undefined' || !ascendingOrder) {
                    return dimension.top(Infinity).map(function(record) { return dimension.accessor(record); });
                }
                return dimension.bottom(Infinity).map(function(record) { return dimension.accessor(record); });
            }

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
                .attr('transform', 'translate(' + 0 + ',' + TIME_AXIS_POS + ')');

            function moveTimePoint(fromTimeInteger, toTimeInteger) {
                var hoveredTimeAxisElement = d3.selectAll('.sr-linegraph-time-element').filter('.timeinteger-' + fromTimeInteger);
                var draggTimeAxisElement = d3.selectAll('.sr-linegraph-time-element').filter('.timeinteger-' + toTimeInteger);
                hoveredTimeAxisElement.data()[0].timeInteger = toTimeInteger;
                draggTimeAxisElement.data()[0].timeInteger = fromTimeInteger;
                hoveredTimeAxisElement.classed('timeinteger-' + fromTimeInteger, false);
                hoveredTimeAxisElement.classed('timeinteger-' + toTimeInteger, true);
                draggTimeAxisElement.classed('timeinteger-' + toTimeInteger, false);
                draggTimeAxisElement.classed('timeinteger-' + fromTimeInteger, true);
                hoveredTimeAxisElement.attr('transform', 'translate(' + (x(toTimeInteger)) + ',' + (TICK_HEIGHT) + ')');
                tmpByTimeInteger.filterExact(toTimeInteger);
                tmpByTimeInteger.top(Infinity).forEach(function(d) { d.timeInteger = fromTimeInteger; });
                tmpByTimeInteger.filterExact(fromTimeInteger);
                tmpByTimeInteger.top(Infinity).forEach(function(d) { d.timeInteger = toTimeInteger; });
                tmpByTimeInteger.filterAll();
                renderNumericPlots();
                renderCategoricPlots();
            }

            function updateXAxis() {
                // temporary dimension because we don't want to affect the time filter
                var timeAxisData = [];
                smartRUtils.unique(getValuesForDimension(byTimeInteger, true)).forEach(function(timeInteger) {
                    tmpByTimeInteger.filterExact(timeInteger);
                    var timeString = byTimeInteger.top(1)[0].timeString;
                    timeAxisData.push({timeInteger: timeInteger, timeString: timeString});
                });
                tmpByTimeInteger.filterAll();

                var potentialSpacePerTimeAxisElement = LINEGRAPH_WIDTH / timeAxisData.length;
                var timeAxisElementWidth = potentialSpacePerTimeAxisElement > MAX_XAXIS_ELEMENT_WIDTH ?
                    MAX_XAXIS_ELEMENT_WIDTH : potentialSpacePerTimeAxisElement;

                var xAxis = d3.svg.axis()
                    .scale(x)
                    .tickFormat('')
                    .tickSize(TICK_HEIGHT, 0);
                d3.select('.sr-linegraph-x-axis')
                    .call(xAxis);

                var timeZones = timeAxisData.map(function(d, i) {
                    var left = i === 0 ? 0 : x(d.timeInteger) - (x(d.timeInteger) - x(timeAxisData[i-1].timeInteger)) / 2;
                    var right = i === timeAxisData.length - 1 ? 
                        LINEGRAPH_WIDTH : x(d.timeInteger) + (x(timeAxisData[i+1].timeInteger) - x(d.timeInteger)) / 2;
                    return {left: left, right: right, timeInteger: d.timeInteger};
                });

                var timeIntegers = timeZones.map(function(d) { return d.timeInteger; });
                var drag = d3.behavior.drag()
                    .on('drag', function(draggedEl) {
                        var newX = d3.event.x;
                        newX = newX < 0 ? 0 : newX;
                        newX = newX > LINEGRAPH_WIDTH ? LINEGRAPH_WIDTH : newX;
                        d3.select(this).attr('transform', 'translate(' + (newX) + ',' + TICK_HEIGHT + ')');

                        var matchingTimeZones = timeZones.filter(function(timeZone) {
                            return timeZone.left <= newX && newX <= timeZone.right;
                        });
                        var timeIntegerHovered = matchingTimeZones[0].timeInteger;
                        if (timeIntegerHovered !== draggedEl.timeInteger) {
                            var indexHovered = timeIntegers.indexOf(timeIntegerHovered);
                            var indexDragged = timeIntegers.indexOf(draggedEl.timeInteger);
                            // this loop fixes a bug that occurs when dragging too fast and timepoints are skipped
                            var dist = null;
                            while (Math.abs(dist = indexDragged - indexHovered) > 0) {
                                var toIndex = indexHovered;
                                if (dist > 1) {
                                    toIndex = indexDragged - 1;
                                } else if (dist < -1) {
                                    toIndex = indexDragged + 1;
                                }
                                moveTimePoint(timeIntegers[toIndex], timeIntegers[indexDragged]);
                                indexDragged = toIndex;
                            }
                        }
                    })
                    .on('dragend', function(draggedEl) {
                        moveTimePoint(draggedEl.timeInteger, draggedEl.timeInteger);
                    });

                // DATA JOIN
                var timeAxisElement = d3.select('.sr-linegraph-x-axis').selectAll('.sr-linegraph-time-element')
                    .data(timeAxisData, function(d) { return d.timeInteger; });

                // ENTER g
                var timeAxisElementEnter = timeAxisElement.enter()
                    .append('g')
                    .attr('class', function(d) {
                        return 'sr-linegraph-time-element' + ' timeinteger-' + d.timeInteger;
                    })
                    .call(drag);

                // ENTER text
                timeAxisElementEnter.append('text')
                    .attr('text-anchor', 'start')
                    .attr('dy', '.35em')
                    .attr('font-size', function(d) {
                        return smartRUtils.scaleFont(d.timeString, {}, timeAxisElementWidth, MARGIN.bottom, 0, 1);
                    })
                    .text(function(d) { return d.timeString; });

                // ENTER rect
                timeAxisElementEnter.append('rect')
                    .on('mouseenter', function(d) {
                        highlightTimepoint(d.timeInteger);
                    })
                    .on('mouseleave', disableHighlightTimepoint);

                // UPDATE g
                timeAxisElement.attr('transform', function(d) {
                    return 'translate(' + (x(d.timeInteger)) + ',' + (TICK_HEIGHT) + ')';
                });

                // UPDATE text
                timeAxisElement.select('text')
                    .attr('transform', function() {
                        return 'translate(' + (0) + ',' + (0) + ')rotate(90)';
                    });

                // UPDATE rect
                timeAxisElement.select('rect')
                    .attr('x', - timeAxisElementWidth / 2)
                    .attr('y', 0)
                    .attr('height', MARGIN.bottom)
                    .attr('width', timeAxisElementWidth);

                // REMOVE g
                timeAxisElement.exit()
                    .remove();
            }
            updateXAxis();

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
                iconTable.forEach(function(d, i) { d.id = i; });

                var cache = {};
                return function(bioMarker) {
                    // if argument not given this will return the whole cache instead
                    if (typeof bioMarker === 'undefined') {
                        return cache;
                    }
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
                if (byPatientID.top(Infinity).length === 0) { return; }
                tmpByType.filterExact('numeric');

                var plotTypeKeys = {
                    meanWithSd: {valueKey: 'mean', errorBarKey: 'sd'},
                    medianWithSd: {valueKey: 'median', errorBarKey: 'sd'}
                };
                var valueKey = plotTypeKeys[plotTypeSelect.value].valueKey;
                var errorBarKey = plotTypeKeys[plotTypeSelect.value].errorBarKey;

                var bioMarkers = smartRUtils.unique(getValuesForDimension(byBioMarker))
                    .sort(function(a, b) { return a.localeCompare(b); }); // for determinism

                var numPlotBoxHeight = NUM_PLOTS_HEIGHT / bioMarkers.length;

                // DATA JOIN
                var numPlotBox = svg.selectAll('.sr-linegraph-num-plot')
                    .data(bioMarkers);

                // ENTER g
                var numPlotBoxEnter = numPlotBox.enter()
                    .append('g')
                    .attr('class', function(d) { return 'sr-linegraph-num-plot biomarker-' + smartRUtils.makeSafeForCSS(d); })
                    .attr('transform', function(d) {
                        return 'translate(' + 0 + ',' +
                            (NUM_PLOTS_POS + bioMarkers.indexOf(d) * numPlotBoxHeight) + ')';
                    });

                // ENTER rect (box)
                numPlotBoxEnter.append('rect')
                    .attr('width', LINEGRAPH_WIDTH)
                    .attr('height', numPlotBoxHeight);

                // ENTER rect (legend cohort 1)
                numPlotBoxEnter.append('rect')
                    .attr('x', LINEGRAPH_WIDTH + LEGEND_OFFSET)
                    .attr('y', numPlotBoxHeight / 2 - LEGEND_ITEM_SIZE)
                    .attr('height', LEGEND_ITEM_SIZE)
                    .attr('width', LEGEND_ITEM_SIZE)
                    .style('fill', '#CCFFCC');

                // ENTER rect (legend cohort 2)
                numPlotBoxEnter.append('rect')
                    .attr('x', LINEGRAPH_WIDTH + LEGEND_OFFSET)
                    .attr('y',  numPlotBoxHeight / 2 + LEGEND_ITEM_SIZE)
                    .attr('height', LEGEND_ITEM_SIZE)
                    .attr('width', LEGEND_ITEM_SIZE)
                    .style('fill', '#FFCCCC');

                // ENTER text (legend cohort 1)
                numPlotBoxEnter.append('text')
                    .attr('x', LINEGRAPH_WIDTH + LEGEND_OFFSET + LEGEND_ITEM_SIZE + 5)
                    .attr('y', numPlotBoxHeight / 2 - LEGEND_ITEM_SIZE * (1 - 0.5))
                    .attr('dy', '.35em')
                    .style('font-size', '15px')
                    .text('Cohort 1');

                // ENTER text (legend cohort 1)
                numPlotBoxEnter.append('text')
                    .attr('x', LINEGRAPH_WIDTH + LEGEND_OFFSET + LEGEND_ITEM_SIZE + 5)
                    .attr('y', numPlotBoxHeight / 2 + LEGEND_ITEM_SIZE * (1 + 0.5))
                    .attr('dy', '.35em')
                    .style('font-size', '15px')
                    .text('Cohort 2');

                // Add items to each numbox ---
                d3.selectAll('.sr-linegraph-num-plot').each(function(bioMarker) {
                    var currentNumPlot = d3.select(this);
                    tmpByBioMarker.filterExact(bioMarker);

                    // Compute y ---
                    var upperBounds = byBioMarker.top(Infinity).map(function(d) { return d[valueKey] + d[errorBarKey]; });
                    var lowerBounds = byBioMarker.top(Infinity).map(function(d) { return d[valueKey] - d[errorBarKey]; });
                    var y = d3.scale.linear()
                        .domain(d3.extent(upperBounds.concat(lowerBounds)).reverse())
                        .range([0, numPlotBoxHeight]);
                    var yAxis = d3.svg.axis()
                        .scale(y)
                        .orient('left');
                    // --- Compute y

                    // Render y axis ---
                    // DATA JOIN
                    var axis = currentNumPlot.selectAll('.sr-linegraph-y-axis')
                        .data([bioMarker], function(d) { return d; });

                    // ENTER g
                    var axisEnter = axis.enter()
                        .append('g')
                        .attr('class', 'sr-linegraph-y-axis');

                    // ENTER text
                    axisEnter.append('text')
                        .attr('text-anchor', 'middle')
                        .attr('transform', 'translate(' + (-30) + ',' + (numPlotBoxHeight / 2) + ')rotate(-90)')
                        .text(function(d) { return d; });

                    // UPDATE g
                    axisEnter.call(yAxis);
                    // --- Render y axis

                    // Render timeline elements for each subset ---
                    var subsets = smartRUtils.unique(getValuesForDimension(bySubset));
                    subsets.forEach(function(subset){
                        bySubset.filterExact(subset);

                        // Generate data for timeline elements ---
                        var timeIntegers = smartRUtils.unique(getValuesForDimension(byTimeInteger));
                        var boxplotData = timeIntegers.map(function(timeInteger) {
                            tmpByTimeInteger.filterExact(timeInteger);

                            var data = byTimeInteger.top(1)[0];
                            var value = data[valueKey];
                            var errorBar = data[errorBarKey];

                            return {timeInteger: timeInteger, errorBar: errorBar, value: value};
                        });
                        tmpByTimeInteger.filterAll();
                        // --- Generate data for timeline elements

                        var lineGen = d3.svg.line()
                            .x(function(d) { return x(d.timeInteger) + (subset === 1 ? - ERROR_BAR_WIDTH / 2 : ERROR_BAR_WIDTH + 2); })
                            .y(function(d) { return y(d.value); });

                        // DATA JOIN
                        var timeline = currentNumPlot.selectAll('.sr-linegraph-timeline.subset-' + subset)
                            .data([boxplotData]);

                        // ENTER path
                        timeline.enter()
                            .append('path')
                            .attr('class', 'sr-linegraph-timeline subset-' + subset);

                        // UPDATE path
                        timeline.attr('d', lineGen);

                        // REMOVE path
                        timeline.exit().remove();

                        // DATA JOIN
                        var boxplot = currentNumPlot.selectAll('.sr-linegraph-boxplot.subset-' + subset)
                            .data(boxplotData, function(d) { return d.timeInteger; });

                        // ENTER g
                        var boxplotEnter = boxplot.enter()
                            .append('g')
                            .attr('class', function(d) {
                                return 'sr-linegraph-boxplot' +
                                    ' timeinteger-' + smartRUtils.makeSafeForCSS(d.timeInteger) +
                                    ' bioMarker-' + smartRUtils.makeSafeForCSS(bioMarker) +
                                    ' subset-' + subset;
                            });

                        // ENTER rect
                        boxplotEnter.append('rect')
                            .on('mouseover', function(d) {
                                tip.show(d); // TODO
                            })
                            .on('mouseout', function() {
                                tip.hide();
                            });

                        // UPDATE g
                        boxplot.attr('transform', function(d) {
                            return 'translate(' + (x(d.timeInteger)) + ',' + (y(d.value)) + ')';
                        });

                        // UPDATE rect
                        boxplot.select('rect')
                            .attr('height', function(d) { return y(d.value - d.errorBar) - y(d.value + d.errorBar); })
                            .attr('width', ERROR_BAR_WIDTH)
                            .attr('x', subset === 1 ? - ERROR_BAR_WIDTH / 2 - 1 : ERROR_BAR_WIDTH / 2)
                            .attr('y', function(d) { return - (y(d.value) - y(d.value + d.errorBar)); });

                        // EXIT g
                        boxplot.exit().remove();

                    });
                    bySubset.filterAll();
                    // --- Render timeline elements for each subset

                    tmpByBioMarker.filterAll();
                });
                // --- Add items to each numbox

                tmpByType.filterAll();
            }
            renderNumericPlots();

            function renderCategoricPlots() {
                if (byPatientID.top(Infinity).length === 0) { return; }
                tmpByType.filterExact('categoric');
                var iconSize = 1 / byPatientID.top(Infinity).length * CAT_PLOTS_HEIGHT;

                var catPlotInfo = smartRUtils.unique(getValuesForDimension(byPatientID)).map(function(patientID) {
                    tmpByPatientID.filterExact(patientID);
                    var maxCount = 0;
                    var times = smartRUtils.unique(getValuesForDimension(byTimeInteger));
                    times.forEach(function(time) {
                        tmpByTimeInteger.filterExact(time);
                        var count = byBioMarker.top(Infinity).length;
                        maxCount = count > maxCount ? count : maxCount;
                        // we need to disable this filter temporarily, otherwise it will affect the next iteration step
                        tmpByTimeInteger.filterAll();
                    });
                    return {patientID: patientID, maxDensity: maxCount};
                });

                catPlotInfo.forEach(function(d) {
                    d.height = d.maxDensity * iconSize;
                    tmpByPatientID.filterExact(d.patientID);
                    d.subset = smartRUtils.unique(getValuesForDimension(bySubset));
                });

                tmpByTimeInteger.filterAll();
                tmpByPatientID.filterAll();

                var patientIDs = byPatientID.top(Infinity);
                var rowHeight = 1 / patientIDs.length * CAT_PLOTS_HEIGHT;
                var patientIDFontSize = smartRUtils.scaleFont(
                    patientIDs[0], {}, rowHeight * 2 / 3, MARGIN.left - 10, 0, 1);

                /**
                 * BOX & PATIENTID SECTION
                 */

                // DATA JOIN
                var catPlot = svg.selectAll('.sr-linegraph-cat-plot')
                    .data(catPlotInfo, function(d) {
                        return 'patientID-' + d.patientID + ' subset-' + d.subset; // unique identifier for row
                    });

                // ENTER g
                var catPlotEnter = catPlot.enter()
                    .append('g')
                    .attr('class', function(d) {
                        return 'sr-linegraph-cat-plot' +
                            ' patientid-' + smartRUtils.makeSafeForCSS(d.patientID) +
                            ' subset-' + d.subset;
                    });

                // ENTER rec
                catPlotEnter.append('rect')
                    .attr('width', LINEGRAPH_WIDTH)
                    .attr('fill', function(d) {
                        if (d.subset.length === 2) { return '#FFFFCC'; }
                        if (d.subset[0] === 1) { return '#CCFFCC'; }
                        return '#FFCCCC';
                    });

                // ENTER path
                catPlotEnter.append('path');

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
                    var y = CAT_PLOTS_POS + previousHeight;
                    return 'translate(' + 0 + ',' + y + ')';
                });

                // UPDATE text
                catPlot.select('text')
                    .style('font-size', patientIDFontSize)
                    .attr('x', - MARGIN.left + 10)
                    .attr('y', function(d) { return d.height - patientIDFontSize / 2; });

                // UPDATE path
                catPlot.select('path')
                    .attr('d', function(d) {
                        var x1 = 0,
                            y1 = d.height,
                            x2 = - MARGIN.left + 10,
                            y2 = d.height,
                            x3 = Math.cos(225 * Math.PI / 180) * 10 + x2,
                            y3 = Math.sin(225 * Math.PI / 180) * 10 + y2;
                        return 'M' + x1 + ',' + y1 + 'L' + x2 + ',' + y2 + 'L' + x3 + ',' + y3;
                    });

                // UPDATE rect
                catPlot.select('rect')
                    .attr('height', function(d) { return d.height; });

                // EXIT g
                catPlot.exit().remove();

                /**
                 * ICON SECTION
                 */

                // start ENTER UPDATE EXIT cycle for each separate plot to render data points
                d3.selectAll('.sr-linegraph-cat-plot').each(function(d) {
                    tmpByPatientID.filterExact(d.patientID);

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
                        .on('mouseout', function() {
                            tip.hide();
                        });

                    // UPDATE path
                    icon.attr('d', function(d) { return iconGen(d.bioMarker).shape(iconSize); })
                        .attr('transform', function(d) {
                            return 'translate(' + (x(d.timeInteger) - iconSize / 2) + ',' + 0 + ')';
                        });
                });
                tmpByPatientID.filterAll();

                /**
                 * LEGEND SECTION
                 */

                var iconCache = iconGen();
                var legendData = [];
                for (var key in iconCache) {
                    if (iconCache.hasOwnProperty(key)) {
                        var icon = iconCache[key];
                        icon.bioMarker = key;
                        legendData.push(icon);
                    }
                }
                var longestBioMarker = legendData.map(function(d) { return d.bioMarker; })
                    .reduce(function(prev, curr) { return prev.length > curr.length ? prev : curr; }, '');
                var legendTextSize = smartRUtils.scaleFont(longestBioMarker, {}, LEGEND_ITEM_SIZE,
                    MARGIN.right - LEGEND_OFFSET - LEGEND_ITEM_SIZE, 0, 2);

                // DATA JOIN
                var legendItem = svg.selectAll('.sr-linegraph-legend-item')
                    .data(legendData, function(d) { return d.id; });

                // ENTER g
                var legendItemEnter = legendItem.enter()
                    .append('g')
                    .attr('class', 'sr-linegraph-legend-item')
                    .attr('transform', function(d, i) {
                        return 'translate(' + (LINEGRAPH_WIDTH + LEGEND_OFFSET) + ',' +
                            (CAT_PLOTS_POS + i * LEGEND_ITEM_SIZE) + ')';
                    });

                // ENTER path
                legendItemEnter.append('path')
                    .attr('d', function(d) { return d.shape(LEGEND_ITEM_SIZE); })
                    .style('fill', function(d) { return d.fill; })
                    .on('mouseover', function(d) {
                        svg.selectAll('.sr-linegraph-cat-icon')
                            .filter('.biomarker-' + smartRUtils.makeSafeForCSS(d.bioMarker))
                            .classed('icon-highlight', true);
                    })
                    .on('mouseout', function() {
                        svg.selectAll('.sr-linegraph-cat-icon')
                            .classed('icon-highlight', false);
                    });


                // ENTER text
                legendItemEnter.append('text')
                    .attr('x', LEGEND_OFFSET + LEGEND_ITEM_SIZE)
                    .attr('y', LEGEND_ITEM_SIZE / 2)
                    .attr('dy', '0.35em')
                    .style('font-size', function() { return legendTextSize + 'px'; })
                    .text(function(d) { return d.bioMarker; });

                // EXIT g
                legendItem.exit()
                    .remove();

                /**
                 * CONTROL ELEMENTS SECTION
                 */

                svg.selectAll('.sr-linegraph-shift-element').remove();
                svg.append('path')
                    .attr('class', 'sr-linegraph-shift-element')
                    .attr('d', 'M' + (-MARGIN.left + MARGIN.left / 4) + ',' + (CAT_PLOTS_POS) +
                        'h' + (MARGIN.left / 2) +
                        'l' + (- MARGIN.left / 4) + ',' + (- MARGIN.left / 3) + 'Z')
                    .on('click', function() {
                        firstPatientToShow += 5;
                        firstPatientToShow = firstPatientToShow + parseInt(patientRange.value) >
                        parseInt(patientRange.max) ? parseInt(patientRange.max) - parseInt(patientRange.value) :
                            firstPatientToShow;
                        updateShownPatients();
                        renderCategoricPlots();
                    });

                svg.append('path')
                    .attr('class', 'sr-linegraph-shift-element')
                    .attr('d', 'M' + (-MARGIN.left + MARGIN.left / 4) + ',' + (CAT_PLOTS_POS + CAT_PLOTS_HEIGHT + 10) +
                        'h' + (MARGIN.left / 2) +
                        'l' + (- MARGIN.left / 4) + ',' + (MARGIN.left / 3) + 'Z')
                    .on('click', function() {
                        firstPatientToShow -= 5;
                        firstPatientToShow = firstPatientToShow < 0 ? 0 : firstPatientToShow;
                        updateShownPatients();
                        renderCategoricPlots();
                    });

                tmpByType.filterAll();
            }
            renderCategoricPlots();

            function highlightTimepoint(timeInteger) {
                var highlightWidth = document.querySelector('.sr-linegraph-time-element rect').getBBox().width; // HACK
                // DATA JOIN
                var highlightZone = svg.selectAll('.sr-linegraph-highlight-zone')
                    .data(['left', 'right']);

                // ENTER rect
                highlightZone.enter()
                    .append('rect')
                    .attr('class', function(d) { return 'sr-linegraph-highlight-zone ' + d; });

                // UPDATE rect
                highlightZone
                    .attr('height', TIME_AXIS_POS)
                    .attr('width', function(d) {
                        return d === 'left' ? x(timeInteger) - highlightWidth / 2 : LINEGRAPH_WIDTH - x(timeInteger) - highlightWidth / 2;
                    })
                    .attr('x', function(d) {
                        return d === 'left' ? 0 : x(timeInteger) + highlightWidth / 2;
                    })
                    .attr('y', 0);
            }

            function disableHighlightTimepoint() {
                d3.selectAll('.sr-linegraph-highlight-zone').remove();
            }
        }
    }
]);

