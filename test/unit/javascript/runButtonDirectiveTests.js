'use strict';

describe('runButton', function() {
    var $compile,
        $rootScope,
        $httpBackend,
        $q,
        element,
        smartRUtils,
        rServeService;

    beforeEach(module('smartRApp'));
    beforeEach(module('smartRTemplates'));

    beforeEach(inject(function(_$compile_, _$rootScope_, _$httpBackend_, _$q_, _smartRUtils_, _rServeService_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        smartRUtils = _smartRUtils_;
        rServeService = _rServeService_;

        $rootScope.scriptResults = {};
    }));

    it('should show "Creating plot, please wait"', function() {
        var html = '<run-button button-name="Create Plot" store-results-in="scriptResults" script-to-run="run"></run-button>';
        element = $compile(angular.element(html))($rootScope);
        $rootScope.$digest();
        expect(element.isolateScope().waitMessage).toEqual("Creating plot, please wait");
    });

    it('should show "Running R-script, please wait"', function() {
        var html = '<run-button button-name="Create Plot" store-results-in="scriptResults" script-to-run="run" wait-message="Running R-script, please wait"></run-button>';
        element = $compile(angular.element(html))($rootScope);
        $rootScope.$digest();
        expect(element.isolateScope().waitMessage).toEqual("Running R-script, please wait");
    });
});

