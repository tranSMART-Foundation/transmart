'use strict';

describe('ajaxServices', function() {

    it('foo', function() {
        var bar = '';
        expect(bar).toEqual('');    
    });

    // FIXME: These tests are for the old ajaxService. Adapt them to angular.


    // var BASE_PATH = 'http://localhost/test_bpath';
    // var TEST_WORKFLOW  = 'testwf';
    // var executionId = '27480883-9019-401b-9035-48ca4e87aa18';
    // var ajaxServices = smartR.ajaxServices(BASE_PATH, TEST_WORKFLOW);
    // var standardFailure = {
    //     status: 400,
    //     statusText: 'Bad Request',
    //     contextType: 'application/json',
    //     responseText: JSON.stringify({
    //         httpStatus: 400,
    //         type: 'InvalidArgumentsException',
    //         message: 'Parameter conceptKeys not passed',
    //     }),
    // };

    // function sharedExampleForPromiseFailure(name) {
    //     it(name, function() {
    //         var spy = jasmine.createSpy();
    //         this.promise.fail(spy);
    //         expect(spy).toHaveBeenCalledWith({
    //             status: standardFailure.status,
    //             statusText: standardFailure.statusText,
    //             response: JSON.parse(standardFailure.responseText),
    //         });
    //     });
    // }

    // function sharedEnvironmentForPromiseCancellation(name) {
    //     describe(name, function() {
    //         beforeEach(function() {
    //             this.promise.cancel();
    //         });

    //         it('the promise fails with status \'abort\'', function() {
    //             var spy = jasmine.createSpy();
    //             this.promise.fail(spy);
    //             expect(spy).toHaveBeenCalledWith({
    //                 status: 0,
    //                 statusText: 'abort',
    //             });
    //         });
    //     });
    // }

    // function sharedExampleForExecutingTouches(sessionId) {
    //     sessionId = sessionId || null
    //     it('executes touching for 9 min after', function() {
    //         jasmine.clock().tick(9 * 60 * 1000 - 1);
    //         var numRequests = jasmine.Ajax.requests.count();
    //         jasmine.clock().tick(1);
    //         expect(jasmine.Ajax.requests.count()).toBe(numRequests + 1);

    //         expect(this.mostRecentRequest().url)
    //             .toBe(BASE_PATH + '/RSession/touch');
    //         expect(this.mostRecentRequest().data().sessionId).toBe(sessionId);
    //     });
    // }

    // function defineJsonResponse(data) {
    //     beforeEach(function() {
    //         this.mostRecentRequest().respondWith({
    //             status: 200,
    //             contentType: 'application/json',
    //             responseText: JSON.stringify(data)
    //         });
    //     });
    // }

    // function addExecutionIdToResponse(data) {
    //     var copy = jQuery.extend({}, data);
    //     copy.executionId = executionId;
    //     return copy;
    // }

    // beforeEach(function () {
    //     jasmine.Ajax.install();
    //     jasmine.clock().install();
    //     this.mostRecentRequest = function() {
    //         return jasmine.Ajax.requests.mostRecent();
    //     }
    // });

    // afterEach(function () {
    //     jasmine.Ajax.uninstall();
    //     jasmine.clock().uninstall();
    //     ajaxServices.abandonCurrentSession();
    // });

    // describe('call startSession', function() {
    //     beforeEach(function() {
    //         this.promise = ajaxServices.startSession();
    //     });

    //     it('launches a request', function() {
    //         expect(this.mostRecentRequest()).toBeDefined();
    //     });

    //     it('goes to the right URL', function() {
    //         expect(this.mostRecentRequest().url)
    //             .toBe(BASE_PATH + '/RSession/create');
    //     });

    //     it('send the right request data', function() {
    //         expect(this.mostRecentRequest().method).toBe('POST');
    //         expect(this.mostRecentRequest().data().workflow).toBe(TEST_WORKFLOW);
    //     });

    //     describe('the ajax call returning success', function() {
    //         var sessionId = '80f29282-efa3-4c35-bc2d-27c81f91d67f';

    //         beforeEach(function() {
    //             this.mostRecentRequest().respondWith({
    //                 status: 201,
    //                 contentType: 'application/json',
    //                 responseText: '{"sessionId":"' + sessionId + '"}'
    //             });
    //         });

    //         it('returns the correct session id', function() {
    //             var spy = jasmine.createSpy();
    //             this.promise.done(spy);
    //             expect(spy).toHaveBeenCalledWith(sessionId);
    //         });

    //         sharedExampleForExecutingTouches(sessionId);

    //         it('executes second touching 9 min after first', function() {
    //             jasmine.clock().tick(9 * 60 * 1000);
    //             this.mostRecentRequest().respondWith({ status: 204 });
    //             jasmine.clock().tick(9 * 60 * 1000 - 1);
    //             expect(jasmine.Ajax.requests.count()).toBe(2);
    //             jasmine.clock().tick(1);
    //             expect(jasmine.Ajax.requests.count()).toBe(3);
    //         });
    //     });

    //     describe('the ajax call failing', function() {
    //         beforeEach(function() {
    //             this.mostRecentRequest().respondWith(standardFailure);
    //         });

    //         sharedExampleForPromiseFailure('returns the correct session id');
    //     });
    // });

    // describe('call startScriptExecution', function() {
    //     var taskData = {
    //         arguments: { arg1: 'arg1 value' },
    //         taskType: 'sampleTaskType',
    //         phase: 'run'
    //     };

    //     beforeEach(function() {
    //         this.promise = ajaxServices.startScriptExecution(taskData);
    //     });

    //     it('launches a request', function() {
    //         expect(this.mostRecentRequest()).toBeDefined();
    //     });

    //     it('goes to the right URL', function() {
    //         expect(this.mostRecentRequest().url)
    //             .toBe(BASE_PATH + '/ScriptExecution/run');
    //     });

    //     it('contains matching payload', function() {
    //         expect(this.mostRecentRequest().data().arguments).toEqual(taskData.arguments);
    //         expect(this.mostRecentRequest().data().workflow).toBe(TEST_WORKFLOW);
    //         expect(this.mostRecentRequest().data().taskType).toBe(taskData.taskType);
    //     });

    //     describe('the initial call fails', function() {
    //         beforeEach(function() {
    //             this.mostRecentRequest().respondWith(standardFailure);
    //         });

    //         sharedExampleForPromiseFailure('returns the error the server returned');
    //     });

    //     sharedEnvironmentForPromiseCancellation('the initial call is cancelled');

    //     describe('another call to startScriptExecution is made', function() {
    //         beforeEach(function() {
    //             ajaxServices.startScriptExecution(taskData);
    //         });

    //         it('the first promise reports abortion', function() {
    //             var spy = jasmine.createSpy();
    //             this.promise.fail(spy);
    //             expect(spy).toHaveBeenCalledWith({
    //                 status: 0,
    //                 statusText: 'abort'
    //             });
    //         });
    //     });

    //     describe('the initial call succeeds', function() {
    //         var response = { executionId: executionId };

    //         defineJsonResponse(response);

    //         it('there is a second call', function() {
    //             expect(jasmine.Ajax.requests.count()).toBe(2);
    //         });

    //         it('the second call is to /status', function() {
    //             var url = this.mostRecentRequest().url.split('?')[0];
    //             expect(url).toBe(BASE_PATH + '/ScriptExecution/status');
    //         });

    //         it('the call to /status includes the correct executionId', function() {
    //             var params = this.mostRecentRequest().url.split('?')[1].split('&');
    //             expect(params).toContain('executionId=' + executionId);
    //         });

    //         describe('the first call to /status fails', function() {
    //             beforeEach(function() {
    //                 this.mostRecentRequest().respondWith(standardFailure);
    //             });

    //             sharedExampleForPromiseFailure('the promise returns the error status');
    //         });

    //         describe('the first call to /status returns FAILED', function() {
    //             var response = {
    //                 state: 'FAILED',
    //                 result: {
    //                     successful: false,
    //                     exception: 'Sample error during execution'
    //                 }
    //             };

    //             defineJsonResponse(response);

    //             it('the promise returns failure', function() {
    //                 var spy = jasmine.createSpy();
    //                 this.promise.fail(spy);
    //                 expect(spy).toHaveBeenCalledWith({
    //                     status: 0,
    //                     statusText: response.result.exception
    //                 });
    //             });

    //             sharedExampleForExecutingTouches();
    //         });

    //         sharedEnvironmentForPromiseCancellation(
    //             'the first call to /status is cancelled');

    //         describe('the first call to /status returns FINISHED', function() {
    //             var response = {
    //                 state: 'FINISHED',
    //                 result: {
    //                     successful: true,
    //                     artifacts: ['foo']
    //                 }
    //             };

    //             defineJsonResponse(response);

    //             it('returns the status response data', function() {
    //                 var spy = jasmine.createSpy();
    //                 this.promise.done(spy);
    //                 expect(spy).toHaveBeenCalledWith(addExecutionIdToResponse(response));
    //             });

    //             sharedExampleForExecutingTouches();
    //         });

    //         describe('the first call to /status returns RUNNING', function() {
    //             var response = {
    //                 status: 'RUNNING'
    //             };

    //             defineJsonResponse(response);

    //             describe('the second call to /status is scheduled', function() {
    //                 beforeEach(function() {
    //                     jasmine.clock().tick(200 /* < 1000 */);
    //                 });

    //                 sharedEnvironmentForPromiseCancellation(
    //                     'the second call /status is unscheduled');
    //             });

    //             it('makes a second call to /status 1 second later', function() {
    //                 expect(jasmine.Ajax.requests.count()).toBe(2);
    //                 jasmine.clock().tick(1001);
    //                 expect(jasmine.Ajax.requests.count()).toBe(3);

    //                 var url = this.mostRecentRequest().url.split('?')[0];
    //                 expect(url).toBe(BASE_PATH + '/ScriptExecution/status');
    //             });

    //             describe('the second call to /status is underway', function() {
    //                 beforeEach(function() {
    //                     jasmine.clock().tick(1001);
    //                 });

    //                 sharedEnvironmentForPromiseCancellation(
    //                     'the second call /status is cancelled');
    //             });

    //             describe('the second call to /status returns FAILED', function() {
    //                 var response = {
    //                     state: 'FAILED',
    //                     result: {
    //                         successful: false,
    //                         exception: 'Sample error during execution'
    //                     }
    //                 };

    //                 beforeEach(function() {
    //                     jasmine.clock().tick(1001);
    //                 });

    //                 defineJsonResponse(response);

    //                 it('the promise returns failure', function() {
    //                     var spy = jasmine.createSpy();
    //                     this.promise.fail(spy);
    //                     expect(spy).toHaveBeenCalledWith({
    //                         status: 0,
    //                         statusText: response.result.exception
    //                     });
    //                 });
    //             });

    //             describe('the second call to /status returns FINISHED', function() {
    //                 var response = {
    //                     state: 'FINISHED',
    //                     result: {
    //                         successful: true,
    //                         artifacts: ['foo'],
    //                     }
    //                 };

    //                 beforeEach(function() {
    //                     jasmine.clock().tick(1001);
    //                 });

    //                 defineJsonResponse(response);

    //                 it('returns the status response data', function() {
    //                     var spy = jasmine.createSpy();
    //                     this.promise.done(spy);
    //                     expect(spy).toHaveBeenCalledWith(addExecutionIdToResponse(response));
    //                 });
    //             });
    //         });
    //     });
    // });
});
