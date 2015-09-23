<!DOCTYPE html>
<meta charset="utf-8">
<style>
    ul li {
      list-style: none;
    }

    .dropdown {
        border-radius: 3px;
        color: #ffffff;
        background-color: #009ac9;
        padding: 13px 30px;
        width: 200px;
        vertical-align: middle;
        margin: 0 auto;
        text-align: center;
        font-size: 13px;
    }

    .dropdown li ul li {
        color: #000000;
        background-color: #E3E3E3;
        width: 200px;
        padding: 13px 30px;
        vertical-align: middle;
        margin-left: -30px;
        border-bottom: 0px solid #ffffff;
    }

    .dropdown li ul li:hover {
        cursor: pointer;
        color: #ffffff;
        background-color: #009ac9;
    }

    .dropdown li ul li:first-child {
        margin-top: -13px;
    }

    .dropdown li .dropdown-box {
        height: 0;
        overflow: hidden;
        display: none;
        width: 100%;
        margin-left: -30px;
        margin-bottom: -15px;
        margin-top: 13px;
        border-top: 1px solid #ffffff;
        padding: 0;
        padding: 13px 30px;
        width: 200px;
        vertical-align: middle;
    }

    @keyframes anim275 {
        from {
            height: 0px;
        }
        to {
            height: 290px;
        }
    }

    #clusterSelection li:hover .dropdown-box, #clusterSelection li:active .dropdown-box {
        display: block;
        animation-name: anim275;
        animation-duration: 1s;
        animation-timing-function: ease
        animation-iteration-count: 1;
        animation-fill-mode: forwards;
    }

    @keyframes anim150 {
        from {
            height: 0px;
        }
        to {
            height: 190px;
        }
    }

    #colorSelection li:hover .dropdown-box, #colorSelection li:active .dropdown-box {
        display: block;
        animation-name: anim150;
        animation-duration: 1s;
        animation-timing-function: ease
        animation-iteration-count: 1;
        animation-fill-mode: forwards;
    }

    .niceButton {
        width: 200px;
        height: 44px;
        display: block;
        background-color: #009ac9;
        border: 1px solid transparent;
        color: #ffffff;
        border-radius: 3px;
        -webkit-transition: all 0.3s ease-in-out;
        -moz-transition: all 0.3s ease-in-out;
        transition: all 0.3s ease-in-out;
    }

    .niceButton:hover {
        cursor: pointer;
        background-color: #ffffff;
        color: #009ac9;
        border-color: #009ac9;
    }

    .text {
        font-family: 'Roboto', sans-serif;
        font-size: 14px;
    }

    .square {
        stroke: white;
        stroke-width: 0px;
    }

    .extraSquare {
        stroke: white;
        stroke-width: 0px;
    }

    .feature {
        font-size: 10px;
    }

    .selected {
        opacity: 1;
    }

    .square:hover {
        opacity: 0.4;
    }

    .squareHighlighted {
        opacity: 0.4;
    }

    .extraSquare:hover {
        opacity: 0.4;
    }

    .bar {
        fill: steelblue;
        stroke: black;
        shape-rendering: crispEdges;
    }

    .bar:hover {
        opacity: 0.4;
    }

    .barHighlighted {
        opacity: 0.4;
    }

    .cuttoffHighlight {
        opacity: 0.4;    
    }

    .box {
        fill-opacity: 0;
        shape-rendering: crispEdges;
    }

    .box:hover {
        cursor: pointer;
        fill-opacity: 0.2;
    }

    .probe {
        font-size: 9pt;
    }

    .selectText {
        font-size: 16pt;
    }

    .highlight {
        font-weight: bold;
        fill: red;
    }

    .node {
        fill: white;
        stroke: rgb(0, 126, 126);
        stroke-width: 1.5px;
    }

    .node:hover {
        cursor: pointer;
        fill: rgb(0, 126, 126);
    }

    .link {
        fill: none;
        stroke: #FF8700;
        stroke-width: 2px;
    }

    .tooltip {
        position: absolute;
        text-align: center;
        display: inline-block;
        padding: 0px;
        font-size: 12px;
        font-weight: bold;
        color: black;
        background: white;
        pointer-events: none;
    }

    .ios7-switch {
        display: inline-block;
        position: relative;
        cursor: pointer;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
        -webkit-tap-highlight-color: transparent;
        tap-highlight-color: transparent;
    }

    .ios7-switch input {
        opacity: 0;
        position: absolute;
    }

    .ios7-switch input + span {
        position: relative;
        display: inline-block;
        width: 1.65em;
        height: 1em;
        background: white;
        box-shadow: inset 0 0 0 0.0625em #e9e9e9;
        border-radius: 0.5em;
        vertical-align: -0.15em;
        transition: all 0.40s cubic-bezier(.17,.67,.43,.98);
    }

    .ios7-switch:active input + span,
    .ios7-switch input + span:active {
        box-shadow: inset 0 0 0 0.73em #e9e9e9;
    }

    .ios7-switch input + span:after {
        position: absolute;
        display: block;
        content: '';
        width: 0.875em;
        height: 0.875em;
        border-radius: 0.4375em;
        top: 0.0625em;
        left: 0.0625em;
        background: white;
        box-shadow: inset 0 0 0 0.03em rgba(0,0,0,0.1),
                    0 0 0.05em rgba(0,0,0,0.05),
                    0 0.1em 0.2em rgba(0,0,0,0.2);
        transition: all 0.25s ease-out;
    }

    .ios7-switch:active input + span:after,
    .ios7-switch input + span:active:after {
        width: 1.15em;
    }

    .ios7-switch input:checked + span {
        box-shadow: inset 0 0 0 0.73em #009ac9;
    }

    .ios7-switch input:checked + span:after {
        left: 0.7125em;
    }

    .ios7-switch:active input:checked + span:after,
    .ios7-switch input:checked + span:active:after {
        left: 0.4375em;
    }

    /* accessibility styles */
    .ios7-switch input:focus + span:after {
        box-shadow: inset 0 0 0 0.03em rgba(0,0,0,0.15),
                    0 0 0.05em rgba(0,0,0,0.08),
                    0 0.1em 0.2em rgba(0,0,0,0.3);
        background: #fff;
    }

    .ios7-switch input:focus + span {
        box-shadow: inset 0 0 0 0.0625em #dadada;
    }

    .ios7-switch input:focus:checked + span {
        box-shadow: inset 0 0 0 0.73em #009ac9;
    }

    /* reset accessibility style on hover */
    .ios7-switch:hover input:focus + span:after {
        box-shadow: inset 0 0 0 0.03em rgba(0,0,0,0.1),
                    0 0 0.05em rgba(0,0,0,0.05),
                    0 0.1em 0.2em rgba(0,0,0,0.2);
        background: #fff;
    }

    .ios7-switch:hover input:focus + span {
        box-shadow: inset 0 0 0 0.0625em #e9e9e9;
    }

    .ios7-switch:hover input:focus:checked + span {
        box-shadow: inset 0 0 0 0.73em #009ac9;
    }
