
window.smartRApp.factory('rServeService', ['smartRUtils', '$q', '$http', function(smartRUtils, $q, $http) {

    var service = {};

    var NOOP_ABORT = function() {};
    var TIMEOUT = 10000 /* 10 s */;
    var CHECK_DELAY = 1000;
    var SESSION_TOUCH_DELAY = 9 * 60 * 1000; /* 9 min; session timeout is 10 */

    /* we only support one session at a time */

    var state = {
        currentRequestAbort: NOOP_ABORT,
        sessionId: null,
        touchTimeout: null // for current session id
    };

    var workflow = '';
    /* returns a promise with the session id and
     * saves the session id for future calls */
    service.startSession = function rServeService_startSession(name) {
        workflow = name;
        return $.ajax({
                url: pageInfo.basePath + '/RSession/create',
                type: 'POST',
                timeout: TIMEOUT,
                contentType: 'application/json',
                data: JSON.stringify({
                    workflow: workflow
                })
            })
            .then(function(response) {
                return response.sessionId;
            }, transformAjaxFailure)
            .done((function(sessionId) {
                state.sessionId = sessionId;
                rServeService_scheduleTouch.call(this);
            }).bind(this));
    };

    service.touch = function rServeService_touch(sessionId) {
        if (sessionId != state.sessionId) {
            return;
        }

        $.ajax({
            url: pageInfo.basePath + '/RSession/touch',
            type: 'POST',
            timeout: TIMEOUT,
            contentType: 'application/json',
            data: JSON.stringify( {
                sessionId: sessionId
            })
        }).done(rServeService_scheduleTouch.bind(this)); // schedule another
    };

    function rServeService_scheduleTouch() {
        var sessionId = state.sessionId;
        window.clearTimeout(state.touchTimeout);
        state.touchTimeout = window.setTimeout(function() {
            service.touch(sessionId);
        }, SESSION_TOUCH_DELAY);
    }

    service.destroySession = function rServeService_destroySession(sessionId) {
        sessionId = sessionId || state.sessionId;

        if (!sessionId) {
            throw new Error('No session to destroy');
        }

        return $.ajax({
                url: pageInfo.basePath + '/RSession/delete',
                type: 'POST',
                timeout: TIMEOUT,
                contentType: 'application/json',
                data: JSON.stringify( {
                    sessionId: sessionId
                })
            })
            .then(function(x) { return x; }, transformAjaxFailure)
            .done(function() {
                if (state.sessionId != sessionId) {
                    return;
                }
                this.abandonCurrentSession();
            }.bind(this));
    };

    service.abandonCurrentSession = function rServeService_abandonSession() {
        window.clearTimeout(state.touchTimeout);
        state.sessionId = null;
    };

    /*
     * taskData = {
     *     arguments: { ... },
     *     taskType: 'fetchData' or name of R script minus .R,
     *     phase: 'fetch' | 'preprocess' | 'run',
     * }
     *
     * If it succeeds, the result will be the data returned by the server.
     * If it fails, it will return an object with at least these fields:
     * {
     *   status: 0 | <http error code>,
     *   statusText: <a description of the error>,
     * }
     */
    service.startScriptExecution = function rServeService_startScriptExecution(taskDataOrig) {

        var taskData = $.extend({}, taskDataOrig); // clone the thing
        state.currentRequestAbort();

        var runRequest = $http({
            url: pageInfo.basePath + '/ScriptExecution/run',
            method: 'POST',
            timeout: TIMEOUT,
            responseType: 'json',
            data: JSON.stringify({
                sessionId: state.sessionId,
                arguments: taskData.arguments,
                taskType: taskData.taskType,
                workflow: workflow
            })
        });

        _setCancellationForAjaxCall(runRequest);

        /* schedule checks */
        var promise = runRequest.then(
            function(response) {
                taskData.executionId = response.data.executionId;
                return _checkStatus(taskData.executionId, CHECK_DELAY);
            },
            transformAjaxFailure
        );

        promise.cancel = function timeoutRequest_cancel() {
            // calling this method should by itself resolve the promise
            state.currentRequestAbort();
        };

        // no touching necessary when a task is running
        window.clearTimeout(state.touchTimeout);
        promise.finally(rServeService_scheduleTouch.bind(this));

        return promise;
    };

    function _setCancellationForAjaxCall(ajax) {
        /* request in-flight; aborting is cancelling this request */
        state.currentRequestAbort = function() { ajax.abort(); };
        /* once the request finishes, there's nothing to abort.
         * this needs to be the 1st callback, so that later callbacks can
         * override this */
        ajax.finally(function() {
            state.currentRequestAbort = NOOP_ABORT;
        });
    }

    /* set a function to be executed after a period of time and
     * simultaneously define the request cancellation function
     * as the cancellation of this timeout.
     * Return the result as a promise. */
    function _setStatusRequestTimeout(funcToCall, delay /*, ... */) {
        var defer = $.Deferred();
        var promise = defer.promise();
        var restOfArguments = Array.prototype.slice.call(arguments)
            .splice(2); // arguments after delay

        function _setStatusRequestTimeout_wrappedFunc() {
            // we cannot abort by calling clearTimeout() anymore
            // funcToCall will probably set its own abort method
            state.currentRequestAbort = NOOP_ABORT;

            // funcToCall should itself return a promise or a final result
            // but let's cover the case where it throws
            var result;
            try {
                result = funcToCall.apply(undefined, restOfArguments); // promise
                result.done(function(x) { defer.resolve(x); });
                result.fail(function(x) { defer.reject(x); });
            } catch (e) {
                defer.fail(e);
            }
        }

        var timeout = window.setTimeout(
            _setStatusRequestTimeout_wrappedFunc, delay);

        state.currentRequestAbort = function() {
            clearTimeout(timeout);
            if (defer.state() == 'pending') {
                defer.reject({
                    status: 0,
                    statusText: 'abort'
                });
            }
            state.currentRequestAbort = NOOP_ABORT;
        };

        return promise;
    }

    /* aux function of _startScriptExecution. Needs to follow its contract
     * with respect to the fail and success result of the promise */
    function _checkStatus(executionId, delay) {
        var ajax = $.ajax({
            type: 'GET',
            url : pageInfo.basePath + '/ScriptExecution/status',
            data: {
                sessionId  : state.sessionId,
                executionId: executionId
            }
        });

        state.currentRequestAbort = function() { ajax.abort(); };
        ajax.always(function() { state.currentRequestAbort = NOOP_ABORT; });

        return ajax.then(function (d) {
            if (d.state === 'FINISHED') {
                d.executionId = executionId;
                return d;
            } else if (d.state === 'FAILED') {
                return $.Deferred().reject({
                    status: 0,
                    statusText: d.result.exception
                }).promise();
            } else {
                // else still pending
                return _setStatusRequestTimeout(
                    _checkStatus, delay, executionId, delay);
            }
        }, transformAjaxFailure);
    }

    function transformAjaxFailure(jqXHR, textStatus, errorThrown) {
        var ret = {
            status: jqXHR.status,
            statusText: jqXHR.statusText
        };
        if (jqXHR.responseJSON !== undefined) {
            ret.response = jqXHR.responseJSON;
        }

        return ret;
    }

    service.downloadJsonFile = function(executionId, filename) {
        return $.ajax({
            url: this.urlForFile(executionId, filename),
            dataType: 'json'
        });
    };


    service.urlForFile = function rServeService_urlForFile(executionId, filename) {
        return pageInfo.basePath +
            '/ScriptExecution/downloadFile?sessionId=' +
            state.sessionId +
            '&executionId=' +
            executionId +
            '&filename=' +
            filename;
    };

    service.loadDataIntoSession = function rServeService_loadDataIntoSession(conceptKeys) {
        return $q(function(resolve, reject) {
            smartRUtils.getSubsetIds().then(
                function(subsets) {
                    service.startScriptExecution({
                        taskType: 'fetchData',
                        arguments: {
                            conceptKeys: conceptKeys,
                            resultInstanceIds: subsets
                        }
                    }).then(
                        function(ret) { resolve('Task complete! State: ' + ret.state); },
                        function(ret) { reject(ret.response); }
                    );
                },
                function() {
                    reject('Could not create subsets. Did you select a cohort?');
                }
            );
        });
    };

    return service;
}]);
