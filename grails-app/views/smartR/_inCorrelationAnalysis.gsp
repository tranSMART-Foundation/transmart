<mark>Step 1:</mark> Drop two numerical variables into this window.<br/>
(Example: Age & Survival Time)<br/>
<div id='datapoints' class="queryGroupIncludeSmall"></div>
<input type="button" class='txt' onclick="clearVarSelection('datapoints')" value="Clear Window"><br/>
<br/>
<mark>Step 2 (optional):</mark> Drop annotations into this window.<br/>
(Example: TumorStages/NA, Tumor Stages/T1, Tumor Stages/T2)<br/>
<div id='annotations' class="queryGroupIncludeSmall"></div>
<input type="button" class='txt' onclick="clearVarSelection('annotations')" value="Clear Window"><br/>
<br/>
<mark>Step 3:</mark> Choose a method for the correlation analysis.<br/>
<select id="methodSelect" class='txt'>
	<option value="pearson">Pearson (Default)</option>
	<option value="kendall">Kendall</option>
	<option value="spearman">Spearman</option>
</select><br/>
<br/>

<script>
	activateDragAndDrop('datapoints');
	activateDragAndDrop('annotations');

	function register() {
		registerConceptBox('datapoints', [1], 'valueicon', 2, 2);
		registerConceptBox('annotations', [1], 'alphaicon', 0, undefined);
	}

	function getSettings() {
	    return {method: jQuery('#methodSelect').val()};
	}

	function customSanityCheck() {
		return true;
	}
</script>
