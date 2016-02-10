
smartRApp.directive('runButton', ['rServeService', function(rServeService) {
    return {
        restrict: 'E',
        scope: {
            storage: '=resultsStorage',
            script: '@scriptToRun',
            name: '@buttonName',
            arguments: '=parameterMap'
        },
        template: '<input type="button" value="{{name}}"><span></span>',
        link: function(scope, element) {
            var template_btn = element.children()[0];
            var template_msg = element.children()[1];

            template_btn.onclick = function() {
                rServeService.startScriptExecution({
                    taskType: scope.script,
                    arguments: scope.arguments
                }).then(
                    function (msg) { scope.storage = JSON.parse(msg.result.artifacts.value); },
                    function (msg) { template_msg.innerHTML = '  Failure: ' + msg; }
                );
            };
        }
    };
}]);
