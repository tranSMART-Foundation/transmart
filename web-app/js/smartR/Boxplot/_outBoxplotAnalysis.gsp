<!DOCTYPE html>
<style>
	.text {
	    font-family: 'Roboto', sans-serif;
        fill: black;
	}

    .boxplotValue {
        font-size: 9px;
    }

    #boxplot1 {
        background-color: white;
    }

    #boxplot2 {
        background-color: white;
    }

	.whisker {
		stroke: black;
		stroke-width: 1px;
	}

	.hinge {
		stroke: black;
		stroke-width: 1px;
	}

	.connection {
		stroke: black;
		stroke-width: 1px;
	}

	.point {
	}

	.point:hover {
		fill: red;
	}

    .outlier {
        stroke: red;
        stroke-width: 2px;
    }

    .brushed {
        fill: red;
        stroke: red;
    }

	.box {
		fill: #008BFF;
	}

    .box.upper {
        opacity: 0.3;
    }

    .box.lower {
        opacity: 0.2;
    }

    .line {
      fill: none;
      stroke: black;
      stroke-width: 1.5px;
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

    .brush .extent {
        fill: blue;
        opacity: .25;
        shape-rendering: crispEdges;

    }

    .axis path, .axis line {
        fill: none;
        stroke: black;
        stroke-width: 1px;
        shape-rendering: crispEdges;
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
<g:javascript src="resource/d3.min.js"/>

<div id="visualization">
    <div id="controls" style='float: left; padding-right: 10px'></div>
    <div id="boxplot1" style='float: left; padding-right: 10px'></div>
    <div id="boxplot2" style='float: left; padding-right: 10px; border-left: 1px solid black'></div>
</div>

<script>
    var animationDuration = 1000;
    var tmpAnimationDuration = animationDuration;
    function switchAnimation(checked) {
        if (! checked) {
            tmpAnimationDuration = animationDuration;
            animationDuration = 0;
        } else {
            animationDuration = tmpAnimationDuration;
        }
    }

	var results = ${raw(results)};

    results.cohort2 = results.cohort2 === undefined ? {concept: 'undefined', subsets: []} : results.cohort2;

	var margin = {top: 10, right: 60, bottom: 200, left: 60};
    var width = jQuery("#etrikspanel").width() / 2 - 200 - margin.left - margin.right;
    var height = jQuery("#etrikspanel").height() * 0.65 - margin.top - margin.bottom;

    var controls = d3.select('#controls').append('svg')
    .attr('width', '220px')
    .attr('height', height * 2);

    var boxplot1 = d3.select("#boxplot1").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var boxplot2 = d3.select("#boxplot2").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	var x1 = d3.scale.ordinal()
    .domain(results.cohort1.subsets)
    .rangeBands([0, width], 1, 0.5);

    var x2 = d3.scale.ordinal()
    .domain(results.cohort2.subsets)
    .rangeBands([0, width], 1, 0.5);

    var y1 = d3.scale.linear()
    .domain([results.cohort1.globalMin, results.cohort1.globalMax])
    .range([height, 0]);

    var y2 = d3.scale.linear()
    .domain([results.cohort2.globalMin, results.cohort2.globalMax])
    .range([height, 0]);

    var colorScale = d3.scale.quantile()
    .range(['rgb(158,1,66)','rgb(213,62,79)','rgb(244,109,67)','rgb(253,174,97)','rgb(254,224,139)','rgb(255,255,191)','rgb(230,245,152)','rgb(171,221,164)','rgb(102,194,165)','rgb(50,136,189)','rgb(94,79,162)']);

    // var xAxis1 = d3.svg.axis()
    // .scale(x1)
    // .orient("bottom");

    // var xAxis2 = d3.svg.axis()
    // .scale(x2)
    // .orient("bottom");

    var yAxis1 = d3.svg.axis()
    .scale(y1)
    .orient("left");

    var yAxis2 = d3.svg.axis()
    .scale(y2)
    .orient("right");

    // boxplot1.append("g")
    // .attr("class", "y axis text")
    // .attr("transform", "translate(" + 0 + "," + height + ")")
    // .call(xAxis1)
    // .selectAll("text")
    // .attr('class', 'text')
    // .attr("y", 10)
    // .attr("x", 5)
    // .attr("dy", ".35em")
    // .attr("transform", "rotate(45)");

    // boxplot2.append("g")
    // .attr("class", "y axis text")
    // .attr("transform", "translate(" + 0 + "," + height + ")")
    // .call(xAxis2);

    boxplot1.append("g")
    .attr("class", "y axis text")
    .attr("transform", "translate(" + 0 + "," + 0 + ")")
    .call(yAxis1);

    boxplot2.append("g")
    .attr("class", "y axis text")
    .attr("transform", "translate(" + width + "," + 0 + ")")
    .call(yAxis2);

    var yAxisLabel1 = boxplot1.append("text")
    .attr("class", "text")
    .attr("transform", "translate(" + (-40) + "," + (height / 2) + ")rotate(-90)")
    .attr('text-anchor', 'middle')
    .text(shortenConcept(results.cohort1.concept[0]));

    var yAxisLabel2 = boxplot2.append("text")
    .attr("class", "text")
    .attr("transform", "translate(" + (width + 40) + "," + (height / 2) + ")rotate(90)")
    .attr('text-anchor', 'middle')
    .text(shortenConcept(results.cohort2.concept[0]));

    var tooltip = d3.select("#visualization").append("div")
    .attr("class", "tooltip text")
    .style("visibility", "hidden");

    var brush1 = d3.svg.brush()
	.x(d3.scale.identity().domain([0, width]))
	.y(d3.scale.identity().domain([-5, height + 5]))
	.on("brushend", function() {
	    contextMenu
	    .style("visibility", "hidden");
	    updateSelection('cohort1');
	});

    var brush2 = d3.svg.brush()
    .x(d3.scale.identity().domain([0, width]))
    .y(d3.scale.identity().domain([-5, height + 5]))
    .on("brushend", function() {
        contextMenu
        .style("visibility", "hidden");
        updateSelection('cohort2');
    });

    boxplot1.append("g")
    .attr('id', 'brush1')
    .attr("class", "brush")
    .on("mousedown", function(){
        if(d3.event.button === 2){
            d3.event.stopImmediatePropagation();
        }
    })
    .call(brush1);

    boxplot2.append("g")
    .attr('id', 'brush2')
    .attr("class", "brush")
    .on("mousedown", function(){
        if(d3.event.button === 2){
            d3.event.stopImmediatePropagation();
        }
    })
    .call(brush2);

    var contextMenu = d3.select("#visualization").append("div")
    .attr("class", "contextMenu text")
    .style("visibility", "hidden")
    .html("<input id='excludeButton' class='mybutton text' type='button' value='Exclude' onclick='excludeSelection()'/><input id='resetButton' class='mybutton text' type='button' value='Reset' onclick='reset()'/>")
    .on('click', function() {
        d3.select(this)
        .style('visibility', 'hidden')
        removeBrushes();
    });

    d3.select('#visualization')
    .on("contextmenu", function() {
        d3.event.preventDefault();
        contextMenu
        .style("visibility", "visible")
        .style("left", mouseX() + "px")
        .style("top", mouseY() + "px");
    });

    var currentSelection = [];
	function updateSelection(cohort) {
        var y;
        var brush;
        if (cohort === 'cohort1') {
            y = y1;
            brush = brush1;
        } else {
            y = y2;
            brush = brush2;
        }
        currentSelection = [];
        d3.selectAll('.point')
        .classed('brushed', false);

		var extent = brush.extent();
        var left = extent[0][0],
            top = extent[0][1],
            right = extent[1][0],
            bottom = extent[1][1];

        d3.selectAll('.point.' + cohort).each(function(d) {
            var point = d3.select(this);
	        if (y(d.value) >= top && y(d.value) <= bottom && point.attr('cx') >= left && point.attr('cx') <= right) {
                d3.selectAll('.point.patientID-' + d.patientID)
                .classed('brushed', true);
	        	currentSelection.push(d.patientID);
	        }
	    });
	}

    var excludedPatientIDs = [];
    function excludeSelection() {
        excludedPatientIDs = excludedPatientIDs.concat(currentSelection);
        var settings = { excludedPatientIDs: excludedPatientIDs };
        var onResponse = function(response) {
            results = response;
            init();
        };
        startWorkflow(onResponse, settings, false, false);
    }

    function removeOutliers() {
        currentSelection = [];
        d3.selectAll('.outlier').each(function(d) {
            currentSelection.push(d.patientID);
        });
        if (currentSelection.length !== 0) {
            excludeSelection();
        }
    }

    function kernelDensityEstimator(kernel, x) {
        return function(sample) {
            return x.map(function(x) {
                return [x, d3.mean(sample, function(v) { return kernel(x - v); })];
            });
        };
    }

    function epanechnikovKernel(scale) {
        return function(u) {
            return Math.abs(u /= scale) <= 1 ? 0.75 * (1 - u * u) / scale : 0;
        };
    }

    function gaussKernel(scale) {
        return function(u) {
            return Math.exp(- u * u / 2) / Math.sqrt(2 * Math.PI) / scale;
        };
    }

    function swapKDE(checked) {
        if (! checked) {
            d3.selectAll('.line')
            .attr('visibility', 'hidden');
        } else {
            d3.selectAll('.line')
            .attr('visibility', 'visible');
        }
    }

    function shortenNodeLabel(label) {
        label = label.replace(/\W+/g, '');
        return label;
    }

    var jitterWidth = 1.0;
    var jitterChecked = false;
    function swapJitter(checked) {
        if (! checked) {
            for (var i = 0; i < results.cohort1.subsets.length; i++) {
                d3.selectAll('.point.cohort1.' + shortenNodeLabel(results.cohort1.subsets[i]))
                .transition()
                .duration(animationDuration)
                .attr("cx", x1(results.cohort1.subsets[i]));
            }
            for (i = 0; i < results.cohort2.subsets.length; i++) {
                d3.selectAll('.point.cohort2.' + shortenNodeLabel(results.cohort2.subsets[i]))
                .transition()
                .duration(animationDuration)
                .attr("cx", x2(results.cohort2.subsets[i]));
            }
            jitterChecked = false;
        } else {
            for (var j = 0; j < results.cohort1.subsets.length; j++) {
                d3.selectAll('.point.cohort1.' + shortenNodeLabel(results.cohort1.subsets[j]))
                .transition()
                .duration(animationDuration)
                .attr("cx", function(d) { return x1(results.cohort1.subsets[j]) + boxplotWidth * jitterWidth * d.jitter; });
            }
            for (j = 0; j < results.cohort2.subsets.length; j++) {
                d3.selectAll('.point.cohort2.' + shortenNodeLabel(results.cohort2.subsets[j]))
                .transition()
                .duration(animationDuration)
                .attr("cx", function(d) { return x2(results.cohort2.subsets[j]) + boxplotWidth * jitterWidth * d.jitter; });
            }
            jitterChecked = true;
        }
    }

    function swapBackgroundColor(checked) {
        if (! checked) {
            d3.selectAll('#boxplot1,#boxplot2')
            .transition()
            .duration(animationDuration)
            .style('background-color', 'black');
            d3.selectAll('.whisker, .hinge, .connection, .line, .axis path, .axis line')
            .transition()
            .duration(animationDuration)
            .style('stroke', 'white');
            d3.selectAll('.text')
            .transition()
            .duration(animationDuration)
            .style('fill', 'white');
            d3.selectAll('.box')
            .transition()
            .duration(animationDuration)
            .style('fill', 'white');
            d3.selectAll('.brush, .extent')
            .transition()
            .duration(animationDuration)
            .style('fill', 'white');
        } else {
            d3.selectAll('#boxplot1,#boxplot2')
            .transition()
            .duration(animationDuration)
            .style('background-color', 'white');
            d3.selectAll('.whisker, .hinge, .connection, .line, .axis path, .axis line')
            .transition()
            .duration(animationDuration)
            .style('stroke', 'black');
            d3.selectAll('.text')
            .transition()
            .duration(animationDuration)
            .style('fill', 'black');
            d3.selectAll('.box')
            .transition()
            .duration(animationDuration)
            .style('fill', '#008BFF');
            d3.selectAll('.brush, .extent')
            .transition()
            .duration(animationDuration)
            .style('fill', 'blue');
        }

    }

    var boxplots = {cohort1: {}, cohort2: {}};
    for (var i = 0; i < results.cohort1.subsets.length; i++) {
        var subset = results.cohort1.subsets[i];
        boxplots.cohort1[subset] = boxplot1.append('g');
    }
    for (var j = 0; j < results.cohort2.subsets.length; j++) {
        var subset = results.cohort2.subsets[j];
        boxplots.cohort2[subset] = boxplot2.append('g');
    }

    var boxplotWidth = 0.12 * width;
    function createBoxplot(params, cohort, subset, x, y) {
        var shortenedSubset = shortenNodeLabel(subset);
        colorScale.domain(d3.extent(y.domain()));

    	var whiskerLength = boxplotWidth / 6;
    	var whisker = boxplots[cohort][subset].selectAll('.whisker')
    	.data([params.upperWhisker, params.lowerWhisker], function(d, i) { return cohort + '-' + shortenedSubset + '-whisker-' + i; });

    	whisker.enter()
    	.append('line')
    	.attr('class', 'whisker');

        whisker
        .transition()
        .duration(animationDuration)
    	.attr('x1', x(subset) - whiskerLength / 2)
    	.attr('y1', function(d) { return y(d); })
    	.attr('x2', x(subset) + whiskerLength / 2)
    	.attr('y2', function(d) { return y(d); });

        var whiskerLabel = boxplots[cohort][subset].selectAll('.whiskerLabel')
        .data([params.upperWhisker, params.lowerWhisker], function(d, i) { return cohort + '-' + shortenedSubset + '-whiskerLabel-' + i; });

        whiskerLabel
        .enter()
        .append('text')
        .attr('class', 'text whiskerLabel boxplotValue');

        whiskerLabel
        .transition()
        .duration(animationDuration)
        .attr('x', x(subset) + whiskerLength / 2)
        .attr('y', function(d) { return y(d); })
        .attr("dx", ".35em")
        .attr("dy", ".35em")
        .attr('text-anchor', 'start')
        .text(function(d) { return d; });

    	var hingeLength = boxplotWidth;
    	var hinge = boxplots[cohort][subset].selectAll('.hinge')
    	.data([params.upperHinge, params.lowerHinge], function(d, i) { return cohort + '-' + shortenedSubset + '-hinge-' + i; });

    	hinge.enter()
    	.append('line')
    	.attr('class', 'hinge');

        hinge
        .transition()
        .duration(animationDuration)
    	.attr('x1', x(subset) - hingeLength / 2)
    	.attr('y1', function(d) { return y(d); })
    	.attr('x2', x(subset) + hingeLength / 2)
    	.attr('y2', function(d) { return y(d); });

        var hingeLabel = boxplots[cohort][subset].selectAll('.hingeLabel')
        .data([params.upperHinge, params.lowerHinge], function(d, i) { return cohort + '-' + shortenedSubset + '-hingeLabel-' + i; });

        hingeLabel.enter()
        .append('text')
        .attr('class', 'text hingeLabel boxplotValue');

        hingeLabel
        .transition()
        .duration(animationDuration)
        .attr('x', x(subset) - hingeLength / 2)
        .attr('y', function(d) { return y(d); })
        .attr("dx", "-.35em")
        .attr("dy", ".35em")
        .attr('text-anchor', 'end')
        .text(function(d) { return d; });

    	var connection = boxplots[cohort][subset].selectAll('.connection')
        .data([[params.upperWhisker, params.upperHinge],
            [params.lowerWhisker, params.lowerHinge]],
            function(d, i) { return cohort + '-' + shortenedSubset + '-connection-' + i; });

        connection.enter()
        .append('line')
    	.attr('class', 'connection');

        connection
        .transition()
        .duration(animationDuration)
    	.attr('x1', x(subset))
    	.attr('y1', function(d) { return y(d[0]); })
    	.attr('x2', x(subset))
    	.attr('y2', function(d) { return y(d[1]); });

    	var upperBox = boxplots[cohort][subset].selectAll('.box.upper')
        .data(params.upperHinge, function(d) { return cohort + '-' + shortenedSubset + '-upperBox'; });

        upperBox.enter()
        .append('rect')
    	.attr('class', 'box upper');

        upperBox
        .transition()
        .duration(animationDuration)
    	.attr('x', x(subset) - hingeLength / 2)
    	.attr('y', y(params.upperHinge))
    	.attr('height', Math.abs(y(params.upperHinge) - y(params.median)))
    	.attr('width', hingeLength);

    	var lowerBox = boxplots[cohort][subset].selectAll('.box.lower')
        .data(params.lowerHinge, function(d) { return cohort + '-' + shortenedSubset + '-lowerBox'; });

        lowerBox.enter()
        .append('rect')
        .attr('class', 'box lower');

        lowerBox
        .transition()
        .duration(animationDuration)
    	.attr('x', x(subset) - hingeLength / 2)
    	.attr('y', y(params.median))
    	.attr('height', Math.abs(y(params.median) - y(params.lowerHinge)))
    	.attr('width', hingeLength);

        var medianLabel = boxplots[cohort][subset].selectAll('.medianLabel')
        .data(params.median, function(d) { return cohort + '-' + shortenedSubset + '-medianLabel'; });

        medianLabel.enter()
        .append('text')
        .attr('class', 'text medianLabel boxplotValue');

        medianLabel
        .transition()
        .duration(animationDuration)
        .attr('x', x(subset) + hingeLength / 2)
        .attr('y', function(d) { return y(params.median); })
        .attr("dx", ".35em")
        .attr("dy", ".35em")
        .attr('text-anchor', 'start')
        .text(params.median);

    	var point = boxplots[cohort][subset].selectAll('.point')
        .data(params.points, function(d) { return cohort + '-' + shortenedSubset + '-' + d.patientID; });

        point
        .enter()
        .append('circle')
        .attr("cx", function(d) { return jitterChecked ? x(subset) + boxplotWidth * jitterWidth * d.jitter : x(subset); })
        .attr("r", 0)
        .attr('fill', function(d) { return colorScale(d.value); })
        .on("mouseover", function(d) {
            tooltip
            .style("visibility", "visible")
            .html('Value: ' + d.value + '</br>' +
                'PatientID: ' + d.patientID + '</br>' +
                'Outlier: ' + d.outlier)
            .style("left", mouseX() + "px")
            .style("top", mouseY() + "px");
        })
        .on("mouseout", function(d) {
            tooltip.style("visibility", "hidden");
        });

        point
        .attr('class', function (d) {
            return 'point patientID-' + d.patientID + (d.outlier ? ' outlier ' : ' ') + cohort + ' ' + shortenedSubset;
        }) // This is here and not in the .enter() because points might become outlier on removal of other points
        .transition()
        .duration(animationDuration)
        .attr("cy", function(d) { return y(d.value); })
        .attr("r", 3);

        point
        .exit()
        .transition()
        .duration(animationDuration)
        .attr("r", 0)
        .remove();

        yCopy = y.copy();
        yCopy.domain([params.lowerWhisker, params.upperWhisker]);
        var kde = kernelDensityEstimator(epanechnikovKernel(6), yCopy.ticks(100));
        var values = params.points.map(function(d) { return d.value; });
        var estFun = kde(values);

        var kdeDomain = d3.extent(estFun, function(d) { return d[1]; });

        var kdeScale = d3.scale.linear()
        .domain(kdeDomain)
        .range([0, boxplotWidth / 2]);

        var lineGen = d3.svg.line()
        .x(function(d) { return x(subset) - kdeScale(d[1]); })
        .y(function(d) { return y(d[0]); });

        var line = boxplots[cohort][subset].selectAll('.line')
        .data([cohort], function(d) { return cohort; });

        line.enter()
        .append("path")
        .attr("class", "line")
        .attr('visibility', 'hidden');

        line
        .datum(estFun)
        .transition()
        .duration(animationDuration)
        .attr("d", lineGen);

        var label = boxplots[cohort][subset].selectAll('.label')
        .data([cohort], function(d) { return cohort + '-label'; });

        label.enter()
        .append('text')
        .attr('class', 'text label');

        label
        .attr("transform", "translate(" + (x(subset)) + "," + (height + 20) + ")rotate(45)")
        .attr('text-anchor', 'start')
        .text(subset);
    }

    d3.selection.prototype.moveToFront = function() {
        return this.each(function(){
            this.parentNode.appendChild(this);
        });
    };

    function removeBrushes() {
        d3.select('#brush1')
        .call(brush1.clear());
        d3.select('#brush2')
        .call(brush2.clear());
    }

    function init() {
        if (results.cohort1 !== undefined) {
            for (var i = 0; i < results.cohort1.subsets.length; i++) {
                var subset1 = results.cohort1.subsets[i];
                createBoxplot(results.cohort1[subset1], 'cohort1', subset1, x1, y1);
            }
        }
        if (results.cohort2 !== undefined) {
            for (var j = 0; j < results.cohort2.subsets.length; j++) {
                var subset2 = results.cohort2.subsets[j];
                createBoxplot(results.cohort2[subset2], 'cohort2', subset2, x2, y2);
            }
        }

        d3.selectAll('.text, .line, .point').moveToFront();
    }

    function reset() {
        removeBrushes();
        excludedPatientIDs = [];
        currentSelection = [];
        excludeSelection(); // Abusing the method because I can
    }

    init();

    var buttonWidth = 200;
    var buttonHeight = 40;
    var padding = 5;

    createD3Button({
        location: controls,
        label: 'Remove Outliers',
        x: 2,
        y: 2,
        width: buttonWidth,
        height: buttonHeight,
        callback: removeOutliers
    });

    createD3Button({
        location: controls,
        label: 'Reset',
        x: 2,
        y: 2 + padding + buttonHeight,
        width: buttonWidth,
        height: buttonHeight,
        callback: reset
    });

    createD3Switch({
        location: controls,
        onlabel: 'Light Background ON',
        offlabel: 'Light Background OFF',
        x: 2,
        y: 2 + padding * 2 + buttonHeight * 2,
        width: buttonWidth,
        height: buttonHeight,
        callback: swapBackgroundColor,
        checked: true
    });

    createD3Switch({
        location: controls,
        onlabel: 'Density Estimation ON',
        offlabel: 'Density Estimation OFF',
        x: 2,
        y: 2 + padding * 3 + buttonHeight * 3,
        width: buttonWidth,
        height: buttonHeight,
        callback: swapKDE,
        checked: false
    });

    createD3Switch({
        location: controls,
        onlabel: 'Jitter Datapoints ON',
        offlabel: 'Jitter Datapoints OFF',
        x: 2,
        y: 2 + padding * 4 + buttonHeight * 4,
        width: buttonWidth,
        height: buttonHeight,
        callback: swapJitter,
        checked: false
    });

    createD3Switch({
        location: controls,
        onlabel: 'Animation ON',
        offlabel: 'Animation OFF',
        x: 2,
        y: 2 + padding * 5 + buttonHeight * 5,
        width: buttonWidth,
        height: buttonHeight,
        callback: switchAnimation,
        checked: true
    });

</script>
