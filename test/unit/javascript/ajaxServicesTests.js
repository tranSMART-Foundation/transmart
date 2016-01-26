describe('ajaxServices', function() {
    var BASE_PATH = 'http://localhost/test_bpath';
    var TEST_WORKFLOW  = 'testwf';
    var ajaxServices = smartR.ajaxServices(BASE_PATH, TEST_WORKFLOW);
    var standardFailure = { status: 400, statusText: 'Bad Request!' };

    function sharedExampleForPromiseFailure(name) {
        it(name, function() {
            var spy = jasmine.createSpy();
            this.promise.fail(spy);
            expect(spy).toHaveBeenCalledWith({
                status: standardFailure.status,
                statusText: standardFailure.statusText
            });
        });
    }

    function sharedEnvironmentForPromiseCancellation(name) {
        describe(name, function() {
            beforeEach(function() {
                this.promise.cancel();
            });

            it('the promise fails with status \'abort\'', function() {
                var spy = jasmine.createSpy();
                this.promise.fail(spy);
                expect(spy).toHaveBeenCalledWith({
                    status: 0,
                    statusText: 'abort'
                });
            });
        });
    }

    function defineJsonResponse(data) {
        beforeEach(function() {
            this.mostRecentRequest().respondWith({
                status: 200,
                contentType: 'application/json',
                responseText: JSON.stringify(data)
            });
        });
    }

    beforeEach(function () {
        jasmine.Ajax.install();
        jasmine.clock().install();
        this.mostRecentRequest = function() {
            return jasmine.Ajax.requests.mostRecent();
        }
    });

    afterEach(function () {
        jasmine.Ajax.uninstall();
        jasmine.clock().uninstall();
    });

    describe('call startSession', function() {
        beforeEach(function() {
            this.promise = ajaxServices.startSession();
        });

        it('launches a request', function() {
            expect(this.mostRecentRequest()).toBeDefined();
        });

        it('goes to the right URL', function() {
            expect(this.mostRecentRequest().url)
                .toBe(BASE_PATH + '/RSession/create');
        });

        it('send the right request data', function() {
            expect(this.mostRecentRequest().method).toBe('POST');
            expect(this.mostRecentRequest().data().workflow).toBe(TEST_WORKFLOW);
        });

        describe('the ajax call returning success', function() {
            var sessionId = '80f29282-efa3-4c35-bc2d-27c81f91d67f';

            beforeEach(function() {
                this.mostRecentRequest().respondWith({
                    status: 201,
                    contentType: 'application/json',
                    responseText: '{"sessionId":"' + sessionId + '"}'
                });
            });

            it('returns the correct session id', function() {
                var spy = jasmine.createSpy();
                this.promise.done(spy);
                expect(spy).toHaveBeenCalledWith(sessionId);
            });
        });

        describe('the ajax call failing', function() {
            beforeEach(function() {
                this.mostRecentRequest().respondWith(standardFailure);
            });

            sharedExampleForPromiseFailure('returns the correct session id');
        });
    });

    describe('call startScriptExecution', function() {
        var taskData = {
            arguments: { arg1: 'arg1 value' },
            taskType: 'sampleTaskType',
            phase: 'run'
        };

        beforeEach(function() {
            this.promise = ajaxServices.startScriptExecution(taskData);
        });

        it('launches a request', function() {
            expect(this.mostRecentRequest()).toBeDefined();
        });

        it('goes to the right URL', function() {
            expect(this.mostRecentRequest().url)
                .toBe(BASE_PATH + '/ScriptExecution/run');
        });

        it('contains matching payload', function() {
            expect(this.mostRecentRequest().data().arguments).toEqual(taskData.arguments);
            expect(this.mostRecentRequest().data().workflow).toBe(TEST_WORKFLOW);
            expect(this.mostRecentRequest().data().taskType).toBe(taskData.taskType);
        });

        describe('the initial call fails', function() {
            beforeEach(function() {
                this.mostRecentRequest().respondWith(standardFailure);
            });

            sharedExampleForPromiseFailure('returns the error the server returned');
        });

        sharedEnvironmentForPromiseCancellation('the initial call is cancelled');

        describe('another call to startScriptExecution is made', function() {
            beforeEach(function() {
                ajaxServices.startScriptExecution(taskData);
            });

            it('the first promise reports abortion', function() {
                var spy = jasmine.createSpy();
                this.promise.fail(spy);
                expect(spy).toHaveBeenCalledWith({
                    status: 0,
                    statusText: 'abort'
                });
            });
        });

        describe('the initial call succeeds', function() {
            var executionId = '27480883-9019-401b-9035-48ca4e87aa18';
            var response = { executionId: executionId };

            defineJsonResponse(response);

            it('there is a second call', function() {
                expect(jasmine.Ajax.requests.count()).toBe(2);
            });

            it('the second call is to /status', function() {
                var url = this.mostRecentRequest().url.split('?')[0];
                expect(url).toBe(BASE_PATH + '/ScriptExecution/status');
            });

            it('the call to /status includes the correct executionId', function() {
                var params = this.mostRecentRequest().url.split('?')[1].split('&');
                expect(params).toContain('executionId=' + executionId);
            });

            describe('the first call to /status fails', function() {
                beforeEach(function() {
                    this.mostRecentRequest().respondWith(standardFailure);
                });

                sharedExampleForPromiseFailure('the promise returns the error status');
            });

            describe('the first call to /status returns FAILED', function() {
                var response = {
                    state: 'FAILED',
                    result: {
                        successful: false,
                        exception: 'Sample error during execution'
                    }
                };

                defineJsonResponse(response);

                it('the promise returns failure', function() {
                    var spy = jasmine.createSpy();
                    this.promise.fail(spy);
                    expect(spy).toHaveBeenCalledWith({
                        status: 0,
                        statusText: response.result.exception
                    });
                });
            });

            sharedEnvironmentForPromiseCancellation(
                'the first call to /status is cancelled');

            describe('the first call to /status returns FINISHED', function() {
                var response = {
                    state: 'FINISHED',
                    result: {
                        successful: true,
                        artifacts: ['foo']
                    }
                };

                defineJsonResponse(response);

                it('returns the status response data', function() {
                    var spy = jasmine.createSpy();
                    this.promise.done(spy);
                    expect(spy).toHaveBeenCalledWith(response);
                });
            });

            describe('the first call to /status returns RUNNING', function() {
                var response = {
                    status: 'RUNNING'
                };

                defineJsonResponse(response);

                describe('the second call to /status is scheduled', function() {
                    beforeEach(function() {
                        jasmine.clock().tick(200 /* < 1000 */);
                    });

                    sharedEnvironmentForPromiseCancellation(
                        'the second call /status is unscheduled');
                });

                it('makes a second call to /status 1 second later', function() {
                    expect(jasmine.Ajax.requests.count()).toBe(2);
                    jasmine.clock().tick(1001);
                    expect(jasmine.Ajax.requests.count()).toBe(3);

                    var url = this.mostRecentRequest().url.split('?')[0];
                    expect(url).toBe(BASE_PATH + '/ScriptExecution/status');
                });

                describe('the second call to /status is underway', function() {
                    beforeEach(function() {
                        jasmine.clock().tick(1001);
                    });

                    sharedEnvironmentForPromiseCancellation(
                        'the second call /status is cancelled');
                });

                describe('the second call to /status returns FAILED', function() {
                    var response = {
                        state: 'FAILED',
                        result: {
                            successful: false,
                            exception: 'Sample error during execution'
                        }
                    };

                    beforeEach(function() {
                        jasmine.clock().tick(1001);
                    });

                    defineJsonResponse(response);

                    it('the promise returns failure', function() {
                        var spy = jasmine.createSpy();
                        this.promise.fail(spy);
                        expect(spy).toHaveBeenCalledWith({
                            status: 0,
                            statusText: response.result.exception
                        });
                    });
                });

                describe('the second call to /status returns FINISHED', function() {
                    var response = {
                        state: 'FINISHED',
                        result: {
                            successful: true,
                            artifacts: ['foo']
                        }
                    };

                    beforeEach(function() {
                        jasmine.clock().tick(1001);
                    });

                    defineJsonResponse(response);

                    it('returns the status response data', function() {
                        var spy = jasmine.createSpy();
                        this.promise.done(spy);
                        expect(spy).toHaveBeenCalledWith(response);
                    });
                });
            });
        });
    });
});
