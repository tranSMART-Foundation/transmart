'use strict';

describe('fetchButton', function() {
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

        $rootScope.conceptMap = {};

        element = $compile(angular.element('<fetch-button concept-map="conceptMap"></fetch-button>'))($rootScope);
        $rootScope.$digest();
    }));

    var _clickButton = function(loadDataIntoSessionSuccess) {
        var defer = $q.defer();
        spyOn(rServeService, 'loadDataIntoSession').and.returnValue(defer.promise);
        element.find('input').click();
        if (loadDataIntoSessionSuccess) {
            defer.resolve();
        } else {
            defer.reject('foobar');
        }
        $rootScope.$digest();
    };

    var _prepareScope = function(cohorts, showSummaryStats, conceptMap) {
        spyOn(smartRUtils, 'countCohorts').and.returnValue(cohorts);
        element.isolateScope().showSummaryStats = showSummaryStats;
        element.isolateScope().conceptMap = conceptMap;
        $rootScope.$digest();
    };

    it('replaces element with content', function() {
        expect(element.find('input')).toBeDefined();
        expect(element.find('span')).toBeDefined();
    });

    it('should contain initially no text', function() {
        expect(element.find('span').text()).toEqual('');
    });

    it('should show text when clicked', function() {
        try { // we just want to see if the progress message is there
            element.find('input').click();
        } catch(e) {}
        expect(element.find('span').text()).toContain('Fetching data');
    });

    it('should show another text after data is loaded if showSummaryStats is enabled', function() {
        _prepareScope(1, true, {foo: ['concept']});
        try { // we just want to see if the progress message is there
            _clickButton(true);
        } catch (e) {}
        expect(element.find('span').text()).toContain('Execute summary statistics');
    });

    it('should succeed', function() {
        _prepareScope(2, false, {foo: ['concept']});
        _clickButton(true);
        expect(element.find('span').text()).toBe('Task complete!');
    });

    it('should show error due to missing cohorts', function() {
        _prepareScope(0, false, {foo: ['concept']});
        _clickButton(true);
        expect(element.find('span').text()).toBe('Error: No cohorts selected!');
    });

    it('should show error due to missing concepts', function() {
        _prepareScope(1, false, {});
        _clickButton(true);
        expect(element.find('span').text()).toBe('Error: No concepts selected!');
    });
});
