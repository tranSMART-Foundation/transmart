smartRApp
    .directive('conceptBox', function() {
        return {
            restrict: 'E',
            scope: {
                conceptType: '=info'
            },
            template: '<div class="queryGroupIncludeSmall">__TEXT1__ {{conceptType}} __TEXT2__</div>',
            controller: function($scope) {
                 // $scope.conceptBoxes[$scope.conceptGroup] = [];
            },
            link: function(scope, el, attrs) {
                //scope.$watch(
                //    function() { return el[0].childNodes.length; },
                //    function() { console.log(el); scope.conceptBoxes[scope.conceptGroup] = getConcepts(el); }
                //)
            }
        };
    });
