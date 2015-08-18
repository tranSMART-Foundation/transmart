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

    d3.selection.prototype.moveToFront = function() {
      return this.each(function(){
          this.parentNode.appendChild(this);
      });
    };

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

    var padding = 10;

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

    function arrangeFields(zoom) {
        d3.selectAll('.square').each(function() {
            var square = d3.select(this);
            var squareInfo = getInfo(square);
            var spacing = square.classed('static') ? 0 : padding;
            square
            .attr("x", squareInfo.col * gridFieldWidth)
            .attr("y", squareInfo.row * gridFieldHeight + spacing)
            .attr("width", gridFieldWidth)
            .attr("height", gridFieldHeight);
        });
    }


    function initFields() {
        field = heatmap.selectAll('.square')
        .data(fields)
        .enter()
        .append("rect")
        .attr('class', function(d, i) {
            var col = (i % patientIDs.length);
            var row = Math.floor(i / patientIDs.length);
            var static = d.SIGNIFICANCE === undefined ? ' static' : '';
            return 'square col-' + col + ' ' + 'row-' + row + static;
        })
        .attr("rx", 0)
        .attr("ry", 0)
        .style("fill", 'white')
        .on("mouseover", function(d) {
            var square = d3.select(this);
            var squareInfo = getInfo(square);
            var html = '';
            for(var key in d) {
                html += key + ': ' + d[key] + '<br/>';
            }
            d3.select('.patientID.col-' +  squareInfo.col).classed("highlight", true);
            d3.selectAll('.label.row-' +  squareInfo.row).classed("highlight", true);
            tooltip
            .style("visibility", "visible")
            .html(html)
            .style("left", mouseX() + "px")
            .style("top", mouseY() + "px");
        })
        .on("mouseout", function(d) {
            d3.selectAll(".patientID").classed("highlight", false);
            d3.selectAll(".label").classed("highlight", false);
            tooltip.style("visibility", "hidden");
        });
        arrangeFields();
    }

    function arrangeColSortBoxItems() {
        d3.selectAll('.colSortBoxLabel')
        .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight);

        d3.selectAll('.colSortBox')
        .attr('x', function(d, i) { return i * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight)
        .attr('width', gridFieldWidth)
        .attr('height', gridFieldHeight);
    }

    function initColSortBoxItems() {
        var colSortBox = heatmap.selectAll('.colSortBox')
        .data(patientIDs)
        .enter().append('g');

        colSortBox
        .append('text')
        .attr('class', 'text colSortBoxLabel')
        .attr('id', function(d, i) { return 'colSortBoxLabel-' + i; })
        .attr('dy', '0.35em')
        .attr("text-anchor", "middle")
        .text('↑↓');

        colSortBox
        .append('rect')
        .attr('class', 'box colSortBox')
        .attr('id', function(d, i) { return 'colSortBox-' + i; })
        .on("click", function(box, selectedCol) {
            jQuery('#selectedCluster').val('');
            var rowValues = [];
            for(var row = 0, len1 = probes.length; row < len1; row++) {
                rowValues.push([row, d3.select('.square.col-' + selectedCol + '.row-' + row).property('__data__').ZSCORE]);
            }
            if (isSorted(rowValues)) {
               rowValues.sort(function(x, y) { return x[1] - y[1]; });
            } else {
               rowValues.sort(function(x, y) { return y[1] - x[1]; });
            }
            var sortValues = [];
            for (var i = 0, len2 = rowValues.length; i < len2; i++) {
                sortValues.push(rowValues[i][0]);
            }
            updateRowOrder(sortValues);
        });
        arrangeColSortBoxItems();
    }

    function arrangeRowSortBoxItems() {
        d3.selectAll('.rowSortBoxLabel').each(function() {
            var label = d3.select(this);
            var spacing = label.classed('static') ? 0 : padding;
            label
            .attr("transform", function(d, i) { return "translate(" + (width + 2 + 0.5 * gridFieldWidth) + ",0)" + "translate(0," + (i * gridFieldHeight + 0.5 * gridFieldHeight + spacing) + ")rotate(-90)";});
        });
        

        d3.selectAll('.rowSortBox')
        .attr('x', width + 2)
        .attr('y', function(d, i) { return i * gridFieldHeight; })
        .attr('width', gridFieldWidth)
        .attr('height', gridFieldHeight);
    }

    function initRowSortBoxItems() {
        var rowSortBox = heatmap.selectAll('.rowSortBox')
        .data(probes)
        .enter().append('g');

        rowSortBox
        .append('text')
        .attr('class', 'text rowSortBoxLabel')
        .attr('id', function(d, i) { return 'rowSortBoxLabel-' + i; })
        .attr('dy', '0.35em')
        .attr("text-anchor", "middle")
        .text('↑↓');

        rowSortBox
        .append('rect')
        .attr('class', 'box rowSortBox')
        .attr('id', function(d, i) { return 'rowSortBox-' + i; })
        .on("click", function(boxObj, selectedRow) {
            jQuery('#selectedCluster').val('');
            var colValues = [];
            for(var col = 0, len1 = patientIDs.length; col < len1; col++) {
                colValues.push([col, d3.select('.square.col-' + col + '.row-' + selectedRow).property('__data__').ZSCORE]);
            }
            if (isSorted(colValues)) {
               colValues.sort(function(x, y) { return x[1] - y[1]; });
            } else {
               colValues.sort(function(x, y) { return y[1] - x[1]; });
            }
            var sortValues = [];
            for (var i = 0, len2 = colValues.length; i < len2; i++) {
                sortValues.push(colValues[i][0]);
            }
            updateColOrder(sortValues);
        });
        arrangeRowSortBoxItems();
    }

    function arrangeSelectBoxItems() {
        d3.selectAll('.selectBoxLabel')
        .attr('x', function(d, i) { return i * gridFieldWidth + 0.5 * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight);

        d3.selectAll('.selectBox')
        .attr('x', function(d, i) { return i * gridFieldWidth; })
        .attr('y', -2 - gridFieldHeight * 2)
        .attr('width', gridFieldWidth)
        .attr('height', gridFieldHeight);
    }

    function initSelectBoxItems() {
        var selectBox = heatmap.selectAll('.selectBox')
        .data(patientIDs)
        .enter().append('g');

        selectBox
        .append('text')
        .attr('class', 'text selectBoxLabel')
        .attr('id', function(d, i) { return 'selectBoxLabel-' + i; })
        .attr('dy', '0.35em')
        .attr("text-anchor", "middle")
        .text('□');

        selectBox
        .append('rect')
        .attr('class', 'box selectBox')
        .attr('id', function(d, i) { return 'selectBox-' + i; })
        .on("click", function(d, i) {
            var col = i % patientIDs.length;
            selectCol(col);
        });
        arrangeSelectBoxItems();
    }

    function arrangePatientIDs() {
        d3.selectAll('.patientID').each(function() {
            var patientID = d3.select(this);
            var patientIDInfo = getInfo(patientID);
            patientID
            .attr("transform", function() {
                return "translate(" + (patientIDInfo.col * gridFieldWidth) + ",0)" +
                    "translate(" + (gridFieldWidth / 2) + "," + (-4 - gridFieldHeight * 2) + ")rotate(-45)";
            });
        });
    }

    function initPatientIDs() {
        var patientID = heatmap.selectAll('.patientID')
        .data(patientIDs, function(d) { return d; })
        .enter()
        .append("text")
        .attr('class', function(d, i) { return 'patientID row-0 col-' + i; })
        .style("text-anchor", "start")
        .text(function(d) { return d; });
        arrangePatientIDs();
    }

    var probe;
    function arrangeProbes() {
        var x = width + gridFieldWidth + 7;
        probe.each(function() {
            var probeLabel = d3.select(this);
            var probeLabelInfo = getInfo(probeLabel);
            probeLabel
            .attr('x', x)
            .attr('y', probeLabelInfo.row * gridFieldHeight + 0.5 * gridFieldHeight);
        });
    }

    function initProbes() {
        probe = heatmap.selectAll('.probe')
        .data(probes)
        .enter()
        .append("text")
        .attr('class', function(d, i) { return 'label text col-' + 0 + ' ' + 'row-' + i;})
        .attr('dy', '0.35em')
        .style("text-anchor", "start")
        .text(function(d) { return d; });
        arrangeProbes();
    }

    var geneID;
    function arrangeGeneIDs() {
        var probeMaxWidth = getMaxWidth(probe);
        var x = width + gridFieldWidth + 7 + probeMaxWidth + 7;
        geneID.each(function() {
            var geneID = d3.select(this);
            var geneIDInfo = getInfo(geneID);
            geneID
            .attr('x', x)
            .attr('y', geneIDInfo.row * gridFieldHeight + 0.5 * gridFieldHeight);
        });
    }

    function initGeneIDs() {
        geneID = heatmap.selectAll('.geneID')
        .data(geneIDs)
        .enter()
        .append("text")
        .attr('class', function(d, i) { return 'label text col-' + 1 + ' ' + 'row-' + i;})
        .attr('dy', '0.35em')
        .style("text-anchor", "start")
        .text(function(d) { return d; });
        arrangeGeneIDs();
    }

    var geneSymbol;
    function arrangeGeneSymbols() {
        var pobeMaxWidth = getMaxWidth(probe);
        var geneIDMaxWidth = getMaxWidth(geneID);
        var x = width + gridFieldWidth + 7 + pobeMaxWidth + 7 + geneIDMaxWidth + 7;
        geneSymbol.each(function() {
            var geneSymbol = d3.select(this);
            var geneSymbolInfo = getInfo(geneSymbol);
            geneSymbol
            .attr('x', x)
            .attr('y', geneSymbolInfo.row * gridFieldHeight + 0.5 * gridFieldHeight);
        });
    }

    function initGeneSymbols() {
         geneSymbol = heatmap.selectAll('.geneSymbol')
        .data(geneSymbols)
        .enter()
        .append("text")
        .attr('class', function(d, i) { return 'label text col-' + 2 + ' ' + 'row-' + i;})
        .attr('dy', '0.35em')
        .style("text-anchor", "start")
        .text(function(d) { return d; });
        arrangeGeneSymbols();
    }

    var currentZoomLevel = 1;
    function zoom() {
        var zoomLevel = jQuery("#zoomSlider").val();
        jQuery("#zoomLevel").html(zoomLevel + "% Zoom");
        zoomLevel /= 100;
        currentZoomLevel = zoomLevel;
        gridFieldWidth = 40 * zoomLevel;
        gridFieldHeight = 40 * zoomLevel;
        width = gridFieldWidth * patientIDs.length;
        height = gridFieldHeight * probes.length;
        heatmap
        .attr('width', width + margin.left + margin.right)
        .attr('height', width + margin.top + margin.bottom);
        arrangeFields(zoomLevel);
        arrangePatientIDs();
        arrangeProbes();
        arrangeGeneIDs();
        arrangeGeneSymbols();
        arrangeColSortBoxItems();
        arrangeSelectBoxItems();
        arrangeRowSortBoxItems();
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

    function getInfo(element) {
        var info = {};
        var squareData = element.property('__data__');
        var classes = element.attr('class').split(' ');
        var colName = classes.filter(function(c) { return c.indexOf('col-') > -1; })[0];
        var col = parseInt(colName.substring('col-'.length));
        var rowName = classes.filter(function(c) { return c.indexOf('row-') > -1; })[0];
        var row = parseInt(rowName.substring('row-'.length));
        info.colName = colName;
        info.col = col;
        info.rowName = rowName;
        info.row = row;
        return info;
    }

    function updateColorss(colorIdx) {
        colorScale = d3.scale.quantile()
        .domain([0, 1])
        .range(colorSets[colorIdx]);
        heatmap.selectAll('.square')
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
        updateInitialOrder(initialColOrder, sortValues);
        unselectAll();
        hideColDendrogram();
        for (var i = 0, len = sortValues.length; i < len; i++) {
            heatmap.selectAll('.col-' + sortValues[i] + ':not(.moved):not(.label)')
            .classed('col-' + sortValues[i], false)
            .classed('col-' + i, true)
            .classed('moved', true)
            .transition()
            .duration(animationDuration)
            .attr({
                'x': function() {
                    return d3.select(this).classed('square') ? i * gridFieldWidth : null;
                },
                'transform': function() {
                    return d3.select(this).classed('patientID') ? "translate(" + ( i * gridFieldWidth ) + ",0)" + "translate(" + gridFieldWidth / 2 + "," + (-4 - gridFieldHeight * 2) + ")rotate(-45)" : null;
                }
            });
        }
        d3.selectAll('.square, .patientID')
        .classed('moved', false);
    }

    function updateRowOrder(sortValues) {
        updateInitialOrder(initialRowOrder, sortValues);
        hideRowDendrogram();
        for (var i = 0, len = sortValues.length; i < len; i++) {
            heatmap.selectAll('.row-' + sortValues[i] + ':not(.moved):not(.patientID)')
            .classed('row-' + sortValues[i], false)
            .classed('row-' + i, true)
            .classed('moved', true)
            .transition()
            .duration(animationDuration)
            .attr('y', function() {
                return d3.select(this).classed('square') ? i * gridFieldHeight : i * gridFieldHeight + 0.5 * gridFieldHeight;
            });
        }
        d3.selectAll('.square, .label')
        .classed('moved', false);
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
        initFields();
        initProbes();
        initPatientIDs();
        initGeneIDs();
        initGeneSymbols();
        initSelectBoxItems();
        initColSortBoxItems();
        initRowSortBoxItems();
        reloadDendrograms();
        updateColors(0);
    }

    function reloadHeatmap() {
        heatmap.selectAll('*')
        .remove();
        init();
    }

    init();
</script>
