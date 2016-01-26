HeimExtJSHelper = (function(){

    var helper = {};

    helper.dropOntoNodeSelection = function (source, e, data) {
        var analysisConcept;

        var targetdiv = this.el;

        if (data.node.leaf === false && !data.node.isLoaded()) {
            data.node.reload(function () {
                analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
            });
        }
        else {
            analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
        }

        return true;
    };

    helper.registerDropzone = function (el) {
        var div = Ext.get(el.attr('id'));
        var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
        dtgI.notifyDrop = helper.dropOntoNodeSelection;
    };

    helper.clear = function (el) {
        var div = Ext.get(el.attr('id'));
        //Clear the drag and drop div.
        var qc = Ext.get(div);
        for (var i = qc.dom.childNodes.length - 1; i >= 0; i--) {
            var child = qc.dom.childNodes[i];
            qc.dom.removeChild(child);
        }
    };

    var _fetchConceptPath = function (el) {
        return el.getAttribute('conceptId').trim();
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

    helper.createAnalysisConstraints = function (params) {
        var _conceptKeys = '';

        try {
            _conceptKeys = _generateLabels(params.conceptPaths.split(/\|/));
        } catch (err) {
            throw err;
        }

        // params.conceptPaths are actually keys...
        var _retval = {
            conceptKeys : _conceptKeys,
            resultInstanceIds: params.resultInstanceIds
            //projection: ''
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
     *
     * @param arr
     * @param conceptPaths concecatenated strings
     */
    helper.concatenateConceptPath = function (arr) {
        var allconcepts = '';
        arr.forEach(function (conceptPath, idx) {
            var _str = helper.readConceptVariables(conceptPath.inputEl.attr('id'));
            if (_str.trim().length > 0 ) {
                allconcepts +=  (idx > 0 ? '|' : '') + _str;
            }
        });
        return allconcepts;
    };


    return helper;
})();
