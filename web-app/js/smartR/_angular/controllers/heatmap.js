//# sourceURL=heatmap.js

window.smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'rServeService', '$css', function($scope, smartRUtils, rServeService, $css) {

        // load workflow specific css
        $css.bind({
            href: $scope.smartRPath + '/css/heatmap.css'
        }, $scope);

        // initialize service
        rServeService.startSession('heatmap');

        // ------------------------------------------------------------- //
        // Fetch data                                                    //
        // ------------------------------------------------------------- //
        $scope.conceptBoxes = {
            highDimensional : [],
            numerical : [],
            categorical : []
        };
        $scope.selectedBiomarkers = [];
        $scope.fetchSummaryData = {summary:[]};

        $scope.$on('disable::other::buttons', function (event, disabled) {
            console.log('Ok, msg received.', [event, disabled]);
            // when summary is fetched enable preprocess & run buttons
            $scope.preprocess.disabled = disabled;
            $scope.runAnalysis.disabled = disabled;
        });

        // ------------------------------------------------------------- //
        // Preprocess                                                    //
        // ------------------------------------------------------------- //
        $scope.preprocess = {
            params :  {
                aggregateProbes : false
            },
            disabled : true,
            summaryData : {}
        };

        // ------------------------------------------------------------- //
        // Run Heatmap                                                   //
        // ------------------------------------------------------------- //
        $scope.runAnalysis = {
            params: {
                max_row : 100,
                sorting : 'nodes',
                ranking : 'coef'
            },
            disabled : true,
            scriptResults : {}
        }
    }]);
