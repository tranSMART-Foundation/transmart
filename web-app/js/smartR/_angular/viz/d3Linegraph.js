//# sourceURL=d3Linegraph.js

'use strict';

window.smartRApp.directive('lineGraph', [
    'smartRUtils',
    function(smartRUtils) {

        return {
            restrict: 'E',
            scope: {
                data: '=',
                width: '@',
                height: '@'
            },
            link: function (scope, element) {
                scope.$watch('data', function() {
                    $(element[0]).empty();
                    if (! $.isEmptyObject(scope.data)) {
                        smartRUtils.prepareWindowSize(scope.width, scope.height);
                        createLinegraph(scope, element[0]);
                    }
                });
            }
        };

        function createLinegraph(scope, root) {
            var margin = {top: 50, right: 50, bottom: 50, left: 50};
            var width = parseInt(scope.width) - margin.left - margin.right;
            var height = parseInt(scope.height) - margin.top - margin.bottom;
            var data_matrix = scope.data.data_matrix;
            var cf = crossfilter(data_matrix);
            var byPatientID = cf.dimension(function(d) { return d.patientID; });
            var byValue = cf.dimension(function(d) { return d.value; });
            var byTime = cf.dimension(function(d) { return d.time; });
            var byFullName = cf.dimension(function(d) { return d.fullName; });
            var byType = cf.dimension(function(d) { return d.type; });
            var bySubset = cf.dimension(function(d) { return d.subset; });
            
            var times = data_matrix.reduce(function(prev, curr) { 
                var arr = prev;
                var hits = arr.filter(function(d) { return d.time === curr.time; });
                if (hits.length === 0) {
                    arr.push({time: curr.time, name: curr.name});
                }
                return arr;
            }, []);
            times.sort(function(a, b) { return a.time - b.time; });


            var groupByFullName = byFullName.group();

            byType.filterExact('categoric');
            var catFullNames = groupByFullName.all()
                .filter(function(d) { return d.value > 0; })
                .map(function(d) { return d.key; });

            byType.filterExact('numeric');
            var numFullNames = groupByFullName.all()
                .filter(function(d) { return d.value > 0; })
                .map(function(d) { return d.key; });

            byType.filterAll();
            groupByFullName.dispose();

            var xTicks = times.map(function(d, i) {
                if (i === 0) { return 0; }
                return times[times.length - 1] / d * width;
            });
            var x = d3.scale.linear()
                .domain(times)
                .range(xTicks);

            var svg = d3.select(root).append('svg')
                .attr('width', width + margin.left + margin.right)
                .attr('height', height + margin.top + margin.bottom)
                .append('g')
                .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

            var xAxis = d3.svg.axis()
                .scale(x)
                .tickValues(times.map(function() { return d.time; }));

            svg.append('g')
                .attr('class', 'sr-linegraph-x-axis')
                .call(xAxis);
        }

    }
]);
