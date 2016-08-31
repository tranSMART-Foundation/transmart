//# sourceURL=d3Boxplot.js

'use strict';

window.smartRApp.directive('boxplot', [
    'smartRUtils',
    'rServeService',
    '$rootScope',
    function(smartRUtils, rServeService, $rootScope) {

    return {
        restrict: 'E',
        scope: {
            data: '='
        },
        templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/boxplot.html',
        link: function (scope, element) {
            var vizDiv = element.children()[0];
            /**
             * Watch data model (which is only changed by ajax calls when we want to (re)draw everything)
             */
            scope.$watch('data', function () {
                $(vizDiv).empty();
                if (! $.isEmptyObject(scope.data)) {
                    createBoxplot(scope, vizDiv);
                }
            });
        }
    };

    function createBoxplot(scope, vizDiv) {
        var cf = crossfilter(scope.data.dataMatrix);
        var byValue = cf.dimension(function(d) { return d.value; });
        var bySubset = cf.dimension(function(d) { return d.subset; });

        bySubset.filterExact(1);
        var subset1 = {
            y: byValue.top(Infinity).map(function(d) { return d.value; }),
            type: 'box',
            name: 'Subset 1',
            jitter: 0.3,
            pointpos: -1.8,
            boxmean: 'sd',
            marker: {
                color: 'rgb(7, 40, 89)'
            },
            boxpoints: 'all'
        };

        bySubset.filterExact(2);
        var subset2 = {
            y: byValue.top(Infinity).map(function(d) { return d.value; }),
            type: 'box',
            name: 'Subset 2',
            jitter: 0.3,
            pointpos: -1.8,
            boxmean: 'sd',
            marker: {
                color: '#FF851B'
            },
            boxpoints: 'all'
        };

        var plotData = [subset1, subset2];
        var layout = {
            title: 'Boxplot of ' + scope.data.concept,
            height: 800
        };
        Plotly.newPlot(vizDiv, plotData, layout);
    }
}]);