</style>

<link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
<g:javascript src="resource/d3.js"/>

<label class="ios7-switch" style="font-size: 16px">
    Disable Animations
    <input onclick='switchAnimation()' type="checkbox">
    <span></span>
</label><br/><br/>
<div id='visualization' class='text'>
    <div style='float: left; padding-right: 10px'>
        <ul class='dropdown text' id='colorSelection'>
             <li id="top">Choose Heatmap Coloring
                <span></span>
                <ul class="dropdown-box">
                   <li  onclick=updateColors(0)>Red-Green Color Sheme</li>
                   <li  onclick=updateColors(1)>Color Sheme 2</li>
                   <li  onclick=updateColors(2)>Color Sheme 3</li>
                   <li  onclick=updateColors(3)>Color Sheme 4</li>
                   <li  onclick=updateColors(4)>Color Sheme 5</li>
                </ul>
             </li>
          </ul>
    </div>
    <div style='float: left; padding-right: 10px'>
        <ul class='dropdown text' id='clusterSelection'>
             <li id="top">Choose Heatmap Clustering
                <span></span>
                <ul class="dropdown-box">
                    <li  onclick=cluster('hclustEuclideanAverage')>Hierarchical-Euclidean-Average (Default)</li>
                    <li  onclick=cluster('hclustEuclideanComplete')>Hierarchical-Euclidean-Complete</li>
                    <li  onclick=cluster('hclustEuclideanSingle')>Hierarchical-Euclidean-Single</li>
                    <li  onclick=cluster('hclustManhattanAverage')>Hierarchical-Manhattan-Average</li>                    
                    <li  onclick=cluster('hclustManhattanComplete')>Hierarchical-Manhattan-Complete</li>
                    <li  onclick=cluster('hclustManhattanSingle')>Hierarchical-Manhattan-Single</li>
                </ul>
             </li>
          </ul>
    </div>
    <div style='float: left; padding-right: 10px'>
        <input id='cohortButton' class='text niceButton' type='button' value='Update Cohorts by Selection' onclick='updateCohorts()'/>
    </div>
    <div style='float: left; padding-right: 10px'>
        <input id='loadMoreButton' class='text niceButton' type='button' value='Load 100 additional rows' onclick='loadRows()'/><div id='loadMessage'></div>
    </div>
    <div style='float: left; padding-right: 10px'>
        <input type='range' min='0' max='300' value='100' id='zoomSlider' class='text' step='5' onchange="zoom()">
        <output for='zoomSlider' id='zoomLevel'>100% Zoom</output>
    </div>
    <div style='float: left; padding-right: 10px'>
        <input id='cutoffValue' type='number' class='text' onchange="animateCutoff()">
    </div>
    <div style='float: left; padding-right: 10px'>
        <input id='cutoffButton' class='text niceButton' type='button' value='Apply cutoff' onclick='cutoff()'/>
    </div>
    <div id="heatmap" class='text'></div>
