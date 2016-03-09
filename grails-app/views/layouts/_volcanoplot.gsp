
<script type="text/ng-template" id="volcanoplot">

<div ng-controller="VolcanoplotController">

    <tab-container>

        <workflow-tab tab-name="Fetch Data">
            <concept-box style="display: inline-block"
                         concept-group="conceptBoxes.highDimensional"
                         type="HD"
                         min="1"
                         max="1"
                         label="High Dimensional Variable"
                         tooltip="Select one high dimensional variable you would like to have displayed.">
            </concept-box>
            <br/>
            <br/>
            <fetch-button concept-map="conceptBoxes"
                          show-summary-stats="false">
            </fetch-button>
        </workflow-tab>

        <workflow-tab tab-name="Run Analysis">
            <br/>
            <br/>
            <run-button button-name="Create Plot"
                        store-results-in="scriptResults"
                        script-to-run="run"
                        arguments-to-use="params"
                        serialized="true">
            </run-button>
            <br/>
            <br/>
            <volcano-plot data="scriptResults" width="1200" height="1200"></volcano-plot>
        </workflow-tab>

    </tab-container>

</div>

</script>
