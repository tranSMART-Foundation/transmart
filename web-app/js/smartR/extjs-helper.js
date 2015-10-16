HeimExtJSHelper = (function(){

    var helper = {};

    helper.dropOntoNodeSelection = function (source, e, data) {
        var analysisConcept;

        console.log(source);
        console.log(e);
        console.log(data);
        console.log(this.el);

        var targetdiv = this.el;

        if (data.node.leaf === false && !data.node.isLoaded()) {
            data.node.reload(function () {
                analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
            });
        }
        else {
            analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
        }

        console.log(analysisConcept);
        return true;
    };

    helper.registerDropzone = function (el) {
        var div = Ext.get(el.attr('id'));
        var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
        dtgI.notifyDrop = helper.dropOntoNodeSelection;
    };

    helper.fetchConceptPath = function () {
        var conceptId = el.getAttribute('conceptId').trim();
        var conceptIdPattern = /^\\\\[^\\]+(\\.*)$/;
        var match = conceptIdPattern.exec(conceptId);

        if (match != null) {
            return match[1];
        } else {
            return undefined;
        }
    };

    return helper;
})();
