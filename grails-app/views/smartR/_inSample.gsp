<div id='somedata' class="queryGroupIncludeSmall"></div>
<input type="button" class='txt' onclick="clearVarSelection('somedata')" value="Clear Window"><br/>

<input type="checkbox" id="doStuff" checked> Do stuff?<br/>

<script>
	activateDragAndDrop('somedata');

	// MUST be implemented
	function register() {
		// this registers the concept box which will enable automatic error checks
		// and make sure that the concepts within are queried from the database
		// parameters are conceptBoxName, cohort ([1] or [2] or [1,2]), type of containing
		// concepts (valueicon, alphaicon, hleaficon), minimum and maximum number of concepts to choose
		registerConceptBox('somedata', [1], 'valueicon', 1, undefined);
	}

	// MUST be implemented, even if empty
	function getSettings() {
		// create a map with all your settings. It is up to you how to parse them later on
		// example settings: {foo: 1, bar: ['blub'], foobar: [1,2,3,4,5]}
	    var settings = {};
	    settings.doStuff = jQuery('#doStuff').is(':checked') ? 1 : 0;
	    return settings;
	}

	// MUST be implemented and return true for the sanity check to pass, false otherwise
	function customSanityCheck() {
		// sometimes one needs more sophisticated sanity checks (see boxplot)
		return true;
	}
</script>
