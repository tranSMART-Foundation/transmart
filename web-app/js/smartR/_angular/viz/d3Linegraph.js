//# sourceURL=d3Linegraph.js

'use strict';

window.smartRApp.directive('lineGraph', [
    'smartRUtils',
    '$rootScope',
    function(smartRUtils, $rootScope) {

        return {
            restrict: 'E',
            scope: {
                data: '='
            },
            templateUrl: $rootScope.smartRPath + '/js/smartR/_angular/templates/linegraph.html',
            link: function (scope, element) {
                var template_ctrl = element.children()[0],
                    template_viz = element.children()[1];

                scope.$watch('data', function() {
                    $(template_viz).empty();
                    if (! $.isEmptyObject(scope.data)) {
                        smartRUtils.prepareWindowSize(1000, 1000);
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

            // these dimensions are used temporarily, e.g. in function calls
            var tmpByType = dataCF.dimension(function(d) { return d.type; });
            var tmpByTimeInteger = dataCF.dimension(function(d) { return d.timeInteger; });
            var tmpByBioMarker = dataCF.dimension(function(d) { return d.bioMarker; });
            var tmpByPatientID = dataCF.dimension(function(d) { return d.patientID; });
            var tmpByRanking = dataCF.dimension(function(d) { return d.ranking; });
            var tmpBySubset = dataCF.dimension(function(d) { return d.subset; });

            var groupByPatientIDRanking = byPatientID.group()
                .reduceSum(function(d) { return typeof d.ranking === 'undefined' ? 0 : d.ranking; });

            var plotTypeSelect = smartRUtils.getElementWithoutEventListeners('sr-lg-numplottype-select');
            plotTypeSelect.selectedIndex = 0;
            plotTypeSelect.addEventListener('change', function() {
                renderNumericPlots();
            });

            var evenlyCheck = smartRUtils.getElementWithoutEventListeners('sr-lg-evenly-check');
            evenlyCheck.checked = false;
            evenlyCheck.addEventListener('change', function() {
                calculateXScale();
                renderNumericPlots();
                renderCategoricPlots();
            });

            var smoothCheck = smartRUtils.getElementWithoutEventListeners('sr-lg-smooth-check');
            smoothCheck.checked = false;
            smoothCheck.addEventListener('change', function() {
                renderNumericPlots();
            });

            var patientRange = smartRUtils.getElementWithoutEventListeners('sr-lg-patient-range');
            patientRange.min = 0;
            patientRange.max = smartRUtils.unique(getValuesForDimension(byPatientID)).length;
            patientRange.value = 35;
            patientRange.step = 1;
            patientRange.addEventListener('input', function() {
                renderCategoricPlots();
                computeCatPlotsHeight();
                updateXAxis();
            });

            // convert string to string to numeric type for numeric data
            tmpByType.filterExact('numeric');
            var totalNumOfNumBoxes = smartRUtils.unique(getValuesForDimension(tmpByBioMarker)).length;
            var data = tmpByType.bottom(Infinity);
            dataCF.remove();
            data.forEach(function(d) { d.value = parseInt(d.value); });
            dataCF.add(data);
            tmpByType.filterExact('categoric');
            var totalNumOfCatBoxes = smartRUtils.unique(getValuesForDimension(byPatientID)).length;
            tmpByType.filterAll();

            var NUM_PLOT_HEIGHT = 200;
            var NUM_PLOT_PADDING = 50;
            var ICON_SIZE = 20;

            var NUM_PLOTS_HEIGHT = totalNumOfNumBoxes * (NUM_PLOT_HEIGHT + NUM_PLOT_PADDING);

            var CAT_PLOTS_POS = NUM_PLOTS_HEIGHT;

            var LEGEND_OFFSET = 10;

            var ERROR_BAR_WIDTH = 5;
            var MAX_XAXIS_ELEMENT_WIDTH = 40;
            var TICK_HEIGHT = 8;

            var MARGIN = {
                top: 50,
                right: 200,
                bottom: 100,
                left: 100
            };

            var LINEGRAPH_WIDTH = document.getElementById('sr-index').getBoundingClientRect().width - MARGIN.left - MARGIN.right;
            var LINEGRAPH_HEIGHT = NUM_PLOTS_HEIGHT - MARGIN.top - MARGIN.bottom;

            var ANIMATION_DURATION = 500;
            var tmp_animation_duration = ANIMATION_DURATION;
            // set to 0 for creating the plot initially
            ANIMATION_DURATION = 0;

            function computeCatPlotsHeight() {
                var catPlotsHeight = d3.selectAll('.sr-lg-cat-plot rect').data()
                    .reduce(function(prev, curr) { return prev + curr.height; }, 0);
                LINEGRAPH_HEIGHT = NUM_PLOTS_HEIGHT + catPlotsHeight - MARGIN.top - MARGIN.bottom;
                d3.select(vizDiv).select('svg').attr('height', NUM_PLOTS_HEIGHT + catPlotsHeight + MARGIN.top + MARGIN.bottom);
                return catPlotsHeight;
            }

            var x = d3.scale.linear();
            // recomputes x scale for current filters
            function calculateXScale() {
                tmpByType.filterExact('categoric');
                var padding = totalNumOfCatBoxes > 0 ? ICON_SIZE : MAX_XAXIS_ELEMENT_WIDTH / 2;
                tmpByType.filterAll();
                var times = smartRUtils.unique(getValuesForDimension(byTimeInteger)).sort(function(a, b) {
                    return a - b;
                });
                if (evenlyCheck.checked) {
                    var range = d3.range(padding,
                            LINEGRAPH_WIDTH - padding + (LINEGRAPH_WIDTH - 2 * padding) / times.length,
                            (LINEGRAPH_WIDTH - 2 * padding) / (times.length - 1));
                    x.domain(times).range(range);
                } else {
                    x.domain(d3.extent(times)).range([padding, LINEGRAPH_WIDTH - padding]);
                }
                updateXAxis(); // after changing the scale we need to update the x axis too
            }
            calculateXScale();

            // helper function
            d3.selection.prototype.moveToFront = function() {  
                return this.each(function(){
                    this.parentNode.appendChild(this);
                });
            };

            function getValuesForDimension(dimension, ascendingOrder) {
                var values = [];
                if (typeof ascendingOrder === 'undefined' || !ascendingOrder) {
                    values = dimension.top(Infinity).map(function(record) { return dimension.accessor(record); });
                } else {
                    values =dimension.bottom(Infinity).map(function(record) { return dimension.accessor(record); });
                }

                return values;
            }

            var svg = d3.select(vizDiv).append('svg')
                .attr('width', LINEGRAPH_WIDTH + MARGIN.left + MARGIN.right)
                .attr('height', LINEGRAPH_HEIGHT + MARGIN.top + MARGIN.bottom)
                .append('g')
                .attr('transform', 'translate(' + MARGIN.left + ',' + MARGIN.top + ')');

            var tip = d3.tip()
                .attr('class', 'd3-tip')
                .html(function(d) { return d; });

            svg.call(tip);

            svg.append('g')
                .attr('class', 'sr-lg-x-axis')
                .attr('transform', 'translate(' + 0 + ',' + NUM_PLOT_HEIGHT + ')');

            // WARNING: using this function will reset all global filters to make sure all data are modified correctly
            function swapTimeIntegerData(fromTimeInteger, toTimeInteger) {
                byPatientID.filterAll();
                byTimeInteger.filterAll();

                byTimeInteger.filterExact(fromTimeInteger);
                var fromEntries = byTimeInteger.bottom(Infinity);
                dataCF.remove();
                byTimeInteger.filterExact(toTimeInteger);
                var toEntries = byTimeInteger.bottom(Infinity);
                dataCF.remove();
                byTimeInteger.filterAll();

                fromEntries.forEach(function(d) { d.timeInteger = toTimeInteger; });
                toEntries.forEach(function(d) { d.timeInteger = fromTimeInteger; });

                dataCF.add(fromEntries);
                dataCF.add(toEntries);
            }

            // WARNING: using this function will reset all global filters to make sure all data are modified correctly
            function swapBioMarkerRanking(fromBioMarker, toBioMarker) {
                byPatientID.filterAll();
                byTimeInteger.filterAll();

                tmpByType.filterExact('categoric');
                tmpByBioMarker.filterExact(fromBioMarker);
                var fromEntries = byTimeInteger.bottom(Infinity);
                var fromRanking = fromEntries[0].ranking;
                dataCF.remove();
                tmpByBioMarker.filterExact(toBioMarker);
                var toEntries = byTimeInteger.bottom(Infinity);
                var toRanking = toEntries[0].ranking;
                dataCF.remove();
                tmpByType.filterAll();
                tmpByBioMarker.filterAll();

                fromEntries.forEach(function(d) { d.ranking = toRanking; });
                toEntries.forEach(function(d) { d.ranking = fromRanking; });

                dataCF.add(fromEntries);
                dataCF.add(toEntries);
            }

            function updateXAxis() {
                var timeAxisData = smartRUtils.unique(byTimeInteger.bottom(Infinity), function(d) { return d.timeInteger; });

                // compute size of time axis elements
                var timeAxisElementWidth = timeAxisData.reduce(function(prev, curr, idx) {
                    if (idx === 0) { return prev; }
                    var dist = x(timeAxisData[idx].timeInteger) - x(timeAxisData[idx-1].timeInteger);
                    return dist < prev ? dist : prev;
                }, MAX_XAXIS_ELEMENT_WIDTH);

                var xAxis = d3.svg.axis()
                    .scale(x)
                    .tickFormat('')
                    .tickValues(timeAxisData.map(function(d) { return d.timeInteger; }))
                    .innerTickSize(- NUM_PLOTS_HEIGHT - computeCatPlotsHeight());
                d3.select('.sr-lg-x-axis')
                    .attr('transform', function() {
                        return 'translate(0,' + (computeCatPlotsHeight() + NUM_PLOTS_HEIGHT) + ')';
                    })
                    .transition()
                    .duration(ANIMATION_DURATION)
                    .call(xAxis);

                var drag = d3.behavior.drag()
                    .on('drag', function() {
                        permitHighlight = false;
                        var newX = d3.event.x;
                        newX = newX < 0 ? 0 : newX;
                        newX = newX > LINEGRAPH_WIDTH ? LINEGRAPH_WIDTH : newX;
                        d3.select(this).attr('transform', 'translate(' + (newX) + ',' + TICK_HEIGHT + ')');
                    })
                    .on('dragend', function(draggedElement) {
                        var xPos = d3.transform(d3.select(this).attr('transform')).translate[0];
                        timeAxisData = smartRUtils.unique(byTimeInteger.bottom(Infinity), function(d) { return d.timeInteger; });
 
                        var timeZones = timeAxisData.map(function(d, i) {
                            var left = i === 0 ?
                                0 : x(timeAxisData[i-1].timeInteger) + (x(d.timeInteger) - x(timeAxisData[i-1].timeInteger)) / 2;
                            var right = i === timeAxisData.length - 1 ? 
                                LINEGRAPH_WIDTH : x(d.timeInteger) + (x(timeAxisData[i+1].timeInteger) - x(d.timeInteger)) / 2;
                            return {left: left, right: right, timeInteger: d.timeInteger, timeString: d.timeString};
                        });

                        var timeIntegers = timeZones.map(function(d) { return d.timeInteger; });
                        var matchingTimeZones = timeZones.filter(function(timeZone) {
                            return timeZone.left <= xPos && xPos <= timeZone.right;
                        });
                        var timeIntegerDestination = matchingTimeZones[0].timeInteger;
                        var timeIntegerOrigin = draggedElement.timeInteger;
                        if (timeIntegerDestination !== timeIntegerOrigin) {
                            var indexDestination = timeIntegers.indexOf(timeIntegerDestination);
                            var indexOrigin = timeIntegers.indexOf(timeIntegerOrigin);

                            // if two consecutive drag events are fired with a distance > 1 then this loop handles this scenario properly
                            var dist = 0;
                            while (Math.abs(dist = indexOrigin - indexDestination) > 0) {
                                var nextIntermediateIndex = indexDestination;
                                if (dist > 1) {
                                    nextIntermediateIndex = indexOrigin - 1;
                                } else if (dist < -1) {
                                    nextIntermediateIndex = indexOrigin + 1;
                                }
                                swapTimeIntegerData(timeIntegers[indexOrigin], timeIntegers[nextIntermediateIndex]);
                                draggedElement.timeInteger = timeIntegers[nextIntermediateIndex];
                                indexOrigin = nextIntermediateIndex;
                            }
                        }
                        updateXAxis();
                        renderNumericPlots();
                        renderCategoricPlots();
                        permitHighlight = true;
                    });

                // DATA JOIN
                var timeAxisElement = d3.select('.sr-lg-x-axis').selectAll('.sr-lg-time-element')
                    .data(timeAxisData, function(d) { return d.timeString; });

                // ENTER g
                var timeAxisElementEnter = timeAxisElement.enter()
                    .append('g')
                    .attr('class', function(d) {
                        return 'sr-lg-time-element timestring-' + smartRUtils.makeSafeForCSS(d.timeString);
                    })
                    .call(drag);

                // ENTER text
                timeAxisElementEnter.append('text')
                    .attr('transform', 'translate(0,0)rotate(90)')
                    .attr('text-anchor', 'start')
                    .attr('dy', '.35em')
                    .attr('font-size', function(d) {
                        return smartRUtils.scaleFont(d.timeString, {}, timeAxisElementWidth, MARGIN.bottom, 0, 1);
                    })
                    .text(function(d) { return d.timeString; });

                // ENTER rect
                timeAxisElementEnter.append('rect')
                    .on('mouseover', function(d) {
                        highlightTimepoint(d.timeString);

                        var g = d3.select(this.parentNode).moveToFront();

                        g.append('polygon')
                            .attr('points', (timeAxisElementWidth / 2)+ ',' + 0 + ' ' +
                                (timeAxisElementWidth / 2) + ',' + (MARGIN.bottom / 2) + ' ' +
                                (timeAxisElementWidth / 2 + 20) + ',' + (MARGIN.bottom * 1/4));

                        g.append('polygon')
                            .attr('points', (- timeAxisElementWidth / 2)+ ',' + 0 + ' ' +
                                (- timeAxisElementWidth / 2) + ',' + (MARGIN.bottom / 2) + ' ' +
                                (- timeAxisElementWidth / 2 - 20) + ',' + (MARGIN.bottom * 1/4));
                    })
                    .on('mouseout', function() {
                        disableHighlightTimepoint();
                        d3.select(this.parentNode).selectAll('polygon')
                            .remove();
                    });

                // UPDATE g
                timeAxisElement.transition()
                    .duration(ANIMATION_DURATION)
                    .attr('transform', function(d) {
                        return 'translate(' + (x(d.timeInteger)) + ',' + (TICK_HEIGHT) + ')';
                    });

                // UPDATE rect
                timeAxisElement.select('rect')
                    .attr('x', - timeAxisElementWidth / 2)
                    .attr('y', 0)
                    .attr('height', MARGIN.bottom)
                    .attr('width', timeAxisElementWidth);
            }

            function iconGenerator() {
                var square = function(size) { return '0,0 ' + size + ',0 ' + size + ',' + size + ' 0,' + size; };
                var triangle = function(size) { return (size / 2) + ',0 ' + size + ',' + size + ' 0,' + size; };
                var diamond = function(size) {
                    return (size / 2) + ',0 ' + size + ',' + (size / 2) + ' ' + (size / 2) + ',' + size + ' 0,' + (size / 2);
                };
                var revTriangle = function(size) { return '0,0 ' + size + ',0 ' + (size / 2) + ',' + size; };
                var hexagon = function(size) {
                    return (size / 2) + ',0 ' + 
                        size + ',' + (size / 4) + ' ' +
                        size + ',' + (size * 3 / 4) + ' ' +
                        (size / 2) + ',' + size + ' ' +
                        '0,' + (size * 3 / 4) + ' ' +
                        '0,' + (size / 4);
                };
                var fallback = function(size) {
                    return (size / 4) + ',' + (size / 4) + ' ' +
                        (size * 3 / 4) + ',' + (size / 4) + ' ' +
                        (size * 3 / 4) + ',' + (size * 3 / 4) + ' ' +
                        (size / 4) + ',' + (size * 3 / 4);
                };
                var iconTable = [
                    {shape: square, fill: '#006980'},
                    {shape: triangle, fill: '#4B0080'},
                    {shape: diamond, fill: '#F6910E'},
                    {shape: revTriangle, fill: '#0EF611'},
                    {shape: hexagon, fill: '#F60E1E'},

                    {shape: triangle, fill: '#006980'},
                    {shape: diamond, fill: '#4B0080'},
                    {shape: revTriangle, fill: '#F6910E'},
                    {shape: hexagon, fill: '#0EF611'},
                    {shape: square, fill: '#F60E1E'},

                    {shape: diamond, fill: '#006980'},
                    {shape: revTriangle, fill: '#4B0080'},
                    {shape: hexagon, fill: '#F6910E'},
                    {shape: square, fill: '#0EF611'},
                    {shape: triangle, fill: '#F60E1E'},

                    {shape: revTriangle, fill: '#006980'},
                    {shape: hexagon, fill: '#4B0080'},
                    {shape: square, fill: '#F6910E'},
                    {shape: triangle, fill: '#0EF611'},
                    {shape: diamond, fill: '#F60E1E'},

                    {shape: hexagon, fill: '#006980'},
                    {shape: square, fill: '#4B0080'},
                    {shape: triangle, fill: '#F6910E'},
                    {shape: diamond, fill: '#0EF611'},
                    {shape: revTriangle, fill: '#F60E1E'},

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
                    return icon;
                };
            }
            var iconGen = iconGenerator();


            function renderNumericPlots() {
                tmpByType.filterExact('numeric');
                if (byTimeInteger.bottom(Infinity).length === 0) {
                    tmpByType.filterAll();
                    return;
                }

                // map the selection to the object key names
                var plotTypeKeys = {
                    meanWithSD: {valueKey: 'mean', errorKey: 'sd'},
                    medianWithSD: {valueKey: 'median', errorKey: 'sd'},
                    meanWithSEM: {valueKey: 'mean', errorKey: 'sem'},
                    medianWithSEM: {valueKey: 'median', errorKey: 'sem'},
                    noGrouping: {valueKey: 'value'}
                };
                var valueKey = plotTypeKeys[plotTypeSelect.value].valueKey;
                var errorKey = plotTypeKeys[plotTypeSelect.value].errorKey;

                var bioMarkers = smartRUtils.unique(getValuesForDimension(tmpByBioMarker))
                    .sort(function(a, b) { return a.localeCompare(b); }); // for determinism

                var numPlotBoxHeight = (NUM_PLOTS_HEIGHT - bioMarkers.length * NUM_PLOT_PADDING) / bioMarkers.length;

                // DATA JOIN
                var numPlotBox = svg.selectAll('.sr-lg-num-plot')
                    .data(bioMarkers);

                // ENTER g
                var numPlotBoxEnter = numPlotBox.enter()
                    .append('g')
                    .attr('class', function(d) { return 'sr-lg-num-plot biomarker-' + smartRUtils.makeSafeForCSS(d); })
                    .attr('transform', function(d) {
                        return 'translate(' + 0 + ',' +
                            (bioMarkers.indexOf(d) * (numPlotBoxHeight + NUM_PLOT_PADDING)) + ')';
                    });

                // ENTER rect (box)
                numPlotBoxEnter.append('rect')
                    .attr('width', LINEGRAPH_WIDTH)
                    .attr('height', numPlotBoxHeight);

                // Add items to each numbox ---
                d3.selectAll('.sr-lg-num-plot').each(function(bioMarker) {
                    var currentNumPlot = d3.select(this);
                    tmpByBioMarker.filterExact(bioMarker);

                    // Add legend items ---
                    // DATA JOIN
                    var numPlotLegend = currentNumPlot.selectAll('.sr-lg-num-legend')
                        .data([1,2], function(d) { return d; });

                    // ENTER g
                    var numPlotLegendEnter = numPlotLegend.enter()
                        .append('g')
                        .attr('class', function(d) { return 'sr-lg-num-legend subset-' + d; })
                        .attr('transform', function(d) {
                            return 'translate(' + (LINEGRAPH_WIDTH + LEGEND_OFFSET) + ',' +
                                (numPlotBoxHeight / 2 + (d === 1 ? - ICON_SIZE : ICON_SIZE)) + ')';
                        })
                        .on('mouseover', function(d) { 
                            d3.selectAll('.sr-lg-boxplot').filter(function() {
                                var that = d3.select(this);
                                return !that.classed('bioMarker-' + smartRUtils.makeSafeForCSS(bioMarker)) ||
                                    !that.classed('subset-' + smartRUtils.makeSafeForCSS(d));
                            }).classed('timeline-lowlight', true);
                            d3.selectAll('.sr-lg-timeline').filter(function() {
                                var that = d3.select(this);
                                return !that.classed('bioMarker-' + smartRUtils.makeSafeForCSS(bioMarker)) ||
                                    !that.classed('subset-' + smartRUtils.makeSafeForCSS(d));
                            }).classed('timeline-lowlight', true);
                        })
                        .on('mouseout', function() {
                            d3.selectAll('.sr-lg-boxplot').classed('timeline-lowlight', false);
                            d3.selectAll('.sr-lg-timeline').classed('timeline-lowlight', false);
                        });

                    // ENTER rect
                    numPlotLegendEnter.append('rect')
                        .attr('height', ICON_SIZE)
                        .attr('width', ICON_SIZE);


                    // ENTER text
                    numPlotLegendEnter.append('text')
                        .attr('x', ICON_SIZE + 5)
                        .attr('y', ICON_SIZE / 2)
                        .attr('dy', '.35em')
                        .style('font-size', smartRUtils.scaleFont('Cohort X', {}, 15, MARGIN.right - ICON_SIZE - 5, 0, 1))
                        .text(function(d) { return 'Cohort ' + d; });
                    // --- Add legend items

                    // Compute y ---
                    var upperBounds = byTimeInteger.bottom(Infinity).map(function(d) {
                        var error = d[errorKey];
                        error = typeof error === 'undefined' ? 0 : error; // there is no error if we plot individuals
                        return d[valueKey] + error;
                    });
                    var lowerBounds = byTimeInteger.bottom(Infinity).map(function(d) {
                        var error = d[errorKey];
                        error = typeof error === 'undefined' ? 0 : error; // there is no error if we plot individuals
                        return d[valueKey] - error;
                    });
                    var boundaries = d3.extent(upperBounds.concat(lowerBounds));
                    var y = d3.scale.linear()
                        .domain(boundaries.slice().reverse())
                        .range([0, numPlotBoxHeight]);
                    var yAxis = d3.svg.axis()
                        .scale(y)
                        .orient('left')
                        .tickValues(function() {
                            var stepSize = (boundaries[1] - boundaries[0]) / 8;
                            var ticks = [];
                            for (var i = 0; i <= 8; i++) {
                                ticks.push(boundaries[0] + stepSize * i);
                            }
                            return ticks;
                        })
                        .innerTickSize(- LINEGRAPH_WIDTH);
                    // --- Compute y

                    // Render y axis ---
                    // DATA JOIN
                    var axis = currentNumPlot.selectAll('.sr-lg-y-axis')
                        .data([bioMarker], function(d) { return d; });

                    // ENTER g
                    var axisEnter = axis.enter()
                        .append('g')
                        .attr('class', 'sr-lg-y-axis');

                    // ENTER text
                    axisEnter.append('text')
                        .attr('text-anchor', 'start')
                        .attr('transform', 'translate(' + (0) + ',' + (-10) + ')')
                        .attr('font-size', '15px')
                        .text(function(d) { return d; });

                    // UPDATE g
                    axis.call(yAxis);
                    // --- Render y axis

                    // Render timeline elements for each subset ---
                    var subsets = smartRUtils.unique(getValuesForDimension(tmpBySubset));
                    // FIXME: Is this loop really necessary?
                    subsets.forEach(function(subset){
                        tmpBySubset.filterExact(subset);

                        // Generate data for timeline elements ---
                        var numericalData = [];
                        
                        if (plotTypeSelect.value === 'noGrouping') {
                            numericalData = smartRUtils.unique(getValuesForDimension(byPatientID)).map(function(patientID) {
                                tmpByPatientID.filterExact(patientID);
                                return byTimeInteger.bottom(Infinity).map(function(d) {
                                    return {
                                        patientID: d.patientID,
                                        bioMarker: d.bioMarker,
                                        timeInteger: d.timeInteger,
                                        timeString: d.timeString,
                                        error: 0,
                                        value: d[valueKey],
                                        subset: d.subset
                                    };
                                });
                                tmpByPatientID.filterAll();
                            });
                        } else {
                            numericalData.push(smartRUtils.unique(byTimeInteger.bottom(Infinity), function(d) {
                                return d.timeInteger;
                            }).map(function(d) {
                                return {
                                    timeInteger: d.timeInteger,
                                    timeString: d.timeString,
                                    error: d[errorKey],
                                    value: d[valueKey],
                                    subset: d.subset
                                }; 
                            }));
                        }
                        // --- Generate data for timeline elements

                        var lineGen = d3.svg.line()
                            .x(function(d) {
                                var offset = plotTypeSelect.value === 'noGrouping' ?
                                    0 : (subset === 1 ? - ERROR_BAR_WIDTH / 2 : ERROR_BAR_WIDTH + 2);
                                return x(d.timeInteger) + offset;
                            })
                            .y(function(d) { return y(d.value); })
                            .interpolate(smoothCheck.checked ? 'basis' : 'linear');

                        // DATA JOIN
                        var timeline = currentNumPlot.selectAll('.sr-lg-timeline' + '.subset-' + subset)
                            .data(numericalData, function(d) { return 'patientid-' + d[0].patientID + ' subset' + d[0].subset; });

                        // ENTER path
                        timeline.enter()
                            .append('path')
                            .attr('class', function(d) {
                                return 'sr-lg-timeline' + 
                                ' subset-' + subset +
                                ' bioMarker-' + smartRUtils.makeSafeForCSS(bioMarker) +
                                ' patientid-' + d[0].patientID; // might or might not be defined
                            })
                            .on('mouseover', function(d) {
                                d3.select(this).moveToFront();
                                var html = '';

                                if (plotTypeSelect.value === 'noGrouping') {
                                    html += 'PatientID: ' + d[0].patientID + '<br/>';
                                    html += 'BioMarker: ' + d[0].bioMarker + '<br/>';
                                }

                                var maxValue = d3.max(d, function(el) { return el.value; });
                                var minValue = d3.min(d, function(el) { return el.value; });
                                html += 'Maximum: ' + maxValue + '<br/>';
                                html += 'Minimum: ' + minValue + '<br/>';

                                var context = d3.select('.sr-lg-num-plot.biomarker-' + smartRUtils.makeSafeForCSS(bioMarker) + ' rect')
                                    .node();
                                tip.direction('e')
                                    .offset(function() {
                                        var yOffset = y(d[d.length - 1].value) - numPlotBoxHeight / 2;
                                        return [yOffset, 0];
                                    })
                                    .show(html, context);
                            })
                            .on('mouseout', function() {
                                tip.hide();
                            });

                        // UPDATE path
                        timeline
                            .transition()
                            .duration(ANIMATION_DURATION)
                            .attr('d', lineGen);

                        // REMOVE path
                        timeline.exit().remove();

                        if (plotTypeSelect.value !== 'noGrouping') {
                            // DATA JOIN
                            var boxplot = currentNumPlot.selectAll('.sr-lg-boxplot.subset-' + subset)
                            // [0] works because all relevant data in the array are identical
                                .data(numericalData[0], function(d) { return d.timeString; });

                            // ENTER g
                            var boxplotEnter = boxplot.enter()
                                .append('g')
                                .attr('class', function(d) {
                                    return 'sr-lg-boxplot' +
                                        ' timestring-' + smartRUtils.makeSafeForCSS(d.timeString) +
                                        ' bioMarker-' + smartRUtils.makeSafeForCSS(bioMarker) +
                                        ' subset-' + subset;
                                })
                                .on('mouseover', function(d) {
                                    var html = '';
                                    for (var key in d) {
                                        if (d.hasOwnProperty(key)) {
                                            html += key + ': ' + d[key] + '<br/>';
                                        }
                                    }
                                    tip.direction('n')
                                        .offset([-10, 0])
                                        .show(html, this);
                                })
                                .on('mouseout', function() {
                                    tip.hide();
                                });

                            // ENTER rect
                            boxplotEnter.append('rect');


                            // UPDATE g
                            boxplot.transition()
                                .duration(ANIMATION_DURATION)
                                .attr('transform', function(d) {
                                    return 'translate(' + (x(d.timeInteger)) + ',' + (y(d.value)) + ')';
                                });

                            // UPDATE rect
                            boxplot.select('rect')
                                .transition()
                                .duration(ANIMATION_DURATION)
                                .attr('height', function(d) {
                                    return y(d.value - (d.error ? d.error : 0)) - y(d.value + (d.error ? d.error : 0));
                                })
                                .attr('width', ERROR_BAR_WIDTH)
                                .attr('x', subset === 1 ? - ERROR_BAR_WIDTH / 2 - 1 : ERROR_BAR_WIDTH / 2)
                                .attr('y', function(d) { return - (y(d.value) - y(d.value + (d.error ? d.error : 0))); });

                            // EXIT g
                            boxplot.exit().remove();
                        } else {
                            currentNumPlot.selectAll('.sr-lg-boxplot.subset-' + subset).remove();
                        }
                    });
                    tmpBySubset.filterAll();
                    // --- Render timeline elements for each subset
                    
                    tmpByBioMarker.filterAll();
                });
                // --- Add items to each numbox

                tmpByType.filterAll();

            }
            renderNumericPlots();

            function renderCategoricPlots() {
                tmpByType.filterExact('categoric');

                if (byTimeInteger.bottom(Infinity).length === 0) {
                    tmpByType.filterAll();
                    return;
                }

                // FIXME: up and down arrow
                // TODO: visualize ranking value
                // show highest ranked patients first
                var topRankedPatients = groupByPatientIDRanking.top(parseInt(patientRange.value)).map(function(d) { return d.key; });

                var catPlotInfo = topRankedPatients.map(function(patientID) {
                    tmpByPatientID.filterExact(patientID);
                    var maxCount = 0;
                    var timeIntegers = smartRUtils.unique(getValuesForDimension(byTimeInteger));
                    timeIntegers.forEach(function(timeInteger) {
                        tmpByTimeInteger.filterExact(timeInteger);
                        var count = byTimeInteger.bottom(Infinity).length;
                        maxCount = count > maxCount ? count : maxCount;
                        // we need to disable this filter temporarily, otherwise it will affect the next iteration step
                        tmpByTimeInteger.filterAll();
                    });
                    tmpByPatientID.filterAll();
                    return {patientID: patientID, maxDensity: maxCount};
                });

                catPlotInfo.forEach(function(d) {
                    d.height = d.maxDensity * ICON_SIZE;
                    tmpByPatientID.filterExact(d.patientID);
                    d.subset = smartRUtils.unique(getValuesForDimension(tmpBySubset));
                    tmpByPatientID.filterAll();
                });


                /**
                 * BOX & PATIENTID SECTION
                 */

                // DATA JOIN
                var catPlot = svg.selectAll('.sr-lg-cat-plot')
                    .data(catPlotInfo, function(d) {
                        return 'patientID-' + d.patientID + ' subset-' + d.subset; // unique identifier for row
                    });

                // ENTER g
                var catPlotEnter = catPlot.enter()
                    .append('g')
                    .attr('class', function(d) {
                        return 'sr-lg-cat-plot' +
                            ' patientid-' + smartRUtils.makeSafeForCSS(d.patientID) +
                            ' subset-' + d.subset;
                    });

                // ENTER rec
                catPlotEnter.append('rect')
                    .attr('width', LINEGRAPH_WIDTH);

                // ENTER text
                catPlotEnter.append('text')
                    .attr('dy', '0.35em')
                    .attr('x', -5)
                    .style('text-anchor', 'end')
                    .style('font-size', '14px')
                    .text(function(d) { return d.patientID; });

                // UPDATE g
                catPlot.attr('transform', function(d, i) {
                    var previousHeight = 0;
                    for (var j = i - 1; j >= 0; j--) {
                        previousHeight += catPlotInfo[j].height;
                    }
                    var y = CAT_PLOTS_POS + previousHeight;
                    return 'translate(' + 0 + ',' + y + ')';
                });

                // UPDATE text
                catPlot.select('text')
                    .attr('y', function(d) { return d.height / 2; });

                // UPDATE rect
                catPlot.select('rect')
                    .attr('height', function(d) { return d.height; });

                // EXIT g
                catPlot.exit().remove();

                /**
                 * ICON SECTION
                 */

                // start ENTER UPDATE EXIT cycle for each separate plot to render data points
                d3.selectAll('.sr-lg-cat-plot').each(function(d) {
                    tmpByPatientID.filterExact(d.patientID);

                    var iconPlacement = {};

                    // DATA JOIN
                    var icon = d3.select(this).selectAll('.sr-lg-cat-icon')
                        .data(tmpByRanking.top(Infinity), function(d) { return d.id; });

                    // ENTER polygon
                    icon.enter()
                        .append('polygon')
                        .attr('class', function(d) {
                            return 'sr-lg-cat-icon' +
                                ' patientid-' + smartRUtils.makeSafeForCSS(d.patientID) +
                                ' timestring-' + smartRUtils.makeSafeForCSS(d.timeString) +
                                ' biomarker-' + smartRUtils.makeSafeForCSS(d.bioMarker) +
                                ' subset-' + d.subset;
                        })
                        .style('fill', function(d) { return iconGen(d.bioMarker).fill; })
                        .on('mouseover', function(d) {
                            var html = '';
                            for (var key in d) {
                                if (d.hasOwnProperty(key)) {
                                    html += key + ': ' + d[key] + '<br/>';
                                }
                            }
                            tip.direction('n')
                                .offset([-10, 0])
                                .show(html, this);
                        })
                        .on('mouseout', function() {
                            tip.hide();
                        });

                    // UPDATE polygon
                    icon.attr('points', function(d) { return iconGen(d.bioMarker).shape(ICON_SIZE); })
                        .attr('transform', function(d) {
                            iconPlacement[d.timeInteger] = typeof iconPlacement[d.timeInteger] === 'undefined' ?
                                0 : iconPlacement[d.timeInteger] + 1;
                            var innerIconRow = iconPlacement[d.timeInteger];
                            return 'translate(' + (x(d.timeInteger) - ICON_SIZE / 2) + ',' + (innerIconRow * ICON_SIZE) + ')';
                        });
                    tmpByPatientID.filterAll();
                });

                /**
                 * CONTROL ELEMENTS SECTION
                 */
//
//                svg.selectAll('.sr-lg-shift-element').remove();
//                svg.append('path')
//                    .attr('class', 'sr-lg-shift-element')
//                    .attr('d', 'M' + (-MARGIN.left + MARGIN.left / 4) + ',' + (CAT_PLOTS_POS) +
//                        'h' + (MARGIN.left / 2) +
//                        'l' + (- MARGIN.left / 4) + ',' + (- MARGIN.left / 3) + 'Z')
//                    .on('click', function() {
//                        renderCategoricPlots();
//                    });
//
//                svg.append('path')
//                    .attr('class', 'sr-lg-shift-element')
//                    .attr('d', 'M' + (-MARGIN.left + MARGIN.left / 4) + ',' + (CAT_PLOTS_POS + CAT_PLOTS_HEIGHT + 10) +
//                        'h' + (MARGIN.left / 2) +
//                        'l' + (- MARGIN.left / 4) + ',' + (MARGIN.left / 3) + 'Z')
//                    .on('click', function() {
//                        renderCategoricPlots();
//                    });
//
                tmpByType.filterAll();

                renderLegend();
                // we need to update the xAxis because the plot size could have changed
                updateXAxis();
            }
            renderCategoricPlots();

            function renderLegend() {
                tmpByType.filterExact('categoric');
                var iconCache = iconGen();
                var legendData = smartRUtils.unique(tmpByRanking.top(Infinity), function(d) { return d.bioMarker; }).map(function(d, i) {
                    return {bioMarker: d.bioMarker, ranking: d.ranking, icon: iconCache[d.bioMarker], row: i};
                });
                tmpByType.filterAll();

                var longestBioMarker = legendData.map(function(d) { return d.bioMarker; })
                    .reduce(function(prev, curr) { return prev.length > curr.length ? prev : curr; }, '');
                var legendTextSize = smartRUtils.scaleFont(longestBioMarker, {}, ICON_SIZE,
                    MARGIN.right - LEGEND_OFFSET - ICON_SIZE, 0, 2);

                var drag = d3.behavior.drag()
                    .on('drag', function(draggedLegendItem) {
                        var newY = d3.event.y;
                        newY = newY < CAT_PLOTS_POS ? CAT_PLOTS_POS : newY;
                        newY = newY > LINEGRAPH_HEIGHT ? LINEGRAPH_HEIGHT : newY;
                        d3.select(this)
                            .attr('transform', 'translate(' + (LINEGRAPH_WIDTH + LEGEND_OFFSET) + ',' + (newY - ICON_SIZE / 2) + ')');
                        var thisRow = draggedLegendItem.row;
                        var thatRow = Math.floor((newY - CAT_PLOTS_POS) / ICON_SIZE);
                        thatRow = thatRow >= legendData.length ? legendData.length - 1 : thatRow;
                        // if we hover over another another
                        if (thisRow !== thatRow) {
                            var dist = 0;
                            while (Math.abs(dist = thisRow - thatRow) > 0) {
                                var nextRow = thatRow;
                                if (dist > 1) {
                                    nextRow = thisRow - 1;
                                } else if (dist < -1) {
                                    nextRow = thisRow + 1;
                                }
                                var nextLegendItem = d3.select('.sr-lg-legend-item.row-' + nextRow);
                                var nextBioMarker = nextLegendItem.data()[0].bioMarker;
                                nextLegendItem.transition()
                                    .duration(ANIMATION_DURATION)
                                    .attr('transform', 'translate(' + (LINEGRAPH_WIDTH + LEGEND_OFFSET) + ',' +
                                        (CAT_PLOTS_POS + thisRow * ICON_SIZE)  + ')');
                                nextLegendItem
                                    .classed('row-' + nextRow, false)
                                    .classed('row-' + thisRow, true);

                                d3.select('.sr-lg-legend-item.biomarker-' + smartRUtils.makeSafeForCSS(draggedLegendItem.bioMarker))
                                    .classed('row-' + thisRow, false)
                                    .classed('row-' + nextRow, true);

                                swapBioMarkerRanking(nextBioMarker, draggedLegendItem.bioMarker);
                                draggedLegendItem.row = nextRow;
                                thisRow = nextRow;
                            }
                        }
                    })
                    .on('dragend', function(draggedLegendItem) {
                        d3.select('.sr-lg-legend-item.biomarker-' + smartRUtils.makeSafeForCSS(draggedLegendItem.bioMarker))
                            .transition()
                            .duration(ANIMATION_DURATION)
                            .attr('transform', 'translate(' + (LINEGRAPH_WIDTH + LEGEND_OFFSET) + ',' +
                                (CAT_PLOTS_POS + draggedLegendItem.row * ICON_SIZE)  + ')');
                        renderCategoricPlots();
                    });

                // DATA JOIN
                var legendItem = svg.selectAll('.sr-lg-legend-item')
                    .data(legendData, function(d) { return d.icon.id; });

                // ENTER g
                var legendItemEnter = legendItem.enter()
                    .append('g')
                    .attr('class', function(d) {
                        return 'sr-lg-legend-item' + ' biomarker-' + smartRUtils.makeSafeForCSS(d.bioMarker) + ' row-' + d.row;
                    })
                    .attr('transform', function(d, i) {
                        return 'translate(' + (LINEGRAPH_WIDTH + LEGEND_OFFSET) + ',' + (CAT_PLOTS_POS + i * ICON_SIZE) + ')';
                    })
                    .on('mouseover', function() { d3.select(this).select('rect').style('opacity', 0.4); })
                    .on('mouseout', function() { d3.select(this).select('rect').style('opacity', 0); })
                    .call(drag);

                // ENTER rect
                legendItemEnter.append('rect')
                    .attr('height', ICON_SIZE)
                    .attr('width', MARGIN.right);

                // ENTER polygon
                legendItemEnter.append('polygon')
                    .attr('points', function(d) { return d.icon.shape(ICON_SIZE); })
                    .style('fill', function(d) { return d.icon.fill; })
                    .on('mouseover', function(d) {
                        svg.selectAll('.sr-lg-cat-icon')
                            .filter('.biomarker-' + smartRUtils.makeSafeForCSS(d.bioMarker))
                            .classed('icon-highlight', true);
                    })
                    .on('mouseout', function() {
                        svg.selectAll('.sr-lg-cat-icon')
                            .classed('icon-highlight', false);
                    });

                // ENTER text
                legendItemEnter.append('text')
                    .attr('x', LEGEND_OFFSET + ICON_SIZE)
                    .attr('y', ICON_SIZE / 2)
                    .attr('dy', '0.35em')
                    .style('font-size', function() { return legendTextSize + 'px'; })
                    .text(function(d) { return d.bioMarker; });

                // EXIT g
                legendItem.exit()
                    .remove();
            }

            var permitHighlight = true;
            function highlightTimepoint(timeString) {
                if (! permitHighlight || plotTypeSelect.value === 'noGrouping') {
                    disableHighlightTimepoint();
                    return;
                }
                // show tooltip for all associated boxplots
                d3.selectAll('.sr-lg-boxplot.timestring-' + smartRUtils.makeSafeForCSS(timeString)).each(function(d) {
                    var tmpTip = d3.tip()
                        .attr('class', 'd3-tip temp-tip')
                        .html(function(d) { return d; });
                    svg.call(tmpTip);

                    var html = '';
                    for (var key in d) {
                        if (d.hasOwnProperty(key)) {
                            html += key + ': ' + d[key] + '<br/>';
                        }
                    }
                    if (d3.select(this).classed('subset-1')) {
                        tmpTip.direction('w')
                            .offset([0, -10])
                            .show(html, this);
                    } else {
                        tmpTip.direction('e')
                            .offset([0, 10])
                            .show(html, this);
                    }
                });

            }

            function disableHighlightTimepoint() {
                d3.selectAll('.temp-tip').remove();
            }

            updateXAxis();
            ANIMATION_DURATION = tmp_animation_duration;
        }
    }
]);

