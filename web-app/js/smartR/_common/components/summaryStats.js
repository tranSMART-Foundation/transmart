//# sourceURL=summaryStats.js

"use strict";

window.smartR.components.summaryStats = function summaryStats(ajaxServices,
                                                              executionStatus,
                                                              fetchDataModel,
                                                              phase
) {
    var TEMPLATE = `
        <table class="sr-summary-table">
            <tbody>
                <tr>
                    <th>Loaded</th>
                    <th>Subset 1</th>
                    <th>Subset 2</th>
                </tr>
                {{for summaryStat}}
                <tr>
                    <td>{{:key}}</td>
                    <td>{{:val1}}</td>
                    <td>{{:val2}}</td>
                </tr>
                {{/for}}
            </tbody>
        </table>
    `.trim();

    var model = new window.smartR.Observable();
    model.clear = function SummaryStatsModel_clear() {
        this.summaryStatsData = {};
        this.trigger('summaryStatsClear', phase);
    };
    model.clear();
    model.setData = function SummaryStatusModel_setData(data) {
        console.log(`New data for phase '${phase}':`, data);
        if (data.executionId === undefined || data.statistics === undefined) {
            throw new Error('bad data argument');
        }
        this.summaryStatsData = data;
        this.trigger('summaryStats', phase);
    };

    var controller = {
        run: function SummaryStatsController_run() {
            var fileSuffixes;
            if (phase === 'preprocess') {
                fileSuffixes = ['all'];
            } else {
                fileSuffixes = SummaryStatsController_getAllNodes();
            }

            if (fileSuffixes.length == 0) {
                throw new Error('No relevant data is loaded for the summary statiscs script');
            }

            var promise = ajaxServices.startScriptExecution({
                arguments: {
                    phase: phase,
                    projection: 'default_real_projection', // always required, even for low-dim data
                },
                taskType: 'summary',
            });

            promise = promise.pipe(function(data) {
                return jQuery.when.apply(jQuery,
                    fileSuffixes.map(function (label) {
                        return ajaxServices.downloadJsonFile(
                            data.executionId,
                            phase + '_summary_stats_node_' + label + '.json')
                            .pipe(function(nodeData) {
                                return {label: label, data: nodeData};
                            });
                    })
                ).pipe(function() {
                    var allData = {
                        executionId: data.executionId,
                        statistics: {},
                    };
                    Array.prototype.forEach.call(arguments, function(result) {
                        allData.statistics[result.label] = result.data;
                    });

                    return allData;
                });
            });

            executionStatus.bindPromise(promise, 'Generating summary statistics');

            promise.done(function(data) {
                model.setData(data);
            });

            return promise;
        }
    };

    function SummaryStatsController_getAllNodes() {
        var sett = {};
        fetchDataModel.loadedVariables.forEach(function(v) {
            sett[v.replace(/_s\d/, '')] = undefined;
        });

        return Object.keys(sett).sort();
    }

    var view = {
        init: function SummaryStatsView_init(outputDivId) {
            this.jQueryOutputDiv = jQuery('#' + outputDivId);
            if (this.jQueryOutputDiv.length != 1) {
                throw new Error('Could not find ' + outputDivId);
            }

            SummaryStatusView_bindModel.call(this);
        },
    };

    function SummaryStatusView_bindModel() {
        model.on('summaryStatsClear', function() {
            this.jQueryOutputDiv.find('.summary-stats-container').remove();
        }.bind(this));

        model.on('summaryStats', SummaryStatusView_onSummaryStatsAvailable.bind(this));
    }

    function SummaryStatusView_onSummaryStatsAvailable(event) {
        var containerDiv = jQuery('<div class="summary-stats-container">').hide();
        var nodes = Object.keys(model.summaryStatsData.statistics);

        nodes.forEach(function(node) {
            var nodeDiv = jQuery('<div class="summary-stats-node"">');
            containerDiv.append(nodeDiv);

            SummaryStatusView_appendImage(
                nodeDiv, node, model.summaryStatsData.executionId);

            var data = model.summaryStatsData.statistics[node];
            var markup = SummaryStatusView_generateSummaryTable(data);

            nodeDiv.append(jQuery(markup));
        });

        this.jQueryOutputDiv.append(containerDiv);
        containerDiv.show();
    }

    function SummaryStatusView_appendImage(div, node, executionId) {
        var filename = ajaxServices.urlForFile(executionId,
            phase + '_box_plot_node_' + node + '.png');
        var plot = jQuery('<img>')
            .attr('src', filename);
        div.append(plot);
    }

    function SummaryStatusView_generateSummaryTable(data /* for a node individually! */) {
        // sort labels so
        data.sort(function (a, b) {
            a = a.subset;
            b = b.subset;
            return a.localeCompare(b);
        });

        var rowTemplate = jQuery.templates(TEMPLATE);

        var _summaryObj = {summaryStat : []};

        // return null when there's no data from both subsets defined
        if (data[0] === undefined && data[1] === undefined) {
            return null;
        }
        // use any available data
        var _data = data[0] === undefined ? data[1] : data[0];

        for (var key in _data) {
            if (_data.hasOwnProperty(key)) {
                _summaryObj.summaryStat.push({
                    key: key,
                    val1: data[0] === undefined ? '-' : data[0][key],
                    val2: data[1] === undefined ? '-' : data[1][key],
                });
            }
        }
        // return and render
        return rowTemplate.render(_summaryObj);
    }

    return {
        forModel: { clear: model.clear.bind(model), },
        forView: view,
        forController: controller,
    };
};
