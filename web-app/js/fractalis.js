//# sourceURL=fractalis.js

window.fractalisPanel = new Ext.Panel({
    id: 'fractalisPanel',
    title: 'Fractalis',
    region: 'center',
    split: true,
    height: 90,
    layout: 'fit',
    collapsible: true,
    autoScroll: true,
    autoLoad: {
        url: pageInfo.basePath + '/fractalis/index',
        method: 'POST',
        scripts: false,
        callback: function() {
            var conceptBox = document.querySelector('.fjs-concept-box');
            if (typeof window.fjs === 'undefined') {
                window.fjs = fjs_initFractalis();
            }
            fjs_activateDragAndDrop(conceptBox);
            fjs_observeConceptBox(conceptBox);
        }
    },
    listeners: {
        activate: function () {
            fjs_setUrl();
            fjs_showLoadingScreen(true);
            fjs_getPatientIDs().then(
                function (ids) {
                    var subset1 = ids.filter(function (d) {
                        return d.subset === 1;
                    }).map(function (d) {
                        return d.id;
                    });
                    var subset2 = ids.filter(function (d) {
                        return d.subset === 2;
                    }).map(function (d) {
                        return d.id;
                    });
                    window.fjs.setSubsets([subset1, subset2]);
                },
                function (error) {
                    alert('Could not retrieve patient ids. Reason: ' + error);
                }
            ).then(function () {
                fjs_showLoadingScreen(false);
            });
        }
    }
});


window.addFractalisPanel = function addFractalisPanel(parentPanel) {
    parentPanel.insert(4, window.fractalisPanel);
};

function fjs_initFractalis () {
    return fractal.init({
        handler: 'pic-sure',
        dataSource: 'https://nhanes.hms.harvard.edu',
        fractalisNode: 'http://127.0.0.1:5000',
        getAuth: function () {
            return {token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzYW1scHxhdmxib3RAZGJtaS5obXMuaGFydmFyZC5lZHUiLCJhdWQiOiJ5d0FxNFh1NEtsM3VZTmRtM20wNUNjNW93ME9pYnZYdCIsImlzcyI6MTUxNTQzMTA3MCwiZXhwIjoxNTE3MjcwNDAwLCJlbWFpbCI6ImF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImRlc2NyaXB0aW9uIjoiQXV0b2dlbmVyYXRlZCB0b2tlbiBmb3IgbmhhbmVzLmhtcy5oYXJ2YXJkLmVkdSJ9.dK698TIevR2BpY9RL7qPHEA39C0YhrgtCGlRAfpKRuw'}
        }
    });
}

function fjs_activateDragAndDrop (conceptBox) {
    var extObj = Ext.get(conceptBox);
    var dtgI = new Ext.dd.DropTarget(extObj, {ddGroup: 'makeQuery'});
    dtgI.notifyDrop = dropOntoCategorySelection;
}

function fjs_observeConceptBox (conceptBox) {
    new MutationObserver(function (targets) {
        var descriptors = [];
        targets.forEach(function (target) {
            Array.prototype.map.call(target.addedNodes, function (node) {
                return fjs_getConceptAttributes(node);
            }).map(function (attr) {
                return {query: fjs_buildPicSureQuery(attr.path, attr.dataType), dataType: attr.dataType};
            }).forEach(function (d) {
                descriptors.push({dataType: d.dataType, query: d.query});
            });
        });
        window.fjs.loadData(descriptors);
    }).observe(conceptBox, { childList: true });
}

function fjs_getConceptAttributes (node) {
    return {
        path: node.getAttribute('conceptid'),
        dataType: node.getAttribute('setnodetype') === 'valueicon' ? 'numerical' : 'categorical'
    };
}

function fjs_buildPicSureQuery (path, type) {
    var alias = fjs_shortenConcept(path);
    return {
        "select": [
            {"field": {"pui": path}, "alias": alias}
        ],
        "where": [
            {
                "field": {"pui": path, "dataType": "STRING"},  // FIXME: dataType should be attr.dataType but PIC-SURE only knows STRING
                "predicate": "CONTAINS",
                "fields": {"ENOUNTER": "YES"}
            }
        ]
    }
}

function fjs_shortenConcept (concept) {
    var split = concept.split('\\');
    split = split.filter(function(str) { return str !== ''; });
    return split[split.length - 2] + '/' + split[split.length - 1];
}

var chartStates = {};
function fjs_setUrl () {
    var url = pageInfo.basePath + '/fractalis/state/' + Object.values(chartStates).join('+');
    window.history.pushState(null, '', url)
}

function fjs_handleStateIDs (stateIDs) {
    stateIDs.forEach(function (stateID) {
        var chartID = fjs_addChartContainer();
        window.fjs.id2chart(chartID, stateID);
    });
    fjs_setUrl();
}

function fjs_addChartContainer () {
    var chart = document.createElement('div');
    var container = document.querySelector('.fjs-placeholders');
    chart.id = 'fjs-chart-' + container.children.length;
    container.appendChild(chart);
    Array.prototype.forEach.call(container.children, function (chart) {
        chart.style.width = (Math.floor(100 / container.children.length)) + '%';
    });
    return chart.id;
}

function fjs_setChart () {
    var chartID = fjs_addChartContainer();
    var vm = window.fjs.setChart(document.querySelector('.fjs-analysis-select').value, '#' + chartID);
    window.fjs.chart2id(vm, function (id) {
        chartStates[chartID] = id;
        fjs_setUrl();
    });
}

function fjs_clearCache () {
    window.fjs.clearCache();
    document.querySelector('.fjs-concept-box').innerHTML = '';
}

function fjs_getPatientIDs () {
    var dfd = jQuery.Deferred();
    runAllQueries(function () {
        jQuery.ajax({
            url: pageInfo.basePath + '/chart/clearGrid',
            method: 'POST',
            data: {
                charttype: 'cleargrid'
            }
        }).then(function () {
            jQuery.ajax({
                url: pageInfo.basePath + '/chart/analysisGrid',
                type: 'POST',
                data: {
                    concept_key: '',
                    result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                    result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
                }
            }).then(function (res) {
                var ids = [];
                JSON.parse(res).rows.map(function (d) {
                    ids.push({id: d.patient, subset: d.subset === 'subset1' ? 1 : 2});
                });
                dfd.resolve(ids);
            });

        });
    });
    return dfd.promise();
}

function fjs_showLoadingScreen (bb) {
    var container = document.querySelector('.fjs-spinner');
    if (bb) {
        container.style.display = 'block'
    } else {
        container.style.display = 'none'
    }
}
