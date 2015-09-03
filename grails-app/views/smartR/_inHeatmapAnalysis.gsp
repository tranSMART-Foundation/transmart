<table>
	<tr>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 1:</mark> Drop high dimensional mRNA data node into this box.<br/>
			<div id='mRNAData1' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('mRNAData1')" value="Clear Window">
		</td>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 2 (optional):</mark> Drop high dimensional mRNA data node into this box for marker selection.<br/>
			<div id='mRNAData2' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('mRNAData2')" value="Clear Window">
		</td>
	</tr>
	<tr>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 3 (optional):</mark> Drop numerical low dimensional data for manual heatmap expansion into this box.<br/>
			<div id='additionalFeatures_numerical' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('additionalFeatures_numerical')" value="Clear Window">
		</td>
	</tr>
	<tr>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 4 (optional):</mark> Drop alphabetical low dimensional data for manual heatmap expansion into this box.<br/>
			<div id='additionalFeatures_alphabetical' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('additionalFeatures_alphabetical')" value="Clear Window">
		</td>
	</tr>
</table>

<mark>Step 4:</mark> Sort features/genes by significance according to...<br/>
<select id="significanceMeassure" class='txt'>
	<option value="zScoreRange">ZScore Range (Default)</option>
	<option value="variance">Variance</option>
</select><br/>
<br/>
<input type="checkbox" id="discardNullGenes" checked> Discard features/genes with no identifier/name<br/>
<br/>

<script>
	activateDragAndDrop('mRNAData1');
	activateDragAndDrop('mRNAData2');
	activateDragAndDrop('additionalFeatures_numerical');
	activateDragAndDrop('additionalFeatures_alphabetical');

	function register() {
		registerConceptBox('mRNAData1', 1, 'hleaficon', 1, 1);
		registerConceptBox('mRNAData2', 2, 'hleaficon', 0, 1);
		registerConceptBox('additionalFeatures_numerical', 1, 'valueicon', 0, undefined);
		registerConceptBox('additionalFeatures_alphabetical', 1, 'alphaicon', 0, undefined);
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
