<mark>Step 1:</mark> Drop any number of timepoints into this window.<br/>
(Example: BloodPressure/Visit1, BloodPressure/Visit2, ... AND Pulse/Visit1, Pulse/Visit2, ...)<br/>
(Note: This works with any numerical variable (such as Age) across different folders/concepts)<br/>
<div id='datapoints' class="queryGroupIncludeSmall"></div>
<input type="button" class='txt' onclick="clearVarSelection('datapoints')" value="Clear Window"><br/>
<br/>

<script>
	activateDragAndDrop('datapoints');

	function register() {
		registerConceptBox('datapoints', [1], 'valueicon', 2, undefined);
	}

	function getSettings() {
		return {};
	}

	function customSanityCheck() {
		return true;
	}
</script>