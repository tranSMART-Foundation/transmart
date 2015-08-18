<table>
	<tr>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 1:</mark> Drop exactly one numerical node into this window.<br/>
			NOTE: This window maps to the first cohort!<br/>
			(Example: Age, Pulse, Blood Pressure)<br/>
			<div id='concept_cohort1' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('concept_cohort1')" value="Clear Window">
		</td>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 2 (optional):</mark> Drop any number of concepts subsetting the choice made on the left into this window.<br/>
			NOTE: This window maps to the first cohort!<br/>
			(Example: Demographics/Male, Demographics/Female)<br/>
			<div id='subsets_cohort1' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('subsets_cohort1')" value="Clear Window">
		</td>
	</tr>
	<tr>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 3:</mark> Drop exactly one numerical node into this window.<br/>
			NOTE: This window maps to the second cohort!<br/>
			(Example: Age, Pulse, Blood Pressure)<br/>
			<div id='concept_cohort2' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('concept_cohort2')" value="Clear Window">
		</td>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 4 (optional):</mark> Drop any number of concepts subsetting the choice made on the left into this window.<br/>
			NOTE: This window maps to the second cohort!<br/>
			(Example: Demographics/Male, Demographics/Female)<br/>
			<div id='subsets_cohort2' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('subsets_cohort2')" value="Clear Window">
		</td>
	</tr>
</table>




<script>
	activateDragAndDrop('concept_cohort1');
	activateDragAndDrop('concept_cohort2');
	activateDragAndDrop('subsets_cohort1');
	activateDragAndDrop('subsets_cohort2');

	function register() {
		registerConceptBox('concept_cohort1', 1, 'valueicon', 1, 1);
		registerConceptBox('concept_cohort2', 2, 'valueicon', 0, 1);
		registerConceptBox('subsets_cohort1', 1, 'alphaicon', 0, undefined);
		registerConceptBox('subsets_cohort2', 2, 'alphaicon', 0, undefined);
	}

	function getSettings() {
		return {};
	}

	function customSanityCheck() {
		var conceptBox2 = Ext.get('concept_cohort2').dom;
		if (conceptBox2.childNodes.length === 0 && ! isSubsetEmpty(2)) {
			alert('You have specified a second cohort but you have not defined a second concept!\nConsider placing the same concept in the second box.');
			return false;
		}
		return true;
	}
</script>
