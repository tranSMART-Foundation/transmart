//# sourceURL=initServices.js

smartR.initServices = function(ajaxServices) {
    var service = {};

    var conceptBoxes = [];

    service.initConceptBox = function(box) {
        activateDragAndDrop(box);
        conceptBoxes.push(box);
    };

    service.initClearWindowButton = function(button, box) {
        button.on('click', function() {
            clearBox(box);
        })
    };

    service.initFetchDataButton = function(button) {
        button.on('click', function() {
            var allConcepts = {};

            conceptBoxes.map(function(box) {
                var concepts = getConcepts(box);
                Object.keys(concepts).each(function(key) {
                    allConcepts[box.attr('id') + '_' + key] = concepts[key];
                });
            });
            smartR.util.getSubsetIds().pipe(function(subsets) {
                ajaxServices.startScriptExecution({
                    taskType: 'fetchData',
                    arguments: {
                        conceptKeys: allConcepts,
                        resultInstanceIds: subsets
                    }
                });
            }, function() {
                alert('Could not create subsets.');
            });
        });
    };

    service.initRunButton = function(button) {
        button.on('click', function() {
            ajaxServices.startScriptExecution({
                taskType: 'run',
                arguments: {}
            }).pipe(function(data) {
                var foo = data.result.artifacts.value;
                window.SmartRCorrelation.create(JSON.parse(foo));
            })
        })
    };

    function activateDragAndDrop(box) {
        var obj = Ext.get(box.attr('id'));
        var dtgI = new Ext.dd.DropTarget(obj, {ddGroup: 'makeQuery'});
        dtgI.notifyDrop = dropOntoCategorySelection;
    }

    function clearBox(box) {
        box.children().remove()
    }

    function getConcepts(box) {
        var concepts = {};
        box.children().toArray().each(function(d, i) {
            concepts['n' + i] = d.getAttribute('conceptid');
        });
        return concepts;
    }

    return service;
};
