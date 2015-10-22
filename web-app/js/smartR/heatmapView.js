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

    /**
     *
     * @param v
     * @returns {{conceptPath: *, identifier: *, resultInstanceId: *}}
     * @private
     */
    var _getFetchDataViewValues = function (v) {
        var _conceptPath = extJSHelper.readConceptVariables(v.conceptPathsInput.attr('id'));
        console.log(v.identifiersInput.val());
        return {
            conceptPath : _conceptPath,
            //identifier : v.identifiersInput.val(), // TODO convert to array
            identifier : 'TP53', // TODO convert to array
            resultInstanceId : GLOBAL.CurrentSubsetIDs[1]
        };
    };

    /**
     * Fetch data
     * @param eventObj
     * @private
     */
    var _fetchDataAction = function (eventObj) {
        var _fetchDataParams =  _getFetchDataViewValues(view.fetchDataView);
        heatmapService.fetchData(_fetchDataParams);
    };

    /**
     * Register event handlers
     * @private
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

    /**
     * Initialize helper
     * @param service
     * @param helper
     */
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
