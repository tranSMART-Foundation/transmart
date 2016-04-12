'use strict';

describe('fetchButton', function() {
    var $compile,
        $rootScope,
        $httpBackend,
        element;

    beforeEach(module('smartRApp'));
    beforeEach(module('smartRTemplates'));

    beforeEach(inject(function(_$compile_, _$rootScope_, _$httpBackend_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $httpBackend = _$httpBackend_;

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
});
