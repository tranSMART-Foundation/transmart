'use strict';

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; })();

function buildCorrelationAnalysis(results) {
    var animationDuration = 500;
    var bins = 10;
    var w = 1200;
    var h = 1200;
    var margin = { top: 20, right: 20, bottom: h / 4, left: w / 4 };
    var width = w * 3 / 4 - margin.left - margin.right;
    var height = h * 3 / 4 - margin.top - margin.bottom;
    var bottomHistHeight = margin.bottom;
    var leftHistHeight = margin.left;
    var colors = ['#33FF33', '#3399FF', '#CC9900', '#CC99FF', '#FFFF00', 'blue'];
    var x = d3.scale.linear().domain(d3.extent(results.points, function (d) {
        return d.x;
    })).range([0, width]);
    var y = d3.scale.linear().domain(d3.extent(results.points, function (d) {
        return d.y;
    })).range([height, 0]);

    var correlation = undefined,
        pvalue = undefined,
        regLineSlope = undefined,
        regLineYIntercept = undefined,
        patientIDs = undefined,
        tags = undefined,
        points = undefined,
        xArrLabel = undefined,
        yArrLabel = undefined,
        method = undefined,
        minX = undefined,
        maxX = undefined,
        minY = undefined,
        maxY = undefined;
    function setData(data) {
        correlation = data.correlation[0];
        pvalue = data.pvalue[0];
        regLineSlope = data.regLineSlope[0];
        regLineYIntercept = data.regLineYIntercept[0];
        xArrLabel = data.xArrLabel[0];
        yArrLabel = data.yArrLabel[0];
        method = data.method[0];
        patientIDs = data.patientIDs;
        tags = data.tags.sort();
        points = data.points;
        var _d3$extent = d3.extent(data.points, function (d) {
            return d.x;
        });

        var _d3$extent2 = _slicedToArray(_d3$extent, 2);

        minX = _d3$extent2[0];
        maxX = _d3$extent2[1];

        var _d3$extent3 = d3.extent(data.points, function (d) {
            return d.y;
        });

        var _d3$extent4 = _slicedToArray(_d3$extent3, 2);

        minY = _d3$extent4[0];
        maxY = _d3$extent4[1];
    }

    setData(results);

    function updateStatistics(patientIDs) {
        var scatterUpdate = arguments.length <= 1 || arguments[1] === undefined ? false : arguments[1];
        var init = arguments.length <= 2 || arguments[2] === undefined ? false : arguments[2];

        var settings = { patientIDs: patientIDs };
        var onResponse = function onResponse(response) {
            if (init) {
                d3.selectAll('#scatterplot *').remove();
                buildCorrelationAnalysis(response);
                return;
            }
            setData(response);
            if (scatterUpdate) updateScatterplot();
            updateRegressionLine();
            updateLegend();
            updateHistogram();
        };
        startWorkflow(onResponse, settings, false, false);
    }

    var svg = d3.select('#scatterplot').append('svg').attr('width', width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom).append('g').attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')').on('contextmenu', function () {
        d3.event.preventDefault();
        contextMenu.style('visibility', 'visible').style('left', mouseX() + 'px').style('top', mouseY() + 'px');
    });

    var tooltip = d3.select('#scatterplot').append('div').attr('class', 'tooltip').style('visibility', 'hidden');

    function dragmove() {
        legend.style('left', mouseX() + 'px').style('top', mouseY() + 'px');
    }

    var drag = d3.behavior.drag().on('drag', dragmove);

    var legend = d3.select('#scatterplot').append('div').attr('class', 'legend').style('left', 0).style('top', $('#scatterplot').offsetTop + 'px').call(drag);

    svg.append('g').attr('class', 'x axis').attr('transform', 'translate(0, 0)').call(d3.svg.axis().scale(x).ticks(10).tickFormat('').innerTickSize(height).orient('bottom'));

    svg.append('text').attr('class', 'axisLabels').attr('transform', 'translate(' + width / 2 + ', ' + -margin.top / 2 + ')').text(shortenConcept(xArrLabel));

    svg.append('g').attr('class', 'y axis').attr('transform', 'translate(' + width + ', ' + 0 + ')').call(d3.svg.axis().scale(y).ticks(10).tickFormat('').innerTickSize(width).orient('left'));

    svg.append('text').attr('class', 'axisLabels').attr('transform', 'translate(' + (width + margin.right / 2) + ', ' + height / 2 + ')rotate(90)').text(shortenConcept(yArrLabel));

    svg.append('g').attr('class', 'x axis').attr('transform', 'translate(' + 0 + ', ' + height + ')').call(d3.svg.axis().scale(x).orient('top'));

    svg.append('g').attr('class', 'y axis').attr('transform', 'translate(' + 0 + ', ' + 0 + ')').call(d3.svg.axis().scale(y).orient('right'));

    function updateCohorts() {
        alert('This feature will be available in TranSMART 1.3');
    }

    function excludeSelection() {
        var remainingPatientIDs = d3.selectAll('.point:not(.selected)').map(function (d) {
            return d.patientID;
        });
        updateStatistics(remainingPatientIDs, true);
    }

    function zoomSelection() {
        if (d3.selectAll('.point.selected').size() < 2) {
            alert('Please select at least two elements before zooming!');
            return;
        }
        var selectedPatientIDs = d3.selectAll('.point.selected').map(function (d) {
            return d.patientID;
        });
        updateStatistics(selectedPatientIDs, false, true);
    }

    var ctxHtml = '<input id=\'updateCohortsButton\' class=\'mybutton\' type=\'button\' value=\'Update Cohorts\'/><br/>\n<input id=\'zoomButton\' class=\'mybutton\' type=\'button\' value=\'Zoom\'/><br/>\n<input id=\'excludeButton\' class=\'mybutton\' type=\'button\' value=\'Exclude\'/><br/>\n<input id=\'resetButton\' class=\'mybutton\' type=\'button\' value=\'Reset\'/>';
    var contextMenu = d3.select('#scatterplot').append('div').attr('class', 'contextMenu').style('visibility', 'hidden').html(ctxHtml);
    $('#updateCohortsButton').on('click', function () {
        contextMenu.style('visibility', 'hidden');updateCohorts();
    });
    $('#zoomButton').on('click', function () {
        contextMenu.style('visibility', 'hidden');zoomSelection();
    });
    $('#excludeButton').on('click', function () {
        contextMenu.style('visibility', 'hidden');excludeSelection();
    });
    $('#resetButton').on('click', function () {
        contextMenu.style('visibility', 'hidden');reset();
    });

    function updateSelection() {
        var extent = brush.extent();

        var _map = [extent[0][0], extent[1][0]].map(function (d) {
            return x.invert(d);
        });

        var _map2 = _slicedToArray(_map, 2);

        var x0 = _map2[0];
        var x1 = _map2[1];

        var _map3 = [extent[0][1], extent[1][1]].map(function (d) {
            return y.invert(d);
        });

        var _map4 = _slicedToArray(_map3, 2);

        var y0 = _map4[0];
        var y1 = _map4[1];

        svg.selectAll('.point').classed('selected', false).style('fill', function (d) {
            return getColor(d.tag);
        }).style('stroke', 'white');
        if (brush.empty()) {
            d3.selectAll('.point').classed('selected', true);
        } else {
            svg.selectAll('.point').filter(function (d) {
                return x0 <= d.x && d.x <= x1 && y1 <= d.y && d.y <= y0;
            }).classed('selected', true).style('fill', 'white').style('stroke', function (d) {
                return getColor(d.tag);
            });
        }
    }

    var brush = d3.svg.brush().x(d3.scale.identity().domain([0, width])).y(d3.scale.identity().domain([0, height])).on('brushend', function () {
        contextMenu.style('visibility', 'hidden').style('top', -100 + 'px');
        updateSelection();
        var selectedPatientIDs = d3.selectAll('.point.selected').map(function (d) {
            return d.patientID;
        });
        updateStatistics(selectedPatientIDs);
    });

    svg.append('g').attr('class', 'brush').on('mousedown', function () {
        return d3.event.button === 2 ? d3.event.stopImmediatePropagation() : null;
    }).call(brush);

    function getColor(tag) {
        return tag ? colors[tags.indexOf(tag)] : '#000000';
    }

    function updateScatterplot() {
        var point = svg.selectAll('.point').data(points, function (d) {
            return d.patientID;
        });

        point.enter().append('circle').attr('class', 'point').attr('cx', function (d) {
            return x(d.x);
        }).attr('cy', function (d) {
            return y(d.y);
        }).attr('r', 5).style('fill', function (d) {
            return getColor(d.tag);
        }).on('mouseover', function (d) {
            d3.select(this).style('fill', '#FF0000');
            tooltip.style('left', 10 + mouseX() + 'px').style('top', 10 + mouseY() + 'px').style('visibility', 'visible').html(shortenConcept(xArrLabel) + ': ' + d.x + '<br/>\n' + shortenConcept(yArrLabel) + ': ' + d.y + '<br/>\nPatient ID: ' + d.patientID + '<br/>\n' + (d.tag ? 'Tag: ' + d.tag : ''));
        }).on('mouseout', function () {
            var p = d3.select(this);
            if (p.classed('selected')) {
                p.style('fill', '#FFFFFF');
            } else {
                p.style('fill', function (d) {
                    return getColor(d.tag);
                });
            }
            tooltip.style('visibility', 'hidden');
        });

        point.exit().classed('selected', false).transition().duration(animationDuration).attr('r', 0).remove();
    }

    function updateHistogram() {
        var bottomHistData = d3.layout.histogram().bins(bins)(points.map(function (d) {
            return d.x;
        }));
        var leftHistData = d3.layout.histogram().bins(bins)(points.map(function (d) {
            return d.y;
        }));

        var bottomHistHeightScale = d3.scale.linear().domain([0, bottomHistData.max(function (d) {
            return d.y;
        })]).range([1, bottomHistHeight]);
        var leftHistHeightScale = d3.scale.linear().domain([0, leftHistData.max(function (d) {
            return d.y;
        })]).range([2, leftHistHeight]);

        var bottomHistGroup = svg.selectAll('.bar.bottom').data(Array(bins).fill().map(function (_, i) {
            return i;
        }));
        var bottomHistGroupEnter = bottomHistGroup.enter().append('g').attr('class', 'bar bottom');
        var bottomHistGroupExit = bottomHistGroup.exit();

        bottomHistGroupEnter.append('rect').attr('y', height + 1);
        bottomHistGroup.selectAll('rect').transition().delay(function (d) {
            return d * 25;
        }).duration(animationDuration).attr('x', function (d) {
            return x(bottomHistData[d].x);
        }).attr('width', function () {
            return (x(maxX) - x(minX)) / bins;
        }).attr('height', function (d) {
            return bottomHistHeightScale(bottomHistData[d].y) - 1;
        });
        bottomHistGroupExit.selectAll('rect').transition().duration(animationDuration).attr('height', 0);

        bottomHistGroupEnter.append('text').attr('dy', '.35em').attr('text-anchor', 'middle');
        bottomHistGroup.selectAll('text').text(function (d) {
            return bottomHistData[d].y || '';
        }).transition().delay(function (d) {
            return d * 25;
        }).duration(animationDuration).attr('x', function (d) {
            return x(bottomHistData[d].x) + (x(maxX) - x(minX)) / bins / 2;
        }).attr('y', function (d) {
            return height + bottomHistHeightScale(bottomHistData[d].y) - 10;
        });
        bottomHistGroupExit.selectAll('text').text('');

        var leftHistGroup = svg.selectAll('.bar.left').data(Array(bins).fill().map(function (_, i) {
            return i;
        }));
        var leftHistGroupEnter = leftHistGroup.enter().append('g').attr('class', 'bar left');
        var leftHistGroupExit = leftHistGroup.exit();

        leftHistGroupEnter.append('rect');
        leftHistGroup.selectAll('rect').transition().delay(function (d) {
            return d * 25;
        }).duration(animationDuration).attr('x', function (d) {
            return -leftHistHeightScale(leftHistData[d].y) + 1;
        }).attr('y', function (d) {
            return y(leftHistData[d].x) - (y(minY) - y(maxY)) / bins;
        }).attr('width', function (d) {
            return leftHistHeightScale(leftHistData[d].y) - 2;
        }).attr('height', function () {
            return (y(minY) - y(maxY)) / bins;
        });
        leftHistGroupExit.selectAll('rect').transition().duration(animationDuration).attr('height', 0);

        leftHistGroupEnter.append('text').attr('dy', '.35em').attr('text-anchor', 'middle');
        leftHistGroup.selectAll('text').text(function (d) {
            return leftHistData[d].y || '';
        }).transition().delay(function (d) {
            return d * 25;
        }).duration(animationDuration).attr('x', function (d) {
            return -leftHistHeightScale(leftHistData[d].y) + 10;
        }).attr('y', function (d) {
            return y(leftHistData[d].x) - (y(minY) - y(maxY)) / bins / 2;
        });
        leftHistGroupExit.selectAll('text').text('');
    }

    function updateLegend() {
        var html = 'Correlation Coefficient: ' + correlation + '<br/>\np-value: ' + pvalue + '<br/>\nMethod: ' + method + '<br/><br/>\nSelected: ' + (d3.selectAll('.point.selected').size() || d3.selectAll('.point').size()) + '<br/>\nDisplayed: ' + d3.selectAll('.point').size() + '<br/><br/>';
        html = html + '<p style=\'background:#000000; color:#FFFFFF\'>Default</p>';
        var _iteratorNormalCompletion = true;
        var _didIteratorError = false;
        var _iteratorError = undefined;

        try {
            for (var _iterator = tags[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                var tag = _step.value;

                if (tag) html += '<p style=\'background:' + getColor(tag) + '; color:#FFFFFF\'>' + tag + '</p>';
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

        legend.html(html);
    }

    function updateRegressionLine() {
        var regressionLine = svg.selectAll('.regressionLine').data(regLineSlope === 'NA' ? [] : [0], function (d) {
            return d;
        });
        regressionLine.enter().append('line').attr('class', 'regressionLine').on('mouseover', function () {
            d3.select(this).attr('stroke', 'red');
            tooltip.style('visibility', 'visible').html('slope: ' + regLineSlope + '<br/>intercept: ' + regLineYIntercept).style('left', mouseX() + 'px').style('top', mouseY() + 'px');
        }).on('mouseout', function () {
            d3.select(this).attr('stroke', 'orange');
            tooltip.style('visibility', 'hidden');
        });

        regressionLine.transition().duration(animationDuration).attr('x1', x(minX)).attr('y1', y(regLineYIntercept + regLineSlope * minX)).attr('x2', x(maxX)).attr('y2', y(regLineYIntercept + regLineSlope * maxX));

        regressionLine.exit().remove();
    }

    function reset() {
        updateStatistics([], false, true);
    }

    updateScatterplot();
    updateHistogram();
    updateRegressionLine();
    updateLegend();
}
