/**
*   Gets the x position of the mouse on the screen (TODO: this is not as precise as I want it to be)
*
*   @return {int}: x coordinate of the mouse
*/
function mouseX() {
    var mouseXPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageX : d3.event.clientX;
    return mouseXPos - jQuery("#westPanel").width() + 20;
}

/**
*   Gets the y position of the mouse on the screen (TODO: this is not as precise as I want it to be)
*
*   @return {int}: y coordinate of the mouse
*/
function mouseY() {
    var mouseYPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageY : d3.event.clientY;
    return mouseYPos + jQuery("#index").parent().scrollTop() - 50;
}

/**
*   Finds the maximum width of several drawn text elements
*
*   @param {[]} elements: array of already drawn text elements
*   @return {int}: maximal width of given elements
*/
function getMaxWidth(elements) {
    var MIN_SAFE_INTEGER = -(Math.pow(2, 53) - 1);
    var currentMax = MIN_SAFE_INTEGER;
    elements.each(function() {
        var len = this.getBBox().width;
        if (len > currentMax) {
            currentMax = len;
        }
    });
    return currentMax;
}

/**
*   Checks if array is sorted (descending)
*
*   @param {[]} arr: arbitray array
*   @return {bool}: true if array is sorted descending, false otherwise
*/
function isSorted(arr) {
    var sorted = true;

    for (var i = 0, len = arr.length - 1; i < len; i++) {
        if (arr[i][1] < arr[i+1][1]) {
            sorted = false;
            break;
        }
    }
    return sorted;
}

/**
*   Compares two arrays with each other
*
*   @param {[]} arr1: first array
*   @param {[]} arr2: second array
*   @return {bool}: true if arrays are equal, false otherwise
*/
function arrEqual(arr1, arr2) {
    if (arr1.length !== arr2.length) {
        return false;
    }
    for (var i = 0, len1 = arr1.length; i < len1; i++) {
        var found = false;
        for (var j = 0, len2 = arr2.length; j < len2; j++) {
            if (arr2[j] === arr1[i]) {
                found = true;
            }
        }
        if (! found) {
            return false;
        }
    }
    return true;
}

/**
*   Creates a special object which can be used for updating the cohorts
*
*   @param {string} ...: I suggest to just use your browser to check all parameters of such an element and copy them
*   @return {{}}: object containing specified constrains for cohort selection update
*/
function createQueryCriteriaDIV(conceptid, normalunits, setvaluemode, setvalueoperator, setvaluelowvalue, setvaluehighvalue, setvalueunits, oktousevalues, setnodetype) {
    return {
        conceptid : conceptid,
        conceptname : shortenConcept(conceptid),
        concepttooltip : conceptid.substr(1, conceptid.length),
        conceptlevel : '',
        concepttablename : "CONCEPT_DIMENSION",
        conceptdimcode : conceptid,
        conceptcomment : "",
        normalunits : normalunits,
        setvaluemode : setvaluemode,
        setvalueoperator : setvalueoperator,
        setvaluelowvalue : setvaluelowvalue,
        setvaluehighvalue : setvaluehighvalue,
        setvaluehighlowselect : "N",
        setvalueunits : setvalueunits,
        oktousevalues : oktousevalues,
        setnodetype : setnodetype,
        visualattributes : "LEAF,ACTIVE",
        applied_path : "@",
        modifiedNodePath : "undefined",
        modifiedNodeId : "undefined",
        modifiedNodeLevel : "undefined"
    };
}

