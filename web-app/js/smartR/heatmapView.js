/**
 * Heatmap View
 */

HeatmapView = (function(){
    var heatmapService, extJSHelper;


    var view = {
        container : $j('#heim-tabs'),
        fetchDataView : {
            conceptPathsInput : $j('#divIndependentVariable'),
            identifiersInput : $j('#heim-input-txt-identifiers'),
            actionBtn : $j('#heim-btn-fetch-data'),
            checkStatusBtn : $j('#heim-btn-check'),
            getResultBtn : $j('#heim-btn-get-output'),
            outputArea : $j('#heim-fetch-data-output')
        },
        preprocessView : {
            // TODO
        },
        runHeatmapView : {
            clusteringOptionsDiv : $j('#clusteringOptionsDiv'),
            methodSelect : $j('#methodSelect'),
            noClustersDiv : $j('#noOfClustersDiv'),
            noMarkersDiv : $j('#noOfMarkersDiv'),
            runAnalysisBtn : $j('#heim-btn-run-heatmap')
        }
    };

    var _geneAutocomplete = function(request, response){
        jQuery.get("/transmart/search/loadSearchPathways", {
            query: request.term
        }, function (data) {
            data = data.substring(5, data.length - 1);// loadSearchPathways returns String with null(JSON). This strips it off
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

    var _getFetchDataViewValues = function (v) {
        return {
            conceptPath : extJSHelper.readConceptVariables(v.conceptPathsInput.attr('id')),
            identifier : v.identifiersInput.val(), // TODO convert to array
            resultInstanceId : GLOBAL.CurrentSubsetIDs[1]
        };
    };

    var _fetchDataAction = function (eventObj) {
        var _fetchDataParams =  _getFetchDataViewValues(view.fetchDataView);
        heatmapService.fetchData(_fetchDataParams);
    };

    /**
     * Register event handlers for DOM elements
     */
    var _registerEventHandlers = function () {

        // init tabs
        view.container.tabs();

        // fetch data btn
        view.fetchDataView.actionBtn.click(
            view.fetchDataView,
            _fetchDataAction
        );

        // auto completion
        view.runHeatmapView.methodSelect.on('change', function() {
            if( !(this.value === 'none') ){
                view.runHeatmapView.clusteringOptionsDiv.show();
                view.runHeatmapView.noMarkersDiv.hide();
                view.runHeatmapView.noClustersDiv.hide();
                if(this.value === 'marker-selection'){
                    view.runHeatmapView.noMarkersDiv.show();
                }
                else if(this.value === 'k-means-clustering'){
                    view.runHeatmapView.noClustersDiv.show();
                }
            } else {
                view.runHeatmapView.clusteringOptionsDiv.hide();
            }
        });

        // identifiers autocomplete
        view.fetchDataView.identifiersInput.autocomplete({
            source: heatmapService.getIndentifierSuggestions,
            minLength: 2
        });

        view.fetchDataView.checkStatusBtn.click(heatmapService.checkStatus);

        view.fetchDataView.getResultBtn.click(heatmapService.getResultFiles);

        // TODO Run Analysis
        view.runHeatmapView.runAnalysisBtn.click (heatmapService.runAnalysis);
    };

    view.init = function (service, helper) {
        // injects dependencies
        heatmapService = service;
        extJSHelper = helper;
        // register dropzone
        extJSHelper.registerDropzone(view.fetchDataView.conceptPathsInput);
        // register event handles
        _registerEventHandlers();
        // init analysis
        heatmapService.initialize();
    };

    return view;
})();

HeatmapView.init(HeatmapService, HeimExtJSHelper);
