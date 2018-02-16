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
        deactivate: function () {
            fjs_resetUrl();
        },
        activate: function () {
            fjs_setUrl();
            fjs_showLoadingScreen(true);
            fjs_getPatientIDs().then(
                function (ids) {
                    var subset1 = ids.subjectIDs1.split(',');
                    var subset2 = ids.subjectIDs2.split(',');
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
            return {token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjb21tb258cHVibGljdXNlckBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoicHVibGljdXNlciJ9.LLfNCgifHzzxNhor8mALUXoPR18g8beAWpwTG1dv4YY'}
        },
        options: {
            controlPanelPosition: 'right'
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
    path = path.replace(/\\+/g, '/');
    path = '/nhanes/Demo' + path; // #FIXME This is a VERY ugly hardcoded hack that should not be in production
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
    return
    var url = pageInfo.basePath + '/fractalis/state/' + Object.values(chartStates).join('+');
    window.history.pushState(null, '', url)
}

function fjs_resetUrl () {
    return
    var url = pageInfo.basePath + '/datasetExplorer';
    window.history.pushState(null, '', url)
}

function fjs_handleStateIDs (stateIDs) {
    Ext.Msg.alert('The url you specified contains a Fractalis state.\n' +
        'We will attempt to recover the associated charts and inform you once this has been done.');
    window.fjs_clearCache();
    Promise.all(stateIDs.map(function (stateID) {
        var chartID = fjs_addChartContainer();
        return window.fjs.id2chart(chartID, stateID);
    })).then(
        function () {
            Ext.Msg.alert('All charts have been successfully recovered. Please proceed to the Fractalis tab.');
        }
    ).catch(function () {
        Ext.Msg.alert('Could not recover one or more charts from URL.\n' +
            'Contact your administrator if this issue persists.');
    });
    fjs_setUrl();
}

function fjs_addChartContainer () {
    var chart = document.createElement('div');
    var container = document.querySelector('.fjs-placeholders');
    chart.id = 'fjs-chart-' + container.children.length;
    container.appendChild(chart);
    return chart.id;
}

function fjs_setChart () {
    var chartID = fjs_addChartContainer();
    var vm = window.fjs.setChart(document.querySelector('.fjs-analysis-select').value, '#' + chartID);
    return
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
            url: pageInfo.basePath + '/fractalis/patients',
            type: 'POST',
            data: {
                result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
            }
        }).then(function (res) {
            dfd.resolve(res);
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
