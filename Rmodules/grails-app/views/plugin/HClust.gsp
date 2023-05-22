<!DOCTYPE html>
<html>
    <head>
    </head>
    <body>
	<asset:javascript src="r-modules.js"/>

	<div id="analysisWidget">

	    <h2>
		Variable Selection
		<a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.hierarchicalClustering ?: 'transmartmanual/advanced_workflow.html#hierarchical-clustering'}">
		    <asset:image src="help/helpicon_white.jpg" alt="Help" border="0"
		     width="18pt" style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;"/>
		</a>
	    </h2>

	    <div id="analysisForm">
		<fieldset class="inputFields">

		    %{--High dimensional input--}%
		    <div class="highDimContainer">
			<span>Select a High Dimensional Data node from the Data Set Explorer Tree and drag it into the box.</span>
			<div id='divIndependentVariable' class="queryGroupIncludeSmall highDimBox"></div>
			<div class="highDimBtns">
			    <button type="button" onclick="highDimensionalData.gather_high_dimensional_data('divIndependentVariable')">High Dimensional Data</button>
			    <button type="button" onclick="hierarchicalClusteringView.clear_high_dimensional_input('divIndependentVariable')">Clear</button>
			</div>
		    </div>

		    %{--Display independent variable--}%
		    <div id="displaydivIndependentVariable" class="independentVars"></div>

		    <label for="txtMaxDrawNumber">
			Max rows to display:
			<a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.hierarchicalClusteringMaxRows ?: '/transmartmanual/advanced_workflow.html#max-rows-to-display'}">
			    <asset:image src="help/helpicon_white.jpg" alt="Help"/>
			</a>
		    </label>
		    <input type="text" id="txtMaxDrawNumber"  value="50"/>

		    <label for="txtPixelsPerCell">
			Pixels per cell:
			10-50, or empty to calculate size
			    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.heatPixelsPerCell ?: '/transmartmanual/advanced_workflow.html#pixels-per-cell'}">
			    <asset:image src="help/helpicon_white.jpg" alt="Help"/>
			    </a>
		    </label>
		    <input type="text" id="txtPixelsPerCell"  value=""/>

		</fieldset>

		<fieldset class="toolFields">
		    <div>
			<input type="checkbox" id="chkClusterRows" name="doClusterRows" checked="true">
			<span>Apply clustering for rows</span>
		    </div>
		    <div>
			<input type="checkbox" id="chkClusterColumns" name="doClusterColumns" checked="true">
			<span>Apply clustering for columns</span>
		    </div>
		    <div>
			<input type="checkbox" id="chkCalculateZscore" name="calculateZscore">
			<span>Calculate z-score on the fly</span>
		    </div>
		    <input type="button" value="Run" onClick="hierarchicalClusteringView.submit_job(this.form);" class="runAnalysisBtn">
		</fieldset>
	    </div>

	</div>
    </body>
</html>
