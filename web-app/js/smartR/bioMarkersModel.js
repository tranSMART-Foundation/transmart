//# sourceURL=bioMarkersModel.js

window.BioMarkersModel = (function() {
    var Observable = function Observable() {
        this.jquery = jQuery(this);
    };
    jQuery.extend(Observable.prototype, {
        on: function() {
            this.jquery.on.apply(this.jquery, arguments);
            return this;
        },
        trigger: function() {
            this.jquery.trigger.apply(this.jquery, arguments);
            return this;
        },
        unbind: function() {
            this.jquery.unbind.apply(this.jquery, arguments);
            return this;
        }
    });

    var BioMarkersModel = function BioMarkersModel() {
        Observable.call(this)
        this.selectedBioMarkers = {};
    };
    BioMarkersModel.prototype = Object.create(Observable.prototype)
    jQuery.extend(BioMarkersModel.prototype, {
        addBioMarker: function BioMarkersModel_addBioMarker(id, type, name, synonyms) {
            if (this.selectedBioMarkers.hasOwnProperty(id)) {
                return; // nothing to do
            }

            this.selectedBioMarkers[String(id)] = {
                type:     type,
                name:     name,
                synonyms: synonyms,
            };
            this.trigger('biomarkers');
        },
        removeBioMarker: function BioMarkersModel_removeBioMarker(id) {
            if (this.selectedBioMarkers.hasOwnProperty(id)) {
                delete this.selectedBioMarkers[id];
                this.trigger('biomarkers');
            }
        },
        getBioMarkers: function BioMarkersModel_getBioMarkers() {
            return Object.getOwnPropertyNames(this.selectedBioMarkers).map(
                function(id) {
                    var v = this.selectedBioMarkers[id]
                    return {
                        id: id,
                        type: v.type,
                        name: v.name,
                        synonyms: v.synonyms
                    };
                }.bind(this));
        },
    });
    BioMarkersModel.prototype.constructor = BioMarkersModel;

    return BioMarkersModel;
})();
