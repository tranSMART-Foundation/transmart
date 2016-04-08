'use strict';

describe('smartRUtils service', function() {
    var smartRUtils;

    beforeEach(module('smartRApp'));

    beforeEach(inject(function(_smartRUtils_) {
        smartRUtils = _smartRUtils_;
    }));

    it('has functional conceptBoxMapToConceptKeys()', function() {
        var conceptBoxMap = {a: ['c1', 'c2'], 'foo bar __-!*()123 abc': 'c3', b: [], c: ['12--_- c31/??/*&^/foobar']};
        var expected = {a_n0: 'c1', a_n1: 'c2', 'foo bar __-!*()123 abc_n0': 'c3', c_n0: '12--_- c31/??/*&^/foobar'};
        var result = smartRUtils.conceptBoxMapToConceptKeys(conceptBoxMap);
        expect(result).toEqual(expected);
    });
    
});
