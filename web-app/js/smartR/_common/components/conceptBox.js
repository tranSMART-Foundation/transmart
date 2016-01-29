//# sourceURL=conceptBox.js

"use strict";

smartR.components.conceptBox = function() {

    /* MODEL */
    var conceptBoxModel = {
        concepts: [], // objects of type Concept (i2b2common.js)

        clear: function ConceptBoxModel_clear() {
            this.concepts = [];
            this.trigger('clearConcepts');
        },

        addConcept: function ConceptBoxModel_setConcepts(/* Concept */ concept) {
            var dups = this.concepts.filter(function(el) {
                return el.key === concept.key;
            });
            if (dups.length > 0) {
                throw new Error('Duplicate concept key: ' + concept.key)
            }

            this.concepts.push(concept);
            this.trigger('newConcept', concept);
        },

        /* function getting an array of concept keys and returning
         * an object whose keys are n0, n1, ... and the values are
         * the concept keys passed. Throws if there duplicates. */
        getLabelledConcepts: function ConceptBoxModel_getLabelledConcepts() {
            var n = 0;
            var conceptKeys = this.concepts.map(function(concept) {
                return concept.key;
            });
            return conceptKeys.reduce(function(result, currentItem) {
                result['n' + n++] = currentItem;
                return result;
            }, {});
        }
    };

    jQuery.extend(conceptBoxModel, smartR.Observable.prototype);


    /* VIEW */
    function ConceptBoxView(conceptBoxModel) {
        this.conceptBoxModel = conceptBoxModel;
    }

    jQuery.extend(ConceptBoxView.prototype, {
        init: function ConceptBoxView_init(boxId, clearId) {
            this.boxExtJs = Ext.get(boxId);
            this.clearEl = jQuery('#' + clearId);

            /* bind UI elements */
            registerDropZone.call(this);
            this.clearEl.on('click', function() {
                this.conceptBoxModel.clear();
            }.bind(this));

            this.conceptBoxModel.on('clearConcepts', clearBox.bind(this));
            this.conceptBoxModel.on('newConcept', function(ev, concept) {
                window.createPanelItemNew(this.boxExtJs, concept);
            }.bind(this));
        }
    });

    function registerDropZone() {
        var div = this.boxExtJs;
        var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
        dtgI.notifyDrop = dropOntoNodeSelection.bind(this);
    }

    function dropOntoNodeSelection(source, e, data) {
        if (data.node.leaf === false && !data.node.isLoaded()) {
            data.node.reload(function() {
                dropOntoCategorySelection2.call(this, data);
            }.bind(this));
        } else {
            dropOntoCategorySelection2.call(this, data);
        }

        return true;
    }

    function dropOntoCategorySelection2(data) {
        var foundLeafNode;

        if (!data.node.leaf) { // is folder
            for (var i = 0; i < data.node.childNodes.length; i++) {
                var child = data.node.childNodes[i];
                if (!child.leaf) {
                    continue;
                }

                foundLeafNode = true;
                this.conceptBoxModel.addConcept(window.convertNodeToConcept(child));
            }

            if (!foundLeafNode) {
                Ext.Msg.alert('No Nodes in Folder',
                    'When dragging in a folder you must select a folder that has leaf nodes directly under it.');
            }
        } else {
            // convertNodeToConcept is a transmartApp function
            this.conceptBoxModel.addConcept(window.convertNodeToConcept(data.node));
        }
    }

    function clearBox() {
        var qc = this.boxExtJs;
        for (var i = qc.dom.childNodes.length - 1; i >= 0; i--) {
            var child = qc.dom.childNodes[i];
            qc.dom.removeChild(child);
        }
    }

    var conceptBoxView = new ConceptBoxView(conceptBoxModel);

    return {
        forView: {
            init: conceptBoxView.init.bind(conceptBoxView)
        },
        forModel: {
            getLabelledConcepts: conceptBoxModel.getLabelledConcepts.bind(conceptBoxModel)
        }
    };
};
