/**
 * Heatmap Service
 */

HeatmapService = (function(){

    var service = {};

    var _createAnalysisConstraints = function () {
        console.log(GLOBAL);
        return  {
            conceptKey : '\\\\Public Studies\\Public Studies\\GSE8581\\Biomarker Data\\Affymetrix Human Genome U133A 2.0 Array\\Lung\\',
            dataType: 'mrna',
            resultInstanceId: GLOBAL.CurrentSubsetIDs[1],
            assayConstraints: {},
            dataConstraints : {
                //search_keyword_ids:{
                //    keyword_ids : [1837633]
                //},
                //genes : {
                //    names : ['TP53']
                //}
            },
            projection: 'zscore',
            label: '_TEST_LABEL_'
        };
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
    service.fetchData = function (eventObj) {

        var _fetch_concept_path = function (el) {
            var conceptId = el.getAttribute('conceptId').trim();
            var conceptIdPattern = /^\\\\[^\\]+(\\.*)$/;
            var match = conceptIdPattern.exec(conceptId);

            if (match != null) {
                return match[1];
            } else {
                return undefined;
            }
        };

        var _x = function (divIds) {

                var variableConceptPath = '';
                var variableEle = Ext.get(divIds);

                //If the variable element has children, we need to parse them and concatenate their values.
                if (variableEle && variableEle.dom.childNodes[0]) {
                    //Loop through the variables and add them to a comma separated list.
                    for(nodeIndex = 0; nodeIndex < variableEle.dom.childNodes.length; nodeIndex++) {
                        //If we already have a value, add the separator.
                        if (variableConceptPath != '') {
                            variableConceptPath += '|'
                        }

                        //Add the concept path to the string.
                        variableConceptPath += _fetch_concept_path(variableEle.dom.childNodes[nodeIndex])
                    }
                }
                return variableConceptPath;
            };

        var _tmp =  eventObj.data.conceptPathsInput.attr('id');

        var _args = _createAnalysisConstraints();

        $j.ajax({
            type: 'POST',
            url: pageInfo.basePath + '/ScriptExecution/run',
            data: JSON.stringify({
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                arguments : _args,
                taskType : 'fetchData',
                workflow : 'heatmap'
            }),
            contentType: 'application/json',
            complete: function(data) {
                var scriptExecObj = JSON.parse(data.responseText);
                GLOBAL.HeimAnalyses.executionId = scriptExecObj.executionId;
                console.log(GLOBAL.HeimAnalyses);
                //$j('#heim-fetch-data-output').html(data.responseText);
            }
        });
    };

    service.checkStatus = function (eventObj) {

        $j.ajax({
            type : 'GET',
            url : pageInfo.basePath + '/ScriptExecution/status',
            data : {
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                executionId : GLOBAL.HeimAnalyses.executionId
            },
            success : function (data, status, jqXHR) {
                console.log(data);
                console.log(status);
                console.log(jqXHR);
            }
        });


      //$j.ajax({
      //    type : 'GET',
      //    url : pageInfo.basePath + '/ScriptExecution/status',
      //    data: JSON.stringify({
      //        sessionId : '3203b8f6-775c-4b08-b8ef-fa1cb371aaec',
      //        executionId : '5f9e52ff-0ccc-4f13-bc0c-c886f5fe4482'
      //    }),
      //    contentType: 'application/json',
      //    complete: function(data) {
      //        console.log('data', data);
      //    }
      //});
    };

    service.getResult = function (eventObj) {
        $j.ajax({
            type : 'POST',
            url : pageInfo.basePath + '/ScriptExecution/result',
            data: JSON.stringify({
                sessionId : 'eee1c089-b67d-43c4-bd45-927897d0536c',
                checkStatus : '1710ca96-a422-4e2c-8994-700634a2d7e2'
            }),
            contentType: 'application/json',
            complete: function(data) {

                console.log('data', data);
            }
        });
    };

    service.getOutput = function (eventObj) {
        $j.ajax({
            type : 'GET',
            url : pageInfo.basePath + '/ScriptExecution/output',
            data: JSON.stringify({
                sessionId : 'eee1c089-b67d-43c4-bd45-927897d0536c',
                checkStatus : '1710ca96-a422-4e2c-8994-700634a2d7e2'
            }),
            contentType: 'application/json',
            complete: function(data) {

                console.log('data', data);
            }
        });
    };

    return service;
})();
