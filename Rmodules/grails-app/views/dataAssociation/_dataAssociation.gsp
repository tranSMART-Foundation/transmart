%{--Toolbar--}%
<div id="toolbar"></div>

%{-- Data Association Content --}%
<div id="dataAssociationBody">

    %{-- Variable Selection --}%
    <div class="subsettable" >

	%{--display selected analysis--}%
	<label for="selectedAnalysis">
		<span>Analysis:</span>
		<span id="selectedAnalysis" style="color: green" class="gentle_warning">Select an analysis from the drop-down menu above</span>

		%{-- TBD: help hyperlink
        <a href='JavaScript:D2H_ShowHelp(1503,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
            <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help" border=0
                 width=18pt style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;"/>
        </a>--}%

		<input type="hidden" id="analysis" name="analysis"/>
	</label>
	<hr style="height: 1px; margin-top: 5px"/>
	%{--display selected cohort--}%
	<label for="cohortSummary">
		<span>Cohorts:</span>
		<span id="cohortWarningMsg" style="color: red">WARNING: You have not selected a study and the analysis will not work. Please go back to the Comparison tab and make a cohort selection.
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