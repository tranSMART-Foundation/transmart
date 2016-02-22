<script type="text/ng-template" id="heatmap">
<div ng-controller="HeatmapController">

    <tab-container>
        %{--========================================================================================================--}%
        %{-- Fetch Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Fetch Data">
            <concept-box
                concept-group="conceptBoxes.highDimensional"
                label="High Dimensional"
                alt="Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into the box. The nodes needs to be from the same platform.">
            </concept-box>

            %{--<concept-box concept-group="conceptBoxes.numerical"></concept-box>--}%
            %{--<concept-box concept-group="conceptBoxes.categorical"></concept-box>--}%

            <biomarker-selection biomarkers="selectedBiomarkers"></biomarker-selection>
            <hr class="sr-divider">
            <fetch-button concept-map="conceptBoxes"
                          biomarkers="selectedBiomarkers"
                          show-summary-stats="true"
                          summary-data="fetchSummaryData">
            </fetch-button>
            <br/>
            <summary-stats summary-data="fetchSummaryData"></summary-stats>
        </workflow-tab>

        %{--========================================================================================================--}%
        %{-- Preprocess Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Preprocess">
            %{--Aggregate Probes--}%
            <div class="heim-input-field">
                <input type="checkbox" ng-model="preprocessArgs.aggregateProbes">
                <span>Aggregate probes</span>
            </div>

            <hr class="sr-divider">

            <preprocess-button params="preprocessArgs"
                               show-summary-stats="true"
                               summary-data="preprocessSummaryData">
            </preprocess-button>
            <br/>
            <summary-stats summary-data="preprocessSummaryData"></summary-stats>
        </workflow-tab>


        %{--========================================================================================================--}%
        %{--Run Analysis--}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Run Analysis">

            %{--TODO: Implement run analysis selections--}%

            <hr class="sr-divider">

            <run-button button-name="Create Plot"
                        store-results-in="scriptResults"
                        script-to-run="run"
                        arguments-to-use="runArgs"
                        serialized="true"></run-button>
            <br/>
            <heatmap-plot data="scriptResults" width="1200" height="1200"></heatmap-plot>

        </workflow-tab>

    </tab-container>
</div>
</script>
