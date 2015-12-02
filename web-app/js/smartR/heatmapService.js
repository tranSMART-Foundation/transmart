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

    var _fetchConceptPath = function (el) {
        return el.getAttribute('conceptId').trim();
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

    var _createAnalysisConstraints = function (params) {
        console.log(params);
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
        var retval;

        console.log('About to get load data summary');

        jQuery.ajax({
            type: 'POST',
            url: pageInfo.basePath + '/ScriptExecution/run',
            data: JSON.stringify({
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                arguments : {},
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
                    service.checkStatus('getSummary');
                }, 1000);
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            });
        return retval;
    };

    /**
     * Fetch data
     * @param eventObj
     */
    service.fetchData = function (params) {
        var _args = _createAnalysisConstraints(params);
        console.log('Analysis Constraints', _args);

        jQuery.ajax({
            url: pageInfo.basePath + '/ScriptExecution/run',
            type: 'POST',
            timeout: '600000',
            contentType: 'application/json',
            data: JSON.stringify({
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                arguments : _args,
                taskType : 'fetchData',
                workflow : 'heatmap'
            })
        }).done(function (d) {
            console.log(d);
            //var scriptExecObj = JSON.parse(d.responseText);
            GLOBAL.HeimAnalyses.executionId = d.executionId;
            console.log(GLOBAL.HeimAnalyses);
            service.statusInterval =  setInterval(function () {
                service.checkStatus('fetchData');
            }, 1000);
        })
            .fail(function (jqXHR, textStatus, errorThrown) {
                var _err = JSON.parse(jqXHR.responseText);
                console.error(jqXHR);
                console.error(textStatus);
                console.error(errorThrown);
                jQuery('#heim-fetch-data-output')
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
                    service.checkStatus('preprocess');
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
     * TODO: Refactor
     * @param task
     */
    service.checkStatus = function (task) {
        if (task === 'fetchData') {
            jQuery('#heim-fetch-data-output').html('<p class="sr-log-text"><span class="blink_me">_</span>Fetching data, please wait ..</p>');
        } else if (task === 'preprocess') {
            jQuery('#heim-preprocess-output').show();
            jQuery('#heim-preprocess-output').html('<p class="sr-log-text"><span class="blink_me">_</span>Preprocessing, please wait ..</p>');
        } else if (task === 'runHeatmap') {
            jQuery('#heim-run-output').show();
            jQuery('#heim-run-output').html('<p class="sr-log-text"><span class="blink_me">_</span>Calculating, please wait ..</p>');
        } else if (task === 'getSummary') {
            jQuery('#heim-fetch-data-output').html('<p class="sr-log-text"><span class="blink_me">_</span>Getting summary, please wait ..</p>');
        }

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

                    if (task === 'fetchData') {
                        jQuery('#heim-fetch-data-output')
                            .html('<p class="heim-fetch-success" style="color: green";> ' +
                            'Data is successfully fetched in . Proceed with Run Heatmap</p>');

                        // render summary stat
                        service.getSummary();
                    } else if (task === 'preprocess') {
                        jQuery('#heim-preprocess-output')
                            .html('<p class="heim-fetch-success" style="color: green";> ' +
                            'Preprocessed completed successfully.</p>');

                        // TODO Render summary stat
                        //service.getSummary();

                    } else if (task === 'runHeatmap') {
                        jQuery('#heim-run-output').hide();
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
                                smartRHeatmap.create(d);
                            });
                    } else if (task === 'getSummary') {

                        console.log('getSummary');

                        jQuery('#heim-run-output').hide();
                        var _files = _getSummaryFiles(d.result.artifacts.files);

                        jQuery('#heim-fetch-data-output').hide();
                        jQuery.ajax({
                            url : pageInfo.basePath
                            + '/ScriptExecution/downloadFile?sessionId='
                            + GLOBAL.HeimAnalyses.sessionId
                            + '&executionId='
                            + GLOBAL.HeimAnalyses.executionId
                            + '&filename=summary_stats_node.json',
                            dataType : 'json'
                        })
                            .done(function (d, status, jqXHR) {
                                console.log(d);
                                console.log(status);
                                console.log( jqXHR);
                                var _summaryObj = service.displaySummaryStats(d);
                                console.log(_summaryObj.table);
                                console.log(_summaryObj.plot);
                                jQuery('#heim-fetch-data-output')
                                    .empty()
                                    .append(_summaryObj.plot)
                                    .append(_summaryObj.table);

                            });
                    }
                } else if (d.state === 'FAILED') {

                    clearInterval(service.statusInterval); // stop checking backend
                    var _errHTML = '<span style="color: red";>' + d.result.exception +'</span>',
                        _elId;

                    if (task === 'runHeatmap') {
                        _elId = '#heim-run-output';
                    } else if (task === 'preprocess') {
                        _elId = '#heim-preprocess-output';
                    }

                    jQuery(_elId).html(_errHTML);
                    console.error('FAILED', d.result);
                }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {

                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);

                clearInterval(service.statusInterval); // stop checking backend

                if (task === 'fetchData') {
                    jQuery('#heim-fetch-data-output').html('<span style="color: red";>'+errorThrown+'</span>');
                } else if (task === 'preprocess') {
                    jQuery('#heim-preprocess-output').html('<span style="color: red";>'+errorThrown+'</spanp>');
                } else if (task === 'runHeatmap') {
                    jQuery('#heim-run-output').html('<span style="color: red";>'+errorThrown+'</spanp>');
                }
        })
        .always(function () {
            console.log('checked!');
        });
    };

    /**
     * Display Plot
     * @param data
     * @returns {{table: *, plot: *}}
     */
    service.displaySummaryStats = function (data, imgFile) {

        var tmpl = jQuery.templates("Name: {{:name}}");

        console.log('displaySummaryStats', tmpl);
        console.log('displaySummaryStats', data);

        var _table = jQuery('<table></table>').addClass('sr-summary-table');
        _table.append('<tr><th>Loaded</th><th>Values</th></tr>');

        jQuery.each(data,  function (idx, item) {
            _table.append('<tr><td>Variable Label</td><td>' + item.variableLabel + '</td></tr>');
            _table.append('<tr><td>Max</td><td>' + item.max + '</td></tr>');
            _table.append('<tr><td>Mean</td><td>' + item.mean + '</td></tr>');
            _table.append('<tr><td>Median</td><td>' + item.median + '</td></tr>');
            _table.append('<tr><td>No. of missing values</td><td>' + item.numberOfMissingValues + '</td></tr>');
            _table.append('<tr><td>Q1</td><td>' + item.q1 + '</td></tr>');
            _table.append('<tr><td>Q3</td><td>' + item.q3 + '</td></tr>');
            _table.append('<tr><td>Standard Deviation</td><td>' + item.standardDeviation + '</td></tr>');
            _table.append('<tr><td>Total no. of values (incl. missing)</td><td>' +
                item.totalNumberOfValuesIncludingMissing + '</td></tr>');
        });

        var _plot = jQuery('<img>')
            .attr('src', pageInfo.basePath
            + '/ScriptExecution/downloadFile?sessionId='
            + GLOBAL.HeimAnalyses.sessionId
            + '&executionId='
            + GLOBAL.HeimAnalyses.executionId
            + '&filename=' + imgFile);

        return {table:_table,  plot:_plot};
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
                    service.checkStatus('runHeatmap');
                }, 1000);
            }
        });
    };

    return service;
})(SmartRHeatmap);
