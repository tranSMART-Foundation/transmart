/**
 * Heatmap Service
 */

HeatmapService = (function(smartRHeatmap){

    var service = {
        statusInterval : 0
    };

    var _createAnalysisConstraints = function (params) {

        var _retval = {
            conceptKey : params.conceptPath,
            dataType: 'mrna',
            resultInstanceId: params.resultInstanceId,
            projection: 'log_intensity',
            label: '_TEST_LABEL_'
        };

        // TODO to include data constraints

        return  _retval;
    };

    /**
     * Create r-session id
     * @returns {*}
     */
    service.initialize = function () {

        // ajax call to session creation
        $j.ajax({
            url: pageInfo.basePath + '/RSession/create',
            type: 'POST',
            timeout: '600000',
            contentType: 'application/json',
            data : JSON.stringify( {
                workflow : 'heatmap'
            })
        }).done(function(response) {
            result = response;
            GLOBAL.HeimAnalyses = {
                type : 'heatmap',
                sessionId :response.sessionId
            };
            console.log(GLOBAL.HeimAnalyses);
            return GLOBAL.HeimAnalyses;
        }).fail(function() {
            // TODO: error displayed in a placeholder somewhere in main heim-analysis page
            console.error('Cannot create r-session');
            return null;
        });

    };

    /**
     * fetch data
     * @param eventObj
     */
    service.fetchData = function (params) {
        var _args = _createAnalysisConstraints(params);
        console.log('Analysis Constraints', _args);

        $j.ajax({
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
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
                $j('#heim-fetch-data-output').html('<span style="color: red";>Error:'+ errorThrown +'</span>');
            });
    };

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

    service.checkStatus = function (task) {
        if (task === 'fetchData') {
            $j('#heim-fetch-data-output').html('<p>Fetching data, please wait ..</p>');
        } else if (task === 'runHeatmap') {
            $j('#heim-run-output').html('<p>Calculating, please wait ..</p>');
        }

        $j.ajax({
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
                        $j('#heim-fetch-data-output')
                            .html('<p class="heim-fectch-success" style="color: green";> ' +
                            'Data is successfully fetched. Proceed with Run Heatmap</p>');
                    } else if (task === 'runHeatmap') {
                        $j('#heim-run-output').hide();
                        $j.get(
                            pageInfo.basePath
                                + '/ScriptExecution/downloadFile?sessionId='
                                + GLOBAL.HeimAnalyses.sessionId
                                + '&executionId='
                                + GLOBAL.HeimAnalyses.executionId
                                + '&filename=heatmap.json' // TODO get filename from run analysis result
                        )
                            .done(function (d) {
                                var _d = (JSON.parse(d));
                                smartRHeatmap.create(_d);
                            });
                    }
                }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
                clearInterval(service.statusInterval);
                if (task === 'fetchData') {
                    $j('#heim-fetch-data-output').html('<span style="color: red";>'+errorThrown+'</span>');
                } else if (task === 'runHeatmap') {
                    $j('#heim-run-output').html('<span style="color: red";>'+errorThrown+'</spanp>');
                }
        })
        .always(function () {
            console.log('checked!');
        });
    };

    service.getResultFiles = function (eventObj) {
        // NOTHING
    };

    service.runAnalysis = function (params) {
        $j.ajax({
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
