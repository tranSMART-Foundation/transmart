<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
    </head>
    <body>
	<asset:javascript src="r-modules.js"/>

	<div id="analysisWidget">

	    <h2>
		Variable Selection
		<a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.heatMap ?: "JavaScript:D2H_ShowHelp(1505,helpURL,'wndExternal',CTXT_DISPLAY_FULLHELP )"}">
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
			    <button type="button" onclick="heatMapView.clear_high_dimensional_input('divIndependentVariable')">Clear</button>
			</div>
		    </div>

		    %{--Display independent variable--}%
		    <div id="displaydivIndependentVariable" class="independentVars"></div>

		    <label for="txtMaxDrawNumber">
			Max rows to display:
			<g:if test="${grailsApplication.config.org.transmartproject.helpUrls.heatMapMaxRows}">
			    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.heatMapMaxRows}">
				<asset:image src="help/helpicon_white.jpg" alt="Help" border="0"
					     width="18pt" style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;"/>
			    </a>
			</g:if>
		    </label>
		    <input type="text" id="txtMaxDrawNumber"  value="50"/>

		</fieldset>

		<fieldset class="toolFields">
		    <div>
			<input type="checkbox" id="chkGroupBySubject" name="doGroupBySubject"/>
			<span>Group by subject (instead of node) for multiple nodes</span>
		    </div>
		    <div>
			<input type="checkbox" id="chkCalculateZscore" name="calculateZscore"/>
			<span>Calculate z-score on the fly</span>
		    </div>
		    <input type="button" value="Run" onClick="heatMapView.submit_job(this.form);" class="runAnalysisBtn"/>
		</fieldset>
	    </div>

	</div>
    </body>
</html>
