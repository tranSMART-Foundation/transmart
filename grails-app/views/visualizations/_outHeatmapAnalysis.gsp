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
            height: 275px;
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
            height: 150px;
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
        /*shape-rendering: crispEdges;*/
    }

    .selected {
        opacity: 1;
    }

    .square:hover {
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

    .label {
        font-size: 9pt;
    }

    .selectBoxLabel {
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
</style>

<link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
<g:javascript src="resource/d3.js"/>

<div id='visualization' class='text'>
    <p><font size="2" color="red" class='text'>ATTENTION! Handling mRNA data within a browser is very difficult due to the extremly limited recources. Any action (such as sorting) might take up to several minutes!</font></p><br/>
    <div style='float: left; padding-right: 10px'>
        <ul class='dropdown text' id='colorSelection'>
             <li id="top">Choose Heatmap Coloring
                <span></span>
                <ul class="dropdown-box">
                   <li  onclick=updateColors(0)>Divergent Color Sheme 1</li>
                   <li  onclick=updateColors(1)>Divergent Color Sheme 2</li>
                   <li  onclick=updateColors(2)>Sequential Color Sheme 1</li>
                   <li  onclick=updateColors(3)>Sequential Color Sheme 2</li>
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
        <input id='loadMoreButton' class='text niceButton' type='button' value='Load 100 additional rows' onclick='loadMore()'/><div id='loadMessage'></div><br/><br/><br/><br/>
    </div><br/>
    <div style='float: left; padding-right: 10px'>
        <input type='range' min='0' max='300' value='100' id='zoomSlider' class='text' step='5' onchange="zoom()">
        <output for='zoomSlider' id='zoomLevel'>100% Zoom</output>
    </div>
    <div id="heatmap" class='text'></div>
</div>

<script>
    jQuery(function() {
        jQuery("#zoomSlider").slider();
    });

    var data = ${raw(results)};
    var fields = data.fields;
    var patientIDs = data.patientIDs;
    var probes = data.probes;
    var geneIDs = data.geneIDs;
    var geneSymbols = data.geneSymbols;

    function ownColorSheme() {
        var colorSet = [];
        var i = 100;
        while(i--) {
            colorSet.push(d3.rgb((255 * i) / 100, 0, 0));
        }
        i = 100;
        while(i--) {
            colorSet.push(d3.rgb(0, (255 * (100 - i)) / 100, 0));
        }
        return colorSet;
    }

    var colorSets = [ownColorSheme(),
        ['rgb(84,48,5)','rgb(140,81,10)','rgb(191,129,45)','rgb(223,194,125)','rgb(246,232,195)','rgb(245,245,245)','rgb(199,234,229)','rgb(128,205,193)','rgb(53,151,143)','rgb(1,102,94)','rgb(0,60,48)'],
        ['rgb(255,255,217)','rgb(237,248,177)','rgb(199,233,180)','rgb(127,205,187)','rgb(65,182,196)','rgb(29,145,192)','rgb(34,94,168)','rgb(37,52,148)','rgb(8,29,88)'],
        ['rgb(255,247,236)','rgb(254,232,200)','rgb(253,212,158)','rgb(253,187,132)','rgb(252,141,89)','rgb(239,101,72)','rgb(215,48,31)','rgb(179,0,0)','rgb(127,0,0)']
    ];

    var gridFieldWidth = 40;
    var gridFieldHeight = 40;

    var margin = { top: gridFieldHeight * 2 + 300, right: gridFieldWidth + 300 + 300, bottom: 10, left: 10 };

    var width = gridFieldWidth * patientIDs.length;
    var height = gridFieldHeight * probes.length;

    var selectedPatientIDs = [];
    var animationDuration = 1500;

    var heatmap = d3.select("#heatmap").append("svg")
    .attr("width", (width + margin.left + margin.right) * 4)
    .attr("height", (height + margin.top + margin.bottom) * 4)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var tooltip = d3.select("#heatmap").append("div")
    .attr("class", "tooltip text")
    .style("visibility", "hidden");

    var squareItems = heatmap.append('g');
    var colSortItems = heatmap.append('g');
    var selectItems = heatmap.append('g');
    var patientIDItems = heatmap.append('g');
    var rowSortItems = heatmap.append('g');
    var labelItems = heatmap.append('g');

    function updateHeatmap() {
        var square = squareItems.selectAll('.square')
        .data(fields, function(d) { return d.PATIENTID + d.PROBE; });

        square
        .enter()
        .append("rect")
        .attr('class', function(d) {
            var static = d.SIGNIFICANCE === undefined ? ' static' : '';
            return 'square patientID-' + d.PATIENTID + ' probe-' + d.PROBE + static;
        })
        .attr("x", function(d) { return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth; })
        .attr("y", function(d) { return probes.indexOf(d.PROBE) * gridFieldHeight; })
        .attr("width", gridFieldWidth)
        .attr("height", gridFieldHeight)
        .attr("rx", 0)
        .attr("ry", 0)
        .style("fill", 'white')
        .on("mouseover", function(d) {
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
            tooltip.style("visibility", "hidden");
        });

        square
        .transition()
        .duration(animationDuration)
        .attr("x", function(d) { return patientIDs.indexOf(d.PATIENTID) * gridFieldWidth; })
        .attr("y", function(d) { return probes.indexOf(d.PROBE) * gridFieldHeight; })
        .attr("width", gridFieldWidth)
        .attr("height", gridFieldHeight);

        var colSortText = colSortItems.selectAll('.colSortText')
        .data(patientIDs, function(d) { return d; })
        .enter()
        .append('text')
        .attr('class', 'text colSortText')
        .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight)
        .attr('dy', '0.35em')
        .attr("text-anchor", "middle")
        .text('↑↓');

        var colSortBox = colSortItems.selectAll('.colSortBox')
        .data(patientIDs, function(d) { return d; })
        .enter()
        .append('rect')
        .attr('class', 'box colSortBox')
        .attr('x', function(d, i) { return i * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight)
        .attr('width', gridFieldWidth)
        .attr('height', gridFieldHeight)
        .on("click", function(d, boxIdx) {
            var patientID = patientIDs[boxIdx];
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

        var rowSortText = rowSortItems.selectAll('.rowSortText')
        .data(probes, function(d) { return d; })
        .enter()
        .append('text')
        .attr('class', 'text rowSortText')
        .attr("transform", function(d, i) { return "translate(" + (width + 2 + 0.5 * gridFieldWidth) + ",0)" + "translate(0," + (i * gridFieldHeight + 0.5 * gridFieldHeight) + ")rotate(-90)";})
        .attr('dy', '0.35em')
        .attr("text-anchor", "middle")
        .text('↑↓');

        var rowSortBox = rowSortItems.selectAll('.rowSortBox')
        .data(probes, function(d) { return d; })
        .enter()
        .append('rect')
        .attr('class', 'box rowSortBox')
        .attr('x', width + 2)
        .attr('y', function(d, i) { return i * gridFieldHeight; })
        .attr('width', gridFieldWidth)
        .attr('height', gridFieldHeight)
        .on("click", function(d, boxIdx) {
            var probe = probes[boxIdx];
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

        var selectText = heatmap.selectAll('.selectText')
        .data(patientIDs, function(d) { return d; })
        .enter()
        .append('text')
        .attr('class', 'text selectText')
        .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight)
        .attr('dy', '0.35em')
        .attr("text-anchor", "middle")
        .text('□');

        var selectBox = heatmap.selectAll('.selectBox')
        .data(patientIDs, function(d) { return d; })
        .enter()
        .append('rect')
        .attr('class', 'box selectBox')
        .attr('x', function(d, i) { return i * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight * 2)
        .attr('width', gridFieldWidth)
        .attr('height', gridFieldHeight)
        .on("click", function(patientID) {
            selectCol(patientIDs.indexOf(patientID));
        });

        var patientID = patientIDItems.selectAll('.patientID')
        .data(patientIDs, function(d) { return d; });

        patientID
        .enter()
        .append("text")
        .attr('class', 'patientID')
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
        .attr('class', function(d, i) { return 'probe text';})
        .attr('x', width + gridFieldWidth + 7)
        .attr('y', function(d) { return probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight; })
        .attr('dy', '0.35em')
        .style("text-anchor", "start")
        .text(function(d) { return d; });

        probe
        .transition()
        .duration(animationDuration)
        .attr('x', width + gridFieldWidth + 7)
        .attr('y', function(d) { return probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight; });
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
        updateHeatmap();
        reloadDendrograms();
    }

    function reloadDendrograms() {
        if (colDendrogramVisible) {
            hideColDendrogram();
            showColDendrogram();
        }
        if (rowDendrogramVisible) {
            hideRowDendrogram();
            showRowDendrogram();
        }
    }

    function selectCol(col) {
        if (d3.select('.square.col-' + col).classed('selected')) {
            var index = selectedPatientIDs.indexOf(patientIDs[col]);
            selectedPatientIDs.splice(index, 1);
            d3.selectAll('.square.col-' + col)
            .classed('selected', false);
            d3.select('#selectBoxLabel-' + col)
            .text('□');
        } else {
            selectedPatientIDs.push(parseInt(d3.select('.patientID.col-' + col).text())); // Ugly hack
            d3.selectAll('.square.col-' + col)
            .classed('selected', true);
            d3.select('#selectBoxLabel-' + col)
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
        colorScale = d3.scale.quantile()
        .domain([0, 1])
        .range(colorSets[colorIdx]);
        squareItems.selectAll('.square')
        .transition()
        .duration(animationDuration)
        .style("fill", function(d) { return colorScale(1 / (1 + Math.pow(Math.E, - d.ZSCORE))); });
    }

    function unselectAll() {
        for (var i = 0, len = patientIDs.length; i < len; i++) {
            d3.select('#selectBoxLabel-' + i)
            .text('□');
        }
        d3.selectAll('.square')
        .classed('selected', false)
        .attr('opacity', 1);
        selectedPatientIDs = [];
    }

    var colDendrogramVisible = false;
    function showColDendrogram() {
        var w = 200;

        var cluster = d3.layout.cluster()
        .size([width, w])
        .separation(function(a, b) {
            return 1;
        });

        var diagonal = d3.svg.diagonal()
        .projection(function (d) {
            return [d.x, d.y - margin.top + 20];
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
            return "translate(" + d.x + "," + (d.y - margin.top + 20) + ")";
        }).on('click', function(d) {
            var previousSelection = selectedPatientIDs.slice();
            unselectAll();
            var leafs = d.index.split(' ');
            var i = leafs.length;
            while (i--) {
                selectCol(leafs[i]);
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
    function showRowDendrogram() {
        var h = 280;

        var cluster = d3.layout.cluster()
        .size([height, h])
        .separation(function(a, b) {
            return 1;
        });

        var diagonal = d3.svg.diagonal()
        .projection(function (d) {
            return [width + margin.right - 20 - d.y, d.x];
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
            return "translate(" + (width + margin.right - 20 - d.y) + "," + d.x + ")";
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

    function hideColDendrogram() {
        heatmap.selectAll(".colDendrogram").remove();
        colDendrogramVisible = false;
    }

    function hideRowDendrogram() {
        heatmap.selectAll(".rowDendrogram").remove();
        rowDendrogramVisible = false;
    }

    var initialColOrder = d3.range(patientIDs.length);
    var initialRowOrder = d3.range(probes.length);
    function updateInitialOrder(order, sortValues) {
        var oldOrder = order.slice();
        for (var i = 0; i < sortValues.length; i++) {
            order[i] = oldOrder[sortValues[i]];
        }
    }

    function updateColOrder(sortValues) {
        var sortedPatientIDs = [];
        for (var i = 0; i < sortValues.length; i++) {
            sortedPatientIDs.push(patientIDs[sortValues[i]]);
        }
        patientIDs = sortedPatientIDs;
        unselectAll();
        hideColDendrogram();
        updateHeatmap();
    }

    function updateRowOrder(sortValues) {
        var sortedProbes = [];
        for (var i = 0; i < sortValues.length; i++) {
            sortedProbes.push(probes[sortValues[i]]);
        }
        probes = sortedProbes;
        hideRowDendrogram();
        updateHeatmap();
    }

    function transformClusterOrderWRTInitialOrder(clusterOrder, initialOrder) {
        var newOrder = clusterOrder.slice();
        for (var i = 0; i < clusterOrder.length; i++) {
            newOrder[i] = initialOrder.indexOf(clusterOrder[i]);
        }
        return newOrder;
    }

    function cluster(clustering) {
        var clusterData = data[clustering];
        colDendrogram = JSON.parse(clusterData[2]);
        rowDendrogram = JSON.parse(clusterData[3]);
        updateRowOrder(transformClusterOrderWRTInitialOrder(clusterData[1], initialRowOrder));
        setTimeout(function() {
            updateColOrder(transformClusterOrderWRTInitialOrder(clusterData[0], initialColOrder));
        }, animationDuration);
        setTimeout(function() {
            showColDendrogram();
        }, animationDuration * 2);
        setTimeout(function() {
            showRowDendrogram(JSON.parse(clusterData[3]));
        }, animationDuration * 2 + 200);
    }

    function updateCohorts() {
        alert('This feature will be available as soon as tranSMART supports high dimensional patient selection.');
        return;
        var CONCEPT = '';
        var patientDIVs = [];
        for(var i = 0, len = selectedPatientIDs.length; i < len; i++) {
            var conceptid = CONCEPT + selectedPatientIDs[i] + '\\';
            patientDIVs.push(createQueryCriteriaDIV(conceptid, '', 'novalue', 'LT', '', '', '', 'N', 'alphaicon'));
        }
        setCohorts(patientDIVs, false, false, true);
    }

    function loadMore() {
        var data = prepareFormData();
        var maxRows = probes.length + 100;
        data = addSettingsToData(data, { maxRows: maxRows });
        jQuery("#loadMoreButton").attr("disabled", true);
        jQuery("#loadMoreButton").val('This will last a moment...');
        jQuery.ajax({
            url: pageInfo.basePath + '/SmartR/recomputeOutputDIV',
            type: "POST",
            timeout: '600000',
            data: data
        }).done(function(serverAnswer) {
            if (serverAnswer.error) {
                alert(serverAnswer.error);
                return;
            }
            jQuery("#outputDIV").html(serverAnswer);
            jQuery("#loadMoreButton").attr("disabled", false);
            jQuery("#loadMoreButton").val('Load 100 additional rows');
        }).fail(function() {
            jQuery("#outputDIV").html("AJAX CALL FAILED!");
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
