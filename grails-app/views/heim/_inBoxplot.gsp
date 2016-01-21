<r:require modules="smartR_boxplothack"/>
<r:layoutResources disposition="defer"/>
<style>
.text {
	font-family: 'Roboto', sans-serif;
	fill: black;
}

.boxplotValue {
	font-size: 9px;
}

#boxplot1 {
	background-color: white;
}

#boxplot2 {
	background-color: white;
}

.whisker {
	stroke: black;
	stroke-width: 1px;
}

.hinge {
	stroke: black;
	stroke-width: 1px;
}

.connection {
	stroke: black;
	stroke-width: 1px;
}

.point {
}

.point:hover {
	fill: red;
}

.outlier {
	stroke: red;
	stroke-width: 2px;
}

.brushed {
	fill: red;
	stroke: red;
}

.box {
	fill: #008BFF;
}

.box.upper {
	opacity: 0.3;
}

.box.lower {
	opacity: 0.2;
}

.line {
	fill: none;
	stroke: black;
	stroke-width: 1.5px;
}

.tooltip {
	position: absolute;
	text-align: center;
	display: inline-block;
	padding: 0px;
	font-size: 12px;
	font-weight: bold;
	color: black;
	background: white;
	pointer-events: none;
}

.contextMenu {
	position: absolute;
	text-align: center;
	display: inline-block;
	padding: 0px;
	font-size: 12px;
	font-weight: bold;
	color: black;
	background-color: white;
}

.brush .extent {
	fill: blue;
	opacity: .25;
	shape-rendering: crispEdges;

}

.axis path, .axis line {
	fill: none;
	stroke: black;
	stroke-width: 1px;
	shape-rendering: crispEdges;
}

.mybutton {
	width: 100%;
	height: 30px;
	color: black;
	background-color: white;
	font-size: 12px;
	font-weight: bold;
	border: none;
}

.mybutton:hover {
	color: white;
	background: #326FCB;
}
</style>
<table>
	<tr>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 1:</mark> Drop exactly one numerical node into this window.<br/>
			NOTE: This window maps to the first cohort!<br/>
			(Example: Age, Pulse, Blood Pressure)<br/>
			<div id='concept1' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('concept1')" value="Clear Window">
		</td>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 2 (optional):</mark> Drop any number of concepts subsetting the choice made on the left into this window.<br/>
			NOTE: This window maps to the first cohort!<br/>
			(Example: Demographics/Male, Demographics/Female)<br/>
			<div id='subsets1' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('subsets1')" value="Clear Window">
		</td>
	</tr>
	<tr>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 3:</mark> Drop exactly one numerical node into this window.<br/>
			NOTE: This window maps to the second cohort!<br/>
			(Example: Age, Pulse, Blood Pressure)<br/>
			<div id='concept2' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('concept2')" value="Clear Window">
		</td>
		<td style='padding-right: 2em; padding-bottom: 1em'>
			<mark>Step 4 (optional):</mark> Drop any number of concepts subsetting the choice made on the left into this window.<br/>
			NOTE: This window maps to the second cohort!<br/>
			(Example: Demographics/Male, Demographics/Female)<br/>
			<div id='subsets2' class="queryGroupIncludeSmall"></div>
			<input type="button" class='txt' onclick="clearVarSelection('subsets2')" value="Clear Window">
		</td>
	</tr>
</table>

<input id="submitButton" class='txt' type="button" value="(Re-)Run Analysis" onclick="runBoxplot()"/>
<div id="boxplot-d3"></div>
<script>
	activateDragAndDrop('concept1');
	activateDragAndDrop('concept2');
	activateDragAndDrop('subsets1');
	activateDragAndDrop('subsets2');

	function register() {
		registerConceptBox('concept1', [1], 'valueicon', 1, 1);
		registerConceptBox('concept2', [2], 'valueicon', 0, 1);
		registerConceptBox('subsets1', [1], 'alphaicon', 0, undefined);
		registerConceptBox('subsets2', [2], 'alphaicon', 0, undefined);
	}

	function getSettings() {
		return {};
	}

	function customSanityCheck() {
		var conceptBox2 = Ext.get('concept2').dom;
		if (conceptBox2.childNodes.length === 0 && ! isSubsetEmpty(2)) {
			alert('You have specified a second cohort but you have not defined a second concept!\nConsider placing the same concept in the second box.');
			return false;
		}
		return true;
	}
</script>
