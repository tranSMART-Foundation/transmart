angular.module('smartR.directives.conceptBox', [])
    .directive('conceptBox', function() {
        return {
            restrict: 'E',
            template: '<div class="queryGroupIncludeSmall"></div>',
            scope: {
                conceptGroup: '='
            },
            controller: function($scope) {
                $scope.conceptBoxes[$scope.conceptGroup] = [];
            },
            link: function(scope, el, attrs) {
                scope.$watch(
                    function() { return el[0].childNodes.length; },
                    function() { console.log(el); scope.conceptBoxes[scope.conceptGroup] = getConcepts(el); }
                )

            }
        };
    });