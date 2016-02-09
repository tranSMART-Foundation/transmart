<r:require modules="smartR_correlation"/>
<r:layoutResources/>

<div ng-controller="CorrelationController">

    <tab-container>

        <workflow-tab tab-name="Fetch Data">
            <concept-box concept-group="conceptBoxes.datapoints"></concept-box>
            <concept-box concept-group="conceptBoxes.annotations"></concept-box>
            <br/>
            <br/>
            <fetch-button concept-map="conceptBoxes"></fetch-button>
        </workflow-tab>

        <workflow-tab tab-name="Run Analysis">
            <run-button results-storage="scriptResults" script-to-run="run" button-name="Create Plot"></run-button>
            <br/>
            <br/>
            <correlation-plot data="scriptResults" width="1200" height="1200"></correlation-plot>
        </workflow-tab>

    </tab-container>

</div>



        %{--<form name="sr-correlation-fetch-form">--}%
            %{--<div id="sr-conceptBox-data" class="queryGroupIncludeSmall"></div>--}%
            %{--<input type="button" onclick="clearVarSelection('sr-conceptBox-data')" value="Clear Window"><br/>--}%
            %{--<br/>--}%

            %{--<div id='sr-conceptBox-annotations' class="queryGroupIncludeSmall"></div>--}%
            %{--<input type="button" onclick="clearVarSelection('sr-conceptBox-annotations')" value="Clear Window"><br/>--}%
            %{--<br/>--}%
        %{--</form>--}%
    %{--</div>--}%
%{--</div>--}%

%{--<select id="methodSelect">--}%
    %{--<option value="pearson">Pearson (Default)</option>--}%
    %{--<option value="kendall">Kendall</option>--}%
    %{--<option value="spearman">Spearman</option>--}%
%{--</select><br/>--}%

%{--<button id="sr-btn-fetch-correlation" class="sr-action-button">Fetch Data</button>--}%
%{--<button id="sr-btn-run-correlation" class="sr-action-button">Run Correlation Analysis</button>--}%

%{--<div id="visualization" class="sr-output-container">--}%
    %{--<div id="scatterplot"></div>--}%
%{--</div>--}%

<r:layoutResources/>
