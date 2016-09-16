//# sourceURL=plotlyBoxplot.js

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
        var byBioMarker = cf.dimension(function(d) { return d.bioMarker; });

        var plotData = [];
        smartRUtils.unique(smartRUtils.getValuesForDimension(byBioMarker)).forEach(function(bioMarker) {
            byBioMarker.filterExact(bioMarker);
            smartRUtils.unique(smartRUtils.getValuesForDimension(bySubset, true)).forEach(function(subset) {
                bySubset.filterExact(subset);
                plotData.push({
                    type: 'box',
                    y: smartRUtils.getValuesForDimension(byValue),
                    name: bioMarker + ' s' + subset,
                    boxpoints: 'all',
                    boxmean: 'sd',
                    jitter: 0.5
                });
                bySubset.filterAll();
            });
            byBioMarker.filterAll();
        });
        
        var layout = {
            title: 'Boxplots (' + scope.data.transformation + ')',
            height: 800
        };
        Plotly.newPlot(vizDiv, plotData, layout);
    }
}]);

