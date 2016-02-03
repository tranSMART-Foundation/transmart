
smartRApp.directive('conceptbox', function() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            conceptGroup: '='
        },
        template: '<div class="queryGroupIncludeSmall"></div>',
        controller: function($scope) {
            $scope.conceptBoxes[$scope.conceptGroup] = [];
        },
        link: function(scope, element, attrs) {
            //scope.$watch(
            //    function() { return el[0].childNodes.length; },
            //    function() { console.log(el); scope.conceptBoxes[scope.conceptGroup] = getConcepts(el); }
            //)
        }
    };
});
