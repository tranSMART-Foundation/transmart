//# sourceURL=bioMarkersModel.js

window.BioMarkersModel = (function() {
    var BioMarkersModel = function BioMarkersModel() {
        this.selectedBioMarkers = {};
    };
    BioMarkersModel.prototype = Object.create(smartR.Observable.prototype);
    jQuery.extend(BioMarkersModel.prototype, {
        addBioMarker: function BioMarkersModel_addBioMarker(id, type, name, synonyms) {
            if (this.selectedBioMarkers.hasOwnProperty(id)) {
                return; // nothing to do
            }
            this.selectedBioMarkers[String(id)] = {
                type:     type,
                name:     name,
                synonyms: synonyms
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
                    var v = this.selectedBioMarkers[id];
                    return {
                        id: id,
                        type: v.type,
                        name: v.name,
                        synonyms: v.synonyms
                    };
                }.bind(this));
        }
    });
    BioMarkersModel.prototype.constructor = BioMarkersModel;

    return BioMarkersModel;
})();
