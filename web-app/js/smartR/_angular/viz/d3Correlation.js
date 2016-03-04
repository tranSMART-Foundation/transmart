//# sourceURL=d3Correlation.js

window.smartRApp.directive('correlationPlot', ['smartRUtils', 'rServeService', function(smartRUtils, rServeService) {

    return {
        restrict: 'E',
        scope: {
            data: '=',
            width: '@',
            height: '@'
        },
        link: function (scope, element) {

            /**
             * Watch data model (which is only changed by ajax calls when we want to (re)draw everything)
             */
            scope.$watch('data', function() {
                $(element[0]).empty();
                if (! $.isEmptyObject(scope.data)) {
                    createCorrelationViz(scope, element[0]);
                }
            });
        }
    };

    function createCorrelationViz(scope, root) {
        var animationDuration = 500;
        var bins = 10;
        var w = scope.width;
        var h = scope.height;
        var margin = {top: 20, right: 20, bottom: h / 4, left: w / 4};
        var width = w * 3 / 4 - margin.left - margin.right;
        var height = h * 3 / 4 - margin.top - margin.bottom;
        var bottomHistHeight = margin.bottom;
        var leftHistHeight = margin.left;
        var colors = ['#33FF33', '#3399FF', '#CC9900', '#CC99FF', '#FFFF00', 'blue'];
        var x = d3.scale.linear()
            .domain(d3.extent(scope.data.points, function(d) { return d.x; }))
            .range([0, width]);
        var y = d3.scale.linear()
            .domain(d3.extent(scope.data.points, function(d) { return d.y; }))
            .range([height, 0]);

        var annotations = scope.data.annotations.sort();
        var xArrLabel = scope.data.xArrLabel[0];
        var yArrLabel = scope.data.yArrLabel[0];

        var correlation,
            pvalue,
            regLineSlope,
            regLineYIntercept,
            patientIDs,
            points,
            method,
            minX,
            maxX,
            minY,
            maxY;
        function setData(data) {
            correlation = data.correlation[0];
            pvalue = data.pvalue[0];
            regLineSlope = data.regLineSlope[0];
            regLineYIntercept = data.regLineYIntercept[0];
            method = data.method[0];
            patientIDs = data.patientIDs;
            points = data.points;
            minX = data.points.min(function(d) { return d.x });
            maxX = data.points.max(function(d) { return d.x });
            minY = data.points.min(function(d) { return d.y });
            maxY = data.points.max(function(d) { return d.y });
        }

        setData(scope.data);

        function updateStatistics(patientIDs, scatterUpdate, init) {
            scatterUpdate = scatterUpdate === undefined ? false : scatterUpdate;
            init = init === undefined ? false : init;
            var arguments = { selectedPatientIDs: patientIDs };

            rServeService.startScriptExecution({
                taskType: 'run',
                arguments: arguments
            }).then(
                function (response) {
                    var results = JSON.parse(response.result.artifacts.value);
                    if (init) {
                        scope.data = results;
                    } else {
                        console.log(results);
                        setData(results);
                        if (scatterUpdate) updateScatterplot();
                        updateRegressionLine();
                        updateLegend();
                        updateHistogram();
                    }
                },
                function (response) {
                    alert('  Failure: ' + response.statusText);
                }
            );
        }

        var svg = d3.select(root).append('svg')
            .attr('width', width + margin.left + margin.right)
            .attr('height', height + margin.top + margin.bottom)
            .append('g')
            .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
            .on('contextmenu', function() {
                d3.event.preventDefault();
                contextMenu
                    .style('visibility', 'visible')
                    .style('left', smartRUtils.mouseX(root) + 'px')
                    .style('top', smartRUtils.mouseY(root) + 'px')
            });

        var tooltip = d3.select(root).append('div')
            .attr('class', 'tooltip')
            .style('visibility', 'hidden');

        function dragmove() {
            legend
                .style('left', smartRUtils.mouseX(root) + 'px')
                .style('top', smartRUtils.mouseY(root) + 'px');
        }

        var drag = d3.behavior.drag()
            .on('drag', dragmove);

        var legend = d3.select(root).append('div')
            .attr('class', 'legend')
            .style('left', 0)
            .style('top', $('#scatterplot').offsetTop + 'px')
            .call(drag);

        svg.append('g')
            .attr('class', 'x axis')
            .attr('transform', 'translate(0, 0)')
            .call(d3.svg.axis()
                .scale(x)
                .ticks(10)
                .tickFormat('')
                .innerTickSize(height)
                .orient('bottom'));

        svg.append('text')
            .attr('class', 'axisLabels')
            .attr('transform', 'translate(' + width / 2 + ',' + - margin.top / 2 + ')')
            .text(smartRUtils.shortenConcept(xArrLabel));

        svg.append('g')
            .attr('class', 'y axis')
            .attr('transform', 'translate(' + width + ',' + 0 + ')')
            .call(d3.svg.axis()
                .scale(y)
                .ticks(10)
                .tickFormat('')
                .innerTickSize(width)
                .orient('left'));

        svg.append('text')
            .attr('class', 'axisLabels')
            .attr('transform', 'translate('  + (width + margin.right / 2) + ',' + height / 2 + ')rotate(90)')
            .text(smartRUtils.shortenConcept(yArrLabel));

        svg.append('g')
            .attr('class', 'x axis')
            .attr('transform', 'translate(' + 0 + ',' + height + ')')
            .call(d3.svg.axis()
                .scale(x)
                .orient('top'));

        svg.append('g')
            .attr('class', 'y axis')
            .attr('transform', 'translate(' + 0 + ',' + 0 + ')')
            .call(d3.svg.axis()
                .scale(y)
                .orient('right'));

        function excludeSelection() {
            var remainingPatientIDs = d3.selectAll('.point:not(.selected)').map(function(d) { return d.patientID; });
            updateStatistics(remainingPatientIDs, true);
        }

        function zoomSelection() {
            if (d3.selectAll('.point.selected').size() < 2) {
                alert('Please select at least two elements before zooming!');
                return;
            }
            var selectedPatientIDs = d3.selectAll('.point.selected').map(function(d) { return d.patientID; });
            updateStatistics(selectedPatientIDs, false, true);
        }

        var ctxHtml = '<input id="zoomButton" class="mybutton" type="button" value="Zoom"/><br/> \
            <input id="excludeButton" class="mybutton" type="button" value="Exclude"/><br/> \
            <input id="resetButton" class="mybutton" type="button" value="Reset"/>';

        var contextMenu = d3.select(root).append('div')
            .attr('class', 'contextMenu')
            .style('visibility', 'hidden')
            .html(ctxHtml);
        $('#zoomButton').on('click', function() {
            contextMenu.style('visibility', 'hidden');
            zoomSelection();
        });
        $('#excludeButton').on('click', function() {
            contextMenu.style('visibility', 'hidden');
            excludeSelection();
        });
        $('#resetButton').on('click', function() {
            contextMenu.style('visibility', 'hidden');
            reset();
        });

        function updateSelection() {
            var extent = brush.extent();
            var x0 = x.invert(extent[0][0]);
            var x1 = x.invert(extent[1][0]);
            var y0 = y.invert(extent[0][1]);
            var y1 = y.invert(extent[1][1]);
            svg.selectAll('.point')
                .classed('selected', false)
                .style('fill', function(d) { return getColor(d.annotation); })
                .style('stroke', 'white')
                .filter(function(d) {
                    return x0 <= d.x && d.x <= x1 && y1 <= d.y && d.y <= y0;
                })
                .classed('selected', true)
                .style('fill', 'white')
                .style('stroke', function(d) { return getColor(d.annotation); });
        }

        var brush = d3.svg.brush()
            .x(d3.scale.identity().domain([0, width]))
            .y(d3.scale.identity().domain([0, height]))
            .on('brushend', function() {
                contextMenu
                    .style('visibility', 'hidden')
                    .style('top', -100 + 'px');
                updateSelection();
                var selectedPatientIDs = d3.selectAll('.point.selected').map(function(d) { return d.patientID; });
                updateStatistics(selectedPatientIDs);
            });

        svg.append('g')
            .attr('class', 'brush')
            .on('mousedown', function() {
                return d3.event.button === 2 ? d3.event.stopImmediatePropagation() : null;
            })
            .call(brush);

        function getColor(annotation) {
            console.log('---')
            console.log(annotation)
            console.log(annotations)
            console.log(annotations.indexOf(annotation))
            return annotation ? colors[annotations.indexOf(annotation)] : 'black';
        }

        function updateScatterplot() {
            var point = svg.selectAll('.point')
                .data(points, function(d) { return d.patientID; });

            point.enter()
                .append('circle')
                .attr('class', 'point')
                .attr('cx', function(d) { return x(d.x); })
                .attr('cy', function(d) { return y(d.y); })
                .attr('r', 5)
                .style('fill', function(d) { return getColor(d.annotation); })
                .on('mouseover', function(d) {
                    d3.select(this).style('fill', '#FF0000');
                    tooltip
                        .style('left', 10 + smartRUtils.mouseX(root) + 'px')
                        .style('top', 10 + smartRUtils.mouseY(root) + 'px')
                        .style('visibility', 'visible')
                        .html(smartRUtils.shortenConcept(xArrLabel) + ': ' + d.x + '<br/>' +
                            smartRUtils.shortenConcept(yArrLabel) + ': ' + d.y + '<br/>' +
                            'Patient ID: ' + d.patientID + '<br/>' +
                            (d.annotation ? 'Tag: ' + d.annotation : ''));
                })
                .on('mouseout', function() {
                    var p = d3.select(this);
                    p.style('fill', function(d) { return p.classed('selected') ? '#FFFFFF' : getColor(d.annotation) });
                    tooltip.style('visibility', 'hidden');
                });

            point.exit()
                .classed('selected', false)
                .transition()
                .duration(animationDuration)
                .attr('r', 0)
                .remove();
        }

        function updateHistogram() {
            var bottomHistData = d3.layout.histogram()
                .bins(bins)(points.map(function(d) { return d.x; }));
            var leftHistData = d3.layout.histogram()
                .bins(bins)(points.map(function(d) { return d.y; }));

            var bottomHistHeightScale = d3.scale.linear()
                .domain([0, bottomHistData.max(function(d) { return d.y; })])
                .range([1, bottomHistHeight]);
            var leftHistHeightScale = d3.scale.linear()
                .domain([0, leftHistData.max(function(d) { return d.y; })])
                .range([2, leftHistHeight]);

            var bottomHistGroup = svg.selectAll('.bar.bottom')
                .data(Array(bins).fill().map(function(_, i) { return i; }));
            var bottomHistGroupEnter = bottomHistGroup.enter()
                .append('g')
                .attr('class', 'bar bottom');
            var bottomHistGroupExit = bottomHistGroup.exit();

            bottomHistGroupEnter.append('rect')
                .attr('y', height + 1);
            bottomHistGroup.selectAll('rect')
                .transition()
                .delay(function(d) { return d * 25; })
                .duration(animationDuration)
                .attr('x', function(d) { return x(bottomHistData[d].x); })
                .attr('width', function() { return (x(maxX) - x(minX)) / bins; })
                .attr('height', function(d) { return bottomHistHeightScale(bottomHistData[d].y) - 1; });
            bottomHistGroupExit.selectAll('rect')
                .transition()
                .duration(animationDuration)
                .attr('height', 0);

            bottomHistGroupEnter.append('text')
                .attr('dy', '.35em')
                .attr('text-anchor', 'middle');
            bottomHistGroup.selectAll('text')
                .text(function(d) { return bottomHistData[d].y || ''; })
                .transition()
                .delay(function(d) { return d * 25; })
                .duration(animationDuration)
                .attr('x', function(d) { return x(bottomHistData[d].x) + (x(maxX) - x(minX)) / bins / 2; })
                .attr('y', function(d) { return height + bottomHistHeightScale(bottomHistData[d].y) - 10; });
            bottomHistGroupExit.selectAll('text')
                .text('');

            var leftHistGroup = svg.selectAll('.bar.left')
                .data(Array(bins).fill().map(function(_, i) { return i; }));
            var leftHistGroupEnter = leftHistGroup.enter()
                .append('g')
                .attr('class', 'bar left');
            var leftHistGroupExit = leftHistGroup.exit();

            leftHistGroupEnter.append('rect');
            leftHistGroup.selectAll('rect')
                .transition()
                .delay(function(d) { return d * 25; })
                .duration(animationDuration)
                .attr('x', function(d) { return - leftHistHeightScale(leftHistData[d].y) + 1; })
                .attr('y', function(d) { return y(leftHistData[d].x) - (y(minY) - y(maxY))/ bins; })
                .attr('width', function(d) { return leftHistHeightScale(leftHistData[d].y) - 2; })
                .attr('height', function() { return (y(minY) - y(maxY))/ bins; });
            leftHistGroupExit.selectAll('rect')
                .transition()
                .duration(animationDuration)
                .attr('height', 0);

            leftHistGroupEnter.append('text')
                .attr('dy', '.35em')
                .attr('text-anchor', 'middle');
            leftHistGroup.selectAll('text')
                .text(function(d) { return leftHistData[d].y || ''; })
                .transition()
                .delay(function(d) { return d * 25; })
                .duration(animationDuration)
                .attr('x', function(d) { return - leftHistHeightScale(leftHistData[d].y) + 10; })
                .attr('y', function(d) { return y(leftHistData[d].x) - (y(minY) - y(maxY))/ bins / 2; });
            leftHistGroupExit.selectAll('text')
                .text('');
        }

        function updateLegend() {
            var html = 'Correlation Coefficient: ' + correlation + '<br/>' +
                'p-value: ' + pvalue + '<br/>' +
                'Method: ' + method + '<br/><br/>' +
                'Selected: ' + d3.selectAll('.point.selected').size() || d3.selectAll('.point').size() + '<br/>' +
                'Displayed: ' + d3.selectAll('.point').size() + '<br/><br/>';

            html = html + '<p style="background: #000000; color:#FFFFFF">Default</p>';

            annotations.forEach(function(annotation) {
                html += '<p style=background:' + getColor(annotation) + '; color:#FFFFFF>' + annotation + '</p>';
            });

            legend.html(html);
        }

        function updateRegressionLine() {
            var regressionLine = svg.selectAll('.regressionLine')
                .data(regLineSlope === 'NA' ? [] : [0], function(d) { return d });
            regressionLine.enter()
                .append('line')
                .attr('class', 'regressionLine')
                .on('mouseover', function () {
                    d3.select(this).attr('stroke', 'red');
                    tooltip
                        .style('visibility', 'visible')
                        .html('slope: ' + regLineSlope + '<br/>intercept: ' + regLineYIntercept)
                        .style('left', smartRUtils.mouseX(root) + 'px')
                        .style('top', smartRUtils.mouseY(root) + 'px');
                })
                .on('mouseout', function () {
                    d3.select(this).attr('stroke', 'orange');
                    tooltip.style('visibility', 'hidden');
                });

            regressionLine.transition()
                .duration(animationDuration)
                .attr('x1', x(minX))
                .attr('y1', y(regLineYIntercept + regLineSlope * minX))
                .attr('x2', x(maxX))
                .attr('y2', y(regLineYIntercept + regLineSlope * maxX));

            regressionLine.exit()
                .remove();
        }

        function reset() {
            updateStatistics([], false, true);
        }

        updateScatterplot();
        updateHistogram();
        updateRegressionLine();
        updateLegend();
    }

}]);
