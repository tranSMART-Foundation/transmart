<mark>Step 1:</mark> Drop high dimensional mRNA data node into this box.<br/>
<div id='mRNAData' class="queryGroupIncludeSmall"></div>
<input type="button" class='txt' onclick="clearVarSelection('mRNAData')" value="Clear Window"><br/>
<br/>
<mark>Step 2 (optional):</mark> Drop low dimensional data for manual heatmap expansion into this box.<br/>
<div id='additionalFeatures_numerical' class="queryGroupIncludeSmall"></div>
<input type="button" class='txt' onclick="clearVarSelection('additionalFeatures_numerical')" value="Clear Window"><br/>
<br/>
<mark>Step 3:</mark> Sort features/genes by significance according to...<br/>
<select id="significanceMeassure" class='txt'>
	<option value="zScoreRange">ZScore Range (Default)</option>
	<option value="variance">Variance</option>
</select><br/>
<br/>
<input type="checkbox" id="discardNullGenes" checked> Discard features/genes with no identifier/name<br/>
<br/>

<script>
	activateDragAndDrop('mRNAData');
	activateDragAndDrop('additionalFeatures_numerical');

	function register() {
		registerConceptBox('mRNAData', 1, 'hleaficon', 1, 1);
		registerConceptBox('additionalFeatures_numerical', 1, 'valueicon', 0, undefined);
	}

	function getSettings() {
	    var settings = {significanceMeassure: jQuery('#significanceMeassure').val()};
	    settings.discardNullGenes = jQuery('#discardNullGenes').is(':checked') ? 1 : 0;
	    return settings;
	}

	function customSanityCheck() {
		return true;
	}
</script>
