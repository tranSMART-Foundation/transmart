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
        jQuery('.additionalPlinkAnalysisOptions').each(function(i,e){e.show();});
        jQuery('.phenotypeOption').each(function(i,e){e.show();});
        jQuery('.covariatesOption').each(function(i,e){e.show();});
    } else if (jQuery('#analysisType option:selected').val() == "logistic" ) {
        jQuery('.additionalPlinkAnalysisOptions').each(function(i,e){e.show();});
        jQuery('.phenotypeOption').each(function(i,e){e.hide();});
        jQuery('.covariatesOption').each(function(i,e){e.show();});
    } else {
        jQuery('.additionalPlinkAnalysisOptions').each(function(i,e){e.hide();});
        jQuery('.phenotypeOption').each(function(i,e){e.show();});
        jQuery('.covariatesOption').each(function(i,e){e.hide();});
    }
}

function clearDiv(divId) {
    var div = jQuery('#' + divId);
    for (x = div.children().length - 1; x >= 0; x--) {
        var child = div.children()[x];
        child.remove();
    }
}
