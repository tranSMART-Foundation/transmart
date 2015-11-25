/**
 * Heatmap Service
 */

HeatmapService = (function(smartRHeatmap){

    var service = {
        statusInterval : 0,
        jobs : []
    };

    /**
     * Create analysis constraints
     * @param params
     * @returns {{conceptKey: _conceptPath, dataType: string, resultInstanceId: *, projection: string, label: string}}
     * @private
     */
    var _createAnalysisConstraints = function (params) {

        var _retval = {
            conceptKey : params.conceptPath,
            dataType: 'mrna',
            resultInstanceId: params.resultInstanceId,
            projection: 'log_intensity',
            label: '_TEST_LABEL_'
        };

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
     *
     * @param request
     * @param response
     */
    service.getIndentifierSuggestions = function (request, response) {
        jQuery.get("/transmart/search/loadSearchPathways", {
            query: request.term
        }, function (data) {
            data = data.substring(5, data.length - 1);  // loadSearchPathways returns String with null (JSON).
                                                        // This strips it off
            data = JSON.parse(data);// String rep of JSON to actual JSON
            data = data['rows'];// Response is encapsulated in rows
            var suggestions = [];
            for (var i = 0; i < data.length;i++){
                var geneName = data[i]['keyword']; //I assume we use keywords, not synonyms or IDs
                suggestions.push(geneName);
            }
            response(suggestions);
        });

    };

    /**
     * Check status of a task
     * TODO: Refactor
     * @param task
     */
    service.checkStatus = function (task) {
        if (task === 'fetchData') {
            jQuery('#heim-fetch-data-output').html('<p class="sr-log-text"><span class="blink_me">_</span>Fetching data, please wait ..</p>');
        } else if (task === 'runHeatmap') {
            jQuery('#heim-run-output').show();
            jQuery('#heim-run-output').html('<p>Calculating, please wait ..</p>');
        } else if (task === 'getSummary') {
            jQuery('#heim-fetch-data-output').html('<p><p><span class="blink_me">_</span>Getting summary, please wait ..</p>');
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
                    clearInterval(service.statusInterval);
                    jQuery('#heim-fetch-data-output')
                        .html('<p class="heim-fetch-success" style="color: red";> ' +
                        d.result.exception + '</p>');
                    console.error('FAILED', d.result);
                }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
                clearInterval(service.statusInterval);
                if (task === 'fetchData') {
                    jQuery('#heim-fetch-data-output').html('<span style="color: red";>'+errorThrown+'</span>');
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
    service.displaySummaryStats = function (data) {

        var _table = jQuery('<table></table>').addClass('sr-summary-table');
        _table.append('<tr><th>Loaded</th><th>Values</th></tr>');

        jQuery.each(data,  function (idx, item) {
            _table.append('<tr><td>Max</td><td>' + item.max + '</td></tr>');
            _table.append('<tr><td>Mean</td><td>' + item.mean + '</td></tr>');
            _table.append('<tr><td>Median</td><td>' + item.median + '</td></tr>');
            _table.append('<tr><td>No. of missing values</td><td>' + item.numberOfMissingValues + '</td></tr>');
            _table.append('<tr><td>Q1</td><td>' + item.q1 + '</td></tr>');
            _table.append('<tr><td>Q3</td><td>' + item.q3 + '</td></tr>');
            _table.append('<tr><td>Standard Deviation</td><td>' + item.standardDeviation + '</td></tr>');
            _table.append('<tr><td>Total no. of values (incl. missing)</td><td>' +
                item.totalNumberOfValuesIncludingMissing + '</td></tr>');
            _table.append('<tr><td>Variable Label</td><td>' + item.variableLabel + '</td></tr>');
        });

        var _plot = jQuery('<img>')
            .attr('src', pageInfo.basePath
            + '/ScriptExecution/downloadFile?sessionId='
            + GLOBAL.HeimAnalyses.sessionId
            + '&executionId='
            + GLOBAL.HeimAnalyses.executionId
            + '&filename=box_plot_node.png');

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
