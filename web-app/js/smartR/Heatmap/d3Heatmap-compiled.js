'use strict';

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

//# sourceURL=d3Heatmap.js

var animationDuration = 1500;
var tmpAnimationDuration = animationDuration;

function switchAnimation(checked) {
    // general purpose callback, this is why it is not inside SmartRHeatmap
    if (!checked) {
        tmpAnimationDuration = animationDuration;
        animationDuration = 0;
    } else {
        animationDuration = tmpAnimationDuration;
    }
}

SmartRHeatmap = (function () {
    var service = {};

    /**
     * Create smart-r heatmap with data
     * data is json formatted object.
     * @param data
     */
    service.create = function (data) {
        var extraFields = data.extraFields === undefined ? [] : data.extraFields;
        var features = data.features === undefined ? [] : data.features;
        var fields = data.fields;
        var significanceValues = data.significanceValues;
        var patientIDs = data.patientIDs;
        var probes = data.probes;
        var geneSymbols = data.geneSymbols;
        var numberOfClusteredColumns = data.numberOfClusteredColumns[0];
        var numberOfClusteredRows = data.numberOfClusteredRows[0];
        var maxRows = 100;
        var warning = data.warnings === undefined ? '' : data.warnings;

        var rowClustering = false;
        var colClustering = false;

        var originalPatientIDs = patientIDs.slice();
        var originalProbes = probes.slice();

        function redGreen() {
            var colorSet = [];
            var NUM = 100;[].concat(_toConsumableArray(Array(NUM).keys())).reverse().map(function (i) {
                return colorSet.push(d3.rgb(0, 255 * i / NUM, 0));
            });[].concat(_toConsumableArray(Array(NUM).keys())).map(function (i) {
                return colorSet.push(d3.rgb(255 * i / NUM, 0, 0));
            });
            return colorSet;
        }

        var colorSets = [redGreen()];

        var featureColorSetBinary = ['#FF8000', '#FFFF00'];
        var featureColorSetSequential = ['rgb(247,252,253)', 'rgb(224,236,244)', 'rgb(191,211,230)', 'rgb(158,188,218)', 'rgb(140,150,198)', 'rgb(140,107,177)', 'rgb(136,65,157)', 'rgb(129,15,124)', 'rgb(77,0,75)'];

        var gridFieldWidth = 20;
        var gridFieldHeight = 20;
        var dendrogramHeight = 300;
        var histogramHeight = 200;

        var margin = { top: gridFieldHeight * 2 + 100 + features.length * gridFieldHeight / 2 + dendrogramHeight,
            right: gridFieldWidth + 300 + dendrogramHeight,
            bottom: 10,
            left: histogramHeight + 250
        };

        var width = gridFieldWidth * patientIDs.length;
        var height = gridFieldHeight * probes.length;

        var selectedPatientIDs = [];

        var histogramScale = d3.scale.linear().domain(d3.extent(significanceValues)).range([0, histogramHeight]);

        var heatmap = d3.select('#heatmap').append('svg').attr("width", (width + margin.left + margin.right) * 4).attr("height", (height + margin.top + margin.bottom) * 4).append('g').attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

        function adjustDimensions() {
            // gridFieldWidth/gridFieldHeight are adjusted outside as the zoom changes
            $(heatmap[0]).closest('svg').attr('width', margin.left + margin.right + gridFieldWidth * patientIDs.length).attr('height', margin.top + margin.bottom + gridFieldHeight * probes.length);
        }
        adjustDimensions();

        var tooltip = d3.select('#heatmap').append('div').attr('class', 'tooltip text').style('visibility', 'hidden');

        var featureItems = heatmap.append('g');
        var squareItems = heatmap.append('g');
        var colSortItems = heatmap.append('g');
        var patientIDItems = heatmap.append('g');
        var rowSortItems = heatmap.append('g');
        var significanceSortItems = heatmap.append('g');
        var labelItems = heatmap.append('g');
        var barItems = heatmap.append('g');
        var warningDiv = $('#heim-heatmap-warnings').append('strong').text(warning);

        function updateHeatmap() {
            var square = squareItems.selectAll('.square').data(fields, function (d) {
                return 'patientID-' + d.PATIENTID + '-probe-' + d.PROBE;
            });

            square.enter().append('rect').attr('class', function (d) {
                return 'square patientID-' + d.PATIENTID + ' probe-' + d.PROBE;
            }).attr('x', function (d) {
                return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth;
            }).attr('y', function (d) {
                return probes.indexOf(d.PROBE) * gridFieldHeight;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight).attr('rx', 0).attr('ry', 0).style('fill', 'white').on('mouseover', function (d) {
                d3.select('.patientID.patientID-' + d.PATIENTID).classed('highlight', true);
                d3.select('.probe.probe-' + d.PROBE).classed('highlight', true);
                var html = '';
                var _iteratorNormalCompletion = true;
                var _didIteratorError = false;
                var _iteratorError = undefined;

                try {
                    for (var _iterator = Object.keys(d)[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                        var key = _step.value;

                        html += key + ': ' + d[key] + '<br/>';
                    }
                } catch (err) {
                    _didIteratorError = true;
                    _iteratorError = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion && _iterator.return) {
                            _iterator.return();
                        }
                    } finally {
                        if (_didIteratorError) {
                            throw _iteratorError;
                        }
                    }
                }

                tooltip.style('visibility', 'visible').html(html).style('left', mouseX() + 'px').style('top', mouseY() + 'px');
            }).on('mouseout', function () {
                d3.selectAll('.patientID').classed('highlight', false);
                d3.selectAll('.probe').classed('highlight', false);
                tooltip.style('visibility', 'hidden');
            }).on('click', function (d) {
                return window.open('http://www.genecards.org/cgi-bin/carddisp.pl?gene=' + d.GENESYMBOL);
            });

            square.transition().duration(animationDuration).attr('x', function (d) {
                return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth;
            }).attr('y', function (d) {
                return probes.indexOf(d.PROBE) * gridFieldHeight;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight);

            var colSortText = colSortItems.selectAll('.colSortText').data(patientIDs, function (d) {
                return d;
            });

            colSortText.enter().append('text').attr('class', 'text colSortText').attr('x', function (d, i) {
                return i * gridFieldWidth + 0.5 * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight).attr('dy', '0.35em').attr('text-anchor', 'middle').text('↑↓');

            colSortText.transition().duration(animationDuration).attr('x', function (d, i) {
                return i * gridFieldWidth + 0.5 * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight);

            var colSortBox = colSortItems.selectAll('.colSortBox').data(patientIDs, function (d) {
                return d;
            });

            function getValueForSquareSorting(patientID, probe) {
                var square = d3.select('.square.patientID-' + patientID + '.probe-' + probe);
                return square[0][0] != null ? square.data()[0].ZSCORE : Number.NEGATIVE_INFINITY;
            }

            function isSorted(arr) {
                return arr.every(function (d, i) {
                    return i === arr.length - 1 || arr[i][1] <= arr[i + 1][1];
                }) || arr.every(function (d, i) {
                    return i === arr.length - 1 || arr[i][1] >= arr[i + 1][1];
                });
            }

            colSortBox.enter().append('rect').attr('class', 'box colSortBox').attr('x', function (d, i) {
                return i * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight).attr('width', gridFieldWidth).attr('height', gridFieldHeight).on('click', function (patientID) {
                var rowValues = probes.map(function (probe, idx) {
                    return [idx, getValueForSquareSorting(patientID, probe)];
                });
                isSorted(rowValues) ? rowValues.sort(function (a, b) {
                    return a[1] - b[1];
                }) : rowValues.sort(function (a, b) {
                    return b[1] - a[1];
                });
                var sortValues = rowValues.map(function (rowValue) {
                    return rowValue[0];
                });
                updateRowOrder(sortValues);
            });

            colSortBox.transition().duration(animationDuration).attr('x', function (d, i) {
                return i * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight).attr('width', gridFieldWidth).attr('height', gridFieldHeight);

            var rowSortText = rowSortItems.selectAll('.rowSortText').data(probes, function (d) {
                return d;
            });

            rowSortText.enter().append('text').attr('class', 'text rowSortText').attr('transform', function (d, i) {
                return 'translate(' + (width + 2 + 0.5 * gridFieldWidth) + ', 0)translate(0, ' + (i * gridFieldHeight + 0.5 * gridFieldHeight) + ')rotate(-90)';
            }).attr('dy', '0.35em').attr('text-anchor', 'middle').text('↑↓');

            rowSortText.transition().duration(animationDuration).attr('transform', function (d, i) {
                return 'translate(' + (width + 2 + 0.5 * gridFieldWidth) + ', 0)translate(0, ' + (i * gridFieldHeight + 0.5 * gridFieldHeight) + ')rotate(-90)';
            });

            var rowSortBox = rowSortItems.selectAll('.rowSortBox').data(probes, function (d) {
                return d;
            });

            rowSortBox.enter().append('rect').attr('class', 'box rowSortBox').attr('x', width + 2).attr('y', function (d, i) {
                return i * gridFieldHeight;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight).on('click', function (probe) {
                var colValues = patientIDs.map(function (patientID, idx) {
                    return [idx, getValueForSquareSorting(patientID, probe)];
                });
                isSorted(colValues) ? colValues.sort(function (a, b) {
                    return a[1] - b[1];
                }) : colValues.sort(function (a, b) {
                    return b[1] - a[1];
                });
                var sortValues = colValues.map(function (colValue) {
                    return colValue[0];
                });
                updateColOrder(sortValues);
            });

            rowSortBox.transition().duration(animationDuration).attr('x', width + 2).attr('y', function (d, i) {
                return i * gridFieldHeight;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight);

            var significanceSortText = significanceSortItems.selectAll('.significanceSortText').data(['something'], function (d) {
                return d;
            });

            significanceSortText.enter().append('text').attr('class', 'text significanceSortText').attr('x', -gridFieldWidth - 10 + 0.5 * gridFieldWidth).attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight).attr('dy', '0.35em').attr('text-anchor', 'middle').text('↑↓');

            significanceSortText.transition().duration(animationDuration).attr('x', -gridFieldWidth - 10 + 0.5 * gridFieldWidth).attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight);

            var significanceSortBox = significanceSortItems.selectAll('.significanceSortBox').data(['something'], function (d) {
                return d;
            });

            significanceSortBox.enter().append('rect').attr('class', 'box significanceSortBox').attr('x', -gridFieldWidth - 10).attr('y', -2 - gridFieldHeight).attr('width', gridFieldWidth).attr('height', gridFieldHeight).on('click', function () {
                var rowValues = significanceValues.map(function (significanceValue, idx) {
                    return [idx, Math.abs(significanceValue)];
                });
                isSorted(rowValues) ? rowValues.sort(function (a, b) {
                    return a[1] - b[1];
                }) : rowValues.sort(function (a, b) {
                    return b[1] - a[1];
                });
                var sortValues = rowValues.map(function (rowValue) {
                    return rowValue[0];
                });
                updateRowOrder(sortValues);
            });

            significanceSortBox.transition().duration(animationDuration).attr('x', -gridFieldWidth - 10).attr('y', -2 - gridFieldHeight).attr('width', gridFieldWidth).attr('height', gridFieldHeight);

            var selectText = heatmap.selectAll('.selectText').data(patientIDs, function (d) {
                return d;
            });

            selectText.enter().append('text').attr('class', function (d) {
                return 'text selectText patientID-' + d;
            }).attr('x', function (d, i) {
                return i * gridFieldWidth + 0.5 * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight).attr('dy', '0.35em').attr('text-anchor', 'middle').text('□');

            selectText.transition().duration(animationDuration).attr('x', function (d, i) {
                return i * gridFieldWidth + 0.5 * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight);

            var selectBox = heatmap.selectAll('.selectBox').data(patientIDs, function (d) {
                return d;
            });

            selectBox.enter().append('rect').attr('class', 'box selectBox').attr('x', function (d, i) {
                return i * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight * 2).attr('width', gridFieldWidth).attr('height', gridFieldHeight).on('click', function (patientID) {
                return selectCol(patientID);
            });

            selectBox.transition().duration(animationDuration).attr('x', function (d, i) {
                return i * gridFieldWidth;
            }).attr('y', -2 - gridFieldHeight * 2).attr('width', gridFieldWidth).attr('height', gridFieldHeight);

            var patientID = patientIDItems.selectAll('.patientID').data(patientIDs, function (d) {
                return d;
            });

            patientID.enter().append('text').attr('class', function (d) {
                return 'patientID patientID-' + d;
            }).attr('transform', function (d) {
                return 'translate(' + patientIDs.indexOf(d) * gridFieldWidth + ', 0)translate(' + gridFieldWidth / 2 + ', ' + (-4 - gridFieldHeight * 2) + ')rotate(-45)';
            }).style('text-anchor', 'start').text(function (d) {
                return d;
            });

            patientID.transition().duration(animationDuration).attr('transform', function (d) {
                return 'translate(' + patientIDs.indexOf(d) * gridFieldWidth + ', 0)translate(' + gridFieldWidth / 2 + ', ' + (-4 - gridFieldHeight * 2) + ')rotate(-45)';
            });

            var probe = labelItems.selectAll('.probe').data(probes, function (d) {
                return d;
            });

            probe.enter().append('text').attr('class', function (d) {
                return 'probe text probe-' + d;
            }).attr('x', width + gridFieldWidth + 7).attr('y', function (d) {
                return probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight;
            }).attr('dy', '0.35em').style('text-anchor', 'start').text(function (d) {
                return d + ' // ' + geneSymbols[probes.indexOf(d)];
            });

            probe.transition().duration(animationDuration).attr('x', width + gridFieldWidth + 7).attr('y', function (d) {
                return probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight;
            });

            var significanceIndexMap = significanceValues.map(function (significance, idx) {
                return { significance: significance, idx: idx };
            });

            // Visible offset will be effectively _BAR_OFFSET - _MINIMAL_WIDTH
            var _MINIMAL_WIDTH = 10; // This value will be added to the scaled width. So that it is always >0 (visible)
            var _BAR_OFFSET = 20; // Distance between significance bar and the heatmap.

            var bar = barItems.selectAll('.bar').data(significanceIndexMap, function (d) {
                return d.idx;
            });

            bar.enter().append('rect').attr('class', function (d) {
                return 'bar idx-' + d.idx;
            }).attr('width', function (d) {
                return histogramScale(Math.abs(d.significance)) + _MINIMAL_WIDTH;
            }).attr('height', gridFieldHeight).attr('x', function (d) {
                return -histogramScale(Math.abs(d.significance)) - _BAR_OFFSET;
            }).attr('y', function (d) {
                return gridFieldHeight * d.idx;
            }).style('fill', function (d) {
                return d.significance > 0 ? 'steelblue' : '#990000';
            }).on('mouseover', function (d) {
                var html = 'FEATURE SIGNIFICANCE: ' + d.significance;
                tooltip.style('visibility', 'visible').html(html).style('left', mouseX() + 'px').style('top', mouseY() + 'px');
                d3.selectAll('.square.probe-' + probes[d.idx]).classed('squareHighlighted', true);
                d3.select('.probe.probe-' + probes[d.idx]).classed('highlight', true);
            }).on('mouseout', function () {
                tooltip.style('visibility', 'hidden');
                d3.selectAll('.square').classed('squareHighlighted', false);
                d3.selectAll('.probe').classed('highlight', false);
            });

            bar.transition().duration(animationDuration).attr('height', gridFieldHeight).attr('width', function (d) {
                return histogramScale(Math.abs(d.significance)) + _MINIMAL_WIDTH;
            }).attr('x', function (d) {
                return -histogramScale(Math.abs(d.significance)) - _BAR_OFFSET;
            }).attr("y", function (d) {
                return gridFieldHeight * d.idx;
            }).style('fill', function (d) {
                return d.significance > 0 ? 'steelblue' : '#990000';
            });

            var featurePosY = -gridFieldWidth * 2 - getMaxWidth(d3.selectAll('.patientID')) - features.length * gridFieldWidth / 2 - 20;

            var extraSquare = featureItems.selectAll('.extraSquare').data(extraFields, function (d) {
                return 'patientID-' + d.PATIENTID + '-feature-' + d.FEATURE;
            });

            extraSquare.enter().append('rect').attr('class', function (d) {
                return 'extraSquare patientID-' + d.PATIENTID + ' feature-' + d.FEATURE;
            }).attr('x', function (d) {
                return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth;
            }).attr('y', function (d) {
                return featurePosY + features.indexOf(d.FEATURE) * gridFieldHeight / 2;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight / 2).attr('rx', 0).attr('ry', 0).style('fill', 'white').on('mouseover', function (d) {
                d3.select('.patientID.patientID-' + d.PATIENTID).classed('highlight', true);
                d3.select('.feature.feature-' + d.FEATURE).classed('highlight', true);
                var html = '';
                var _iteratorNormalCompletion2 = true;
                var _didIteratorError2 = false;
                var _iteratorError2 = undefined;

                try {
                    for (var _iterator2 = Object.keys(d)[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
                        var key = _step2.value;

                        html += key + ': ' + d[key] + '<br/>';
                    }
                } catch (err) {
                    _didIteratorError2 = true;
                    _iteratorError2 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion2 && _iterator2.return) {
                            _iterator2.return();
                        }
                    } finally {
                        if (_didIteratorError2) {
                            throw _iteratorError2;
                        }
                    }
                }

                tooltip.style('visibility', 'visible').html(html).style('left', mouseX() + 'px').style('top', mouseY() + 'px');
            }).on('mouseout', function () {
                d3.selectAll('.patientID').classed('highlight', false);
                d3.selectAll('.feature').classed('highlight', false);
                tooltip.style('visibility', 'hidden');
            });

            extraSquare.transition().duration(animationDuration).attr('x', function (d) {
                return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth;
            }).attr('y', function (d) {
                return featurePosY + features.indexOf(d.FEATURE) * gridFieldHeight / 2;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight / 2);

            var feature = featureItems.selectAll('.feature').data(features, function (d) {
                return d;
            });

            feature.enter().append('text').attr('class', function (d) {
                return 'feature text feature-' + d;
            }).attr('x', width + gridFieldWidth + 7).attr('y', function (d) {
                return featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4;
            }).attr('dy', '0.35em').style('text-anchor', 'start').text(function (d) {
                return d;
            });

            feature.transition().duration(animationDuration).attr('x', width + gridFieldWidth + 7).attr('y', function (d) {
                return featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4;
            });

            var featureSortText = featureItems.selectAll('.featureSortText').data(features, function (d) {
                return d;
            });

            featureSortText.enter().append('text').attr('class', 'text featureSortText').attr('transform', function (d) {
                return 'translate(' + (width + 2 + 0.5 * gridFieldWidth) + ', 0)translate(0, ' + (featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4) + ')rotate(-90)';
            }).attr('dy', '0.35em').attr('text-anchor', 'middle').text('↑↓');

            featureSortText.transition().duration(animationDuration).attr('transform', function (d) {
                return 'translate(' + (width + 2 + 0.5 * gridFieldWidth) + ', 0)translate(0, ' + (featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4) + ')rotate(-90)';
            });

            var featureSortBox = featureItems.selectAll('.featureSortBox').data(features, function (d) {
                return d;
            });

            featureSortBox.enter().append('rect').attr('class', 'box featureSortBox').attr('x', width + 2).attr('y', function (d) {
                return featurePosY + features.indexOf(d) * gridFieldHeight / 2;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight / 2).on('click', function (feature) {
                var missingValues = false;
                var featureValues = patientIDs.map(function (patientID) {
                    var value = (-Math.pow(2, 32)).toString();
                    try {
                        var _square = d3.select('.extraSquare.patientID-' + patientID + '.feature-' + feature);
                        value = _square.data()[0].VALUE;
                    } catch (err) {
                        missingValues = true;
                    }
                    return [i, value];
                });
                if (isSorted(featureValues)) {
                    featureValues.sort(function (a, b) {
                        var diff = a[1] - b[1];
                        return isNaN(diff) ? a[1].localeCompare(b[1]) : diff;
                    });
                } else {
                    featureValues.sort(function (a, b) {
                        var diff = b[1] - a[1];
                        return isNaN(diff) ? b[1].localeCompare(a[1]) : diff;
                    });
                }
                var sortValues = featureValues.map(function (d) {
                    return d[0];
                });
                if (missingValues) alert('Feature is missing for one or more patients.\nEvery missing value will be set to lowest possible value for sorting');
                updateColOrder(sortValues);
            });

            featureSortBox.transition().duration(animationDuration).attr('x', width + 2).attr('y', function (d, i) {
                return featurePosY + features.indexOf(d) * gridFieldHeight / 2;
            }).attr('width', gridFieldWidth).attr('height', gridFieldHeight / 2);
        }

        function zoom(zoomLevel) {
            zoomLevel /= 100;
            d3.selectAll('.patientID').style('font-size', Math.ceil(14 * zoomLevel) + 'px');
            d3.selectAll('.selectText').style('font-size', Math.ceil(16 * zoomLevel) + 'px');
            d3.selectAll('.uid').style('font-size', Math.ceil(12 * zoomLevel) + 'px');
            d3.selectAll('.feature').style('font-size', Math.ceil(10 * zoomLevel) + 'px');
            d3.selectAll('.significanceSortText, .rowSortText, .colSortText').style('font-size', Math.ceil(14 * zoomLevel) + 'px');
            d3.selectAll('.featureSortText').style('font-size', Math.ceil(10 * zoomLevel) + 'px');
            gridFieldWidth = 20 * zoomLevel;
            gridFieldHeight = 20 * zoomLevel;
            width = gridFieldWidth * patientIDs.length;
            height = gridFieldHeight * probes.length;
            heatmap.attr('width', width + margin.left + margin.right).attr('height', width + margin.top + margin.bottom);
            var temp = animationDuration;
            animationDuration = 0;
            updateHeatmap();
            reloadDendrograms();
            animationDuration = temp;
            adjustDimensions();
        }

        var cutoffLevel = significanceValues[significanceValues.length - 1];
        function animateCutoff(cutoff) {
            cutoffLevel = cutoff;
            significanceValues.forEach(function (significanceValue, i) {
                d3.selectAll('.square.probe-' + probes[i]).classed('cuttoffHighlight', significanceValue < cutoff);
                d3.select('.bar.idx-' + i).classed('cuttoffHighlight', significanceValue < cutoff);
            });
        }

        function cutoff() {
            cuttoffButton.select('text').text('Loading...');
            var nrows = significanceValues.filter(function (significanceValue) {
                return significanceValue > cutoffLevel;
            }).length;
            loadRows(nrows);
        }

        function reloadDendrograms() {
            if (colDendrogramVisible) {
                removeColDendrogram();
                createColDendrogram();
            }
            if (rowDendrogramVisible) {
                removeRowDendrogram();
                createRowDendrogram();
            }
        }

        function selectCol(patientID) {
            var colSquares = d3.selectAll('.square.patientID-' + patientID);
            if (colSquares.classed('selected')) {
                var index = selectedPatientIDs.indexOf(patientID);
                selectedPatientIDs.splice(index, 1);
                colSquares.classed('selected', false);
                d3.select('.selectText.patientID-' + patientID).text('□');
            } else {
                selectedPatientIDs.push(patientID);
                colSquares.classed('selected', true);
                d3.select('.selectText.patientID-' + patientID).text('■');
            }
            selectedPatientIDs.length ? d3.selectAll('.square:not(.selected)').attr('opacity', 0.4) : d3.selectAll('.square').attr('opacity', 1);
        }

        function updateColors(colorIdx) {
            var colorScale = d3.scale.quantile().domain([0, 1]).range(colorSets[colorIdx]);
            d3.selectAll('.square').transition().duration(animationDuration).style('fill', function (d) {
                return colorScale(1 / (1 + Math.pow(Math.E, -d.ZSCORE)));
            });
            var _iteratorNormalCompletion3 = true;
            var _didIteratorError3 = false;
            var _iteratorError3 = undefined;

            try {
                var _loop = function _loop() {
                    var feature = _step3.value;

                    var categoricalColorScale = d3.scale.category10();
                    d3.selectAll('.extraSquare.feature-' + feature).style('fill', function (d) {
                        switch (d.TYPE) {
                            case 'binary':
                                return featureColorSetBinary[d.VALUE];
                            case 'alphabetical':
                                return categoricalColorScale(d.VALUE);
                            default:
                                colorScale.range(featureColorSetSequential);
                                return colorScale(1 / (1 + Math.pow(Math.E, -d.ZSCORE)));
                        }
                    });
                };

                for (var _iterator3 = features[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
                    _loop();
                }
            } catch (err) {
                _didIteratorError3 = true;
                _iteratorError3 = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion3 && _iterator3.return) {
                        _iterator3.return();
                    }
                } finally {
                    if (_didIteratorError3) {
                        throw _iteratorError3;
                    }
                }
            }
        }

        function unselectAll() {
            d3.selectAll('.selectText').text('□');
            d3.selectAll('.square').classed('selected', false).attr('opacity', 1);
            selectedPatientIDs = [];
        }

        var colDendrogramVisible = false;
        var colDendrogram = undefined;
        function createColDendrogram() {
            var w = 200;
            var colDendrogramWidth = gridFieldWidth * numberOfClusteredColumns;
            var spacing = gridFieldWidth * 2 + getMaxWidth(d3.selectAll('.patientID')) + features.length * gridFieldHeight / 2 + 40;

            var cluster = d3.layout.cluster().size([colDendrogramWidth, w]).separation(function () {
                return 1;
            });

            var diagonal = d3.svg.diagonal().projection(function (d) {
                return [d.x, -spacing - w + d.y];
            });

            var colDendrogramNodes = cluster.nodes(colDendrogram);
            var colDendrogramLinks = cluster.links(colDendrogramNodes);

            heatmap.selectAll('.colDendrogramLink').data(colDendrogramLinks).enter().append('path').attr('class', 'colDendrogram link').attr('d', diagonal);

            heatmap.selectAll('.colDendrogramNode').data(colDendrogramNodes).enter().append('circle').attr('class', 'colDendrogram node').attr('r', 4.5).attr('transform', function (d) {
                return 'translate(' + d.x + ', ' + (-spacing - w + d.y) + ')';
            }).on('click', function (d) {
                var previousSelection = selectedPatientIDs.slice();
                unselectAll();
                var leafs = d.index.split(' ');
                leafs.forEach(function (leaf) {
                    return selectCol(patientIDs[leaf]);
                });
                if (arrEqual(previousSelection, selectedPatientIDs)) unselectAll();
            }).on('mouseover', function (d) {
                tooltip.style('visibility', 'visible').html('Height: ' + d.height).style('left', mouseX() + 'px').style('top', mouseY() + 'px');
            }).on('mouseout', function () {
                return tooltip.style('visibility', 'hidden');
            });
            colDendrogramVisible = true;
        }

        var rowDendrogramVisible = false;
        var rowDendrogram = undefined;
        function createRowDendrogram() {
            var h = 280;
            var rowDendrogramHeight = gridFieldWidth * numberOfClusteredRows;
            var spacing = gridFieldWidth + getMaxWidth(d3.selectAll('.probe')) + 20;

            var cluster = d3.layout.cluster().size([rowDendrogramHeight, h]).separation(function () {
                return 1;
            });

            var diagonal = d3.svg.diagonal().projection(function (d) {
                return [width + spacing + h - d.y, d.x];
            });

            var rowDendrogramNodes = cluster.nodes(rowDendrogram);
            var rowDendrogramLinks = cluster.links(rowDendrogramNodes);

            heatmap.selectAll('.rowDendrogramLink').data(rowDendrogramLinks).enter().append('path').attr('class', 'rowDendrogram link').attr('d', diagonal);

            heatmap.selectAll('.rowDendrogramNode').data(rowDendrogramNodes).enter().append('circle').attr('class', 'rowDendrogram node').attr('r', 4.5).attr('transform', function (d) {
                return 'translate(' + (width + spacing + h - d.y) + ', ' + d.x + ')';
            }).on('click', function (d) {
                alert('Under Construction.');
            }).on('mouseover', function (d) {
                tooltip.style('visibility', 'visible').html('Height: ' + d.height).style('left', mouseX() + 'px').style('top', mouseY() + 'px');
            }).on('mouseout', function () {
                return tooltip.style('visibility', 'hidden');
            });
            rowDendrogramVisible = true;
        }

        function removeColDendrogram() {
            heatmap.selectAll('.colDendrogram').remove();
            colDendrogramVisible = false;
        }

        function removeRowDendrogram() {
            heatmap.selectAll('.rowDendrogram').remove();
            rowDendrogramVisible = false;
        }

        function updateColOrder(sortValues) {
            patientIDs = sortValues.map(function (sortValue) {
                return patientIDs[sortValue];
            });
            unselectAll();
            removeColDendrogram();
            updateHeatmap();
        }

        function updateRowOrder(sortValues) {
            var sortedProbes = [];
            var sortedGeneSymbols = [];
            var sortedSignificanceValues = [];
            sortValues.forEach(function (sortValue) {
                sortedProbes.push(probes[sortValue]);
                sortedGeneSymbols.push(geneSymbols[sortValue]);
                sortedSignificanceValues.push(significanceValues[sortValue]);
            });
            probes = sortedProbes;
            geneSymbols = sortedGeneSymbols;
            significanceValues = sortedSignificanceValues;
            removeRowDendrogram();
            updateHeatmap();
            animateCutoff();
        }

        function transformClusterOrderWRTInitialOrder(clusterOrder, initialOrder) {
            return clusterOrder.map(function (d) {
                return initialOrder.indexOf(d);
            });
        }

        function getInitialRowOrder() {
            return probes.map(function (probe) {
                return originalProbes.indexOf(probe);
            });
        }

        function getInitialColOrder() {
            return patientIDs.map(function (patientID) {
                return originalPatientIDs.indexOf(patientID);
            });
        }

        var lastUsedClustering = null;
        function cluster(clustering) {
            // Nothing should be done if clustering switches are turned on without clustering type set.
            if (!lastUsedClustering && typeof clustering === 'undefined') return;
            clustering = typeof clustering === 'undefined' ? lastUsedClustering : clustering;
            var clusterData = data[clustering];
            if (rowClustering && numberOfClusteredRows > 0) {
                rowDendrogram = JSON.parse(clusterData[3]);
                updateRowOrder(transformClusterOrderWRTInitialOrder(clusterData[1], getInitialRowOrder()));
                createRowDendrogram(rowDendrogram);
            } else {
                removeRowDendrogram();
            }
            if (colClustering && numberOfClusteredColumns > 0) {
                colDendrogram = JSON.parse(clusterData[2]);
                updateColOrder(transformClusterOrderWRTInitialOrder(clusterData[0], getInitialColOrder()));
                createColDendrogram(colDendrogram);
            } else {
                removeColDendrogram();
            }
            lastUsedClustering = clustering;
        }

        function loadRows() {
            var maxRows = arguments.length <= 0 || arguments[0] === undefined ? probes.length + 100 : arguments[0];

            var data = prepareFormData();
            data = addSettingsToData(data, { maxRows: maxRows });
            loadFeatureButton.select('text').text('Loading...');
            $.ajax({
                url: pageInfo.basePath + '/SmartR/recomputeOutputDIV',
                type: 'POST',
                timeout: '600000',
                data: data
            }).done(function (response) {
                $('#outputDIV').html(response);
                loadFeatureButton.select('text').text('Load 100 additional rows');
                cuttoffButton.select('text').text('Apply Cutoff');
            }).fail(function () {
                $('#outputDIV').html('An unexpected error occurred. This should never happen. Ask your administrator for help.');
                loadFeatureButton.select('text').text('Load 100 additional rows');
                cuttoffButton.select('text').text('Apply Cutoff');
            });
        }

        function switchRowClustering() {
            rowClustering = !rowClustering;
            cluster();
        }

        function switchColClustering() {
            colClustering = !colClustering;
            cluster();
        }

        function init() {
            updateHeatmap();
            reloadDendrograms();
            updateColors(0);
        }
        init();

        var buttonWidth = 200;
        var buttonHeight = 40;
        var padding = 20;

        createD3Switch({
            location: heatmap,
            onlabel: 'Animation ON',
            offlabel: 'Animation OFF',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 0 + padding * 0,
            width: buttonWidth,
            height: buttonHeight,
            callback: switchAnimation,
            checked: true
        });

        createD3Switch({
            location: heatmap,
            onlabel: 'Clustering rows ON',
            offlabel: 'Clustering rows OFF',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 5 + padding * 2,
            width: buttonWidth,
            height: buttonHeight,
            callback: switchRowClustering,
            checked: rowClustering
        });

        createD3Switch({
            location: heatmap,
            onlabel: 'Clustering columns ON',
            offlabel: 'Clustering columns OFF',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 5 + padding * 2,
            width: buttonWidth,
            height: buttonHeight,
            callback: switchColClustering,
            checked: colClustering
        });

        createD3Slider({
            location: heatmap,
            label: 'Zoom in %',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 0 + padding * 0 - 10,
            width: buttonWidth,
            height: buttonHeight,
            min: 1,
            max: 200,
            init: 100,
            callback: zoom,
            trigger: 'dragend'
        });

        var loadFeatureButton = createD3Button({
            location: heatmap,
            label: 'Load 100 add. feat.',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 1 + padding * 1,
            width: buttonWidth,
            height: buttonHeight,
            callback: loadRows
        });

        var cuttoffButton = createD3Button({
            location: heatmap,
            label: 'Apply Cutoff',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 2 + padding * 2,
            width: buttonWidth,
            height: buttonHeight,
            callback: cutoff
        });

        createD3Slider({
            location: heatmap,
            label: 'Cutoff',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 2 + padding * 2 - 10,
            width: buttonWidth,
            height: buttonHeight,
            min: significanceValues[significanceValues.length - 1],
            max: significanceValues[0],
            init: significanceValues[significanceValues.length - 1],
            callback: animateCutoff,
            trigger: 'dragend'
        });

        createD3Dropdown({
            location: heatmap,
            label: 'Heatmap Coloring',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 3 + padding * 3,
            width: buttonWidth,
            height: buttonHeight,
            items: [{
                callback: function callback() {
                    return updateColors(0);
                },
                label: 'Color Sheme 1'
            }, {
                callback: function callback() {
                    return updateColors(1);
                },
                label: 'Color Sheme 2'
            }, {
                callback: function callback() {
                    return updateColors(2);
                },
                label: 'Color Sheme 3'
            }, {
                callback: function callback() {
                    return updateColors(3);
                },
                label: 'Color Sheme 4'
            }, {
                callback: function callback() {
                    return updateColors(4);
                },
                label: 'Color Sheme 5'
            }]
        });

        createD3Dropdown({
            location: heatmap,
            label: 'Heatmap Clustering',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 3 + padding * 3,
            width: buttonWidth,
            height: buttonHeight,
            items: [{
                callback: function callback() {
                    return cluster('hclustEuclideanAverage');
                },
                label: 'Hierarch.-Eucl.-Average'
            }, {
                callback: function callback() {
                    return cluster('hclustEuclideanComplete');
                },
                label: 'Hierarch.-Eucl.-Complete'
            }, {
                callback: function callback() {
                    return cluster('hclustEuclideanSingle');
                },
                label: 'Hierarch.-Eucl.-Single'
            }, {
                callback: function callback() {
                    return cluster('hclustManhattanAverage');
                },
                label: 'Hierarch.-Manhat.-Average'
            }, {
                callback: function callback() {
                    return cluster('hclustManhattanComplete');
                },
                label: 'Hierarch.-Manhat.-Complete'
            }, {
                callback: function callback() {
                    return cluster('hclustManhattanSingle');
                },
                label: 'Hierarch.-Manhat.-Single'
            }]
        });
    };

    return service;
})();
