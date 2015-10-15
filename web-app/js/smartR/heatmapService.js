/**
 * Heatmap Service
 */

HeatmapService = (function(){

    var service = {};

    var _createAnalysisConstraints = function () {
        return  {
            assayConstraints: {
                trial_name: null,
                'patient_set': {
                    result_instance_id : [GLOBAL.CurrentSubsetIDs[1], GLOBAL.CurrentSubsetIDs[2]]
                },
                'ontology_term': {
                    concept_key:'\\Public Studies\\GSE8581\\MRNA\\Biomarker_Data\\GPL570\\Lung\\'
                }
            },
            dataConstraints : {
                search_keyword_ids:{
                    keyword_ids : [1837633]
                },
                chromosome_segment : {},
                disjunction : {},
                genes : {
                    names : ['TP53']
                },
                proteins : {},
                pathways : {},
                gene_signatures : {}

            }
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
            timeout: '600000'
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

        var _args = _createAnalysisConstraints();

        $j.ajax({
            type: "POST",
            url: pageInfo.basePath + '/ScriptExecution/run',
            data: JSON.stringify({
                sessionId : GLOBAL.HeimAnalyses.sessionId,
                arguments : _args,
                taskType : 'fetchData'
            }),
            contentType: "application/json",
            dataType: 'json',
            complete: function(data) {
                console.log('data', data);
                $j('#heim-fetch-data-output').html(data.responseText);
            }
        });


        // TODO check status
    };

    return service;
})();
