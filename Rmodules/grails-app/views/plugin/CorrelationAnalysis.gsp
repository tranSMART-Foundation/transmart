<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
    </head>
    <body>
	<asset:javascript src="r-modules.js"/>

	<div id="analysisWidget">
	    <h2>
		Variable Selection
		<a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.correlationAnalysis ?: "JavaScript:D2H_ShowHelp(1290,helpURL,'wndExternal',CTXT_DISPLAY_FULLHELP )"}">
		    <asset:image src="help/helpicon_white.jpg" alt="Help"/>
		</a>
	    </h2>

	    <form id="analysisForm">
		<fieldset class="inputFields">
		    %{--High dimensional input--}%
		    <div class="highDimContainer">
			<span>Drag two or more <b>numerical</b> concepts from the tree into the box below that you wish to generate correlation statistics on.</span>
			<div id='divVariables' class="queryGroupIncludeSmall highDimBox"></div>
			<div class="highDimBtns">
			    <button type="button" onclick="correlationAnalysisView.clear_high_dimensional_input('divVariables')">Clear</button>
			</div>
		    </div>

		    <fieldset class="binningDiv">
			<label for="correlationType">Correlation Type</label>
			<select id = "correlationType">
			    <option value="spearman">Spearman</option>
			    <option value="pearson">Pearson</option>
			    <option value="kendall">Kendall</option>
			</select>
		    </fieldset>
		</fieldset>

		<fieldset class="toolFields">
		    <input type="button" value="Run" onClick="correlationAnalysisView.submit_job(this.form);" class="runAnalysisBtn">
		</fieldset>
	    </form>

	</div>
    </body>
</html>
