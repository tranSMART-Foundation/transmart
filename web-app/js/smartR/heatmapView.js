/**
 * Heatmap View
 */

HeatmapView = (function(){
    var heatmapService;

    var view = {
        container : $j('#heim-tabs'),
        fetchDataView : {
            conceptPathsInput : $j('#displaydivIndependentVariable'),
            identifiersInput : $j('#heim-input-txt-identifiers'),
            actionBtn : $j('#heim-btn-fetch-data'),
            outputArea : $j('#heim-fetch-data-output')
        },
        preprocessView : {

        },
        runHeatmapView : {

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
    };

    view.init = function (service) {
        // assign service
        heatmapService = service;
        // register event handles
        _registerEventHandlers();
        // init analysis
        heatmapService.initialize();
    };

    return view;
})();

HeatmapView.init(HeatmapService);
