<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
    </head>
    <body>
	<asset:javascript src="r-modules.js"/>

	<div id="analysisWidget">

	    <h2>
		Variable Selection
		<a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.kMeansClustering ?: '/transmartmanual/advanced_workflow.html#k-means-clustering'}">
		    <asset:image src="help/helpicon_white.jpg" alt="Help"/>
		</a>
	    </h2>

	    <form id="analysisForm">
		<fieldset class="inputFields">

		    %{--High dimensional input--}%
		    <div class="highDimContainer">
			<span>Select a High Dimensional Data node from the Data Set Explorer Tree and drag it into the box.</span>
			<div id='divIndependentVariable' class="queryGroupIncludeSmall highDimBox"></div>
			<div class="highDimBtns">
			    <button type="button" onclick="highDimensionalData.gather_high_dimensional_data('divIndependentVariable')">High Dimensional Data</button>
			    <button type="button" onclick="kmeansClustering.clear_high_dimensional_input('divIndependentVariable')">Clear</button>
			</div>
		    </div>

		    %{--Display independent variable--}%
		    <div id="displaydivIndependentVariable" class="independentVars"></div>

		    <label for="txtClusters">Number of clusters:</label>
		    <input type="text" id="txtClusters" value="2"/>

		    <label for="txtMaxDrawNumber">
			Max rows to display:
			<a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.kMeansClusteringMaxRows ?: '/transmartmanual/advanced_workflow.html#max-rows-to-display'}">
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
		    <input type="button" value="Run" onClick="kmeansClustering.submit_job(this.form);" class="runAnalysisBtn">
		</fieldset>

	    </form>

	</div>
    </body>
</html>
