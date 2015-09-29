<!DOCTYPE html>
<style>
    .text {
        font-family: 'Roboto', sans-serif;
    }

	.timeBox {
		stroke: white;
		stroke-width: 1px;
		shape-rendering: crispEdges;
		fill: black;
	}

	.line {
  		stroke-width: 3px;
  		fill: none;
	}

    .hovered {
        opacity: 1;
        stroke-width: 5px;
    }

    .selected {
        opacity: 1;
        stroke-width: 4px;
    }

    .assigned {
        stroke-width: 5px;
    }

	.hoverline {
		stroke: white;
  		stroke-width: 1px;
        pointer-events: none;
        shape-rendering: crispedges;
        stroke-dasharray: 5, 10;
	}

	.axis path, .axis line {
    	fill: none;
    	shape-rendering: crispedges;
    	stroke: black;
	}

    .slider {
        fill: black;
    }

    .brush .extent {
        stroke: none;
        stroke-width: 0px;
        fill: steelblue;
        fill-opacity: 0.60;
    }

    .box {
        stroke: none;
        fill: white;
        stroke-width: 1px;
        shape-rendering: crispedges;
        fill-opacity: 0;
    }

    .box:hover {
        cursor: pointer;
        fill: orange;
        fill-opacity: 0.4;
    }

    .draggableTimepoints {
        cursor: pointer;
    }

    .x.brush .extent {
        stroke: none;
        stroke-width: 0px;
        fill: rgb(81, 255, 237);
        fill-opacity: 0.40;
        shape-rendering: crispEdges;
    }

    .x.brush>.resize {
        display: none;
    }

    .brushbutton {
        stroke: black;
        fill: rgb(0, 18, 152);
        stroke-width: 0px;
        shape-rendering: crispedges;
    }

    .brushbutton:hover {
        cursor: pointer;
        stroke: black;
        fill: #E700FF;
    }

    .draggableTimepoints>.box:hover {
        cursor: pointer;
        fill: orange;
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

    .contextMenu {
        position: absolute;
        text-align: center;
        display: inline-block;
        padding: 0px;
        font-size: 12px;
        font-weight: bold;
        color: black;
        background-color: white;
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

    .correlogram {
        stroke-width: 1px;
        shape-rendering: crispEdges;
    }
</style>

<div id="visualization">
</div>

<link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
<g:javascript src="resource/d3.js"/>
<g:javascript src="resource/2D.js"/>

<script type="text/javascript">
    d3.selection.prototype.moveToFront = function() {
        return this.each(function(){
            this.parentNode.appendChild(this);
        });
    };

	var results = ${raw(results)};
    var data = results.data;
    var concepts = results.concepts;
    var patientIDs = results.patientIDs;
    var timepoints = results.timepoints;

    var timelineHeight = 200;
    var timelineWidth = 1000;
    var width = timelineWidth;
    var height = timelineHeight * concepts.length;
    var globalXAxisDist = 100;
    var localXAxisDist = 0;
    var margin = {top: 50, right: 300 + 30, bottom: globalXAxisDist + 100, left: 240};

    var colors = d3.scale.category10()
    .domain(patientIDs);

    var x = d3.scale.ordinal()
	.domain(timepoints)
	.rangePoints([0, timelineWidth]);

    var backgroundCheckChecked = false;
    function swapBackgroundColor(checked) {
        if (! checked) {
            d3.selectAll('.timeBox')
            .transition()
            .duration(1500)
            .style('stroke', 'black')
            .style('fill', 'white');
            d3.selectAll('.hoverline')
            .transition()
            .duration(1500)
            .style('stroke', 'black');
            backgroundCheckChecked = true;
        } else {
            d3.selectAll('.timeBox')
            .transition()
            .duration(1500)
            .style('stroke', 'white')
            .style('fill', 'black');
            d3.selectAll('.hoverline')
            .transition()
            .duration(1500)
            .style('stroke', 'white');
            backgroundCheckChecked = false;
        }

    }

    function updateLineDataPropertyByTimepoints() {
        d3.selectAll('.line').each(function(d) {
            var line = d3.select(this);
            var skiped = 0;
            for (var i = 0; i < x.domain().length; i++) {
                var timepoint = x.domain()[i];
                var index = ownIndexOf(d, getEqualityCheck(undefined, timepoint, undefined, undefined));
                if (index === -1) {
                    skiped += 1;
                    continue;
                }
                var tmp = d[i - skiped];
                d[i - skiped] = d[index];
                d[index] = tmp;
            }
        });
    }

    function invertXEqual(value) {
        var leftEdges = x.range();
        var rangeBandWidth = timelineWidth / x.domain().length;
        for (var i = 0; value > (leftEdges[i] + rangeBandWidth / 2); i++) {}
        if (i > timepoints.length - 1) {
            i = timepoints.length - 1;
        }
        return x.domain()[i];
    }

    function invertXFromLeft(value) {
        var leftEdges = x.range();
        var rangeBandWidth = timelineWidth / x.domain().length;
        for (var i = 0; value > leftEdges[i]; i++) {}
        return x.domain()[i];
    }

    function invertXFromRight(value) {
        var leftEdges = x.range();
        var rangeBandWidth = timelineWidth / x.domain().length;
        for (var i = 0; value > leftEdges[i]; i++) {}
        return x.domain()[i - 1];
    }

    var svg = d3.select("#visualization").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")

    var contextMenu = d3.select("#visualization").append("div")
    .attr("class", "contextMenu text")
    .style("visibility", "hidden")
    .html("<input id='setColorButton' class='mybutton' type='button' value='Assign Unique Color' onclick='assignUniqueColor()'/><br/><input id='resetColorsButton' class='mybutton' type='button' value='Reset Line Colors' onclick='resetLineColors()'/><br/><input id='updateCohortsButton' class='mybutton' type='button' value='Update Cohorts' onclick='updateCohorts()'/>");

    d3.select('#visualization')
    .on("contextmenu", function() {
        d3.event.preventDefault();
        contextMenu
        .style("visibility", "visible")
        .style("left", mouseX() + 'px')
        .style("top", mouseY() + 'px');
    });

    var tooltip = d3.select("#visualization").append("div")
    .attr("class", "tooltip text")
    .style("visibility", "hidden");

    function moveAxisBox(fromTimepoint, toTimepoint) {
        var tickWidth = x.range()[1];
        d3.select('.global.x.axis.box.timepoint-' + fromTimepoint.replace(/ /g, ''))
        .transition()
        .duration(100)
        .attr('x', x(toTimepoint) - tickWidth / 2);
        d3.select('.global.x.axis.text.timepoint-' + fromTimepoint.replace(/ /g, ''))
        .transition()
        .duration(100)
        .attr("transform", "translate(" + x(toTimepoint) + "," + (height + globalXAxisDist + 10 + padding()) + ")rotate(90)");
    }

    function swapTimepoints(fromIndex, toIndex) {
        moveAxisBox(timepoints[fromIndex], timepoints[toIndex]);
        moveAxisBox(timepoints[toIndex], timepoints[fromIndex]);
        var tmp = timepoints[toIndex];
        timepoints[toIndex] = timepoints[fromIndex];
        timepoints[fromIndex] = tmp;
        x.domain(timepoints);
        xAxis.scale(x);
    }

    function dragstart() {
        initBrushButtons();
    }

    function dragmove() {
        var xPos = d3.event.x;
        var timepointDragged = d3.select(this).property('__data__');
        var indexDragged = timepoints.indexOf(timepointDragged);
        var tickWidth = x.range()[1];
        var timepointHovered = invertXEqual(xPos);
        var indexHovered = timepoints.indexOf(timepointHovered);

        while (Math.abs(dist = indexDragged - indexHovered) > 0) {
            var toIndex = indexHovered;
            if (dist > 1) {
                toIndex = indexDragged - 1;
            } else if (dist < -1) {
                toIndex = indexDragged + 1;
            }
            swapTimepoints(indexDragged, toIndex);
            indexDragged = toIndex;
        }
    }

    function dragend() {
        var timepointDragged = d3.select(this).property('__data__');
        moveAxisBox(timepointDragged, timepointDragged);
        updateLineDataPropertyByTimepoints();
        updateTimelines(true);
        updateLocalXAxis();
    }

    var axisLabelDrag = d3.behavior.drag()
    .on('dragstart', dragstart)
    .on("drag", dragmove)
    .on('dragend', dragend);

	var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");

    svg.append("g")
    .attr("class", "local x axis")
    .attr("transform", "translate(0," + (height + localXAxisDist + padding()) + ")")
    .call(xAxis)
    .selectAll("text")
    .attr('class', 'text')
    .attr("y", 10)
    .attr("x", 5)
    .attr("dy", ".35em")
    .attr("transform", "rotate(45)")
    .style("text-anchor", "start");

    svg.append("g")
    .attr("class", "global x axis")
    .attr("transform", "translate(0," + (height + globalXAxisDist + padding()) + ")")
    .call(xAxis)
    .selectAll("text")
    .attr('class', 'text')
    .attr('visibility', 'hidden');

    var globalXAxisLabel = svg.selectAll('.globalXAxisLabels')
    .data(timepoints)
    .enter()
    .append('g')
    .attr('class', 'draggableTimepoints')
    .call(axisLabelDrag);

    globalXAxisLabel
    .append('text')
    .attr('class', function(d) { return 'global x axis text timepoint-' + d.replace(/ /g, ''); })
    .attr("transform", function(d) { return "translate(" + x(d) + "," + (height + globalXAxisDist + 10 + padding()) + ")rotate(90)"; })
    .attr('dy', '0.35em')
    .attr("text-anchor", "start")
    .text(function(d) { return d; });

    maxTextWidth = getMaxWidth(d3.selectAll('.global.x.axis.text'));

    globalXAxisLabel
    .append('rect')
    .attr('class', function(d) { return 'global x axis box timepoint-' + d.replace(/ /g, ''); })
    .attr('x', function(d) { return x(d) - x.range()[1] / 2; })
    .attr('y', height + globalXAxisDist + 10 - 2 + padding())
    .attr('width', timelineWidth / timepoints.length)
    .attr('height', maxTextWidth + 4);

    function smoothData(checked) {
        if (checked) {
            lineGen.interpolate('basis');
        } else {
            lineGen.interpolate('linear');
        }
        updateTimelines();
    }

	var lineGen = d3.svg.line()
	.x(function(d) {
        if (x.domain().indexOf(d.timepoint) < 0) {
            var index = timepoints.indexOf(d.timepoint);
            var firstDomainIndex = timepoints.indexOf(x.domain()[0]);
            if (index < firstDomainIndex) {
                return 0;
            }
            return timelineWidth;
        }
        return x(d.timepoint);
    })
	.y(function(d) { return y(d.value); })
	.interpolate('linear');

    function padding(i) {
        var space = 5;
        return i === undefined ? space * (concepts.length - 1) : space * i;
    }

    var timelineBoxText = svg.selectAll('.timelineBoxText')
    .data(concepts)
    .enter()
    .append('text')
    .attr('class', 'text')
    .attr("transform", function(d, i) { return "translate(" + (timelineWidth + 10) + "," + (i * timelineHeight + timelineHeight / 2 + padding(i)) + ")rotate(90)"; })
    .attr("dy", ".35em")
    .style("text-anchor", "middle")
    .text(function(d) { return shortenConcept(d); });

    var timelineBox = svg.selectAll('.timelineBox')
    .data(concepts)
    .enter()
    .append('rect')
    .attr('class', 'timeBox')
    .attr('width', timelineWidth)
    .attr('height', timelineHeight)
    .attr('x', 0)
    .attr('y', function(d, i) { return timelineHeight * i + padding(i); })
    .on('mousemove', function() {
        updateHoverLine(d3.mouse(this));
    });

    function updateHoverLine(point) {
        var timepoint = invertXEqual(point[0]);
        var x1 = x(timepoint);
        var x2 = x1;
        hoverlineX
        .attr('x1', x1)
        .attr('x2', x2)
        .style("visibility", "visible")
        .moveToFront();

        hoverlineY
        .attr('y1', point[1])
        .attr('y2', point[1])
        .style("visibility", "visible")
        .moveToFront();
    }

    function ownIndexOf(collection, filter) {
        for (var i = 0; i < collection.length; i++) {
            if(filter(collection[i], i, collection)) {
                return i;
            }
        }
        return -1;
    }

    var hoverlineX = svg.append('line')
    .attr('class', 'hoverline')
    .attr('y1', 0)
    .attr('y2', height + localXAxisDist + 7)
    .style("visibility", "hidden");

    var hoverlineY = svg.append('line')
    .attr('class', 'hoverline')
    .attr('x1', -7)
    .attr('x2', timelineWidth)
    .style("visibility", "hidden");

    var brushZone = [-10, timelineWidth + 10];
    var axisBrush = d3.svg.brush()
    .x(d3.scale.identity().domain(brushZone))
    .extent(brushZone)
    .on("brush", function() {
        updateBrushButtonPos();
        axisBrushed();
    });

    var brushHeight = 50;
    svg.append("g")
    .attr("class", "x brush")
    .call(axisBrush)
    .selectAll('rect')
    .attr('y', height + globalXAxisDist + padding() - 0.5 * brushHeight)
    .attr('height', brushHeight);

    var brushButtonDragLeft = d3.behavior.drag()
    .on("drag", function() { return setBrushButtonPos('left', d3.event.x); });

    var brushButtonDragRight = d3.behavior.drag()
    .on("drag", function() { return setBrushButtonPos('right', d3.event.x); });

    var brushButtonWidth = 10;
    var brushButtonLeft = svg.append('rect')
    .attr('class', 'brushbutton left')
    .attr('y', height + globalXAxisDist + padding() - 0.25 * brushHeight)
    .attr('height', brushHeight / 2)
    .attr('width', brushButtonWidth)
    .call(brushButtonDragLeft);

    var brushButtonRight = svg.append('rect')
    .attr('class', 'brushbutton right')
    .attr('y', height + globalXAxisDist + padding() - 0.25 * brushHeight)
    .attr('height', brushHeight / 2)
    .attr('width', brushButtonWidth)
    .call(brushButtonDragRight);

    function setBrushButtonPos(button, xPos) {
        var minXPos = brushZone[0] - brushButtonWidth / 2;
        var maxXPos = brushZone[1] - brushButtonWidth / 2;
        xPos = xPos < minXPos ? minXPos : xPos;
        xPos = xPos > maxXPos ? maxXPos : xPos;
        if (button === 'left' && xPos >= parseInt(brushButtonRight.attr('x'))) {
            xPos = parseInt(brushButtonRight.attr('x')) - 1;
        } else if (button === 'right' && xPos <= parseInt(brushButtonLeft.attr('x'))) {
            xPos = parseInt(brushButtonLeft.attr('x')) + 1;
        }
        d3.select('.brushbutton.' + button)
        .attr('x', xPos);
        if (button === 'left') {
            axisBrush.extent([xPos + brushButtonWidth / 2, axisBrush.extent()[1]]);
        } else {
            axisBrush.extent([axisBrush.extent()[0], xPos + brushButtonWidth / 2]);
        }
        d3.select('.x.brush')
        .call(axisBrush);
        axisBrushed();
    }

    function initBrushButtons() {
        setBrushButtonPos('left', brushZone[0] - brushButtonWidth / 2);
        setBrushButtonPos('right', brushZone[1] - brushButtonWidth / 2);
        axisBrushed();
    }

    function updateBrushButtonPos() {
        var extent = axisBrush.extent();
        var x1 = extent[0];
        var x2 = extent[1];
        setBrushButtonPos('left', x1);
        setBrushButtonPos('right', x2);
    }

    var oldDomain = [];
    function axisBrushed() {
        x.domain(timepoints);
        var extent = axisBrush.extent();
        var x1 = extent[0];
        var x2 = extent[1];
        var startTimePoint = invertXFromLeft(x1);
        var stopTimePoint = invertXFromRight(x2);
        var domain = timepoints.slice(timepoints.indexOf(startTimePoint), timepoints.indexOf(stopTimePoint) + 1);
        if (axisBrush.empty()) {
            initBrushButtons();
            x.domain(timepoints);
        } else {
            x.domain(domain);
        }
        if (oldDomain && domain.toString() === oldDomain.toString()) {
            return;
        }
        oldDomain = domain.slice();
        xAxis.scale(x);
        updateTimelines();
        updateLocalXAxis();
    }

    function clearUnusedBrush(current) {
        for (var i = 0; i < timelineBrushes.length; i++) {
            if (i === current) {
                continue;
            }
            d3.select('.brush.box-' + i)
            .call(timelineBrushes[i].clear());
        }
    }

    function timelineBrushStart(box) {
        clearUnusedBrush(box);
    }

    var selectedPatientIDs = [];
    function timelineBrushed(box, yScale) {
        var extent = timelineBrushes[box].extent();
        var x1 = extent[0][0],
            y1 = extent[0][1],
            x2 = extent[1][0],
            y2 = extent[1][1];
        var startTimePoint = invertXFromLeft(x1);
        var stopTimePoint = invertXFromRight(x2);
        var xDomain = timepoints.slice(timepoints.indexOf(startTimePoint), timepoints.indexOf(stopTimePoint) + 1);
        var yDomain = [yScale.invert(y1), yScale.invert(y2)];

        var brushPath = svg.append('path')
        .attr('id', 'brushPolygon')
        .attr('d', 'M' + x1 + ' ' + y1 + 'L' + x2 + ' ' + y1 + 'L' + x2 + ' ' + y2 + 'L' + x1 + ' ' + y2 + 'Z')
        .style('fill', 'none')
        .node();
        d3.selectAll('.line')
        .classed('selected', false)
        .attr('opacity', 1);
        selectedTimepoints = xDomain;
        selectedPatientIDs = [];
        d3.selectAll('.line').each(function() {
            var line = d3.select(this);
            var linePath = line.node();
            var shape1 = new Path(linePath);
            var shape2 = new Path(brushPath);
            var intersections = Intersection.intersectShapes(shape1, shape2);
            if (intersections.status === 'Intersection') {
                var patientID = d3.select(this).property('__data__')[0].patientID;
                d3.selectAll('.line.patientID-' + patientID)
                .classed('selected', true);
                selectedPatientIDs.push(patientID);
            }
        });
        if (selectedPatientIDs.length > 0) {
            d3.selectAll('.line:not(.selected)')
            .attr('opacity', 0.4);
        }
        d3.select('#brushPolygon').remove();
    }

    function clearControls() {
        contextMenu
        .style("visibility", "hidden");
        clearUnusedBrush(-1);
        d3.selectAll('.line')
        .classed('selected', false);
    }

    function updateUsedColorList() {
        usedColors = [];
        d3.selectAll('.line').each(function() {
            usedColors.push(d3.select(this).attr('stroke'));
        });
    }

    var uniqueColors = ["#1b9e77","#d95f02","#7570b3","#e7298a","#66a61e","#e6ab02","#a6761d","#666666"];
    var usedColors = [];
    function getUnusedColor() {
        updateUsedColorList();
        for (var i = 0; i < uniqueColors.length; i++) {
            var color = uniqueColors[i];
            var index = usedColors.indexOf(color);
            if (index === -1) {
                return color;
            }
        }
        return '';
    }

    function assignUniqueColor(ids) {
        ids = ids === undefined ? selectedPatientIDs : ids;
        if (ids.length === 0) {
            return;
        }
        clearControls();
        var color = getUnusedColor();
        if (color === '') {
            alert('Sorry, no more colors available!');
            return;
        }
        for (var i = 0; i < ids.length; i++) {
            var patientID = ids[i];
            d3.selectAll('.line.patientID-' + patientID)
            .classed('assigned', true)
            .attr('stroke', color);
        }
        d3.selectAll('.line')
        .attr('opacity', 1);
    }

    function resetLineColors() {
        usedColors = [];
        clearControls();
        for (var i = 0; i < patientIDs.length; i++) {
            var patientID = patientIDs[i];
            d3.selectAll('.line.patientID-' + patientID)
            .classed('assigned', false)
            .attr('opacity', 1)
            .attr('stroke', colors(patientID));
        }
    }

    function updateGlobalXAxis() {
        svg.select(".global.x.axis")
        .call(xAxis)
        .selectAll("text")
        .attr('class', 'text')
        .attr("y", 0)
        .attr("x", 5)
        .attr("dy", ".35em")
        .attr("transform", "rotate(90)")
        .style("text-anchor", "start")
        .call(drag);
    }

    function updateLocalXAxis() {
        svg.select(".local.x.axis")
        .call(xAxis)
        .selectAll("text")
        .attr('class', 'text')
        .attr("y", 10)
        .attr("x", 5)
        .attr("dy", ".35em")
        .attr("transform", "rotate(45)")
        .style("text-anchor", "start");
    }

    function updateTimelines(animate) {
        var animationLength = animate ? 500 : 0;
        for (var i = 0; i < concepts.length; i++) {
            var concept = concepts[i];
            y = yScales[concept];
            svg.selectAll('.line.concept-' + i)
            .transition()
            .duration(animationLength)
            .attr('d', lineGen);
        }
    }

    var timelineBrushes = [];
    function addBrushToTimelineBox(box, yScale) {
        timelineBrush = d3.svg.brush()
        .x(d3.scale.identity()
            .domain([0, timelineWidth]))
        .y(d3.scale.identity()
            .domain([box * timelineHeight + padding(box), (box + 1) * timelineHeight + padding(box)]))
        .on('brushstart', function() {
            timelineBrushStart(box);
        })
        .on("brush", function() {
            contextMenu
            .style("visibility", "hidden");
            timelineBrushed(box, yScale);
        });

        svg.append("g")
        .attr("class", "brush timlineBrush box-" + box)
        .on('mousemove', function() {
            updateHoverLine(d3.mouse(this));
        })
        .on("mousedown", function(){
            if(d3.event.button === 2){
                d3.event.stopImmediatePropagation();
            }
        })
        .call(timelineBrush);

        timelineBrushes.push(timelineBrush);
    }

    function addYAxisToTimelineBox(box, yScale) {
        var yAxis = d3.svg.axis()
        .scale(yScale)
        .orient("left");

        svg.append("g")
        .attr("class", "local y axis")
        .call(yAxis)
        .selectAll("text")
        .attr('class', 'text')
        .attr("dy", ".35em")
        .style("text-anchor", "end");
    }

    function highlightLine(patientID, that) {
        d3.selectAll('.line.patientID-' + patientID)
        .classed('hovered', true)
        .moveToFront();

        d3.selectAll('.line:not(.hovered)')
        .attr('opacity', 0.4);

        // if this method is called via mouse...
        if (that) {
            updateHoverLine(d3.mouse(that));
            tooltip
            .style("visibility", "visible")
            .html('PatientID: ' + patientID)
            .style("left", mouseX() + "px")
            .style("top", mouseY() + "px");
        }
    }

    function lowlightLine() {
        d3.selectAll('.line')
        .classed('hovered', false)
        .attr('opacity', 1);
        if (selectedPatientIDs.length > 0) {
            d3.selectAll('.line:not(.selected)')
            .attr('opacity', 0.4);
        }
        tooltip
        .style("visibility", "hidden");
    }

    function lineSelected(d) {
        var patientID = d[0].patientID;
        if (! d3.select('.line.patientID-' + patientID).classed('hovered')) { // fix for IE
            lineUnselected();
            highlightLine(patientID, this);
            computeAutoCorrelationLines(patientID, this);
        }
    }

    function lineUnselected() {
        lowlightLine();
        removeAutoCorrelationLines();
    }

    function removeAutoCorrelationLines() {
        computingIDs = [];
        d3.selectAll('.correlogram')
        .remove();
    }

    function drawCorrelogram(points, concept) {
        var conceptIdx = concepts.indexOf(concept);
        var extent = d3.extent(points, function(d) { return d.value; });
        var barScale = d3.scale.linear()
        .domain([1, -1])
        .range([timelineHeight * conceptIdx + padding(conceptIdx), timelineHeight * (conceptIdx + 1) + padding(conceptIdx)]);
        svg.append('line')
        .attr('class', 'axis correlogram' + ' concept-' + conceptIdx)
        .attr('x1', 0)
        .attr('y1', barScale(0))
        .attr('x2', timelineWidth)
        .attr('y2', barScale(0))
        .attr('stroke', function() {
            if (backgroundCheckChecked) {
                return 'black';
            } else {
                return 'white';
            }
        });
        svg.selectAll('.correlogramBars')
        .data(points)
        .enter()
        .append('rect')
        .attr('class', 'bar correlogram' + ' concept-' + conceptIdx)
        .attr('width', '1px')
        .attr('height', function(d) { return Math.abs( barScale(d.value) -  barScale(0)); })
        .attr('x', function(d) { return x(d.timepoint); })
        .attr('y', function(d) { return barScale(Math.max(0, d.value)); })
        .attr('stroke', function() {
            if (backgroundCheckChecked) {
                return 'black';
            } else {
                return 'white';
            }
        });
    }

    var showCorrelogram = true;
    function swapCorrelogramBoolean(checked) {
        showCorrelogram = checked;
    }

    var computingIDs = [];
    function computeAutoCorrelationLines(patientID, secondTry) {
        if (! showCorrelogram) {
            return;
        }
        computingIDs.push(patientID);
        setTimeout(function() {
            if (computingIDs[computingIDs.length - 1] !== patientID) {
                return;
            }
            var data = prepareFormData();
            data = addSettingsToData(data, { acfPatientID: patientID });
            data = addSettingsToData(data, { xAxisSortOrder: x.domain() });
            data = addSettingsToData(data, { interpolateNAs: interpolateNAs });
            jQuery.ajax({
                url: pageInfo.basePath + '/SmartR/updateOutputDIV',
                type: "POST",
                timeout: '600000',
                data: data
            }).done(function(serverAnswer) {
                serverAnswer = JSON.parse(serverAnswer);
                if (computingIDs[computingIDs.length - 1] !== patientID) {
                    return;
                }
                if (serverAnswer.error) {
                    alert(serverAnswer.error);
                    return;
                }
                var acfEstimates = serverAnswer.acfEstimates;
                for (var i = 0; i < concepts.length; i++) {
                    var concept = concepts[i];
                    var acfEstimate = acfEstimates[concept];
                    var points = [];
                    for (var j = 0, len = acfEstimate.estimate.length; j < len; j++) {
                        points.push({'value': acfEstimate.estimate[j], 'timepoint': acfEstimate.sortOrder[j]});
                    }
                    drawCorrelogram(points, concept);
                }
                if (computingIDs[computingIDs.length - 1] === patientID) {
                    computingIDs = [];
                }
            }).fail(function() {
                if (computingIDs[computingIDs.length - 1] === patientID) {
                    computingIDs = [];
                    if (! secondTry) {
                        computeAutoCorrelationLines(patientID, true);
                    }
                }
            });
        }, 500);
    }

    function getEqualityCheck(concept, timepoint, patientID, value) {
        return function(d) {
            var b1 = concept === undefined ? true : d.concept === concept;
            var b2 = timepoint === undefined ? true : d.timepoint === timepoint;
            var b3 = patientID === undefined ? true : d.patientID === patientID;
            var b4 = value === undefined ? true : d.value === value;
            return b1 && b2 && b3 && b4;
        };
    }

    function uniq_fast(a) {
        var seen = {};
        var out = [];
        for(var i = 0, j = 0, len = a.length; i < len; i++) {
             var item = a[i];
             if(seen[item] !== 1) {
                   seen[item] = 1;
                   out[j++] = item;
             }
        }
        return out;
    }

    var yScales = {};
    for (var i = 0; i < concepts.length; i++) {
        var concept = concepts[i];
        var conceptData = data.filter(getEqualityCheck(concept, undefined, undefined, undefined));
        var conceptPatientIDs = uniq_fast(conceptData.map(function(d) { return d.patientID; }));
        var extent = d3.extent(conceptData, function(d) { return d.value; });
        var yScale = d3.scale.linear()
        .domain(extent.reverse())
        .range([timelineHeight * i + padding(i), timelineHeight * (i + 1) + padding(i)]);
        yScales[concept] = yScale;
        y = yScale;
        addBrushToTimelineBox(i, yScale);
        addYAxisToTimelineBox(i, yScale);
        for (var j = 0; j < conceptPatientIDs.length; j++) {
            var patientID = conceptPatientIDs[j];
            var patientConceptData = data.filter(getEqualityCheck(concept, undefined, patientID, undefined));
            timeline = svg.selectAll('.timelines')
            .data([patientConceptData])
            .enter()
            .append('path')
            .attr('class', 'line concept-' + i + ' patientID-' + patientID)
            .attr('d', lineGen)
            .attr('stroke', colors(patientID))
            .on('mouseover', lineSelected)
            .on('mouseout', lineUnselected)
            .on('mousemove', function() {
                updateHoverLine(d3.mouse(this));
            });
        }
    }
    initBrushButtons();

    function removePointsWithProperties(arr, concept, timepoint, patientID, value) {
        var equalityCheck = getEqualityCheck(concept, timepoint, patientID, value);
        var newArray = [];
        for (var i = 0; i < arr.length; i++) {
            if (! equalityCheck(arr[i])) {
                newArray.push(arr[i]);
            }
        }
        return newArray;
    }

    function updateCohorts() {
        // we don't need to bother the database for the current analysis
        for (var i = 0; i < patientIDs.length; i++) {
            var patientID = patientIDs[i];
            var index = selectedPatientIDs.indexOf(patientID);
            if (index === -1) {
                d3.selectAll('.line.patientID-' + patientID).remove();
            }
        }
        var divs = [];
        var selectedData = data.filter(function(d) { return selectedPatientIDs.indexOf(d.patientID) >= 0; });
        for (i = 0; i < selectedPatientIDs.length; i++) {
            var selectedPatientID = selectedPatientIDs[i];
            // find the first suitable data point
            var point = selectedData.find(getEqualityCheck(undefined, undefined, selectedPatientID, undefined));
            var conceptPath = point.concept;
            var timepoint = point.timepoint;
            var value = point.value;
            var equalPoints = selectedData.filter(getEqualityCheck(conceptPath, timepoint, undefined, value));
            if (equalPoints.length > 1) {
                var oldLength = selectedData.length;
                selectedData = removePointsWithProperties(selectedData, undefined, timepoint, undefined, undefined);
                var newLength = selectedData.length;
                if (oldLength !== newLength) {
                    i--;
                    continue;
                }
            }

            var concept = conceptPath + timepoint + '\\';
            var div = createQueryCriteriaDIV(concept, 'ratio', 'numeric', 'EQ', value, '', 'ratio', 'Y', 'valueicon');
            divs.push(div);
        }
        setCohorts(divs, false, false, false, 1);
    }

    function removeDendrograms() {
        svg.selectAll(".dendrogram").remove();
    }

    function drawDendrogram(dendrogram, ids, fromHeight, toHeight) {
        var w = 280;
        var h = toHeight - fromHeight;
        var cluster = d3.layout.cluster()
        .size([h, w])
        .separation(function(a, b) {
            return 1;
        });

        var diagonal = d3.svg.diagonal()
        .projection(function (d) {
            return [width + margin.right - 20 - d.y, fromHeight + d.x];
        });

        var dendrogramNodes = cluster.nodes(dendrogram);
        var dendrogramLinks = cluster.links(dendrogramNodes);

        dendrogramLink = svg.selectAll(".dendrogramLink")
        .data(dendrogramLinks)
        .enter().append("path")
        .attr("class", "dendrogram link")
        .attr("d", diagonal);

        dendrogramNode = svg.selectAll(".dendrogramNode")
        .data(dendrogramNodes)
        .enter().append("circle")
        .attr("class", "dendrogram node")
        .attr('r', 4.5)
        .attr("transform", function (d) {
            return "translate(" + (width + margin.right - 20 - d.y) + "," + (fromHeight + d.x) + ")";
        })
        .on("mouseover", function(d) {
            var leafs = d.index.split(' ');
            for (var i = 0; i < leafs.length; i++) {
                var patientID = ids[leafs[i]];
                highlightLine(patientID);
            }

            tooltip
            .style("visibility", "visible")
            .html('Height: ' + d.height)
            .style("left", mouseX() + "px")
            .style("top", mouseY() + "px");
        })
        .on('mouseout', function() {
            lowlightLine();
            tooltip.style("visibility", "hidden");
        })
        .on('click', function(d) {
            var leafs = d.index.split(' ');
            var clickedIDs = [];
            for (var i = 0; i < leafs.length; i++) {
                var patientID = ids[leafs[i]];
                clickedIDs.push(patientID);
            }
            assignUniqueColor(clickedIDs);
        });
    }

    var interpolateNAs = 1;
    function swapInterpolateBoolean(checked) {
        if (! checked) {
            alert('Missing timepoints are no longer interpolated.\nThis can massively influence the analysis algorithms (i.e. clustering)\n\nDEACTIVATE ONLY IF YOU KNOW WHAT YOU DO!');
            interpolateNAs = 0;
        } else {
            interpolateNAs = 1;
        }
    }

    function cluster(dist, link) {
        removeDendrograms();
        var similarityMeasure = dist;
        var linkageMeasure = link;
        var data = prepareFormData();
        data = addSettingsToData(data, { similarityMeasure: similarityMeasure });
        data = addSettingsToData(data, { linkageMeasure: linkageMeasure });
        data = addSettingsToData(data, { interpolateNAs: interpolateNAs });
        data = addSettingsToData(data, { xAxisSortOrder: x.domain() });
        clusteringDropdown.select('.buttonText').text('Loading...');
        jQuery.ajax({
                url: pageInfo.basePath + '/SmartR/updateOutputDIV',
                type: "POST",
                timeout: '600000',
                data: data
            }).done(function(serverAnswer) {
                clusteringDropdown.select('.buttonText').text('Timeline Clustering');
                serverAnswer = JSON.parse(serverAnswer);
                if (serverAnswer.error) {
                    alert(serverAnswer.error);
                    return;
                }
                for (var i = 0; i < concepts.length; i++) {
                    var concept = concepts[i];
                    var dendrogram = JSON.parse(serverAnswer.clusterings[concept].dendrogram);
                    var fromHeight = timelineHeight * i + padding(i);
                    var toHeight = timelineHeight * (i + 1) + padding(i);
                    var ids = serverAnswer.clusterings[concept].patientIDs;
                    drawDendrogram(dendrogram, ids, fromHeight, toHeight);
                }
            }).fail(function() {
                clusteringDropdown.select('.buttonText').text('Timeline Clustering');
                alert('Clustering failed for unknown reasons!');
            });
    }

    var buttonWidth = 200;
    var buttonHeight = 40;
    var buffer = 5;

    var clusteringDropdown = createD3Dropdown({
        location: svg,
        label: 'Timeline Clustering',
        x: 2 - margin.left,
        y: 2 - margin.top,
        width: buttonWidth,
        height: buttonHeight,
        items: [
            {
                callback: function() { cluster('COR', 'average'); }, 
                label: 'Hierarch.-Corr.-Avg.'
            },
            {
                callback: function() { cluster('EUCL', 'average'); }, 
                label: 'Hierarch.-Eucl.-Avg.'
            },
            {
                callback: function() { cluster('ACF', 'average'); }, 
                label: 'Hierarch.-Autocorr.-Avg.'
            }
        ]
    });

    createD3Button({
        location: svg,
        label: 'Reset Colors',
        x: 2 - margin.left + buffer + buttonWidth,
        y: 2 - margin.top,
        width: buttonWidth,
        height: buttonHeight,
        callback: resetLineColors
    });

    createD3Switch({
        location: svg,
        onlabel: 'Interp. Miss. Data ON',
        offlabel: 'Interp. Miss. Data OFF',
        x: 2 - margin.left + buffer * 2 + buttonWidth * 2,
        y: 2 - margin.top,
        width: buttonWidth,
        height: buttonHeight,
        callback: swapInterpolateBoolean,
        checked: true
    });

    createD3Switch({
        location: svg,
        onlabel: 'Dark Background',
        offlabel: 'Light Background',
        x: 2 - margin.left + buffer * 3 + buttonWidth * 3,
        y: 2 - margin.top,
        width: buttonWidth,
        height: buttonHeight,
        callback: swapBackgroundColor,
        checked: true
    });

    createD3Switch({
        location: svg,
        onlabel: 'Smooth Data ON',
        offlabel: 'Smooth Data OFF',
        x: 2 - margin.left + buffer * 4 + buttonWidth * 4,
        y: 2 - margin.top,
        width: buttonWidth,
        height: buttonHeight,
        callback: smoothData,
        checked: false
    });

    createD3Switch({
        location: svg,
        onlabel: 'SHOW Correlogram',
        offlabel: 'HIDE Correlogram',
        x: 2 - margin.left + buffer * 5 + buttonWidth * 5,
        y: 2 - margin.top,
        width: buttonWidth,
        height: buttonHeight,
        callback: swapCorrelogramBoolean,
        checked: true
    });
</script>