</div>

<script>
    var animationDuration = 1500;
    var tmpAnimationDuration = animationDuration;
    function switchAnimation() {
        if (animationDuration) {
            tmpAnimationDuration = animationDuration;
            animationDuration = 0;
        } else {
            animationDuration = tmpAnimationDuration;
        }
    }

    var data = ${raw(results)};
    var extraFields = data.extraFields === undefined ? [] : data.extraFields;
    var features = data.features === undefined ? [] : data.features;
    var fields = data.fields;
    var significanceValues = data.significanceValues;
    var patientIDs = data.patientIDs;
    var probes = data.probes;
    var geneSymbols = data.geneSymbols;

    var maxRows = 100;

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
            left: histogramHeight };

    var width = gridFieldWidth * patientIDs.length;
    var height = gridFieldHeight * probes.length;

    var selectedPatientIDs = [];

    var histogramScale = d3.scale.linear()
    .domain(d3.extent(significanceValues))
    .range([0, histogramHeight]);

    var heatmap = d3.select("#heatmap").append("svg")
    .attr("width", (width + margin.left + margin.right) * 4)
    .attr("height", (height + margin.top + margin.bottom) * 4)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var tooltip = d3.select("#heatmap").append("div")
    .attr("class", "tooltip text")
    .style("visibility", "hidden");

    // jQuery(function() {
    //     jQuery("#zoomSlider").slider();
    // });

    
    var cutoffInput = document.getElementById('cutoffValue');
    cutoffValue.setAttribute('value', significanceValues[significanceValues.length - 1]);
    cutoffValue.setAttribute('min', significanceValues[significanceValues.length - 1]);
    cutoffValue.setAttribute('max', significanceValues[0]);
    cutoffValue.setAttribute('step', (significanceValues[0] - significanceValues[significanceValues.length - 1]) / 50);

    var extraSquareItems = heatmap.append('g');
    var squareItems = heatmap.append('g');
    var colSortItems = heatmap.append('g');
    var selectItems = heatmap.append('g');
    var patientIDItems = heatmap.append('g');
    var rowSortItems = heatmap.append('g');
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
                var square = d3.select('.square' + '.patientID-' + patientID + '.probe-' + probe);
                rowValues.push([i, square.property('__data__').ZSCORE]);
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
                var square = d3.select('.square' + '.patientID-' + patientID + '.probe-' + probe);
                colValues.push([i, square.property('__data__').ZSCORE]);
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

        var extraSquare = extraSquareItems.selectAll('.extraSquare')
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

        var feature = labelItems.selectAll('.feature')
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
    }

    function zoom() {
        var zoomLevel = jQuery("#zoomSlider").val();
        jQuery("#zoomLevel").html(zoomLevel + "% Zoom");
        zoomLevel /= 100;
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
    }

    function animateCutoff() {
        var cutoffLevel =  document.getElementById("cutoffValue").value;
        for (var i = 0; i < significanceValues.length; i++) {
            var significanceValue = significanceValues[i];
            if (significanceValue < cutoffLevel) {
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
        var cutoffLevel =  document.getElementById("cutoffValue").value;
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
        var spacing = gridFieldWidth * 2 + getMaxWidth(d3.selectAll('.patientID')) + features.length * gridFieldHeight / 2 + 40;

        var cluster = d3.layout.cluster()
        .size([width, w])
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
        var spacing = gridFieldWidth + getMaxWidth(d3.selectAll('.probe')) + 20;

        var cluster = d3.layout.cluster()
        .size([height, h])
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
            alert('Feature selection is currently not possible.');
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

    function cluster(clustering) {
        var clusterData = data[clustering];
        colDendrogram = JSON.parse(clusterData[2]);
        rowDendrogram = JSON.parse(clusterData[3]);
        updateRowOrder(transformClusterOrderWRTInitialOrder(clusterData[1], getInitialRowOrder()));
        updateColOrder(transformClusterOrderWRTInitialOrder(clusterData[0], getInitialColOrder()));
        createColDendrogram(colDendrogram);
        createRowDendrogram(rowDendrogram);
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
        jQuery("#loadMoreButton").attr("disabled", true);
        jQuery("#loadMoreButton").val('This will last a moment...');
        jQuery.ajax({
            url: pageInfo.basePath + '/SmartR/recomputeOutputDIV',
            type: "POST",
            timeout: '600000',
            data: data
        }).done(function(serverAnswer) {
            jQuery("#outputDIV").html(serverAnswer);
            jQuery("#loadMoreButton").attr("disabled", false);
            jQuery("#loadMoreButton").val('Load 100 additional rows');
        }).fail(function() {
            jQuery("#outputDIV").html("An unexpected error occurred. This should never happen. Ask your administrator for help.");
            jQuery("#loadMoreButton").attr("disabled", false);
            jQuery("#loadMoreButton").val('Load 100 additional rows');
        });
    }

    function init() {
        updateHeatmap();
        reloadDendrograms();
        updateColors(0);
    }

    init();
</script>
