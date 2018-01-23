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
                window.fjs = initFractalis();
            }
            activateDragAndDrop(conceptBox);
            observeConceptBox(conceptBox);
        }
    }
});


window.addFractalisPanel = function addFractalisPanel(parentPanel) {
    parentPanel.insert(4, window.fractalisPanel);
};

function initFractalis () {
    return fractal.init({
        handler: 'pic-sure',
        dataSource: 'https://nhanes.hms.harvard.edu',
        fractalisNode: 'http://127.0.0.1:5000',
        getAuth: function () {
            return {token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzYW1scHxhdmxib3RAZGJtaS5obXMuaGFydmFyZC5lZHUiLCJhdWQiOiJ5d0FxNFh1NEtsM3VZTmRtM20wNUNjNW93ME9pYnZYdCIsImlzcyI6MTUxNTQzMTA3MCwiZXhwIjoxNTE3MjcwNDAwLCJlbWFpbCI6ImF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImRlc2NyaXB0aW9uIjoiQXV0b2dlbmVyYXRlZCB0b2tlbiBmb3IgbmhhbmVzLmhtcy5oYXJ2YXJkLmVkdSJ9.dK698TIevR2BpY9RL7qPHEA39C0YhrgtCGlRAfpKRuw'}
        }
    });
}

function activateDragAndDrop (conceptBox) {
    var extObj = Ext.get(conceptBox);
    var dtgI = new Ext.dd.DropTarget(extObj, {ddGroup: 'makeQuery'});
    dtgI.notifyDrop = dropOntoCategorySelection;
}

function observeConceptBox (conceptBox) {
    new MutationObserver(function (target) {
        Array.prototype.map.call(target[0].addedNodes, function (node) {
            return getConceptAttributes(node);
        }).map(function (attr) {
            return {query: buildPicSureQuery(attr.path, attr.dataType), dataType: attr.dataType};
        }).forEach(function (d) {
            window.fjs.loadData({dataType: d.dataType, query: d.query});
        });
    }).observe(conceptBox, { childList: true });
}

function getConceptAttributes (node) {
    return {
        path: node.getAttribute('conceptid'),
        dataType: node.getAttribute('setnodetype') === 'valueicon' ? 'numerical' : 'categorical'
    };
}

function buildPicSureQuery (path, type) {
    var alias = shortenConcept(path);
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

function shortenConcept (concept) {
    var split = concept.split('\\');
    split = split.filter(function(str) { return str !== ''; });
    return split[split.length - 2] + '/' + split[split.length - 1];
}

function setChart () {
    var placeholder = document.createElement('div');
    placeholder.appendChild(document.createElement('div'));
    var placeholders = document.querySelector('.fjs-placeholders');
    placeholders.appendChild(placeholder);
    Array.prototype.forEach.call(placeholders.children, function (placeholder) {
        placeholder.style.width = (Math.floor(100 / placeholders.children.length)) + '%';
    });
    window.fjs.setChart({
        selector: placeholder.children[0],
        chart: document.querySelector('.fjs-analysis-select').value
    })
}

function clearCache () {
    window.fjs.clearCache();
    Array.prototype.forEach.call(document.querySelector('.fjs-concept-box').children, function (node) { node.remove(); });
}
