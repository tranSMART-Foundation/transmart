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
        scripts: false
    },
    listeners: {
        activate: function() {
            activateDragAndDrop();
        }
    }
});


window.addFractalisPanel = function addFractalisPanel(parentPanel) {
    parentPanel.insert(4, window.fractalisPanel);
};

function activateDragAndDrop () {
    var conceptBox = document.querySelector('.fjs-concept-box');
    var extObj = Ext.get(conceptBox);
    var dtgI = new Ext.dd.DropTarget(extObj, {ddGroup: 'makeQuery'});
    dtgI.notifyDrop = dropOntoCategorySelection;
}