/**
*   Updates the cohort selection in the Comparison tab of tranSMART
*
*   @param {[]} constrains: contains objects created by createQueryCriteriaDIV()
*   @param {boolean} andConcat: should constrains be added via OR or AND?
*   @param {boolean} negate: should constraints be including or excluding?
*   @param {boolean} reCompute: should the current visualization be recomputed after updating the cohorts? (for large db queries it is faster to just handle the update within the visualization itself)
*/
function setCohorts(constrains, andConcat, negate, reCompute, subset) {
    if (typeof appendItemFromConceptInto !== "function") { 
        alert('This functionality is not available in the tranSMART version you use.');
        return;
    }
    if (! confirm("Attention! This action will have the following impact:\n1. Your cohort selection in the 'Comparison' tab will be modified.\n2. Your current analysis will be recomputed based on this selection.\n")) {
        return;
    }

    subset = subset === undefined ? 1 : subset;
    var destination;
    if (andConcat) {
        destination = 1; // TODO; does this makes sense?
    } else {
        destination = jQuery(jQuery("#queryTable tr:last-of-type td")[subset - 1]).find('div[id^=panelBoxList]').last();
    }
    for(var i = 0, len = constrains.length; i < len; i++) {
        appendItemFromConceptInto(destination, constrains[i], negate);
    }
    if (reCompute) {
        runAllQueries(runRScript);
    }
}

/**
*   Shorten any path /A/B/.../X/Y/Z/ to /Y/Z
*
*   @param {string} concept: concept/path to shorten
*   @return {string}: shortened concept/path
*/
function shortenConcept(concept) {
    var splits = concept.split('\\');
    return splits[splits.length - 3] + '/' + splits[splits.length - 2];
}

/**
*   I copied this and have no idea what this does but activating drag and drop for a given div
*
*   @param {string} divName: name of the div element to activate drag and drop for
*/
function activateDragAndDrop(divName) {
    var div = Ext.get(divName);
    var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
    dtgI.notifyDrop = dropOntoCategorySelection;
}

// Panel item for the SmartR plugin
var smartRPanel = new Ext.Panel({
    id: 'smartRPanel',
    title: 'SmartR',
    region: 'center',
    split: true,
    height: 90,
    layout: 'fit',
    collapsible: true,
    autoScroll: true,
    tbar: new Ext.Toolbar({
        id: 'smartRToolbar',
        title: 'R Scripts',
        items: []
    }),
    autoLoad: {
        url: pageInfo.basePath + '/smartR/index',
        method: 'POST',
        evalScripts: false
    },
    listeners: {
        render: function(panel) {
            panel.body.on('click', function() {
                if (typeof updateOnView === "function") {
                    updateOnView();  
                } 
            });
        }
    }
});

/**
*   Clears drag & drop selections from the given div
*
*   @param {string} divName: name of the div element to clear
*/
function clearVarSelection(divName) {
    var div = Ext.get(divName).dom;
    while (div.firstChild) {
        div.removeChild(div.firstChild);
    }
}

/**
*   Returns the concepts defined via drag & drop from the given div
*
*   @param {string} divName: name of the div to get the selected concepts from
*   @return {string[]}: array of found concepts
*/
function getConcepts(divName) {
    var div = Ext.get(divName);
    div = div.dom;
    var variables = [];
    for (var i = 0, len = div.childNodes.length; i < len; i++) {
        variables.push(div.childNodes[i].getAttribute('conceptid'));
    }
    return variables;
}

/**
*   Expands the settings of the form data for a given setting
*
*   @param data: form data recieved by prepareFormData()
*   @param settings: json like string representation of the settings we want to add
*   @return: data with added settings
*/
function addSettingsToData(data, settings) {
    for (var i = 0; i < data.length; i++) {
        var element = data[i];
        if (element.name == "settings") {
            var json = JSON.parse(element.value);
            json = jQuery.extend(json, settings);
            element.value = JSON.stringify(json);
            break;
        }
    }
    return data;
}

var conceptBoxes = [];
var sanityCheckErrors = [];
function registerConceptBox(name, cohorts, type, min, max) {
    var concepts = getConcepts(name);
    var check1 = type === undefined || containsOnly(name, type);
    var check2 = min === undefined || concepts.length >= min;
    var check3 = max === undefined || concepts.length <= max;
    sanityCheckErrors.push( 
        !check1 ? 'Concept box (' + name + ') contains concepts with invalid type! Valid type: ' + type :
        !check2 ? 'Concept box (' + name + ') contains too few concepts! Valid range: ' + min + ' - ' + max :
        !check3 ? 'Concept box (' + name + ') contains too many concepts! Valid range: ' + min + ' - ' + max : '');
    conceptBoxes.push({name: name, cohorts: cohorts, type: type, concepts: concepts});
}

