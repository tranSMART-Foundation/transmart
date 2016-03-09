
window.smartRApp.controller('TimelineController',
    ['$scope', 'smartRUtils', 'rServeService', '$css', function($scope, smartRUtils, rServeService, $css) {

        // load workflow specific css
        $css.bind({
            href: $scope.smartRPath + '/css/timeline.css'
        }, $scope);

        // initialize service
        rServeService.startSession('timeline');

        // model
        $scope.conceptBoxes = {};
        $scope.scriptResults = {};
        $scope.params = {};
    }]);
