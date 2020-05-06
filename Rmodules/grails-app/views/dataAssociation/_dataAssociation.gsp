%{--Toolbar--}%
<div id="toolbar"></div>

%{-- Data Association Content --}%
<div id="dataAssociationBody">

    %{-- Variable Selection --}%
    <div class="subsettable" >

	%{--display selected analysis--}%
	<label for="selectedAnalysis">
	    <span>Analysis:</span>
	    <span id="selectedAnalysis" class="warning">WARNING: Analysis is not selected</span>

	    <a href='/transmartmanual/advanced_workflow.html'>
		<asset:image src="help/helpicon_white.jpg" alt="Help" border="0"
		     width="18pt" style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;"/>
	    </a>
	
	    <input type="hidden" id="analysis" name="analysis"/>
	</label>
	<hr style="height: 1px;"/>
	%{--display selected cohort--}%
	<g:logMsg>render dataAssociation empty page</g:logMsg>
        <label for="cohortSummary">
	    <span>Cohorts:</span>
	    <span id="cohortWarningMsg" class="warning">
	      WARNING: You have not selected a study and the analysis will not work.
	      Please go back to the Comparison tab and make a cohort selection.
	    </span>
	    <div id="cohortSummary"></div>
	</label>
    </div>
  
</div>
  				
%{-- Variable Selection --}%
<div id="variableSelection"></div>

%{-- Page Break for PDF--}%
<div style="page-break-after:always"></div>

%{-- Analysis Output --}%
<div id="analysisOutput" ></div>
