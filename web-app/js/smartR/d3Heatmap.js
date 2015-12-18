//# sourceURL=d3Heatmap.js

var animationDuration = 1500;
var tmpAnimationDuration = animationDuration;

function switchAnimation(checked) { // general purpose callback, this is why it is not inside SmartRHeatmap
    if (! checked) {
        tmpAnimationDuration = animationDuration;
        animationDuration = 0;
    } else {
        animationDuration = tmpAnimationDuration;
    }
}

SmartRHeatmap = (function(){

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

        var rowClustering = false;
        var colClustering = false;

        var originalPatientIDs = patientIDs.slice();
        var originalProbes = probes.slice();

        function redGreen() {
            var colorSet = [];
            var NUM = 100;
            var i = NUM;
            while(i--) {
                colorSet.push(d3.rgb((255 * i) / NUM, 0, 0));
            }
            i = NUM;
            while(i--) {
                colorSet.push(d3.rgb(0, (255 * (NUM - i)) / NUM, 0));
            }
            return colorSet;
        }

        function redBlue() {
            var colorSet = [];
            var STEP = 1 / 200;
            var sR = 255, sG = 0, sB = 0;
            var eR = 0, eG = 0, eB = 255;
            for (var i = 0; i < 1; i += STEP) {
                colorSet.push(d3.rgb((eR - sR) * i + sR, (eG - sG) * i + sG, (eB - sB) * i + sB));
            }
            return colorSet;
        }

        function odd(color) {
            var colorSet = [];
            var STEP = 1 / 200;
            var idx1, idx2, fractBetween;
            for (var i = 0; i < 1; i += STEP) {
                var value = i * (color.length - 1);
                idx1 = Math.floor(value);
                idx2 = idx1 + 1;
                fractBetween = value - idx1;
                var r = (color[idx2][0] - color[idx1][0]) * fractBetween + color[idx1][0];
                var g = (color[idx2][1] - color[idx1][1]) * fractBetween + color[idx1][1];
                var b = (color[idx2][2] - color[idx1][2]) * fractBetween + color[idx1][2];
                colorSet.push(d3.rgb(255 * r, 255 * g, 255 * b));
            }
            return colorSet;
        }

        var colorSets = [
            redGreen(),
            redBlue(),
            odd([[0, 0, 1], [0, 1, 1], [0, 1, 0], [1, 1, 0], [1, 0, 0]]),
            odd([[0, 0, 1], [0, 1, 0], [1, 0, 0]]),
            odd([[0, 0, 1], [1, 1, 0], [1, 0, 0]])
        ];

        var featureColorSetBinary = ['rgb(0, 0, 0)', 'rgb(13, 13, 191)'];
        var featureColorSetSequential = ['rgb(247,252,253)','rgb(224,236,244)','rgb(191,211,230)','rgb(158,188,218)','rgb(140,150,198)','rgb(140,107,177)','rgb(136,65,157)','rgb(129,15,124)','rgb(77,0,75)'];

        var gridFieldWidth = 40;
        var gridFieldHeight = 40;
        var dendrogramHeight = 300;
        var histogramHeight = 200;

        var margin = { top: gridFieldHeight * 2 + 100 + features.length * gridFieldHeight / 2 + dendrogramHeight,
            right: gridFieldWidth + 300 + dendrogramHeight,
            bottom: 10,
            left: histogramHeight + 250 };

        var width = gridFieldWidth * patientIDs.length;
        var height = gridFieldHeight * probes.length;

        var selectedPatientIDs = [];

        var histogramScale = d3.scale.linear()
            .domain(d3.extent(significanceValues))
            .range([0, histogramHeight]);

        var heatmap = d3.select("#heatmap").append("svg")
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        function adjustDimensions() {
            // gridFieldWidth/gridFieldHeight are adjusted outside as the zoom changes
            jQuery(heatmap[0]).closest('svg')
                .attr("width", margin.left + margin.right + (gridFieldWidth * patientIDs.length))
                .attr("height", margin.top + margin.bottom + (gridFieldHeight * probes.length));
        }
        adjustDimensions();

        var tooltip = d3.select("#heatmap").append("div")
            .attr("class", "tooltip text")
            .style("visibility", "hidden");

        var featureItems = heatmap.append('g');
        var squareItems = heatmap.append('g');
        var colSortItems = heatmap.append('g');
        var selectItems = heatmap.append('g');
        var patientIDItems = heatmap.append('g');
        var rowSortItems = heatmap.append('g');
        var significanceSortItems = heatmap.append('g');
        var labelItems = heatmap.append('g');
        var barItems = heatmap.append('g');

        function updateHeatmap() {
            var square = squareItems.selectAll('.square')
                .data(fields, function(d) { return 'patientID-' + d.PATIENTID + '-probe-' + d.PROBE; });

            square
                .enter()
                .append("rect")
                .attr('class', function(d) {
                    return 'square patientID-' + d.PATIENTID + ' probe-' + d.PROBE;
                })
                .attr("x", function(d) { return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth; })
                .attr("y", function(d) { return probes.indexOf(d.PROBE) * gridFieldHeight; })
                .attr("width", gridFieldWidth)
                .attr("height", gridFieldHeight)
                .attr("rx", 0)
                .attr("ry", 0)
                .style("fill", 'white')
                .on("mouseover", function(d) {
                    d3.select('.patientID.patientID-' +  d.PATIENTID).classed("highlight", true);
                    d3.select('.probe.probe-' +  d.PROBE).classed("highlight", true);

                    var html = '';
                    for(var key in d) {
                        html += key + ': ' + d[key] + '<br/>';
                    }
                    tooltip
                        .style("visibility", "visible")
                        .html(html)
                        .style("left", mouseX() + "px")
                        .style("top", mouseY() + "px");
                })
                .on("mouseout", function(d) {
                    d3.selectAll(".patientID").classed("highlight", false);
                    d3.selectAll(".probe").classed("highlight", false);

                    tooltip.style("visibility", "hidden");
                })
                .on('click', function(d) {
                    var url = 'http://www.genecards.org/cgi-bin/carddisp.pl?gene=' + d.GENESYMBOL;
                    window.open(url);
                });

            square
                .transition()
                .duration(animationDuration)
                .attr("x", function(d) { return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth; })
                .attr("y", function(d) { return probes.indexOf(d.PROBE) * gridFieldHeight; })
                .attr("width", gridFieldWidth)
                .attr("height", gridFieldHeight);

            var colSortText = colSortItems.selectAll('.colSortText')
                .data(patientIDs, function(d) { return d; });

            colSortText
                .enter()
                .append('text')
                .attr('class', 'text colSortText')
                .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight)
                .attr('dy', '0.35em')
                .attr("text-anchor", "middle")
                .text('↑↓');

            colSortText
                .transition()
                .duration(animationDuration)
                .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight);

            var colSortBox = colSortItems.selectAll('.colSortBox')
                .data(patientIDs, function(d) { return d; });

            function getValueForSquareSorting(patientID, probe) {
                var square = d3.select('.square' + '.patientID-' + patientID + '.probe-' + probe);
                return square[0][0] != null ? square.property('__data__').ZSCORE : Number.NEGATIVE_INFINITY;
            }

            colSortBox
                .enter()
                .append('rect')
                .attr('class', 'box colSortBox')
                .attr('x', function(d, i) { return i * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on("click", function(patientID) {
                    var rowValues = [];
                    for(var i = 0; i < probes.length; i++) {
                        var probe = probes[i];
                        rowValues.push([i, getValueForSquareSorting(patientID, probe)]);
                    }
                    if (isSorted(rowValues)) {
                        rowValues.sort(function(a, b) { return a[1] - b[1]; });
                    } else {
                        rowValues.sort(function(a, b) { return b[1] - a[1]; });
                    }
                    var sortValues = [];
                    for (i = 0; i < rowValues.length; i++) {
                        sortValues.push(rowValues[i][0]);
                    }
                    updateRowOrder(sortValues);
                });

            colSortBox
                .transition()
                .duration(animationDuration)
                .attr('x', function(d, i) { return i * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight);

            var rowSortText = rowSortItems.selectAll('.rowSortText')
                .data(probes, function(d) { return d; });

            rowSortText
                .enter()
                .append('text')
                .attr('class', 'text rowSortText')
                .attr("transform", function(d, i) { return "translate(" + (width + 2 + 0.5 * gridFieldWidth) + ",0)" + "translate(0," + (i * gridFieldHeight + 0.5 * gridFieldHeight) + ")rotate(-90)";})
                .attr('dy', '0.35em')
                .attr("text-anchor", "middle")
                .text('↑↓');

            rowSortText
                .transition()
                .duration(animationDuration)
                .attr("transform", function(d, i) { return "translate(" + (width + 2 + 0.5 * gridFieldWidth) + ",0)" + "translate(0," + (i * gridFieldHeight + 0.5 * gridFieldHeight) + ")rotate(-90)";});

            var rowSortBox = rowSortItems.selectAll('.rowSortBox')
                .data(probes, function(d) { return d; });

            rowSortBox
                .enter()
                .append('rect')
                .attr('class', 'box rowSortBox')
                .attr('x', width + 2)
                .attr('y', function(d, i) { return i * gridFieldHeight; })
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on("click", function(probe) {
                    var colValues = [];
                    for(var i = 0; i < patientIDs.length; i++) {
                        var patientID = patientIDs[i];
                        colValues.push([i, getValueForSquareSorting(patientID, probe)]);
                    }
                    if (isSorted(colValues)) {
                        colValues.sort(function(a, b) { return a[1] - b[1]; });
                    } else {
                        colValues.sort(function(a, b) { return b[1] - a[1]; });
                    }
                    var sortValues = [];
                    for (i = 0; i < colValues.length; i++) {
                        sortValues.push(colValues[i][0]);
                    }
                    updateColOrder(sortValues);
                });

            rowSortBox
                .transition()
                .duration(animationDuration)
                .attr('x', width + 2)
                .attr('y', function(d, i) { return i * gridFieldHeight; })
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight);

            var significanceSortText = significanceSortItems.selectAll('.significanceSortText')
                .data(['something'], function(d) { return d; });

            significanceSortText
                .enter()
                .append('text')
                .attr('class', 'text significanceSortText')
                .attr('x', - gridFieldWidth - 10 + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight)
                .attr('dy', '0.35em')
                .attr("text-anchor", "middle")
                .text('↑↓');

            significanceSortText
                .transition()
                .duration(animationDuration)
                .attr('x', - gridFieldWidth - 10 + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight);

            var significanceSortBox = significanceSortItems.selectAll('.significanceSortBox')
                .data(['something'], function(d) { return d; });

            significanceSortBox
                .enter()
                .append('rect')
                .attr('class', 'box significanceSortBox')
                .attr('x', - gridFieldWidth - 10)
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on("click", function() {
                    var rowValues = [];
                    for(var i = 0; i < significanceValues.length; i++) {
                        var significanceValue = significanceValues[i];
                        rowValues.push([i, significanceValue]);
                    }
                    if (isSorted(rowValues)) {
                        rowValues.sort(function(a, b) { return a[1] - b[1]; });
                    } else {
                        rowValues.sort(function(a, b) { return b[1] - a[1]; });
                    }
                    var sortValues = [];
                    for (i = 0; i < rowValues.length; i++) {
                        sortValues.push(rowValues[i][0]);
                    }
                    updateRowOrder(sortValues);
                });

            significanceSortBox
                .transition()
                .duration(animationDuration)
                .attr('x', - gridFieldWidth - 10)
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight);


            var selectText = heatmap.selectAll('.selectText')
                .data(patientIDs, function(d) { return d; });

            selectText
                .enter()
                .append('text')
                .attr('class', function(d) { return 'text selectText patientID-' + d; })
                .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight)
                .attr('dy', '0.35em')
                .attr("text-anchor", "middle")
                .text('□');

            selectText
                .transition()
                .duration(animationDuration)
                .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight);

            var selectBox = heatmap.selectAll('.selectBox')
                .data(patientIDs, function(d) { return d; });

            selectBox
                .enter()
                .append('rect')
                .attr('class', 'box selectBox')
                .attr('x', function(d, i) { return i * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight * 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on("click", function(patientID) {
                    selectCol(patientID);
                });

            selectBox
                .transition()
                .duration(animationDuration)
                .attr('x', function(d, i) { return i * gridFieldWidth; })
                .attr('y', -2 - gridFieldHeight * 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight);

            var patientID = patientIDItems.selectAll('.patientID')
                .data(patientIDs, function(d) { return d; });

            patientID
                .enter()
                .append("text")
                .attr('class', function(d) { return 'patientID patientID-' + d; })
                .attr("transform", function(d) {
                    return "translate(" + (patientIDs.indexOf(d) * gridFieldWidth) + ",0)" +
                        "translate(" + (gridFieldWidth / 2) + "," + (-4 - gridFieldHeight * 2) + ")rotate(-45)";
                })
                .style("text-anchor", "start")
                .text(function(d) { return d; });

            patientID
                .transition()
                .duration(animationDuration)
                .attr("transform", function(d) {
                    return "translate(" + (patientIDs.indexOf(d) * gridFieldWidth) + ",0)" +
                        "translate(" + (gridFieldWidth / 2) + "," + (-4 - gridFieldHeight * 2) + ")rotate(-45)";
                });

            var probe = labelItems.selectAll('.probe')
                .data(probes, function(d) { return d; });

            probe
                .enter()
                .append("text")
                .attr('class', function(d) { return 'probe text probe-' + d;})
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', function(d) { return probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight; })
                .attr('dy', '0.35em')
                .style("text-anchor", "start")
                .text(function(d) {
                    var i = probes.indexOf(d);
                    return d + '  //  ' + geneSymbols[i];
                });

            probe
                .transition()
                .duration(animationDuration)
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', function(d) { return probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight; });

            var significanceIndexMap = jQuery.map(significanceValues, function(d, i) {
                return {significance: d, idx: i};
            });

            var bar = barItems.selectAll('.bar')
                .data(significanceIndexMap, function(d) { return d.idx; });

            bar
                .enter()
                .append('rect')
                .attr('class', function(d) { return 'bar idx-' + d.idx ; })
                .attr("width", function(d) { return histogramScale(d.significance); })
                .attr("height", gridFieldHeight)
                .attr("x", function(d) { return - histogramScale(d.significance) - 10; })
                .attr("y", function(d) { return gridFieldHeight * d.idx; })
                .on("mouseover", function(d) {
                    var html = 'FEATURE SIGNIFICANCE: ' + d.significance;
                    tooltip
                        .style("visibility", "visible")
                        .html(html)
                        .style("left", mouseX() + "px")
                        .style("top", mouseY() + "px");
                    d3.selectAll('.square.probe-' +  probes[d.idx])
                        .classed("squareHighlighted", true);
                    d3.select('.probe.probe-' +  probes[d.idx])
                        .classed("highlight", true);
                })
                .on("mouseout", function(d) {
                    tooltip.style("visibility", "hidden");
                    d3.selectAll(".square").classed("squareHighlighted", false);
                    d3.selectAll(".probe").classed("highlight", false);
                });

            bar
                .transition()
                .duration(animationDuration)
                .attr("height", gridFieldHeight)
                .attr("width", function(d) { return histogramScale(d.significance); })
                .attr("x", function(d) { return - histogramScale(d.significance) - 10; })
                .attr("y", function(d) { return gridFieldHeight * d.idx; });

            var featurePosY = - gridFieldWidth * 2 - getMaxWidth(d3.selectAll('.patientID')) - features.length * gridFieldWidth / 2 - 20;

            var extraSquare = featureItems.selectAll('.extraSquare')
                .data(extraFields, function(d) { return 'patientID-' + d.PATIENTID + '-feature-' + d.FEATURE; });

            extraSquare
                .enter()
                .append("rect")
                .attr('class', function(d) {
                    return 'extraSquare patientID-' + d.PATIENTID + ' feature-' + d.FEATURE;
                })
                .attr("x", function(d) { return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth; })
                .attr('y', function(d) { return featurePosY + features.indexOf(d.FEATURE) * gridFieldHeight / 2; })
                .attr("width", gridFieldWidth)
                .attr("height", gridFieldHeight / 2)
                .attr("rx", 0)
                .attr("ry", 0)
                .style("fill", 'white')
                .on("mouseover", function(d) {
                    d3.select('.patientID.patientID-' +  d.PATIENTID).classed("highlight", true);
                    d3.select('.feature.feature-' +  d.FEATURE).classed("highlight", true);

                    var html = '';
                    for(var key in d) {
                        html += key + ': ' + d[key] + '<br/>';
                    }
                    tooltip
                        .style("visibility", "visible")
                        .html(html)
                        .style("left", mouseX() + "px")
                        .style("top", mouseY() + "px");
                })
                .on("mouseout", function(d) {
                    d3.selectAll(".patientID").classed("highlight", false);
                    d3.selectAll(".feature").classed("highlight", false);
                    tooltip.style("visibility", "hidden");
                });

            extraSquare
                .transition()
                .duration(animationDuration)
                .attr("x", function(d) { return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth; })
                .attr('y', function(d) { return featurePosY + features.indexOf(d.FEATURE) * gridFieldHeight / 2; })
                .attr("width", gridFieldWidth)
                .attr("height", gridFieldHeight / 2);

            var feature = featureItems.selectAll('.feature')
                .data(features, function(d) { return d; });

            feature
                .enter()
                .append("text")
                .attr('class', function(d) { return 'feature text feature-' + d;})
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', function(d) { return featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4; })
                .attr('dy', '0.35em')
                .style("text-anchor", "start")
                .text(function(d) { return d; });

            feature
                .transition()
                .duration(animationDuration)
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', function(d) { return featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4; });

            var featureSortText = featureItems.selectAll('.featureSortText')
                .data(features, function(d) { return d; });

            featureSortText
                .enter()
                .append('text')
                .attr('class', 'text featureSortText')
                .attr("transform", function(d) { return "translate(" + (width + 2 + 0.5 * gridFieldWidth) + ",0)" + "translate(0," + (featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4) + ")rotate(-90)";})
                .attr('dy', '0.35em')
                .attr("text-anchor", "middle")
                .text('↑↓')
                .attr('visibility', function(d) {
                    if (d3.select('.extraSquare.feature-' + d).property('__data__').TYPE === 'numerical') {
                        return 'visible';
                    } else {
                        return 'hidden';
                    }
                });

            featureSortText
                .transition()
                .duration(animationDuration)
                .attr("transform", function(d) { return "translate(" + (width + 2 + 0.5 * gridFieldWidth) + ",0)" + "translate(0," + (featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4) + ")rotate(-90)";});

            var featureSortBox = featureItems.selectAll('.featureSortBox')
                .data(features, function(d) { return d; });

            featureSortBox
                .enter()
                .append('rect')
                .attr('class', 'box featureSortBox')
                .attr('x', width + 2)
                .attr('y', function(d) { return featurePosY + features.indexOf(d) * gridFieldHeight / 2; })
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight / 2)
                .on("click", function(feature) {
                    var featureValue = [];
                    var missingValues = false;
                    for(var i = 0; i < patientIDs.length; i++) {
                        var patientID = patientIDs[i];
                        var zScore = - Math.pow(2,32);
                        try {
                            var square = d3.select('.extraSquare' + '.patientID-' + patientID + '.feature-' + feature);
                            zScore = square.property('__data__').ZSCORE;
                        } catch (err) {
                            missingValues = true;
                        }
                        featureValue.push([i, zScore]);
                    }
                    if (isSorted(featureValue)) {
                        featureValue.sort(function(a, b) { return a[1] - b[1]; });
                    } else {
                        featureValue.sort(function(a, b) { return b[1] - a[1]; });
                    }
                    var sortValues = [];
                    for (i = 0; i < featureValue.length; i++) {
                        sortValues.push(featureValue[i][0]);
                    }
                    if (missingValues) {
                        alert('Feature is missing for one or more patients.\nEvery missing value will be set to lowest possible value for sorting;');
                    }
                    updateColOrder(sortValues);
                })
                .attr('visibility', function(d) {
                    if (d3.select('.extraSquare.feature-' + d).property('__data__').TYPE === 'numerical') {
                        return 'visible';
                    } else {
                        return 'hidden';
                    }
                });


            featureSortBox
                .transition()
                .duration(animationDuration)
                .attr('x', width + 2)
                .attr('y', function(d, i) { return featurePosY + features.indexOf(d) * gridFieldHeight / 2; })
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight / 2);
        }

        function zoom(zoomLevel) {
            zoomLevel /= 100;

            d3.selectAll('.patientID')
                .style('font-size', Math.ceil(14 * zoomLevel) + 'px');

            d3.selectAll('.selectText')
                .style('font-size', Math.ceil(16 * zoomLevel) + 'px');

            d3.selectAll('.probe')
                .style('font-size', Math.ceil(9 * zoomLevel) + 'px');

            d3.selectAll('.feature')
                .style('font-size', Math.ceil(10 * zoomLevel) + 'px');

            d3.selectAll('.significanceSortText, .rowSortText, .colSortText')
                .style('font-size', Math.ceil(14 * zoomLevel) + 'px');

            d3.selectAll('.featureSortText')
                .style('font-size', Math.ceil(10 * zoomLevel) + 'px');

            gridFieldWidth = 40 * zoomLevel;
            gridFieldHeight = 40 * zoomLevel;
            width = gridFieldWidth * patientIDs.length;
            height = gridFieldHeight * probes.length;
            heatmap
                .attr('width', width + margin.left + margin.right)
                .attr('height', width + margin.top + margin.bottom);
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
            for (var i = 0; i < significanceValues.length; i++) {
                var significanceValue = significanceValues[i];
                if (significanceValue < cutoff) {
                    d3.selectAll('.square.probe-' +  probes[i])
                        .classed("cuttoffHighlight", true);
                    d3.select('.bar.idx-' +  i)
                        .classed("cuttoffHighlight", true);
                } else {
                    d3.selectAll('.square.probe-' +  probes[i])
                        .classed("cuttoffHighlight", false);
                    d3.select('.bar.idx-' +  i)
                        .classed("cuttoffHighlight", false);
                }
            }
        }

        function cutoff() {
            cuttoffButton.select('text').text('Loading...');
            var nrows = 0;
            for (var i = 0; i < significanceValues.length; i++) {
                var significanceValue = significanceValues[i];
                if (significanceValue > cutoffLevel) {
                    nrows += 1;
                }
            }
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
                colSquares
                    .classed('selected', false);
                d3.select('.selectText.patientID-' + patientID)
                    .text('□');
            } else {
                selectedPatientIDs.push(patientID);
                colSquares
                    .classed('selected', true);
                d3.select('.selectText.patientID-' + patientID)
                    .text('■');
            }
            if (selectedPatientIDs.length !== 0) {
                d3.selectAll('.square:not(.selected)')
                    .attr('opacity', 0.4);
            } else {
                d3.selectAll('.square')
                    .attr('opacity', 1);
            }
        }

        function updateColors(colorIdx) {
            var colorScale = d3.scale.quantile()
                .domain([0, 1])
                .range(colorSets[colorIdx]);

            d3.selectAll('.square')
                .transition()
                .duration(animationDuration)
                .style("fill", function(d) { return colorScale(1 / (1 + Math.pow(Math.E, - d.ZSCORE))); });
            for (var i = 0; i < features.length; i++) {
                var feature = features[i];
                var categoricalColorScale = d3.scale.category10();
                d3.selectAll('.extraSquare.feature-' + feature)
                    .style("fill", function(d) {
                        if (d.TYPE === 'binary') {
                            return featureColorSetBinary[d.VALUE];
                        } else if (d.TYPE === 'numerical') {
                            colorScale
                                .range(featureColorSetSequential);
                            return colorScale(1 / (1 + Math.pow(Math.E, - d.ZSCORE)));
                        } else if (d.TYPE === 'alphabetical') {
                            return categoricalColorScale(d.VALUE);
                        } else {
                            alert('Field type does not exist: ' + d.TYPE);
                        }
                    });
            }
        }

        function unselectAll() {
            d3.selectAll('.selectText')
                .text('□');
            d3.selectAll('.square')
                .classed('selected', false)
                .attr('opacity', 1);
            selectedPatientIDs = [];
        }

        var colDendrogramVisible = false;
        var colDendrogram;

        function createColDendrogram() {
            var w = 200;
            var colDendrogramWidth = gridFieldWidth * numberOfClusteredColumns;
            var spacing = gridFieldWidth * 2 + getMaxWidth(d3.selectAll('.patientID')) + features.length * gridFieldHeight / 2 + 40;

            var cluster = d3.layout.cluster()
                .size([colDendrogramWidth, w])
                .separation(function(a, b) {
                    return 1;
                });

            var diagonal = d3.svg.diagonal()
                .projection(function (d) {
                    return [d.x, - spacing - w + d.y];
                });

            var colDendrogramNodes = cluster.nodes(colDendrogram);
            var colDendrogramLinks = cluster.links(colDendrogramNodes);

            colDendrogramLink = heatmap.selectAll(".colDendrogramLink")
                .data(colDendrogramLinks)
                .enter().append("path")
                .attr("class", "colDendrogram link")
                .attr("d", diagonal);

            colDendrogramNode = heatmap.selectAll(".colDendrogramNode")
                .data(colDendrogramNodes)
                .enter().append("circle")
                .attr("class", "colDendrogram node")
                .attr('r', 4.5)
                .attr("transform", function (d) {
                    return "translate(" + d.x + "," + (- spacing - w + d.y) + ")";
                }).on('click', function(d) {
                    var previousSelection = selectedPatientIDs.slice();
                    unselectAll();
                    var leafs = d.index.split(' ');
                    for (var i = 0; i < leafs.length; i++) {
                        var patientID = patientIDs[leafs[i]];
                        selectCol(patientID);
                    }
                    if (arrEqual(previousSelection, selectedPatientIDs)) {
                        unselectAll();
                    }
                })
                .on("mouseover", function(d) {
                    tooltip
                        .style("visibility", "visible")
                        .html('Height: ' + d.height)
                        .style("left", mouseX() + "px")
                        .style("top", mouseY() + "px");
                })
                .on('mouseout', function() {
                    tooltip.style("visibility", "hidden");
                });
            colDendrogramVisible = true;
        }

        var rowDendrogramVisible = false;
        var rowDendrogram;
        function createRowDendrogram() {
            var h = 280;
            var rowDendrogramHeight = gridFieldWidth * numberOfClusteredRows;
            var spacing = gridFieldWidth + getMaxWidth(d3.selectAll('.probe')) + 20;

            var cluster = d3.layout.cluster()
                .size([rowDendrogramHeight, h])
                .separation(function(a, b) {
                    return 1;
                });

            var diagonal = d3.svg.diagonal()
                .projection(function (d) {
                    return [width + spacing + h - d.y, d.x];
                });

            var rowDendrogramNodes = cluster.nodes(rowDendrogram);
            var rowDendrogramLinks = cluster.links(rowDendrogramNodes);

            rowDendrogramLink = heatmap.selectAll(".rowDendrogramLink")
                .data(rowDendrogramLinks)
                .enter().append("path")
                .attr("class", "rowDendrogram link")
                .attr("d", diagonal);

            rowDendrogramNode = heatmap.selectAll(".rowDendrogramNode")
                .data(rowDendrogramNodes)
                .enter().append("circle")
                .attr("class", "rowDendrogram node")
                .attr('r', 4.5)
                .attr("transform", function (d) {
                    return "translate(" + (width + spacing + h - d.y) + "," + d.x + ")";
                }).on('click', function(d) {
                    alert('Under Construction.');
                    return;
                    var leafs = d.index.split(' ');
                    var genes = [];
                    for (var i = 0; i < leafs.length; i++) {
                        var gene = geneSymbols[leafs[i]];
                        genes.push(gene);
                    }
                    jQuery.ajax({
                        url: 'http://biocompendium.embl.de/cgi-bin/biocompendium.cgi',
                        type: "POST",
                        timeout: '600000',
                        data: {
                            section: 'upload_gene_lists',
                            primary_org: 'Human',
                            background: 'whole_genome',
                            Category1: 'Human',
                            gene_list_1: 'gene_list_1',
                            SubCat1: 'ascii',
                            attachment1: genes
                        }
                    }).done(function(serverAnswer) {
                        var newTab = window.open('', '');
                        newTab.document.write(serverAnswer);
                    }).fail(function() {
                        alert('fail');
                    });
                })
                .on("mouseover", function(d) {
                    tooltip
                        .style("visibility", "visible")
                        .html('Height: ' + d.height)
                        .style("left", mouseX() + "px")
                        .style("top", mouseY() + "px");
                })
                .on('mouseout', function() {
                    tooltip.style("visibility", "hidden");
                });
            rowDendrogramVisible = true;
        }

        function removeColDendrogram() {
            heatmap.selectAll(".colDendrogram").remove();
            colDendrogramVisible = false;
        }

        function removeRowDendrogram() {
            heatmap.selectAll(".rowDendrogram").remove();
            rowDendrogramVisible = false;
        }

        function updateColOrder(sortValues) {
            var sortedPatientIDs = [];
            for (var i = 0; i < sortValues.length; i++) {
                sortedPatientIDs.push(patientIDs[sortValues[i]]);
            }
            patientIDs = sortedPatientIDs;
            unselectAll();
            removeColDendrogram();
            updateHeatmap();
        }

        function updateRowOrder(sortValues) {
            var sortedProbes = [];
            var sortedGeneSymbols = [];
            var sortedSignificanceValues = [];
            for (var i = 0; i < sortValues.length; i++) {
                sortedProbes.push(probes[sortValues[i]]);
                sortedGeneSymbols.push(geneSymbols[sortValues[i]]);
                sortedSignificanceValues.push(significanceValues[sortValues[i]]);
            }
            probes = sortedProbes;
            geneSymbols = sortedGeneSymbols;
            significanceValues = sortedSignificanceValues;
            removeRowDendrogram();
            updateHeatmap();
            animateCutoff();
        }

        function transformClusterOrderWRTInitialOrder(clusterOrder, initialOrder) {
            var newOrder = clusterOrder.slice();
            for (var i = 0; i < clusterOrder.length; i++) {
                newOrder[i] = initialOrder.indexOf(clusterOrder[i]);
            }
            return newOrder;
        }

        function getInitialRowOrder() {
            initialRowOrder = [];
            for (var i = 0; i < probes.length; i++) {
                var probe = probes[i];
                initialRowOrder.push(originalProbes.indexOf(probe));
            }
            return initialRowOrder;
        }

        function getInitialColOrder() {
            initialColOrder = [];
            for (var i = 0; i < patientIDs.length; i++) {
                var patientID = patientIDs[i];
                initialColOrder.push(originalPatientIDs.indexOf(patientID));
            }
            return initialColOrder;
        }
        var lastUsedClustering = null;

        function cluster(clustering) {
            if(!lastUsedClustering && typeof clustering === 'undefined'){
                return; // Nothing should be done if clustering switches are turned on without clustering type set.
            }
            clustering = (typeof clustering === 'undefined') ? lastUsedClustering : clustering;
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

        function updateCohorts() {
            alert('This feature will be available in tranSMART 1.3.');
            return;
            var CONCEPT = '';
            var patientDIVs = [];
            for(var i = 0, len = selectedPatientIDs.length; i < len; i++) {
                var conceptid = CONCEPT + selectedPatientIDs[i] + '\\';
                patientDIVs.push(createQueryCriteriaDIV(conceptid, '', 'novalue', 'LT', '', '', '', 'N', 'alphaicon'));
            }
            setCohorts(patientDIVs, false, false, true);
        }

        function loadRows(nrows) {
            var maxRows = nrows === undefined ? probes.length + 100 : nrows;
            var data = prepareFormData();
            data = addSettingsToData(data, { maxRows: maxRows });
            loadFeatureButton.select('text').text('Loading...');
            jQuery.ajax({
                url: pageInfo.basePath + '/SmartR/recomputeOutputDIV',
                type: "POST",
                timeout: '600000',
                data: data
            }).done(function(serverAnswer) {
                jQuery("#outputDIV").html(serverAnswer);
                loadFeatureButton.select('text').text('Load 100 additional rows');
                cuttoffButton.select('text').text('Apply Cutoff');
            }).fail(function() {
                jQuery("#outputDIV").html("An unexpected error occurred. This should never happen. Ask your administrator for help.");
                loadFeatureButton.select('text').text('Load 100 additional rows');
                cuttoffButton.select('text').text('Apply Cutoff');
            });
        }


        function switchRowClustering() {
            rowClustering = !rowClustering;
            cluster();
        };

        function switchColClustering() {
            colClustering = !colClustering;
            cluster();
        };

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

        createD3Button({
            location: heatmap,
            label: 'Update Cohorts',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 1 + padding * 1,
            width: buttonWidth,
            height: buttonHeight,
            callback: updateCohorts
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
            items: [
                {
                    callback: function() { updateColors(0); },
                    label: 'Color Sheme 1'
                },
                {
                    callback: function() { updateColors(1); },
                    label: 'Color Sheme 2'
                },
                {
                    callback: function() { updateColors(2); },
                    label: 'Color Sheme 3'
                },
                {
                    callback: function() { updateColors(3); },
                    label: 'Color Sheme 4'
                },
                {
                    callback: function() { updateColors(4); },
                    label: 'Color Sheme 5'
                }
            ]
        });

        createD3Dropdown({
            location: heatmap,
            label: 'Heatmap Clustering',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 3 + padding * 3,
            width: buttonWidth,
            height: buttonHeight,
            items: [
                {
                    callback: function() { cluster('hclustEuclideanAverage'); },
                    label: 'Hierarch.-Eucl.-Average'
                },
                {
                    callback: function() { cluster('hclustEuclideanComplete'); },
                    label: 'Hierarch.-Eucl.-Complete'
                },
                {
                    callback: function() { cluster('hclustEuclideanSingle'); },
                    label: 'Hierarch.-Eucl.-Single'
                },
                {
                    callback: function() { cluster('hclustManhattanAverage'); },
                    label: 'Hierarch.-Manhat.-Average'
                },
                {
                    callback: function() { cluster('hclustManhattanComplete'); },
                    label: 'Hierarch.-Manhat.-Complete'
                },
                {
                    callback: function() { cluster('hclustManhattanSingle'); },
                    label: 'Hierarch.-Manhat.-Single'
                }
            ]
        });

    }


    return service;

})();






