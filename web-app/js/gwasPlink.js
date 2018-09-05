var GWASPlinkInsView
(function (_this) {

    var gwasPlinkView = function () {
        RmodulesView.call(this);
    }

    gwasPlinkView.prototype = new RmodulesView();
    gwasPlinkView.prototype.constructor = gwasPlinkView;
    GWASPlinkInsView = new gwasPlinkView();
})(this);


function showAdditionalPlinkAnalysisOptions() {
    if (jQuery('#analysisType option:selected').val() == "linear") {
        jQuery('.additionalPlinkAnalysisOptions').show();
        jQuery('.phenotypeOption').show();
        jQuery('.covariatesOption').show();
    } else if (jQuery('#analysisType option:selected').val() == "logistic" ) {
        jQuery('.additionalPlinkAnalysisOptions').show();
        jQuery('.phenotypeOption').hide();
        jQuery('.covariatesOption').show();
    } else {
        jQuery('.additionalPlinkAnalysisOptions').hide();
        jQuery('.phenotypeOption').show();
        jQuery('.covariatesOption').hide();
    }
}

function clearDiv(divId) {
    var div = jQuery('#' + divId);
    for (x = div.children().length - 1; x >= 0; x--) {
        var child = div.children()[x];
        child.remove();
    }
}
