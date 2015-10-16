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
            sas : $j('jajal'),
            getResultBtn : $j('#heim-btn-get-output'),
            outputArea : $j('#heim-fetch-data-output')
        },
        preprocessView : {

        },
        runHeatmapView : {
            clusteringOptionsDiv : $j('#clusteringOptionsDiv'),
            methodSelect : $j('#methodSelect'),
            noClustersDiv : $j('#noOfClustersDiv'),
            noMarkersDiv : $j('#noOfMarkersDiv')
        }
    };

    /**
     * Register event handlers for DOM elements
     */
    var _registerEventHandlers = function () {
        // init tabs
        view.container.tabs();
        //fetch data btn
        view.fetchDataView.actionBtn.click(
            view.fetchDataView,
            heatmapService.fetchData
        );
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
            }
            else {
                view.runHeatmapView.clusteringOptionsDiv.hide();
            }
        });
        view.fetchDataView.getResultBtn.click( heatmapService.checkStatus);
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
