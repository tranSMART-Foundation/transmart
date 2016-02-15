
window.smartRApp.directive('runButton', ['rServeService', function(rServeService) {
    return {
        restrict: 'E',
        scope: {
            storage: '=storeResultsIn',
            script: '@scriptToRun',
            name: '@buttonName',
            arguments: '=argumentsToUse'
        },
        template: '<input type="button" value="{{name}}"><span style="padding-left: 10px;"></span>',
        link: function(scope, element) {
            var template_btn = element.children()[0];
            var template_msg = element.children()[1];

            template_btn.onclick = function() {
                template_btn.disabled = true;
                template_msg.style.color = 'black';
                template_msg.innerHTML = 'Creating plot, please wait <span class="blink_me">_</span>';
                rServeService.startScriptExecution({
                    taskType: scope.script,
                    arguments: scope.arguments
                }).then(
                    function (response) {
                        template_msg.innerHTML = '';
                        scope.storage = JSON.parse(response.result.artifacts.value);
                    },
                    function (response) {
                        template_msg.style.color = 'red';
                        template_msg.innerHTML = '  Failure: ' + response.statusText;
                    }
                ).finally(function() {
                    template_btn.disabled = false;
                });
            };
        }
    };
}]);
