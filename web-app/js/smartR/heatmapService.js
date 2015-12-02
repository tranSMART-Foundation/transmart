//# sourceURL=heatmapService.js

/**
 * Heatmap Service
 */

HeatmapService = (function(smartRHeatmap){
    var CHECK_DELAY = 1000
    var NOOP_ABORT = function() {};

    var service = {
        currentRequestAbort: NOOP_ABORT,
        lastFetchedLabels: [],
    };

    var _setStatusRequestTimeout = function() {
        var timeout = setTimeout.apply(undefined, arguments)
        service.currentRequestAbort = function() {
            clearTimeout(timeout);
            service.currentRequestAbort = NOOP_ABORT;
        }
    }

    /* generate unique labels for the concept paths. It recursively resolves
     * clashes. */
    var _generateLabels = (function() {
        function extractLastComponents(key, keepN) {
            return key.replace(/\\$/, '') // replace away optional trailing /
                .split(/\\/)
                .slice(2) // remove start of concept key
                .reverse()
                .slice(0, keepN)
                .reverse()
                .join('\\');
        }
        function p(o) {
            var unfinishedKeys = Object.keys(o)
                .filter(function(k) { return o[k].elements.length > 1; });

            if (unfinishedKeys.length === 0) {
                return true;
            }

            unfinishedKeys.forEach(function(k) {
                var entry = o[k];
                var keepN = entry.keep + 1;
                delete o[k];
                entry.elements.forEach(function (el) {
                    var newKey = extractLastComponents(el, keepN);
                    if (o[newKey]) {
                        o[newKey].elements.push(el);
                    } else {
                        o[newKey] = {
                            keep: keepN,
                            elements: [el]
                        };
                    }
                });
            })

            return p(o);
        }
        function throwIfDuplicates(arr) {
            var repeated =  arr.filter(function(el, index) {
                return arr.indexOf(el) !== index;
            });
            if (repeated.length > 0) {
                var error = new Error(
                    "Duplicate concept keys: " + repeated, 'dups');
                throw error;
            }
        }

        return function _generateLabels(arr) {
            throwIfDuplicates(arr);
            var o = ({
                '': {
                    keep: 0,
                    elements: arr,
                }
            });
            p(o);
            Object.keys(o).forEach(function(key) {
                o[key] = o[key].elements[0];
            });
            return o;
        }
    })();

    var _createAnalysisConstraints = function (params) {
        console.log(params);
        // params.conceptPaths are actually keys...
        var _retval = {
            conceptKeys : _generateLabels(params.conceptPaths.split(/\|/)),
            resultInstanceIds: params.resultInstanceIds,
            projection: 'log_intensity'
        };
        if (params['searchKeywordIds'].length > 0) {
            _retval.dataConstraints = {
                search_keyword_ids: {
                    keyword_ids: params['searchKeywordIds']
                }
            }
        }
        return  _retval;
    };

    /**
     * Create r-session id
     * @returns {*}
     */
    service.initialize = function () {
        // ajax call to session creation
        jQuery.ajax({
            url: pageInfo.basePath + '/RSession/create',
            type: 'POST',
            timeout: '30000',
            contentType: 'application/json',
            data : JSON.stringify( {
                workflow : 'heatmap'
            })
        }).done(function(response) {
            GLOBAL.HeimAnalyses = {
                type : 'heatmap',
                sessionId :response.sessionId
            };
            return GLOBAL.HeimAnalyses;
        }).fail(function() {
            // TODO: error displayed in a placeholder somewhere in main heim-analysis page
            console.error('Cannot create r-session');
            return null;
        });
    };

    /* TaskData
     * {
     *   taskType: (string),
     *   arguments: (object)
     *   executionId: (uuid),
     *   onUltimateSuccess: function (data) {},
     *   phase: (string) allows finding div id,
     *   progressMessage: (string),
     *   successMessage: (string)
     * }
     */

    var _currentTaskData;

    var _divForPhase = function(phase) {
        return jQuery('#heim-' + phase + '-output');
    }

    var startScriptExecution = function(taskData) {
        service.currentRequestAbort();

        var runRequest = jQuery.ajax({
            url: pageInfo.basePath + '/ScriptExecution/run',
            type: 'POST',
            timeout: '30000',
            contentType: 'application/json',
            data: JSON.stringify({
                sessionId: GLOBAL.HeimAnalyses.sessionId,
                arguments: taskData.arguments,
                taskType : taskData.taskType,
                workflow : 'heatmap'
            })
        }).fail(function (jqXHR, textStatus, errorThrown) {
            var _err = JSON.parse(jqXHR.responseText);
            console.error(jqXHR);
            console.error(textStatus);
            console.error(errorThrown);
            // FIXME: should not write to this place
            _divForPhase(taskData.phase)
                .html('<p style="color: red";><b>Error:'+ errorThrown +'</b> <br> ' + _err.message + '</p>');
        }).done(function(d) {
            taskData.executionId = d.executionId;
            service.checkStatus(taskData, CHECK_DELAY);
        });

        service.currentRequestAbort = function() { runRequest.abort(); };

        return runRequest;
    };

    var urlForFile = function(executionId, filename) {
        return pageInfo.basePath +
            '/ScriptExecution/downloadFile?sessionId=' +
            GLOBAL.HeimAnalyses.sessionId +
            '&executionId=' +
            executionId +
            '&filename=' +
            filename;
    }
    var downloadJsonFile = function(executionId, filename) {
        return jQuery.ajax({
            url: urlForFile(executionId, filename),
            dataType: 'json'
        });
    }

    /**
     * Fetch dat
     * @param eventObj
     */
    service.fetchData = function (params) {
        var _args = _createAnalysisConstraints(params);
        service.lastFetchedLabels = Object.keys(_args.conceptKeys);

        console.log('Analysis Constraints', _args);

        startScriptExecution({
            taskType: 'fetchData',
            arguments: _args,
            onUltimateSuccess: function (data) { service.getSummary(); },
            phase: 'fetch',
            progressMessage: 'Fetching data',
            successMessage: 'Data is successfully fetched in . Proceed with Run Heatmap',
        });
    };

    service.getSummary = function () {
        console.log('About to get load data summary');

        function getSummary_onUltimateSuccess(data) {
            var div = _divForPhase(this.phase);
            div.empty();
            service.lastFetchedLabels.forEach(function(label) {
                var filename = urlForFile(this.executionId, 'box_plot_node' + label + '.png');
                var plot = jQuery('<img>').attr('src', filename);
                div.append(plot);
            }.bind(this));
        
            jQuery.when.apply(jQuery,
                service.lastFetchedLabels.map(function (label) {
                    return downloadJsonFile(
                        this.executionId,
                        'summary_stats_node' + label + '.json');
                }.bind(this))
            ).done(function() {
                Array.prototype.forEach.call(arguments, function(ajaxCbArgs) {
                    var data = ajaxCbArgs[0];
                    var _summaryObj = service.generateSummaryTable(data);
                    div.append(_summaryObj);
                });
                div.show();
            });
        }

        startScriptExecution({
            taskType: 'summary',
            arguments: {},
            onUltimateSuccess: getSummary_onUltimateSuccess,
            phase: 'fetch',
            progressMessage: 'Getting summary',
            successMessage: undefined,
        });
    };

    /**
     * Preprocess service
     * @param params
     */
    service.preprocess = function (params) {
        console.log('service.preprocess', params);

        startScriptExecution({
            taskType: 'preprocess',
            arguments: params,
            onUltimateSuccess: function (data, taskData) { service.getSummary(); },
            phase: 'preprocess',
            progressMessage: 'Preprocessing'
        });
    };

    service.runAnalysis = function (params) {
        console.log('service.runAnalysis', params);

        function showD3HeatMap(data, taskData) {
            downloadJsonFile(taskData.executionId, 'heatmap.json')
                    .then(smartRHeatmap.create(d));
        }

        startScriptExecution({
            taskType: 'runHeatmap',
            arguments: params,
            onUltimateSuccess: showD3HeatMap,
            phase: 'run',
            progressMessage: 'Calculating',
            successMessage: undefined,

        });

        jQuery.ajax({
            type: 'POST',
            url: pageInfo.basePath + '/ScriptExecution/run',
            data: JSON.stringify({
                    sessionId : GLOBAL.HeimAnalyses.sessionId,
                    arguments : params,
                    taskType : 'run'}
            ),
            contentType: 'application/json',
            complete: function(data) {
                GLOBAL.HeimAnalyses.executionId = scriptExecObj.executionId;
            }
        });
    };

    service.generateSummaryTable = function (data) {
        // get template
        var rowTemplate = jQuery.templates('#summary-row-tmp');

        // initiate summary obj
        var _summaryObj = {summaryStat : []};

        // return null when there's no data from both subsets defined
        if (typeof data[0] === 'undefined' && typeof data[0] === 'undefined')
            return null;
        // use any available data
        var _data = typeof data[0] === 'undefined' ? data[1] : data[0];

        for (var key in _data) {
            if (_data.hasOwnProperty(key)) {
                _summaryObj.summaryStat.push({
                    key: key,
                    val1: (typeof data[0] === 'undefined') ? '-' : data[0][key],
                    val2: (typeof data[1] === 'undefined') ? '-' : data[1][key]
                });
            }
        }
        // return and render
        return rowTemplate.render(_summaryObj);
    };

    /**
     *
     * @param request
     * @param response
     */
    service.getIdentifierSuggestions = (function() {
        var curXHR = null;

        return function(model, term, response) {
            if (curXHR && curXHR.state() === 'pending') {
                console.log('Cancelling pending request')
                curXHR.abort();
            }

            curXHR = jQuery.get("/transmart/search/loadSearchPathways", {
                query: term
            })

            curXHR.always(function() { curXHR = null; })
            return curXHR.then(
                function(data) {
                    data = data.substring(5, data.length - 1);  // loadSearchPathways returns String with null (JSON).
                                                                // This strips it off
                    response(JSON.parse(data));
                },
                function() {
                    response({rows: []}); // response must be called even on failure
                }
            );
        };
    })();

    /**
     * Check status of a task
     * @param task
     */
    service.checkStatus = function(taskData, delay) {
        var div = _divForPhase(taskData.phase)
        div.show();
        div.html('<p class="sr-log-text"><span class="blink_me">_</span>' +
            taskData.progressMessage + ', please wait\u2026</p>')

        service.currentRequestAbort();

        var ajax = jQuery.ajax({
            type: 'GET',
            url : pageInfo.basePath + '/ScriptExecution/status',
            data: {
                sessionId  : GLOBAL.HeimAnalyses.sessionId,
                executionId: taskData.executionId
            }
        });

        service.currentRequestAbort = function() { ajax.abort(); };

        ajax.done(function (d) {
            console.log('Done checking', d);

            if (d.state === 'FINISHED') {
                if (taskData.successMessage) {
                    var _html = '<p class="heim-fetch-success" style="color: green";> ' +
                        taskData.successMessage + '</p>';
                    div.html(_html);
                } else {
                    div.hide();
                }

                taskData.onUltimateSuccess(d);
            } else if (d.state === 'FAILED') {
                var _errHTML = '<span style="color: red";>' + d.result.exception +'</span>';
                div.html(_errHTML);
                console.error('FAILED', d.result);
            } else {
                _setStatusRequestTimeout(service.checkStatus, delay, taskData, delay);
            }
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            var _html = '<span style="color: red";>'+errorThrown+'</span>';
            console.log(jqXHR);
            console.log(textStatus);
            console.log(errorThrown);
            div.html(_html);
        });
    };

    return service;
})(SmartRHeatmap);
