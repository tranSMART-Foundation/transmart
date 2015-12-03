//# sourceURL=heatmapService.js

/**
 * Heatmap Service
 */

HeatmapService = (function(smartRHeatmap){

    var service = {
        statusInterval : 0
    };

    /**
     *
     * @param files
     * @returns {{}}
     * @private
     */
    var _getSummaryFiles = function (files) {
        var retv = {}, types = ['png', 'json'];

        types.forEach(function (type) {
            retv[type] = files.filter(function (file) {
                console.log('type',type);
                console.log('file',file);
                console.log('file.indexOf(type)',file.indexOf(type));
                return file.indexOf(type) < 0  ? null : file;
            });
        });

        return retv;
    };

    /**
     *
     * @param elId
     * @returns {string}
     */
    service.readConceptVariables = function (el) {
        var retval = {}, elDOM = el[0];
        for (var i=0; i<elDOM.children.length; i++) {
            console.log(elDOM.children[i].getAttribute('conceptid'));
            retval['n' + i] = elDOM.children[i].getAttribute('conceptid');
        }
        return retval;
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
            timeout: '600000',
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

    /**
     * To get summary loaded data (at the moment for fetched data)
     * @param params
     * @returns {*}
     */
    service.getSummary = function (params) {
        jQuery.ajax({
            type: 'POST',
            url: pageInfo.basePath + '/ScriptExecution/run',
            data: JSON.stringify({
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                arguments : params, // todo add params
                taskType : 'summary'}
            ),
            contentType: 'application/json'
        })
            .done(function (data) {
                console.log(data);
                //var scriptExecObj = JSON.parse(data.responseText);
                GLOBAL.HeimAnalyses.executionId = data.executionId;
                console.log(GLOBAL.HeimAnalyses);
                service.statusInterval =  setInterval(function () {
                    service.checkStatus('summary', params.phase);
                }, 1000);
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            });
    };

    /**
     * Fetch data
     * @param eventObj
     */
    service.fetchData = function (params) {

        /**
         * Create fetch data constraints
         * @param params
         * @returns
         * {{conceptKeys: {_TEST_LABEL_: _conceptPath}, dataType: string, resultInstanceIds: *, projection: string}}
         * @private
         */
        var _createAnalysisConstraints = function (params) {
            var _retval = {
                conceptKeys : {
                    // TODO: support more than one concept path
                    '_TEST_LABEL_': params.conceptPaths
                },
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

        jQuery.ajax({
            url: pageInfo.basePath + '/ScriptExecution/run',
            type: 'POST',
            timeout: '600000',
            contentType: 'application/json',
            data: JSON.stringify({
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                arguments : _createAnalysisConstraints(params),
                taskType : 'fetchData',
                workflow : 'heatmap'
            })
        }).done(function (d) {
            // when it's done :
            // -  store execution id in global
            GLOBAL.HeimAnalyses.executionId = d.executionId;
            console.log(GLOBAL.HeimAnalyses);
            // - check fetching data status
            service.statusInterval =  setInterval(function () {
                service.checkStatus('fetchData', 'fetch');
            }, 1000);
        })
            .fail(function (jqXHR, textStatus, errorThrown) {
                var _err = JSON.parse(jqXHR.responseText);
                console.error(jqXHR);
                console.error(textStatus);
                console.error(errorThrown);
                jQuery('#heim-fetch-output')
                    .html('<p style="color: red";><b>Error:'+ errorThrown +'</b> <br> ' + _err.message + '</p>');
            });
    };

    /**
     * Preprocess service
     * @param params
     */
    service.preprocess = function (params) {

        console.log('service.preprocess', params);

        jQuery.ajax({
            type: 'POST',
            url: pageInfo.basePath + '/ScriptExecution/run',
            data: JSON.stringify({
                    sessionId : GLOBAL.HeimAnalyses.sessionId,
                    arguments : params,
                    taskType : 'preprocess'}
            ),
            contentType: 'application/json'
        })
            .done(function (d) {
                console.log(d);
                GLOBAL.HeimAnalyses.executionId = d.executionId;
                console.log(GLOBAL.HeimAnalyses);
                service.statusInterval =  setInterval(function () {
                    service.checkStatus('preprocess', 'preprocess');
                }, 1000);
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                var _err = JSON.parse(jqXHR.responseText);
                console.error(jqXHR);
                console.error(textStatus);
                console.error(errorThrown);
                jQuery('#heim-preprocess-output')
                    .html('<p style="color: red";><b>Error:'+ errorThrown +'</b> <br> ' + _err.message + '</p>');
            });

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
                console.log('Cancelling pending request');
                curXHR.abort();
            }

            curXHR = jQuery.get("/transmart/search/loadSearchPathways", {
                query: term
            });

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

    service.displaySummary = function (files, task, phase) {

        // empty output area
        var _outputArea =  jQuery('#heim-'+phase+'-output');
        _outputArea.empty();

        // display plots
        files.png.forEach(function (filename) {
            var _plot = jQuery('<img>')
                .attr('src', pageInfo.basePath
                + '/ScriptExecution/downloadFile?sessionId='
                + GLOBAL.HeimAnalyses.sessionId
                + '&executionId='
                + GLOBAL.HeimAnalyses.executionId
                + '&filename=' + filename);
            _outputArea.append(_plot);
        });

        // display summary
        files.json.forEach(function (filename) {
            jQuery.ajax({
                url : pageInfo.basePath
                + '/ScriptExecution/downloadFile?sessionId='
                + GLOBAL.HeimAnalyses.sessionId
                + '&executionId='
                + GLOBAL.HeimAnalyses.executionId
                + '&filename='
                + filename,
                dataType : 'json'
            })
                .done(function (d, status, jqXHR) {
                    console.log(d);
                    console.log(status);
                    console.log( jqXHR);
                    var _summaryObj = service.generateSummaryTable(d,  task, phase);
                    _outputArea
                        .append(_summaryObj);

                });
        });
    };

    service.downloadHeatmapJSON = function (task, phase) {
        var _retval = {};
        return new Promise (function(resolve, reject) {
            jQuery.ajax({
                url : pageInfo.basePath
                + '/ScriptExecution/downloadFile?sessionId='
                + GLOBAL.HeimAnalyses.sessionId
                + '&executionId='
                + GLOBAL.HeimAnalyses.executionId
                + '&filename=heatmap.json',
                dataType : 'json'
            })
                .done(function (d, status, jqXHR) {
                    console.log(d);
                    resolve(d);
                })
                .fail(function (jqXHR, textStatus, errorThrown) {
                    reject(errorThrown)
                });
            return _retval;
        });

    };

    /**
     * Check status of a task
     * TODO: Refactor
     * @param task
     */
    service.checkStatus = function (task, phase) {

        var _displayLoading = function (task, phase) {
            jQuery('#heim-' + phase + '-output')
                .html('<p class="sr-log-text">Executing ' + task
                + ' job, please wait <span class="blink_me">_</span></p>');
        };

        var _displayError = function (task, phase, d) {
            console.log('err', {t:task,  p:phase,  d:d});
            var _errTxt = d.hasOwnProperty('result') ? d.result.exception : d;
            jQuery('#heim-' + phase + '-output').html('<span style="color: red";>' + _errTxt +'</span>');
        };

        _displayLoading(task, phase);

        jQuery.ajax({
            type : 'GET',
            url : pageInfo.basePath + '/ScriptExecution/status',
            data : {
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                executionId : GLOBAL.HeimAnalyses.executionId
            }
        })
        .done(function (d) {
                console.log('Done checking', d);
                if (d.state === 'FINISHED') {
                    clearInterval(service.statusInterval);
                    console.log('Okay, I am finished checking now ..', d);

                    if (task === 'fetchData' || task === 'preprocess') {
                        // get summary
                        service.getSummary({phase:phase});
                    } else if (task === 'runHeatmap') {
                        // load JSON result to create  d3 heatmap
                        service.downloadHeatmapJSON(task, phase)
                            .then(function (d) {
                                jQuery('#heim-'+phase+'-output').hide();
                                smartRHeatmap.create(d);
                            })
                            .catch(function (err) {
                                _displayError(task, phase,  err);
                            });
                    } else if (task === 'summary') {
                        console.log('summary');
                        var _files = _getSummaryFiles(d.result.artifacts.files);
                        console.log(_files);
                        service.displaySummary(_files, task, phase);
                    }
                } else if (d.state === 'FAILED') {
                    console.error('FAILED', d.result);
                    clearInterval(service.statusInterval); // stop checking backend
                    _displayError(task, phase, d);
                }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
                clearInterval(service.statusInterval); // stop checking backend
                _displayError(task, phase, d);
        })
        .always(function () {
            console.log('checked!');
        });
    };

    service.generateSummaryTable = function (data, task, phase) {
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
                    key:key,
                    val1:(typeof data[0] === 'undefined') ? '-' : data[0][key],
                    val2:(typeof data[1] === 'undefined') ? '-' : data[1][key]
                });
            }
        }
        // return and render
        return rowTemplate.render(_summaryObj);
    };

    service.runAnalysis = function (params) {
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
                var scriptExecObj = JSON.parse(data.responseText);
                GLOBAL.HeimAnalyses.executionId = scriptExecObj.executionId;
                console.log(GLOBAL.HeimAnalyses);
                service.statusInterval =  setInterval(function () {
                    service.checkStatus('runHeatmap', 'run');
                }, 1000);
            }
        });
    };

    return service;
})(SmartRHeatmap);
