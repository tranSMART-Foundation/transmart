//# sourceURL=heatmapService.js
'use strict';

/**
 * Heatmap Service
 */

window.HeatmapService = (function(){
    var CHECK_DELAY = 1000;
    var PROJECTION = 'log_intensity';

    var HEATMAP_DATA_FILE = 'heatmap.json';
    var MARKER_SELECTION_TABLE_FILE = 'markerSelectionTable.json';

    var ajaxServices = window.smartR.ajaxServices(window.pageInfo.basePath, 'heatmap');

    var NOOP_ABORT = function() {};

    var service = {
        lastFetchedLabels: []
    };

    var _setStatusRequestTimeout = function() {
        var timeout = setTimeout.apply(undefined, arguments);
        service.currentRequestAbort = function() {
            clearTimeout(timeout);
            service.currentRequestAbort = NOOP_ABORT;
        }
    };

    /* generate unique labels for the concept paths. It recursively resolves
     * clashes. */
    var _generateLabels = (function() {
        function throwIfDuplicates(arr) {
            var repeated =  arr.filter(function(el, index) {
                return arr.indexOf(el) !== index;
            });
            if (repeated.length > 0) {
                var error = new Error(
                    "Duplicate concept keys: " + repeated, 'dups');
                throw error;
            }
        }

        return function _generateLabels(arr) {
            throwIfDuplicates(arr);

            var n = 0;
            return arr.reduce(function(result, currentItem) {
                result['n' + n++] = currentItem;
                return result;
            }, {});
        };
    })();

    var _createAnalysisConstraints = function (params) {
        var _conceptKeys = '';

        try {
            _conceptKeys = _generateLabels(params.conceptPaths.split(/\|/));
        } catch (err) {
            throw err;
        }

        // params.conceptPaths are actually keys...
        var _retval = {
            conceptKeys : _conceptKeys,
            resultInstanceIds: params.resultInstanceIds,
            projection: PROJECTION
        };

        if (params['searchKeywordIds'].length > 0) {
            _retval.dataConstraints = {
                search_keyword_ids: {
                    keyword_ids: params['searchKeywordIds']
                }
            }
        }
        return  _retval;
    };

    /**
     * Create r-session id
     * @returns {*}
     */
    service.initialize = function () {
        window.smartR.currentSessionTearDown();
        ajaxServices.startSession();
        window.smartR.currentSessionTearDown =
            ajaxServices.destroySession.bind(ajaxServices);
    };

    /* TaskData
     * {
     *   taskType: (string),
     *   arguments: (object)
     *   onUltimateSuccess: function (data) {},
     *   phase: (string) allows finding div id,
     *   progressMessage: (string),
     *   successMessage: (string)
     * }
     */

    var _divForPhase = function(phase) {
        return jQuery('#heim-' + phase + '-output');
    };

    function buildErrorMessage(error) {
        var ret;
        
        if (!error) {
            ret = 'Failure with no details.';
        } else if (error.statusText !== undefined) {
            if (error.status) { // for the 0, undefined cases
                 ret = 'Error ' + error.status + ': ' + error.statusText + '.';
            } else {
                ret = error.statusText;
            }

            if (error.response && error.response.type && error.response.message) {
                ret += ' ' + error.response.type + ': ' + error.response.message;
            }
        } else {
            ret = error;
        }

        return ret;
    }

    var startScriptExecution = function(taskData) {
        var promise = ajaxServices.startScriptExecution(taskData)
            .then(function(data) {
                data.phase = taskData.phase;
                return data;
            });
            
        promise.fail(function (e) {
                _divForPhase(taskData.phase)
                    .html('<p style="color: red";><b>Error:'+ buildErrorMessage(e) +'</b>')
                    .show();
            });

        var div = _divForPhase(taskData.phase);
        div.show();
        div.html('<p class="sr-log-text">' +
            taskData.progressMessage + ', please wait<span class="blink_me">_</span></p>');

        if (taskData.successMessage) {
            var _html = '<p class="heim-fetch-success" style="color: green";> ' +
                taskData.successMessage + '</p>';
            promise.done(function() { div.html(_html); });
        } else {
            promise.done(function() { div.hide(); });
        }

        promise.done(taskData.onUltimateSuccess);
        if (taskData.onUltimateFailure) {
            promise.fail(taskData.onUltimateFailure);
        }

        return promise;
    };

    var downloadJsonFile = function(executionId, filename) {
        return jQuery.ajax({
            url: ajaxServices.urlForFile(executionId, filename),
            dataType: 'json'
        });
    };

    /**
     * Fetchs data. Returns the summary statistics data in the form of
     * a promise.
     * @param eventObj
     */

    service.fetchData = function (params) {
        var _defer = jQuery.Deferred(), _args;

        try {
            var _args = _createAnalysisConstraints(params);
        } catch (err) {
            _defer.reject(err);
            return _defer.promise();
        }

        if (_args) {
            service.lastFetchedLabels = Object.keys(_args.conceptKeys);
        }

        var  fetchData_ultimateSuccess = function () {
            // TODO: only resolved(), never rejected()
            service.getSummary('fetch')
                .then(function(data) {
                    _defer.resolve(data);
                });
        };

        var  fetchData_ultimateFailure = function (d) {
            _defer.reject(d);
        };

        startScriptExecution({
            taskType: 'fetchData',
            arguments: _args,
            onUltimateSuccess: fetchData_ultimateSuccess,
            onUltimateFailure: fetchData_ultimateFailure,
            phase: 'fetch',
            progressMessage: 'Fetching data',
            successMessage: 'Data is successfully fetched in . Proceed with Run Heatmap'
        });

        return _defer.promise();
    };

    // returns promise with the data
    service.getSummary = function (phase) {
        var fileSuffixes;
        var defer = jQuery.Deferred();

        if (phase === 'preprocess') {
            fileSuffixes = ['all'];
        } else {
            fileSuffixes = service.lastFetchedLabels;
        }

        function getSummary_onUltimateSuccess(data) {
            var div = _divForPhase(data.phase);
            div.empty();
            fileSuffixes.forEach(function(label) {
                var filename = ajaxServices.urlForFile(data.executionId,
                    data.phase + '_box_plot_node_' + label + '.png');
                var plot = jQuery('<img>').attr('src', filename);
                div.append(plot);
            }.bind(this));

            jQuery.when.apply(jQuery,
                fileSuffixes.map(function (label) {
                    return downloadJsonFile(
                        data.executionId,
                        data.phase + '_summary_stats_node_' + label + '.json');
                }.bind(this))
            ).done(function() {
                var _args = arguments;
                // if there is only one request, each element of arguments will be
                // not a 3-element array (where the data is the 1st), but each
                // of the items of the (single) 3-element array.
                if (_args[1] === 'success') {
                    _args = [_args];
                }
                var allData = [];
                Array.prototype.forEach.call(_args, function(ajaxCbArgs) {
                    var data = ajaxCbArgs[0];
                    var _summaryObj = service.generateSummaryTable(data);
                    div.append(_summaryObj);
                    allData.push(data);
                });
                div.show();

                // TODO: now we only resolve, never reject
                defer.resolve(allData);
            });
        }

        var args = {
            phase: phase,
            projection: PROJECTION
        };

        startScriptExecution({
            taskType: 'summary',
            arguments: args,
            onUltimateSuccess: getSummary_onUltimateSuccess,
            phase: phase,
            progressMessage: 'Getting summary',
            successMessage: undefined
        });

        return defer.promise();
    };

    /**
     * Preprocess service
     * @param params
     */
    service.preprocess = function (params) {
        var defer = jQuery.Deferred();
        var preprocess_ultimateSuccess = function (data, taskData) {
            service.getSummary('preprocess').then(function (data) {
                defer.resolve(data);
            });
        };
        var  preprocess_ultimateFailure = function (d) {
            defer.reject(d);
        };

        startScriptExecution({
            taskType: 'preprocess',
            arguments: params,
            onUltimateSuccess: preprocess_ultimateSuccess,
            onUltimateFailure: preprocess_ultimateFailure,
            phase: 'preprocess',
            progressMessage: 'Preprocessing'
        });

        return defer.promise();
    };

    service.runAnalysis = function (params) {
        var defer = jQuery.Deferred();

        function runAnalysisSuccess(data) {
            var ajaxCalls = [];
            ajaxCalls.push(downloadJsonFile(data.executionId, HEATMAP_DATA_FILE));
            if (data.result.artifacts.files.indexOf(MARKER_SELECTION_TABLE_FILE) != -1) {
                ajaxCalls.push(
                    downloadJsonFile(data.executionId, MARKER_SELECTION_TABLE_FILE));
            }

            jQuery.when.apply(jQuery, ajaxCalls)
                .done(function() {
                    var _args = arguments;
                    // see comment in the other .when call
                    if (_args[1] === 'success') {
                        _args = [_args];
                    }
                    defer.resolve({
                        heatmapData: _args[0][0], // ajax resolves with 3 args, 1st is data
                        markerSelectionData: _args[1] ? _args[1][0] : null
                    });
                })
                .fail(function() { defer.reject.apply(defer, arguments); });
        }

        function runAnalysisFailed (d) {
            defer.reject(d);
        }

        startScriptExecution({
            taskType: 'run',
            arguments: params,
            onUltimateSuccess: runAnalysisSuccess,
            onUltimateFailure: runAnalysisFailed,
            phase: 'run',
            progressMessage: 'Calculating',
            successMessage: undefined
        });

        // having a "ultimateSuccess" event on the return of
        // startScriptExecution would be better
        return defer.promise();
    };

    service.generateSummaryTable = function (data) {
        var _data = [];
        _data[0] = data.find(function(el) { return el.subset == 's1'; });
        _data[1] = data.find(function(el) { return el.subset == 's2'; });

        // get template
        var rowTemplate = jQuery.templates('#summary-row-tmp');

        // initiate summary obj
        var _summaryObj = {summaryStat : []};

        // return null when there's no data from both subsets defined
        if (_data[0] === undefined && _data[1] === undefined) {
            return null;
        }

        for (var key in (_data[0] === undefined ? _data[1] : _data[0])) {
            _summaryObj.summaryStat.push({
                key: key,
                val1: (_data[0] === undefined) ? '-' : _data[0][key],
                val2: (_data[1] === undefined) ? '-' : _data[1][key]
            });
        }
        // return and render
        return rowTemplate.render(_summaryObj);
    };

    /**
     *
     * @param request
     * @param response
     */
    service.getIdentifierSuggestions = (function() {
        var curXHR = null;

        return function(model, term, response) {
            if (curXHR && curXHR.state() === 'pending') {
                curXHR.abort();
            }

            curXHR = jQuery.get("/transmart/search/loadSearchPathways", {
                query: term
            });

            curXHR.always(function() { curXHR = null; })
            return curXHR.then(
                function(data) {
                    data = data.substring(5, data.length - 1);  // loadSearchPathways returns String with null (JSON).
                                                                // This strips it off
                    response(JSON.parse(data));
                },
                function() {
                    response({rows: []}); // response must be called even on failure
                }
            );
        };
    })();

    // aux for downloadSVG
   function copyWithCollapsedCSS(heatmapElement) {
        var relevantProperties = [
            'fill-opacity', 'fill', 'stroke', 'font-size', 'font-family',
            'shape-rendering', 'stroke-width'
        ];
        var clonedSvg = jQuery(heatmapElement).clone().attr('display', 'none');
        clonedSvg.insertAfter(heatmapElement);

        var cachedDefaults = {};
        var scratchSvg = jQuery(document.createElement('svg'))
            .attr('display', 'none')
            .appendTo(jQuery('body'));

        function getDefaultsForElement(jqElement) {
            var nodeName = jqElement.prop('nodeName');
            if (!cachedDefaults[nodeName]) {
                var newElement = jQuery(document.createElement(nodeName))
                    .appendTo(scratchSvg);

                cachedDefaults[nodeName] = window.getComputedStyle(newElement[0]);
            }
            return cachedDefaults[nodeName];
        }

        clonedSvg.find('*').each(function(idx, element) { // for each element in <svg>
            var computedStyle = window.getComputedStyle(element);

            var jqElem = jQuery(element);
            relevantProperties.forEach(function(property) { // for each property
                var effectiveStyle = computedStyle.getPropertyValue(property);
                var defaultStyle = getDefaultsForElement(jqElem).getPropertyValue(property);

                if (effectiveStyle != defaultStyle) {
                    jqElem.attr(property, effectiveStyle);
                }
            })
        });

        scratchSvg.remove();

        return clonedSvg;
    };

    service.downloadSVG = function(event) {
        var serializer = new XMLSerializer();
        var clonedSvg = copyWithCollapsedCSS(event.data());
        var xmlString = serializer.serializeToString(clonedSvg[0]);
        var blob = new Blob([xmlString], { type: 'image/svg+xml' });
        var svgBlobUrl = URL.createObjectURL(blob);
        var link = jQuery('<a/>')
            .attr('href', svgBlobUrl)
            .attr('download', 'heatmap.svg')
            .css('display', 'none');
        jQuery('body').append(link);
        link[0].click();
        link.remove();
        URL.revokeObjectURL(svgBlobUrl);
        clonedSvg.remove();
    };

    service.downloadData = function() {
        function downloadFile(data) {
            var link = jQuery('<a/>')
                .attr('href', urlForFile(data.executionId, 'analysis_data.zip'))
                .attr('download', 'heatmap_data.zip')
                .css('display', 'none');
            jQuery('body').append(link);
            link[0].click();
            link.remove();
        }

        startScriptExecution({
            taskType: 'downloadData',
            arguments: {},
            onUltimateSuccess: downloadFile,
            phase: 'run',
            progressMessage: 'Creating zip',
            successMessage: undefined
        });
    };

    return service;
})();
