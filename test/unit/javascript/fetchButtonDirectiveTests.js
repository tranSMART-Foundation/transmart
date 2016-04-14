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

        element = $compile(angular.element('<fetch-button></fetch-button>'))($rootScope);
        $rootScope.$digest();
    }));

    it('replaces element with content', function() {
        expect(element.find('input')).toBeDefined();
        expect(element.find('span')).toBeDefined();
    });

    it('should contain initially no text', function() {
        expect(element.find('span').text()).toEqual('');
    });

    it('should show text when clicked', function() {
        try { // we do not care about the background functionality at this point
            element.find('input').click();
        } catch(e) {}
        expect(element.find('span').text()).toContain('Fetching data');
    });

    describe('successful', function() {
        beforeEach(function() {
            spyOn(smartRUtils, 'conceptBoxMapToConceptKeys').and.returnValue({foo: 'bar'});
            spyOn(smartRUtils, 'countCohorts').and.returnValue(1);
            var defer = $q.defer();
            spyOn(rServeService, 'loadDataIntoSession').and.returnValue(defer.promise);
            element.find('input').click();
            defer.resolve('SUCCESS');
            $rootScope.$digest();
        });

        it('should show success when successful with showSummaryStats disabled', function() {
            expect(element.find('span').text()).toEqual('SUCCESS');
        });

        element.isolateScope().showSummaryStats = true;

        it('should show success when successful with showSummaryStats enabled', function() {
            expect(element.find('span').text()).toEqual('SUCCESS');
        });
    });

    describe('fail because no cohorts', function() {
        beforeEach(function() {
            spyOn(smartRUtils, 'conceptBoxMapToConceptKeys').and.returnValue({foo: 'bar'});
            spyOn(smartRUtils, 'countCohorts').and.returnValue(0);
        });

        it('should fail with showSummaryStats disabled', function() {
            expect(element.find('span').text()).toEqual('Error: No cohorts selected!');
        });

        element.isolateScope().showSummaryStats = true;

        it('should fail with showSummaryStats enabled', function() {
            expect(element.find('span').text()).toEqual('Error: No cohorts selected!');
        });
    });

    describe('fail because no concepts', function() {
        beforeEach(function() {
            spyOn(smartRUtils, 'conceptBoxMapToConceptKeys').and.returnValue({});
            element.find('input').click();
        });

        it('should fail with showSummaryStats disabled', function() {
            expect(element.find('span').text()).toEqual('Error: No data selected!');
        });

        element.isolateScope().showSummaryStats = true;

        it('should fail with showSummaryStats enabled', function() {
            expect(element.find('span').text()).toEqual('Error: No data selected!');
        });
    });

    describe('fail because loadDataIntoSession fails', function() {
        beforeEach(function() {
            spyOn(smartRUtils, 'conceptBoxMapToConceptKeys').and.returnValue({foo: 'bar'});
            spyOn(smartRUtils, 'countCohorts').and.returnValue(1);
            var defer = $q.defer();
            spyOn(rServeService, 'loadDataIntoSession').and.returnValue(defer.promise);
            element.find('input').click();
            defer.reject('FAILURE');
            $rootScope.$digest();
        });

        it('should fail with showSummaryStats disabled', function() {
            expect(element.find('span').text()).toEqual('FAILURE');
        });

        element.isolateScope().showSummaryStats = true;

        it('should fail with showSummaryStats enabled', function() {
            expect(element.find('span').text()).toEqual('FAILURE');
        });
    });
});
