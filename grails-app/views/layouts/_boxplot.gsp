
<script type="text/ng-template" id="boxplot">

<div ng-controller="BoxplotController">

    <tab-container>

        <workflow-tab tab-name="Fetch Data">
            <concept-box style="display: inline-block;"
                         concept-group="conceptBoxes.datapoints"
                         type="LD-numeric"
                         min="1"
                         max="1"
                         label="Numeric Variable"
                         tooltip="Select a single numeric variable that you would like to have displayed.">
            </concept-box>
            <concept-box style="display: inline-block;"
                         concept-group="conceptBoxes.subsets"
                         type="LD-categoric"
                         min="0"
                         max="10"
                         label="(optional) Categoric Variables"
                         tooltip="Select an arbitrary amount of categoric variables to split the numeric variable into subsets.">
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
                        serialized="false">
            </run-button>
            <br/>
            <br/>
            <boxplot data="scriptResults" width="1000" height="500"></boxplot>
        </workflow-tab>

    </tab-container>

</div>

</script>
