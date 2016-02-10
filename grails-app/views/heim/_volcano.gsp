<r:require modules="smartR_volcanoplot"/>
<r:layoutResources/>

<div ng-controller="VolcanoplotController">

    <tab-container>

        <workflow-tab tab-name="Fetch Data">
            <concept-box concept-group="conceptBoxes.mRNAData"></concept-box>
            <br/>
            <br/>
            <fetch-button concept-map="conceptBoxes"></fetch-button>
        </workflow-tab>

        <workflow-tab tab-name="Run Analysis">
            <run-button button-name="Create Plot" results-storage="scriptResults" script-to-run="run" parameter-map="params"></run-button>
            <br/>
            <br/>
            <volcano-plot data="scriptResults" width="1200" height="1200"></volcano-plot>
        </workflow-tab>

    </tab-container>

</div>

<r:layoutResources/>
