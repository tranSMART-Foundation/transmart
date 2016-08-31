
<script type="text/ng-template" id="boxplot">

<div ng-controller="BoxplotController">

    <tab-container>

        <workflow-tab tab-name="Fetch Data" disabled="fetch.disabled">
            <concept-box style="display: inline-block;"
                         concept-group="fetch.conceptBoxes.highDimensional"
                         type="HD"
                         min="-1"
                         max="-1"
                         label="Highdimensional Variable"
                         tooltip="Select a single high dimensional variable that you would like to have displayed.">
            </concept-box>
            <concept-box style="display: inline-block;"
                         concept-group="fetch.conceptBoxes.numData"
                         type="LD-numerical"
                         min="-1"
                         max="-1"
                         label="Numerical Variable"
                         tooltip="Select a single numerical variable that you would like to have displayed.">
            </concept-box>
            <br/>
            <br/>
            <biomarker-selection biomarkers="fetch.selectedBiomarkers"></biomarker-selection>
            <hr class="sr-divider">
            <fetch-button concept-map="fetch.conceptBoxes"
                          loaded="fetch.loaded"
                          running="fetch.running"
                          biomarkers="fetch.selectedBiomarkers"
                          allowed-cohorts="[1,2]">
            </fetch-button>
        </workflow-tab>

        <workflow-tab tab-name="Run Analysis" disabled="runAnalysis.disabled">
            <br/>
            <br/>
            <run-button button-name="Create Plot"
                        store-results-in="runAnalysis.scriptResults"
                        script-to-run="run"
                        arguments-to-use="runAnalysis.params"
                        running="runAnalysis.running">
            </run-button>
            <br/>
            <br/>
            <boxplot data="runAnalysis.scriptResults"></boxplot>
        </workflow-tab>

    </tab-container>

</div>

</script>
