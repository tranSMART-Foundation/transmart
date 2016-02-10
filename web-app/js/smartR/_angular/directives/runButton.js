
smartRApp.directive('runButton', ['rServeService', function(rServeService) {
    return {
        restrict: 'E',
        scope: {
            storage: '=resultsStorage',
            script: '@scriptToRun',
            name: '@buttonName',
            arguments: '=parameterMap'
        },
        template: '<input type="button" value="{{name}}"><span style="color:red"></span>',
        link: function(scope, element) {
            var template_btn = element.children()[0];
            var template_msg = element.children()[1];

            template_btn.onclick = function() {
                template_msg.innerHTML = '';
                rServeService.startScriptExecution({
                    taskType: scope.script,
                    arguments: scope.arguments
                }).then(
                    function (response) { scope.storage = JSON.parse(response.result.artifacts.value); },
                    function (response) { template_msg.innerHTML = '  Failure: ' + response.statusText; }
                );
            };
        }
    };
}]);
