//# sourceURL=heatmap.js

'use strict';

window.smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        // initialize service
        rServeService.startSession('heatmap');

        // model
        $scope.conceptBoxes = {
            highDimensional : [],
            numerical : [],
            categorical : []
        };

        $scope.fetchSummary = {
            img : {},
            json : {}
        };

        $scope.scriptResults = {};
        $scope.params = {};

    }]);