/**
*   Prepares data for the AJAX call containing all neccesary information for computation
*
*   @return {[]}: array of objects containing the information for server side computations
*/
function prepareFormData() {
    var data = [];
    data.push({name: 'conceptBoxes', value: JSON.stringify(conceptBoxes)});
    data.push({name: 'result_instance_id1', value: GLOBAL.CurrentSubsetIDs[1]});
    data.push({name: 'result_instance_id2', value: GLOBAL.CurrentSubsetIDs[2]});
    data.push({name: 'script', value: jQuery('#scriptSelect').val()});
    data.push({name: 'settings', value: JSON.stringify(getSettings())});
    return data;
}

/**
*   Checks whether the given div only contains the specified icon/leaf
*
*   @param {string} divName: name of the div to check
*   @param {string} icon: icon type to look for (i.e. valueicon or hleaficon)
*   @return {bool}: true if div only contains the specified icon type
*/
function containsOnly(divName, icon) {
    var div = Ext.get(divName).dom;
    for (var i = 0, len = div.childNodes.length; i < len; i++) {
        if (div.childNodes[i].getAttribute('setnodetype') !== icon &&
                icon !== 'alphaicon') { // FIXME: this is just here so SmartR works on the current master branch
            return false;
        }
    }
    return true;
}

/**
*   Checks for general sanity of all parameters and decided which script specific sanity check to call
*
*   @return {bool}: returns true if everything is fine, false otherwise
*/
function sane() { // FIXME: somehow check for subset2 to be non empty iff two cohorts are needed
    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
        alert('No cohorts have been selected. Please drag&drop cohorts to the fields within the "Comparison" tab');
        return false;
    }

    if (jQuery("#scriptSelect").val() === '') {
        alert('Please select the algorithm you want to use!');
        return false;
    }
    for (var i = 0; i < sanityCheckErrors.length; i++) {
        var sanityCheckError = sanityCheckErrors[i];
        if (sanityCheckError !== '') {
            alert(sanityCheckError);
            return false;
        }
    }
    return customSanityCheck(); // method MUST be implemented by _inFoobarAnalysis.gsp
}

/**
*   Initial method for the whole process of computing a visualization
*/
function runRScript() {
    conceptBoxes = [];
    sanityCheckErrors = [];
    register(); // method MUST be implemented by _inFoobarAnalysis.gsp

    if (! sane()) {
        return;
    }
    
    // if no subset IDs exist compute them
    if(!(isSubsetEmpty(1) || GLOBAL.CurrentSubsetIDs[1]) || !( isSubsetEmpty(2) || GLOBAL.CurrentSubsetIDs[2])) {
        runAllQueries(runRScript);
        return false;
    }

    jQuery("#outputDIV").html("Fetching data from database. This might last up to several minutes...");
    jQuery.ajax({
        url: pageInfo.basePath + '/SmartR/renderOutputDIV',
        type: "POST",
        timeout: '600000',
        data: prepareFormData()
    }).done(function(serverAnswer) {
        jQuery("#outputDIV").html(serverAnswer);
    }).fail(function() {
        jQuery("#outputDIV").html("AJAX CALL FAILED!");
    });
}

/**
*   Renders the input form for entering the parameters for a visualization/script
*/
function changeInputDIV() {
    jQuery("#outputDIV").html("");

    jQuery.ajax({
        url: pageInfo.basePath + '/SmartR/renderInputDIV',
        type: "POST",
        timeout: '600000',
        data: {'script': jQuery('#scriptSelect').val()}
    }).done(function(serverAnswer) {
        jQuery("#inputDIV").html(serverAnswer);
    }).fail(function() {
        jQuery("#inputDIV").html("AJAX CALL FAILED!");
    });
}
