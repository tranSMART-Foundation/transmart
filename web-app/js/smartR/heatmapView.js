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
            if( !(this.value == 'none') ){
                view.runHeatmapView.clusteringOptionsDiv.show();
                view.runHeatmapView.noMarkersDiv.hide();
                view.runHeatmapView.noClustersDiv.hide();
                if(this.value == 'marker-selection'){
                    view.runHeatmapView.noMarkersDiv.show();
                }
                else if(this.value == 'k-means-clustering'){
                    view.runHeatmapView.noClustersDiv.show();
                }
            }
            else {
                view.runHeatmapView.clusteringOptionsDiv.hide();
            }
        });
        view.fetchDataView.getResultBtn.click(
            heatmapService.getOutput
        );


        view.fetchDataView.identifiersInput.autocomplete({
            source: _geneAutocomplete,
            minLength: 3
        });

    };

    view.init = function (service) {
        // assign service
        heatmapService = service;
        // register event handles
        _registerEventHandlers();
        // init analysis
        heatmapService.initialize();
    };


    var _geneAutocomplete = function(request,response){
        console.log('in geneAutocomplete');
        jQuery.get("/transmart/search/loadSearchPathways", {
            query: request.term
        }, function (data) {
            data = data.substring(5, data.length - 1);
            data = JSON.parse(data);
            data = data['rows'];
            var suggestions = [];
            for (i = 0; i < data.length;i++){
                var geneName = data[i]['keyword'];
                suggestions.push(geneName);
            }
            response(suggestions);
        });
    };
    return view;
})();

HeatmapView.init(HeatmapService);
