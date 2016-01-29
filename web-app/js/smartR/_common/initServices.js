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
                Object.keys(concepts).each(function(key, concept) {
                    allConcepts[box.attr('id') + '_' + key] = concept;
                });
            });

            smartR.util.getSubsetIds().pipe(function(subsets) {
                console.log(ajaxServices.startScriptExecution({
                    arguments: {
                        conceptKeys: allConcepts,
                        resultInstanceIds: subsets
                    },
                    taskType: 'fetchData'
                }));
            }, function() {
                alert('Could not create subsets.');
            });
        });
    };

    function activateDragAndDrop(box) {
        var obj = Ext.get(box);
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
