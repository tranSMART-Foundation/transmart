//# sourceURL=heatmap.js

function HeatmapController($scope, smartRUtils, commonWorkflowService) {

    commonWorkflowService.initializeWorkflow('heatmap', $scope);

    // ------------------------------------------------------------- //
    // Fetch data                                                    //
    // ------------------------------------------------------------- //
    $scope.fetch = {
        btn: {
            disabled: true
        },
        tab: {
            disabled: false
        },
        conceptBoxes: {
            highDimensional: {concepts: [], valid: false},
            numerical: {concepts: [], valid: false},
            categorical: {concepts: [], valid: false}
        },
        running: false,
        loaded: false,
        selectedBiomarkers: [],
        scriptResults: {},
        totalSamples: 0,
        subsets: 0
    };

    // ------------------------------------------------------------- //
    // Preprocess                                                    //
    // ------------------------------------------------------------- //
    $scope.preprocess = {
        btn: {
            disabled: false
        },
        tab: {
            disabled: true
        },
        params:  {
            aggregateProbes: false
        },
        running: false,
        scriptResults: {}
    };

    // ------------------------------------------------------------- //
    // Run Heatmap                                                   //
    // ------------------------------------------------------------- //
    $scope.runAnalysis = {
        btn: {
            disabled: false
        },
        tab: {
            disabled: true
        },
        params: {
            max_row: 100,
            sorting: 'nodes',
            ranking: 'coef'
        },
        download: {
            disabled: true
        },
        running: false,
        subsets: 0,
        scriptResults: {}
    };

    commonWorkflowService.registerCondition(
        ['fetch.running',
            'preprocess.running',
            'runAnalysis.running',
            'fetch.conceptBoxes.highDimensional.valid'
        ],
        ['fetch', 'preprocess', 'runAnalysis'],
        function(newValues, oldValues, scope, affectedComponents) {
            var fetchRunning = newValues[0],
                preprocessRunning = newValues[1],
                runAnalysisRunning = newValues[2],
                conceptBoxesHighDimensionalValid = newValues[3];
            var fetchModel = affectedComponents[0],
                preprocessModel = affectedComponents[1],
                runAnalysisModel = affectedComponents[2];

            // clear old results
            if (fetchRunning) {
                preprocessModel.scriptResults = {};
                runAnalysisModel.scriptResults = {};
            }

            // clear old results
            if (preprocessRunning) {
                runAnalysisModel.scriptResults = {};
            }

            // disable tabs when certain criteria are not met
            fetchModel.tab.disabled = preprocessRunning || runAnalysisRunning;
            preprocessModel.tab.disabled = fetchRunning || runAnalysisRunning || !scope.fetch.loaded ||
                scope.fetch.totalSamples <= 1;
            runAnalysisModel.tab.disabled = fetchRunning || preprocessRunning || !scope.fetch.loaded;

            // disable buttons when certain criteria are not met
            fetchModel.btn.disabled = !conceptBoxesHighDimensionalValid;
            runAnalysisModel.download.disabled = runAnalysisRunning || $.isEmptyObject(scope.runAnalysis.scriptResults);

            // set ranking criteria
            runAnalysisModel.subsets = scope.fetch.subsets;
            if (scope.fetch.subsets < 2 &&
                    ['logfold', 'bval', 'pval', 'adjpval', 'ttest'].indexOf(runAnalysisModel.params.ranking) !== -1) {
                runAnalysisModel.params.ranking = 'mean';
            } else if (scope.fetch.subsets > 1 &&
                    ['logfold', 'bval', 'pval', 'adjpval', 'ttest'].indexOf(runAnalysisModel.params.ranking) === -1) {
                runAnalysisModel.params.ranking = 'bval';
            }
        }
    );
}

window.smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', HeatmapController]);
