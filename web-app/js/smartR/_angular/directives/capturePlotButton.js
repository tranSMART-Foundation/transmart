//# sourceURL=capturePlotButton.js

'use strict';

window.smartRApp.directive('capturePlotButton', [function() {

    return {
        restrict: 'E',
        scope: {
            disabled: '=?',
            filename: '@',
            target: '@'
        },
        template: '<button id="sr-capture-button" ng-disabled="disabled">Capture SVG</button>',
        link: function(scope, elements) {
            if (!scope.filename) {
                // default filename
                scope.filename = 'image.svg';
            }


            var template_btn = elements.children()[0];
            template_btn.addEventListener('click', function() {
                var svg = $(scope.target + ' svg');
                if (!svg.length) {
                    return;
                }

                var smartRSheets = [];
                for (var i = 0; i < document.styleSheets.length; i++) {
                    var sheet = document.styleSheets[i];
                    if (sheet.href && sheet.href.indexOf('smart-r') !== -1) {
                        smartRSheets.push(sheet);
                    }
                }

                var rules = [];
                smartRSheets.forEach(function(d) {
                    for (var key in d.cssRules) {
                        if (d.cssRules.hasOwnProperty(key) && d.cssRules[key] instanceof CSSStyleRule) {
                            rules.push(d.cssRules[key].cssText);
                        }
                    }
                });

                var defs = '<defs><style type="text/css"><![CDATA[' + rules.join('') + ']]></style></defs>';
                svg.attr({version: '1.1' , xmlns:"http://www.w3.org/2000/svg"});
                svg.append(defs);
                $(scope.target + ' svg').wrap('<div id="sr-capture-container"></div>');
                var b64 = btoa(unescape(encodeURIComponent($('#sr-capture-container').html())));
                var tab = window.open();
                tab.document.write("<a href-lang='image/svg+xml' href='data:image/svg+xml;base64,\n"+b64+"' title='file.svg' download>Download SVG</a>");
                $(scope.target + ' svg').unwrap();
                $(scope.target + ' svg defs').remove();
            });
        }
    };
}]);
