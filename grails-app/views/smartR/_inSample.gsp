<div id='data' class="queryGroupIncludeSmall"></div>
<input type="button" class='txt' onclick="clearVarSelection('data')" value="Clear Window"><br/>

<input type="checkbox" id="doStuff" checked> Do stuff?<br/>

<script>
	activateDragAndDrop('data');

	// MUST be implemented
	function register() {
		// this registers the concept box which will enable automatic error checks
		// and make sure that the concepts within are downloaded
		// parameters are conceptBoxName, cohort (1 or 2), type of containing
		// concepts, minimum and maximum number of concepts to choose
		registerConceptBox('data', [1], 'valueicon', 1, undefined);
	}

	// MUST be implemented
	function getSettings() {
		// create a map with all your settings. It is up to you how to parse them
	    var settings = {};
	    settings.doStuff = jQuery('#doStuff').is(':checked') ? 1 : 0;
	    return settings;
	}

	// MUST be implemented
	function customSanityCheck() {
		// sometimes one needs more sophisticated sanity checks (see boxplot)
		return true;
	}
</script>
