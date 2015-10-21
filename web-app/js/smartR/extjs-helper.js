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

    var _fetchConceptPath = function (el) {
        var conceptId = el.getAttribute('conceptId').trim();
        var conceptIdPattern = /^\\\\[^\\]+(\\.*)$/;
        var match = conceptIdPattern.exec(conceptId);

        if (match != null) {
            return match[1];
        } else {
            return undefined;
        }
    };

    helper.readConceptVariables = function (elId) {
        var variableConceptPath = '';
        var variableEle = Ext.get(elId);

        //If the variable element has children, we need to parse them and concatenate their values.
        if (variableEle && variableEle.dom.childNodes[0]) {
            //Loop through the variables and add them to a comma separated list.
            for (var nodeIndex = 0; nodeIndex < variableEle.dom.childNodes.length; nodeIndex++) {
                //If we already have a value, add the separator.
                if (variableConceptPath != '') {
                    variableConceptPath += '|';
                }
                //Add the concept path to the string.
                variableConceptPath += _fetchConceptPath(variableEle.dom.childNodes[nodeIndex]);
            }
        }
        return variableConceptPath;
    };

    return helper;
})();
