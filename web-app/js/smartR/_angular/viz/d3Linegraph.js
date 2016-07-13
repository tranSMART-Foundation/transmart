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

            var MARGIN = {
                top: scope.height * 0.1,
                right: scope.width * 0.1,
                bottom: scope.height * 0.1,
                left: scope.width * 0.1
            };
            var LINEGRAPH_WIDTH = scope.width - MARGIN.left - MARGIN.right;
            var LINEGRAPH_HEIGHT = scope.height - MARGIN.top - MARGIN.bottom;

            /**
             * In this section we compute the plot sizes
             */

            byType.filterExact('categoric');
            var catBoxes = smartRUtils.unique(byPatientID.top(Infinity)).length;
            var catDataDoExist = catBoxes > 0;
            byType.filterExact('numeric');
            var numBoxes = smartRUtils.unique(byBioMarker.top(Infinity)).length;
            var numDataDoExist = numBoxes > 0;
            byType.filterAll();

            var CAT_PLOTS_HEIGHT = 0;
            var NUM_PLOTS_HEIGHT = 0;
            var CAT_PLOTS_POS = 0;
            var NUM_PLOTS_POS = 0;
            var TIME_AXIS_POS = 0;

            var _MAX_CAT_BOX_HEIGHT = 30;
            var _MAX_NUM_BOX_HEIGHT = 100;
            if (catDataDoExist && !numDataDoExist) {
                if (LINEGRAPH_HEIGHT / catBoxes > _MAX_CAT_BOX_HEIGHT) {
                    CAT_PLOTS_HEIGHT = _MAX_CAT_BOX_HEIGHT * catBoxes;
                } else {
                    CAT_PLOTS_HEIGHT = LINEGRAPH_HEIGHT;
                }
                TIME_AXIS_POS = CAT_PLOTS_POS + CAT_PLOTS_HEIGHT;
            } else if (!catDataDoExist && numDataDoExist) {
                if (LINEGRAPH_HEIGHT / numBoxes > _MAX_NUM_BOX_HEIGHT) {
                    NUM_PLOTS_HEIGHT = _MAX_NUM_BOX_HEIGHT * numBoxes;
                } else {
                    NUM_PLOTS_HEIGHT = LINEGRAPH_HEIGHT;
                }
                TIME_AXIS_POS = NUM_PLOTS_POS + NUM_PLOTS_HEIGHT;
            } else {
                if (LINEGRAPH_HEIGHT / 2 / catBoxes > _MAX_CAT_BOX_HEIGHT) {
                    CAT_PLOTS_HEIGHT = _MAX_CAT_BOX_HEIGHT * catBoxes;
                } else {
                    CAT_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2;
                }

                if (LINEGRAPH_HEIGHT / 2 / numBoxes > _MAX_NUM_BOX_HEIGHT) {
                    NUM_PLOTS_HEIGHT = _MAX_NUM_BOX_HEIGHT * numBoxes;
                } else {
                    NUM_PLOTS_HEIGHT = LINEGRAPH_HEIGHT / 2;
                }

                CAT_PLOTS_POS = NUM_PLOTS_POS + NUM_PLOTS_HEIGHT;
                TIME_AXIS_POS = CAT_PLOTS_POS + CAT_PLOTS_HEIGHT;
            }

            var LEGEND_OFFSET = 10;

            var patientRange = smartRUtils.getElementWithoutEventListeners('sr-linegraph-patient-range');
            patientRange.min = 0;
            patientRange.max = smartRUtils.unique(getValuesForDimension(byPatientID)).length;
            patientRange.value = 25;
            patientRange.step = 1;
            patientRange.addEventListener('input', function() {
                updateShownPatients();
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
                .attr('transform', 'translate(' + 0 + ',' + TIME_AXIS_POS + ')');

            function updateXAxis() {
                // temporary dimension because we don't want to affect the time filter
                var tmpByTimeInteger = cf.dimension(function(d) { return d.timeInteger; });
                var tickFormat = {};
                var longestTimeString = '';
                smartRUtils.unique(getValuesForDimension(byTimeInteger)).forEach(function(timeInteger) {
                    tmpByTimeInteger.filterExact(timeInteger);
                    var timeString = byTimeInteger.top(1)[0].timeString;
                    longestTimeString = timeString.length > longestTimeString.length ? timeString : longestTimeString;
                    tickFormat[timeInteger] = timeString;
                });
                tmpByTimeInteger.dispose();

                var xAxis = d3.svg.axis()
                    .scale(x)
                    .tickFormat(function(d) { return tickFormat[d]; });

                var offset = 8;
                var textRotation = 30;

                var axisFontSize = smartRUtils.scaleFont(longestTimeString,
                    {}, 30, MARGIN.bottom - offset - 10, 90 - textRotation, 1);

                d3.select('.sr-linegraph-x-axis')
                    .call(xAxis)
                    .selectAll('text')
                    .attr('dy', '.35em')
                    .attr('transform', 'translate(0,' + offset + ')rotate(' + textRotation + ')')
                    .style('text-anchor', 'start')
                    .style('font-size', axisFontSize + 'px');
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
                byType.filterExact('numeric');
                if (byPatientID.top(Infinity).length === 0) { return; }
                var bioMarkers = smartRUtils.unique(getValuesForDimension(byBioMarker))
                    .sort(function(a, b) { return a.localeCompare(b); }); // for determinism

                var numPlotBoxHeight = NUM_PLOTS_HEIGHT / bioMarkers.length;

                // DATA JOIN
                var numPlotBox = svg.selectAll('.sr-linegraph-num-plot')
                    .data(bioMarkers);

                // ENTER g
                var numPlotBoxEnter = numPlotBox.enter()
                    .append('g')
                    .attr('class', function(d) { return 'sr-linegraph-num-plot biomarker-' + d; })
                    .attr('transform', function(d) {
                        return 'translate(' + 0 + ',' +
                            (NUM_PLOTS_POS + bioMarkers.indexOf(d) * numPlotBoxHeight) + ')';
                    });

                // ENTER rect
                numPlotBoxEnter.append('rect')
                    .attr('width', LINEGRAPH_WIDTH)
                    .attr('height', numPlotBoxHeight);

                byType.filterAll();
            }
            renderNumericPlots();

            function renderCategoricPlots() {
                byType.filterExact('categoric');
                if (byPatientID.top(Infinity).length === 0) { return; }
                var iconSize = 1 / byPatientID.top(Infinity).length * CAT_PLOTS_HEIGHT;
                // temporary dimensions because we want to keep filters within this function scope
                var tmpByPatientID = cf.dimension(function(d) { return d.patientID; });
                var tmpByTimeInteger = cf.dimension(function(d) { return d.timeInteger; });

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
                    return {patientID: patientID, maxDensity: maxCount};
                });

                catPlotInfo.forEach(function(d) {
                    d.height = d.maxDensity * iconSize;
                    tmpByPatientID.filterExact(d.patientID);
                    d.subset = smartRUtils.unique(getValuesForDimension(bySubset));
                });

                // we don't dispose them because we need them again and dimension creation is expensive
                tmpByTimeInteger.filterAll();
                tmpByPatientID.filterAll();

                var patientIDs = byPatientID.top(Infinity);
                var rowHeight = 1 / patientIDs.length * CAT_PLOTS_HEIGHT;
                var patientIDFontSize = smartRUtils.scaleFont(
                    patientIDs[0], {}, rowHeight * 2 / 3, MARGIN.left - 10, 0, 1);
                var legendItemSize = 20; // TODO

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
                var catPlotExit = catPlot.exit()
                    .remove();

                catPlotExit.select('rect')
                    .attr('y', 0);

                /**
                 * ICON SECTION
                 */

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
                var legendTextSize = smartRUtils.scaleFont(longestBioMarker, {}, legendItemSize,
                        MARGIN.right - LEGEND_OFFSET - legendItemSize, 0, 2);

                // DATA JOIN
                var legendItem = svg.selectAll('.sr-linegraph-legend-item')
                    .data(legendData, function(d) { return d.id; });

                // ENTER g
                var legendItemEnter = legendItem.enter()
                    .append('g')
                    .attr('class', 'sr-linegraph-legend-item')
                    .attr('transform', function(d, i) {
                        return 'translate(' + (LINEGRAPH_WIDTH + LEGEND_OFFSET) + ',' +
                            (CAT_PLOTS_POS + i * legendItemSize) + ')';
                    });

                // ENTER path
                legendItemEnter.append('path')
                    .attr('d', function(d) { return d.shape(legendItemSize); })
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
                    .attr('x', LEGEND_OFFSET + legendItemSize)
                    .attr('y', legendItemSize / 2)
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
