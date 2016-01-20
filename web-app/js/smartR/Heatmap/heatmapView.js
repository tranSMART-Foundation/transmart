//# sourceURL=heatmapView.js

'use strict';

/**
 * Heatmap View
 */
var HeatmapView = (function(){
    var heatmapService, extJSHelper, inputValidator;

    var view = {
        container : $('#heim-tabs'),
        fetchDataView : {
            conceptPathsInput : $('#heim-high-dim-var'),
            identifierInput   : $('#heim-input-txt-identifier'),
            listIdentifiers   : $('#heim-input-list-identifiers'),
            actionBtn         : $('#heim-btn-fetch-data'),
            clearBtn          : $('#heim-btn-clear'),
            checkStatusBtn    : $('#heim-btn-check'),
            getResultBtn      : $('#heim-btn-get-output'),
            outputArea        : $('#heim-fetch-output'),
            fetchDialog       : $('#sr-fetch-dialog')
        },
        preprocessView : {
            aggregateProbesChk : $('#chkAggregateProbes'),
            preprocessBtn      : $('#heim-btn-preprocess-heatmap'),
            outputArea         : $('#heim-preprocess-output')
        },
        runHeatmapView : {
            maxRowInput          : $('#txtMaxRow'),
            clusteringOptionsDiv : $('#clusteringOptionsDiv'),
            methodSelect         : $('[name=rankCriteria]'),
            noClustersDiv        : $('#noOfClustersDiv'),
            noMarkersDiv         : $('#noOfMarkersDiv'),
            sortingSelect        : $('[name=sortingSelect]'),
            singleSubsetDiv      : $('#sr-non-multi-subset'),
            singleSubsetVarDiv   : $('#sr-variability-group'),
            singleSubsetLvlDiv   : $('#sr-expression-level-group'),
            multiSubsetDiv       : $('#sr-multi-subset'),
            runAnalysisBtn       : $('#heim-btn-run-heatmap'),
            snapshotImageBtn     : $('#heim-btn-snapshot-image'),
            downloadFileBtn      : $('#heim-btn-download-file'),
            outputArea           : $('#heim-run-output'),
            d3Heatmap            : $('#heatmap')
        }
    };

    var _renderBiomarkersList = (function() {

        // get template
        var biomarkerListTmp = jQuery.templates('#biomarker-list-tmp');

        return function _renderBiomarkersList() {
            var _biomarker = biomarkerListTmp.render({biomarkers : this.getBioMarkers()});
            view.fetchDataView.listIdentifiers.empty();
            view.fetchDataView.listIdentifiers.append(_biomarker);
        };
    })();

    var bioMarkersModel = new BioMarkersModel();
    bioMarkersModel.on('biomarkers', _renderBiomarkersList);
    view.fetchDataView.listIdentifiers.on('click', 'button', function(ev) {
        bioMarkersModel.removeBioMarker($(this).val());
    });

    /**
     *
     * @param fetchDataView
     * @returns {{conceptKeys: *, identifier: *, resultInstanceIds: *}}
     * @private
     */
    var _getFetchDataViewValues = function (v) {
        var _conceptPath = extJSHelper.readConceptVariables(v.conceptPathsInput.attr('id'));
        return {
            conceptPaths: _conceptPath,
            // CurrentSubsetIDs can contain undefined and null. Pass only nulls forward
            resultInstanceIds : GLOBAL.CurrentSubsetIDs.map(function (v) { return v || null; }),
            searchKeywordIds: Object.getOwnPropertyNames(bioMarkersModel.selectedBioMarkers)
        };
    };

    var _getRunHeatmapViewValues = function (v) {
        // get max_rows
        var _maxRows = v.maxRowInput.val();
        var _sortingType = v.sortingSelect.filter(':checked').val();
        var _ranking = v.methodSelect.filter(':checked').val();
        return {
            max_rows : _maxRows,
            sorting: _sortingType,
            ranking: _ranking
        }
    };

    var _getPreprocessViewValues = function (v) {
        // get max_rows
        var _aggregate = v.aggregateProbesChk.is(":checked");
        return {
            aggregate : _aggregate
        }
    };

    var _emptyOutputs = function (workflow) {
        if (workflow === 'fetch') {
            view.preprocessView.outputArea.empty();
            view.runHeatmapView.outputArea.empty();
            view.runHeatmapView.d3Heatmap.empty();
        } else if (workflow === 'preprocess') {
            view.runHeatmapView.outputArea.empty();
            view.runHeatmapView.d3Heatmap.empty();
        }
    };

    /**
     * Toggle Run Analysis View based on no of subsets & samples
     * @param param
     * @private
     */
    var _toggleAnalysisView = function (param) {
        // toggle widgets
        if (param.noOfSamples === 1) { // when only one sample is fetched
            // show only expression level options
            view.runHeatmapView.singleSubsetVarDiv.hide();
            view.runHeatmapView.multiSubsetDiv.hide();
            view.runHeatmapView.methodSelect.filter('[value="mean"]').attr('checked', true);
            // disable aggregate
            view.preprocessView.aggregateProbesChk.attr("disabled", true);
        } else if (param.noOfSamples > 1) { // > 1 samples
            // enable aggregate
            view.preprocessView.aggregateProbesChk.removeAttr("disabled");
            // show
            view.runHeatmapView.singleSubsetVarDiv.show();
            // and depends on how many subsets
            if (param.subsetNo === 2) { // displays all options
                view.runHeatmapView.multiSubsetDiv.show();
                view.runHeatmapView.methodSelect.filter('[value="bval"]').attr('checked', true);
            } else if (param.subsetNo === 1 ) { // display only exp
                view.runHeatmapView.multiSubsetDiv.hide();
                view.runHeatmapView.methodSelect .filter('[value="coef"]').attr('checked', true);
            }
        } else {
            console.error('Invalid no of samples.');
        }
    };

    var _onFetchData = function () {
        view.preprocessView.preprocessBtn.attr('disabled', 'disabled');
        view.runHeatmapView.runAnalysisBtn.attr('disabled', 'disabled');
        view.runHeatmapView.snapshotImageBtn.attr('disabled', 'disabled');
        view.runHeatmapView.downloadFileBtn.attr('disabled', 'disabled');
    };

    var _onPreprocess = function () {
        view.fetchDataView.actionBtn.attr('disabled', 'disabled');
        view.runHeatmapView.runAnalysisBtn.attr('disabled', 'disabled');
        view.runHeatmapView.snapshotImageBtn.attr('disabled', 'disabled');
        view.runHeatmapView.downloadFileBtn.attr('disabled', 'disabled');
    };

    var _onRunAnalysis = function () {
        view.fetchDataView.actionBtn.attr('disabled', 'disabled');
        view.preprocessView.preprocessBtn.attr('disabled', 'disabled');
        view.runHeatmapView.snapshotImageBtn.attr('disabled', 'disabled');
        view.runHeatmapView.downloadFileBtn.attr('disabled', 'disabled');
    };

    var _resetActionButtons = function (workflow) {
        view.fetchDataView.actionBtn.removeAttr('disabled');
        view.preprocessView.preprocessBtn.removeAttr('disabled');
        view.runHeatmapView.runAnalysisBtn.removeAttr('disabled');
        if (workflow === 'RUN_HEATMAP_SUCCESS') {
            view.runHeatmapView.snapshotImageBtn.removeAttr('disabled');
            view.runHeatmapView.downloadFileBtn.removeAttr('disabled');
        } else if (workflow === 'RUN_HEATMAP_FAILED') {
            view.runHeatmapView.snapshotImageBtn.attr('disabled', 'disabled');
            view.runHeatmapView.downloadFileBtn.attr('disabled', 'disabled');
        }
    };

    var _isEmptyEl = function (el) {
        return !$.trim(el.html());
    };

    /**
     * Fetch data
     * @param rid
     * @private
     */
    var _fetchDataAction = function () {
        var subsetNo = !GLOBAL.CurrentSubsetIDs[1] || !GLOBAL.CurrentSubsetIDs[2] ? 1 : 2,
            _noOfSamples = 0,
            _promise = null,
            _fetchDataParams = _getFetchDataViewValues(view.fetchDataView);

        var _validateFetchInputs = function () {

            var _retval = true,
                _validations = [
                inputValidator.isEmptySubset(_fetchDataParams.resultInstanceIds),
                inputValidator.isEmptyHighDimensionalData(_fetchDataParams.conceptPaths)
            ];

            // clean first all error mesages
            jQuery('.heim-fetch-err').remove();

            if (_validations[0]) {
                view.fetchDataView.outputArea.html('<p class="heim-fetch-err"><b>Error:'+ inputValidator.NO_SUBSET_ERR
                    +'</b></p>');
            }

            if (_validations[1]) {
                view.fetchDataView.conceptPathsInput.after('<p class="heim-fetch-err">'+ inputValidator.NO_HD_ERR
                    +'</p>');
            }

            _validations.forEach(function(validateItem) {
                _retval= _retval && (!validateItem);
            });

            return _retval;
        };

        var _fetch = function (promise) {
            _onFetchData();
            // fetch data
            promise = heatmapService.fetchData(_fetchDataParams);
            // return promise when fetching and calculating summary has finished
            if (promise !== null)
            promise.done(function (data) {
                data.forEach(function (d) {
                    d.forEach(function (summaryJSON) {
                        _noOfSamples += summaryJSON['numberOfSamples'];
                    });
                });
                _resetActionButtons();
                // toggle view
                _toggleAnalysisView({subsetNo: subsetNo, noOfSamples: _noOfSamples});
            })
                .fail(function (d) {
                    view.fetchDataView.outputArea.html('<p style="color: red";><b>'+ d +'</b>');
                    _resetActionButtons();
                });
        };

        // empty outputs
        _emptyOutputs('fetch');

        // validate inputs
        if (!_validateFetchInputs()) {
            return;
        }

        // Notify user when there are outputs from previous jobs
        if (!_isEmptyEl(view.preprocessView.outputArea) || !_isEmptyEl(view.runHeatmapView.outputArea)) {
            view.fetchDataView.fetchDialog.dialog({
                resizable: false,
                height: 140,
                modal: true,
                buttons: {
                    "Proceed": function () {
                        $(this).dialog("close");
                        _fetch(_promise);
                    },
                    Cancel: function () {
                        $(this).dialog("close");
                    }
                }
            });
        } else {
            _fetch(_promise);
        }

    };

    var _runHeatmapAction = function (eventObj) {
        var _runHeatmapInputArgs =  _getRunHeatmapViewValues(view.runHeatmapView);
        _onRunAnalysis();
        view.runHeatmapView.d3Heatmap.empty();
        heatmapService.runAnalysis(_runHeatmapInputArgs)
            .done(function(data) {
                SmartRHeatmap.create(data.heatmapData);
                _resetActionButtons('RUN_HEATMAP_SUCCESS');
                if (data.markerSelectionData) {
                    view.appendSelectionTable({
                        entries: data.markerSelectionData
                    })
                }
            })
            .fail(function(d) {
                _resetActionButtons('RUN_HEATMAP_FAILED');
            });
    };

    var _preprocessAction = function (eventObj) {
        _onPreprocess();
        var _preprocessInputArgs =  _getPreprocessViewValues(view.preprocessView);

        var _preprocess = function () {
            heatmapService.preprocess(_preprocessInputArgs)
                .done(function (data) {
                    _resetActionButtons();
                    // empty outputs
                    _emptyOutputs('preprocess');
                })
                .fail(function () {
                    _resetActionButtons();
                });
        };

        // Notify user when there are outputs from previous jobs
        if (!_isEmptyEl(view.runHeatmapView.outputArea)) {
            view.fetchDataView.fetchDialog.dialog({
                resizable: false,
                height: 140,
                modal: true,
                buttons: {
                    "Proceed": function () {
                        $(this).dialog("close");
                        _preprocess();
                    },
                    Cancel: function () {
                        $(this).dialog("close");
                    }
                }
            });
        } else {
            _preprocess();
        }
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
            function () {
                for (var i = 1; i <= GLOBAL.NumOfSubsets; i++) {
                    if (!isSubsetEmpty(i) && !GLOBAL.CurrentSubsetIDs[i]) {
                        runAllQueries(_fetchDataAction);
                        return;
                    }
                }
                _fetchDataAction();
            }
        );

        // register preprocess btn action
        view.preprocessView.preprocessBtn.click(
            view.preprocessView,
            _preprocessAction
        );

        // on change handler
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

        // download data button
        view.runHeatmapView.downloadFileBtn.click(heatmapService.downloadData);

        // download data button
        view.runHeatmapView.downloadFileBtn.click(
            function() {
                heatmapService.downloadData();
            }
        );

        var _identifierItemTemplate = jQuery.templates('#biomarker-autocompletion-list-tmp');

        view.fetchDataView.identifierInput.autocomplete({
            source: function(request, response) {
                var term = request.term;
                if (term.length < 2) {
                    return function() {
                        return response({rows: []});
                    };
                }
                return heatmapService.getIdentifierSuggestions(
                    bioMarkersModel,
                    term,
                    function(grailsResponse) {
                        // convert Grails response to what jqueryui expects
                        // grails response looks like this:
                        // { "id": 1842083, "source": "", "keyword": "TPO", "synonyms":
                        // "(TDH2A, MSA, TPX)", "category": "GENE", "display": "Gene" }
                        var r = grailsResponse.rows.map(function(v) {
                            return {
                                label: v.keyword,
                                value: v
                            }
                        });
                        return response(r);
                    });
            },

            minLength: 2
        });
        view.fetchDataView.identifierInput.data('ui-autocomplete')._renderItem = function(ul, item) {
            var _item = _identifierItemTemplate.render(item.value);
            return jQuery(_item).appendTo(ul);
        };
        view.fetchDataView.identifierInput.on('autocompleteselect',
            function(event, ui) {
                var v = ui.item.value;
                bioMarkersModel.addBioMarker(v.id, v.display, v.keyword, v.synonyms);
                this.value = '';
                return false;
            });
        view.fetchDataView.identifierInput.on('autocompletefocus',
            function(event, ui) {
                var v = ui.item.value;
                this.value = v.display + ' ' + v.keyword;
                return false;
            });
        view.fetchDataView.identifierInput.on('autocompleteclose',
            function() { this.value = ''; });

        view.fetchDataView.clearBtn.click(view.clearConceptPathInput);

        view.runHeatmapView.runAnalysisBtn.click (
            view.runHeatmapView,
            _runHeatmapAction
        );

        view.runHeatmapView.snapshotImageBtn.click(
            function() {
                return $('#visualization svg')[0];
            },
            heatmapService.downloadSVG);
    };

    view.clearConceptPathInput = function (eventObj) {
        extJSHelper.clear(view.fetchDataView.conceptPathsInput);
    };

    view.appendSelectionTable = function(data) {
        var tmpl = $.templates('#marker-selection-table-tmp');
        var table = tmpl.render(data);
        $('#heatmap').append(table);
        $('#markerSelectionTable').tablesorter();
    }

    /**
     * Initialize helper
     * @param service
     * @param helper
     */
    view.init = function (service, helper, validator) {
        // instantiate tooltips
        $( "[title]" ).tooltip({track: true, tooltipClass:"sr-ui-tooltip"});
        // injects dependencies
        heatmapService = service;
        extJSHelper = helper;
        inputValidator = validator;
        // register dropzone
        extJSHelper.registerDropzone(view.fetchDataView.conceptPathsInput);
        // register event handles
        _registerEventHandlers();
        // init analysis
        heatmapService.initialize();
    };

    return view;
})();

HeatmapView.init(HeatmapService, HeimExtJSHelper, HeatmapValidator);
