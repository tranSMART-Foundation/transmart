<!DOCTYPE html>
<meta charset="utf-8">

<style>
    .text {
        font-family: 'Roboto', sans-serif;
    }

    .point {
        opacity: 0.8;
        stroke: white;
    }

    .brush .extent {
        fill: blue;
        opacity: .125;
        shape-rendering: crispEdges;
    }

    .bar rect {
        fill: #FEEAAF;
        stroke: #DEAC3A;
    }

    .bar text {
        fill: orange;
        font-weight: bold;
    }

    .axis path, .axis line {
        fill: none;
        stroke: black;
        stroke-width: 1px;
        shape-rendering: crispEdges;
    }

    .axis {
        font-size: 10px;
    }

    .tick line {
        shape-rendering: crispEdges;
        opacity: 0.2;
    }

    .legend {
        border: 1px solid black;
        padding: 2px;
        position: absolute;
        display: inline-block;
        color: black;
        background-color: rgba(255, 255, 255, .4);
        box-shadow: 10px 10px 20px grey;
    }

    #svgs {
        float: left;
    }

    #scatterplot {
        float: right;
    }

    #histogram1 {
        float: left;
    }

    #histogram2 {
        float: right;
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
        box-shadow: 10px 10px 20px grey;
    }

    .contextMenu {
        position: absolute;
        text-align: center;
        display: inline-block;
        padding: 0px;
        font-size: 12px;
        font-weight: bold;
        color: black;
        background-color: white;
        box-shadow: 10px 10px 20px grey;
    }

    .axisLabels {
        text-anchor: middle;
        font-size: 14px;
        font-weight: bold;
    }

    .mybutton {
        width: 100%;
        height: 30px;
        color: black;
        background-color: white;
        font-size: 12px;
        font-weight: bold;
        border: none;
    }

    .mybutton:hover {
        color: white;
        background: #326FCB;
    }
</style>

<link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
<g:javascript src="resource/d3.js"/>
<div id='controls'></div>
<div id="visualization">
    <div id="histogram1"></div>
    <div id="scatterplot"></div>
    <div id="histogram2"></div>
</div>

