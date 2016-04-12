'use strict';

describe('fetchButton', function() {
    var $compile,
        $rootScope,
        $httpBackend;

    beforeEach(module('smartRApp'));
    beforeEach(module('smartRTemplates'));

    beforeEach(inject(function(_$compile_, _$rootScope_, _$httpBackend_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $httpBackend = _$httpBackend_;
    }));

    it('replaces element with content', function() {
        var element = angular.element('<fetch-button></fetch-button>');
        $compile(element)($rootScope);
        $rootScope.$digest();
        expect(element.html()).toContain('input');
        expect(element.html()).toContain('span');
    });
});
