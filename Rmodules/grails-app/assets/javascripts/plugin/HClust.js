/**
 * Register drag and drop.
 * Clear out all global variables and reset them to blank.
 */
function loadHclustView(){
    hierarchicalClusteringView.clear_high_dimensional_input('divIndependentVariable');
    hierarchicalClusteringView.register_drag_drop();
}

// constructor
var HierarchicalClusteringView = function () {
    RmodulesView.call(this);
}

// inherit RmodulesView
HierarchicalClusteringView.prototype = new RmodulesView();

// correct the pointer
HierarchicalClusteringView.prototype.constructor = HierarchicalClusteringView;

// submit analysis job
HierarchicalClusteringView.prototype.submit_job = function () {
    var job = this;

    var actualSubmit = function() {
        // get formParams
        var formParams = job.get_form_params();

        if (formParams) { // if formParams is not null
            submitJob(formParams);
        }
    }

    // Check whether we have the node details for the HD node already
    // If not, we should fetch them first
    if (typeof GLOBAL.HighDimDataType !== "undefined" && GLOBAL.HighDimDataType) {
        actualSubmit();
    } else {
        var divId = 'divIndependentVariable';
        runAllQueriesForSubsetId(function () {
            highDimensionalData.fetchNodeDetails(divId, function( result ) {
                highDimensionalData.data = JSON.parse(result.responseText);
                highDimensionalData.populate_data();
                actualSubmit();
            });
        }, divId);
    }
}


// get form params
HierarchicalClusteringView.prototype.get_form_params = function () {
    var formParameters = {}; // init

    //Use a common function to load the High Dimensional Data params.
    loadCommonHighDimFormObjects(formParameters, "divIndependentVariable");

    // instantiate input elements object with their corresponding validations
    var inputArray = this.get_inputs(formParameters);

    // define the validator for this form
    var formValidator = new FormValidator(inputArray);

    if (formValidator.validateInputForm()) { // if input files satisfy the validations

        // get values
        var inputConceptPathVar = readConceptVariables("divIndependentVariable");
        var maxDrawNum = inputArray[1].el.value;
        var pxPerCell = inputArray[2].el.value;
        var doClusterRows = inputArray[3].el.checked;
        var doClusterColumns = inputArray[4].el.checked;
        var calculateZscore = inputArray[5].el.checked;

        // assign values to form parameters
        formParameters['jobType'] = 'RHClust';
        formParameters['independentVariable'] = inputConceptPathVar;
        formParameters['variablesConceptPaths'] = inputConceptPathVar;
        formParameters['txtMaxDrawNumber'] = maxDrawNum;
        formParameters['txtPixelsPerCell'] = pxPerCell;
        formParameters['doClusterRows'] = doClusterRows;
        formParameters['doClusterColumns'] = doClusterColumns;
        formParameters['calculateZscore'] = calculateZscore;

        //get analysis constraints
        var constraints_json = this.get_analysis_constraints('RHClust');
        if(calculateZscore){
        	constraints_json['projections'] = ["log_intensity"];
        }else{
        	constraints_json['projections'] = ["zscore"];
        }
        formParameters['analysisConstraints'] = JSON.stringify(constraints_json);

    } else { // something is not correct in the validation
        // empty form parameters
        formParameters = null;
        // display the error message
        formValidator.display_errors();
    }

    return formParameters;
}

HierarchicalClusteringView.prototype.get_inputs = function (form_params) {
    return  [
        {
            "label" : "High Dimensional Data",
            "el" : Ext.get("divIndependentVariable"),
            "validations" : [
                {type:"REQUIRED"},
                {
                    type:"HIGH_DIMENSIONAL",
                    high_dimensional_type:form_params["divIndependentVariableType"],
                    high_dimensional_pathway:form_params["divIndependentVariablePathway"]
                }
            ]
        },
        {
            "label" : "Max Rows to Display",
            "el" : document.getElementById("txtMaxDrawNumber"),
            "validations" : [{type:"INTEGER", min:1}]
        },
        {
            "label" : "Pixels per Cell",
            "el" : document.getElementById("txtPixelsPerCell"),
            "validations" : [{type:"INTEGER", min:10, max:50}]
        },
        {
            "label" : "Do cluster rows",
            "el" : document.getElementById("chkClusterRows"),
            "validations" : []
        },
        {
            "label" : "Do cluster columns",
            "el" : document.getElementById("chkClusterColumns"),
            "validations" : []
        },
        {
            "label" : "Calculate z-score on the fly",
            "el" : document.getElementById("chkCalculateZscore"),
            "validations" : []
        }
    ];
}


// init heat map view instance
var hierarchicalClusteringView = new HierarchicalClusteringView();