<script>
    var animationDuration = 500;
    var tmpAnimationDuration = animationDuration;
    function switchAnimation(checked) {
        if (! checked) {
            tmpAnimationDuration = animationDuration;
            animationDuration = 0;
        } else {
            animationDuration = tmpAnimationDuration;
        }
    }

    var controls = d3.select('#controls').append('svg')
    .attr('width', jQuery("#smartRPanel").width())
    .attr('height', 45);

    var margin = {top: 20, right: 40, bottom: 5, left: 10};
    var width = jQuery("#smartRPanel").width() / 2 - 10 - margin.left - margin.right;
    var height = jQuery("#smartRPanel").height() / 2 - 10 - margin.top - margin.bottom;

    var results = ${results};
    var xLabel = results.xArrLabel;
    var yLabel = results.yArrLabel;
    var xArr = results.xArr;
    var yArr = results.yArr;
    var patientIDs = results.patientIDs;
    var tags = results.tags;
    var originalData = [];
    for (var i = 0; i < patientIDs.length; i++) {
        originalData.push({x:xArr[i], y:yArr[i], uid:patientIDs[i], tag:tags[i]});
    }

    var firstRun = true;

    var legendPosX = 0;
    var legendPosY = 0;

    init(originalData);

    function init(points) {
        x = d3.scale.linear()
        .domain(d3.extent(points, function(d) { return d.x; }))
        .range([0, width]);

        y = d3.scale.linear()
        .domain(d3.extent(points, function(d) { return d.y; }))
        .range([height, 0]);

        displayedData = points.map(function(d) { return {x:x(d.x), y:y(d.y), uid:d.uid, tag:d.tag}; });

        selectedData = displayedData.slice();

        scatterplot = d3.select("#scatterplot").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        tooltip = d3.select("#scatterplot").append("div")
        .attr("class", "tooltip text")
        .style("visibility", "hidden");

        contextMenu = d3.select("#scatterplot").append("div")
        .attr("class", "contextMenu text")
        .style("visibility", "hidden")
        .html("Number of bins<br/><input id='binNumber' class='mybutton text' type='number' min='1' max='20' step='1' onchange='updateBinNumber()'/><br/><input id='updateCohortsButton' class='mybutton text' type='button' value='Update Cohorts' onclick='updateCohorts()'/><br/><input id='zoomButton' class='mybutton text' type='button' value='Zoom' onclick='zoomSelection()'/><br/><input id='excludeButton' class='mybutton text' type='button' value='Exclude' onclick='excludeSelection()'/><br/><input id='resetButton' class='mybutton text' type='button' value='Reset' onclick='reset()'/>");

        d3.select('#scatterplot')
        .on("contextmenu", function() {
            d3.event.preventDefault();
            contextMenu
            .style("visibility", "visible")
            .style("left", mouseX() + "px")
            .style("top", mouseY() + "px");
        });

        drag = d3.behavior.drag()
        .on("drag", dragmove);

        legend = d3.select("#scatterplot").append("div")
        .attr("class", "legend text")
        .call(drag);

        if (firstRun) {
            legend
            .style("left", jQuery('#scatterplot').position().left + margin.left + "px")
            .style("top", jQuery('#scatterplot').position().top + margin.top + "px");
            legendPosX = jQuery('#scatterplot').position().left + margin.left + "px";
            legendPosY = jQuery('#scatterplot').position().top + margin.top + "px";
        } else {
            legend
            .style("left", legendPosX)
            .style("top", legendPosY);
        }

        scatterXAxis = d3.svg.axis()
        .scale(x)
        .ticks(10)
        .tickFormat("")
        .innerTickSize(height)
        .orient("bottom");

        scatterplot.append("g")
        .attr("class", "x axis text")
        .attr("transform", "translate(" + 0 + "," + 0 + ")")
        .call(scatterXAxis);

        scatterXAxisLabel = scatterplot.append("text")
        .attr("class", "axisLabels text")
        .attr("x", width / 2)
        .attr("y", 0 - margin.top + 15)
        .text(shortenConcept(xLabel.toString()));

        scatterYAxis = d3.svg.axis()
        .scale(y)
        .ticks(10)
        .tickFormat("")
        .innerTickSize(width)
        .orient("left");

        scatterplot.append("g")
        .attr("class", "y axis text")
        .attr("transform", "translate(" + width + "," + 0 + ")")
        .call(scatterYAxis);

        scatterYAxisLabel = scatterplot.append("text")
        .attr("class", "axisLabels text")
        .attr("x", height / 2)
        .attr("y", - width - margin.right + 30)
        .attr("transform", "rotate(90)")
        .text(shortenConcept(yLabel.toString()));

        brush = d3.svg.brush()
        .x(d3.scale.identity().domain([-20, width + 20]))
        .y(d3.scale.identity().domain([-20, height + 20]))
        .on("brushend", function() {
            contextMenu
            .style("visibility", "hidden")
            .style("top", -100 + "px");
            updateSelection();
        });

        scatterplot.append("g")
        .attr("class", "brush")
        .on("mousedown", function(){
            if(d3.event.button === 2){
                d3.event.stopImmediatePropagation();
            }
        })        
        .call(brush);

        detectedTags = [];
        detectTags();
        colors = ["#33FF33", "#3399FF", "#CC9900", "#CC99FF", "#FFFF00", "blue", "black"];

        regressionLine = scatterplot.append("line")
        .on("mouseover", function(d) {
            d3.select(this).attr("stroke", "red");
            tooltip
            .style("visibility", "visible")
            .html("slope: " + results.regLineSlope + "<br/>" + "intercept: " + results.regLineYIntercept)
            .style("left", mouseX() + "px")
            .style("top", mouseY() + "px");
        })
        .on("mouseout", function(d) {
            d3.select(this).attr("stroke", "orange");
            tooltip.style("visibility", "hidden");
        });

        bins = 10;
        d3.select("#binNumber")
        .attr("value", bins)
        .on("change", updateHistogram);

        histogram1 = d3.select("#histogram1").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        hist1xAxis = d3.svg.axis()
        .scale(y)
        .orient("right");

        histogram2 = d3.select("#histogram2").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        hist2xAxis = d3.svg.axis()
        .scale(x)
        .ticks(10)
        .orient("top");

        updateScatterplot();
        updateCorrelation();
        updateHistogram();
    }

    function getOriginalPointWithUID(uid) {
        var i = originalData.length;
        while(i--) {
            if (originalData[i].uid === uid) {
                return originalData[i];
            }
        }
    }

    function detectTags() {
        var i = originalData.length;
        while (i--) {
            var index = detectedTags.indexOf(originalData[i].tag);
            if (index === -1) {
                detectedTags.push(originalData[i].tag);
            }
        }
        detectedTags.sort();
    }

    function getColor(tag) {
        if (! tag) {
            return "black";
        }
        var index = detectedTags.indexOf(tag);
        if (index === -1) {
            alert('An unexpected error occured while setting color tags!');
        }
        return colors[index];
    }

    function dragmove(d) {
        d3.select(this)
        .style("left", mouseX() + "px")
        .style("top", mouseY() + "px");
        legendPosX = mouseX() + "px";
        legendPosY = mouseY() + "px";
    }

    function updateScatterplot() {
        point = scatterplot.selectAll(".point")
        .data(displayedData, function(d) { return d.uid; });

        point.enter()
        .append("circle")
        .attr("class", "point")
        .style("fill", function(d) { return getColor(d.tag); })
        .on("mouseover", function(d) {
            var originalPoint = getOriginalPointWithUID(d.uid);
            d3.select(this).style("fill", "red");
            tooltip
            .style("visibility", "visible")
            .html(shortenConcept(xLabel.toString()) + ": " + originalPoint.x + "<br/>" +
                shortenConcept(yLabel.toString()) + ": " + originalPoint.y + "<br/>" +
                "patientID: " + originalPoint.uid + "<br/>" +
                (originalPoint.tag ? ("Tag: " + originalPoint.tag) : ''))
            .style("left", mouseX() + "px")
            .style("top", mouseY() + "px");
        })
        .on("mouseout", function(d) {
            var p = d3.select(this);
            if (p.classed('selected')) {
                p.style("fill", 'white');
            } else {
                p.style("fill", function(d) { return getColor(d.tag); });
            }
            tooltip.style("visibility", "hidden");
        })
        .attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; })
        .attr("r", 5);

        point.exit()
        .transition().duration(animationDuration)
        .attr("r", 0)
        .remove();
    }

    function updateRegressionLine() {
        regressionLine.attr("stroke", "orange");
        regressionLine.attr("stroke-width", 3);
        var minX, maxX;
        if (! selectedData && displayedData.length > 2) {
            minX = d3.min(displayedData, function(d) { return d.x; });
            maxX = d3.max(displayedData, function(d) { return d.x; });
        } else if (selectedData.length > 3) {
            minX = d3.min(selectedData, function(d) { return d.x; });
            maxX = d3.max(selectedData, function(d) { return d.x; });
        } else {
            regressionLine
            .transition()
            .duration(animationDuration)
            .attr("stroke-width", 10)
            .transition()
            .duration(animationDuration)
            .attr("stroke-width", 0);
            return;
        }

        regressionLine
        .transition()
        .duration(animationDuration)
        .attr("x1", minX)
        .attr("y1", y(parseFloat(results.regLineYIntercept) + parseFloat(results.regLineSlope) * x.invert(minX)))
        .attr("x2", maxX)
        .attr("y2", y(parseFloat(results.regLineYIntercept) + parseFloat(results.regLineSlope) * x.invert(maxX)));
    }

    function updateSelection() {
        point.each(function(d) {
            d3.select(this)
            .classed("selected", false)
            .style('fill', getColor(d.tag))
            .style('stroke', 'white');
        });
        var extent = brush.extent();
        var x0 = extent[0][0],
            y0 = extent[0][1],
            x1 = extent[1][0],
            y1 = extent[1][1];
        var newSelectedData = [];
        point.each(function(d) {
            if (x0 <= d.x && d.x <= x1 && y0 <= d.y && d.y <= y1) {
                d3.select(this)
                .classed("selected", true)
                .style('fill', 'white')
                .style('stroke', getColor(d.tag));
                newSelectedData.push({x:d.x, y:d.y, uid:d.uid, tag:d.tag});
            }
        });
        if ((selectedData.length === displayedData.length && newSelectedData.length === 0) || equalUIDs(newSelectedData, selectedData)) {
            return;
        }
        selectedData = newSelectedData.length ? newSelectedData : displayedData.slice();
        updateCorrelation();
        updateHistogram();
    }

    function updateBinNumber() {
        bins = parseInt(jQuery("#binNumber").val());
    }

    function equalUIDs(arr1, arr2) {
        if (arr1.length !== arr2.length) {
            return false;
        }
        var i = arr2.length;
        while (i--) {
            var index = indexOfObjWithUID(arr1, arr2[i]);
            if (index === -1) {
                return false;
            }
        }
        return true;
    }

    function indexOfObjWithUID(arr, obj) {
        var i = arr.length;
        while (i--) {
            if (arr[i].uid === obj.uid) {
                return i;
            }
        }
        return -1;
    }

    function zoomSelection() {
        if (selectedData.length === displayedData.length || selectedData.length < 2) {
            alert("Please select at least two elements before zooming!");
            return;
        }
        var newPoints = [];
        var i = selectedData.length;
        while(i--) {
            var index = indexOfObjWithUID(originalData, selectedData[i]);
            if (~ index) {
                newPoints.push(originalData[index]);
            }
        }
        cleanup();
        init(newPoints);
    }

    function excludeSelection() {
        if (selectedData.length === displayedData.length || selectedData.length === 0) {
            return;
        }
        var i = selectedData.length;
        while (i--) {
            var index = indexOfObjWithUID(displayedData, selectedData[i]);
            if (~ index) {
                displayedData.splice(index, 1);
            }
        }
        selectedData = displayedData.slice();
        updateScatterplot();
        updateCorrelation();
        updateHistogram();
    }

    function updateCorrelation() {
        if (firstRun) {
            firstRun = false;
            updateRegressionLine();
            updateLegend();
            return;
        }
        updateLegend('Updating...');
        var data = prepareFormData();
        var xLowHigh = d3.extent(selectedData, function(d) { return getOriginalPointWithUID(d.uid).x; });
        var yLowHigh = d3.extent(selectedData, function(d) { return getOriginalPointWithUID(d.uid).y; });
        data = addSettingsToData(data, { xLow: xLowHigh[0] });
        data = addSettingsToData(data, { xHigh: xLowHigh[1] });
        data = addSettingsToData(data, { yLow: yLowHigh[0] });
        data = addSettingsToData(data, { yHigh: yLowHigh[1] });
        jQuery.ajax({
            url: pageInfo.basePath + '/SmartR/updateOutputDIV',
            type: "POST",
            timeout: '600000',
            data: data
        }).done(function(serverAnswer) {
            serverAnswer = JSON.parse(serverAnswer);
            if (serverAnswer.error) {
                alert(serverAnswer.error);
                return;
            }
            results = serverAnswer;
            updateRegressionLine();
            updateLegend();
        }).fail(function() {
            updateLegend("AJAX CALL FAILED");
        });
    }

    function updateCohorts() {
        var concept1LowHigh = d3.extent(selectedData, function(d) { return getOriginalPointWithUID(d.uid).x; });
        var concept2LowHigh = d3.extent(selectedData, function(d) { return getOriginalPointWithUID(d.uid).y; });
        var div1 = createQueryCriteriaDIV(xLabel.toString(), 'ratio', 'numeric', 'BETWEEN', concept1LowHigh[0], concept1LowHigh[1], 'ratio', 'Y', 'valueicon');
        var div2 = createQueryCriteriaDIV(yLabel.toString(), 'ratio', 'numeric', 'BETWEEN', concept2LowHigh[0], concept2LowHigh[1], 'ratio', 'Y', 'valueicon');
        setCohorts([div1, div2], true, false, true);
    }

    function updateLegend(text) {
        var html = (
            'Correlation Coefficient: ' + (text ? text : results.correlation)  + '<br/>' +
            'p-value: ' + (text ? text : results.pvalue) + '<br/>' +
            'Method: ' + (text ? text : results.method) + '<br/>' + '<br/>' +
            'Selected: ' + selectedData.length + '<br/>' +
            'Displayed: ' + displayedData.length + '<br/>' +
            'Excluded: ' + (originalData.length - displayedData.length)) + '<br/>' + '<br/>';

        for (var i = 0, len = detectedTags.length; i < len; i++) {
            if (! detectedTags[i]) {
                html = html + '<p style="background:' + getColor(detectedTags[i]) + '; color:white">Default</p>';
            } else {
                html = html + '<p style="background:' + getColor(detectedTags[i]) + '">' + detectedTags[i] + '</p>';
            }
        }

        legend.html(html);
    }

    function cleanup() {
        d3.select("#scatterplot").selectAll("*").remove();
        d3.select("#histogram1").selectAll("*").remove();
        d3.select("#histogram2").selectAll("*").remove();
    }

    function reset() {
        cleanup();
        init(originalData);
    }

    function updateHistogram() {
        var hist1Data = d3.layout.histogram()
        .bins(bins)
        (selectedData.map(function(d) { return d.y; }));

        var hist2Data = d3.layout.histogram()
        .bins(bins)
        (selectedData.map(function(d) { return d.x; }));

        histogram1.selectAll("*").remove();
        histogram2.selectAll("*").remove();

        var hist1Bar = histogram1.selectAll(".bar")
        .data(hist1Data)
        .enter().append("g")
        .attr("class", "bar");

        var hist2Bar = histogram2.selectAll(".bar")
        .data(hist2Data)
        .enter().append("g")
        .attr("class", "bar");

        var hist1BarScale = d3.scale.linear()
        .domain([0, d3.max(hist1Data, function(d) { return d.y; })])
        .range([0, width]);

        var hist2BarScale = d3.scale.linear()
        .domain([0, d3.max(hist2Data, function(d) { return d.y; })])
        .range([0, height]);

        hist1Bar.append("rect")
        .attr("width", 0)
        .attr("height", hist1Data[0].dx)
        .attr("x", width)
        .attr("y", function(d, i) { return hist1Data[i].x; })
        .transition()
        .delay(function(d, i) { return i * 25; })
        .duration(animationDuration)
        .attr("x", function(d) { return width - hist1BarScale(d.y); })
        .attr("width", function(d) { return hist1BarScale(d.y); });

        hist1Bar.append("text")
        .attr('class', 'text')
        .attr("x", width)
        .attr("y", function(d, i) { return hist1Data[i].x; })
        .transition()
        .delay(function(d, i) { return i * 25; })
        .duration(animationDuration)
        .attr("dy", ".35em")
        .attr("x", function(d) { return width - hist1BarScale(d.y) + 10; })
        .attr("y", function(d, i) { return hist1Data[i].x + hist1Data[i].dx / 2; })
        .text(function(d) { return d.y ? d.y : ''; });

        histogram1.append("g")
        .attr("class", "x axis text")
        .attr("transform", "translate(" + width + "," + 0 + ")")
        .call(hist1xAxis);

        hist2Bar.append("rect")
        .attr("width", hist2Data[0].dx)
        .attr("height", 0)
        .attr("x", function(d, i) { return hist2Data[i].x; })
        .attr("y", 0)
        .transition()
        .delay(function(d, i) { return i * 25; })
        .duration(animationDuration)
        .attr("height", function(d) { return hist2BarScale(d.y); });

        hist2Bar.append("text")
        .attr('class', 'text')
        .attr("x", function(d, i) { return hist2Data[i].x; })
        .attr("y", 0)
        .transition()
        .delay(function(d, i) { return i * 25; })
        .duration(animationDuration)
        .attr("dx", "-.5em")
        .attr("x", function(d, i) { return hist2Data[i].x + hist2Data[i].dx / 2; })
        .attr("y", function(d) { return hist2BarScale(d.y) - 5; })
        .text(function(d) { return d.y ? d.y : ''; });

        histogram2.append("g")
        .attr("class", "x axis text")
        .attr("transform", "translate(" + 0 + "," + 0 + ")")
        .call(hist2xAxis);
    }

    var buttonWidth = 200;
    var buttonHeight = 40;
    var padding = 20;

    createD3Switch({
        location: controls,
        onlabel: 'Animation ON',
        offlabel: 'Animation OFF',
        x: 2,
        y: 2,
        width: buttonWidth,
        height: buttonHeight,
        callback: switchAnimation,
        checked: true
    });
</script>
