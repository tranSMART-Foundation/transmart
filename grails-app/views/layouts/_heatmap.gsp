<script type="text/ng-template" id="heatmap">
<div ng-controller="HeatmapController">

    <tab-container>
        %{--========================================================================================================--}%
        %{-- Fetch Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Fetch Data">
            <concept-box
                concept-group="conceptBoxes.highDimensional"
                type="HD"
                min="1"
                max="1"
                label="High Dimensional"
                tooltip="Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into the box. The nodes needs to be from the same platform.">
            </concept-box>

            %{--TODO include low dimensions--}%
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

            %{--Number of max row to display--}%
            <div class="heim-input-field heim-input-number sr-input-area">
                <label>Max row to display</label>
                <input type="text" id="txtMaxRow" ng-model="runParams.max_row"> rows (< 1000 is preferable).
            </div>

            %{--Type of sorting to apply--}%
            <div class="heim-input-field sr-input-area">
                <h2>Sort on:</h2>
                <fieldset class="heim-radiogroup">
                    <label>
                        <input type="radio" ng-model="runParams.sorting" name="sortingSelect" value="nodes" checked> Nodes
                    </label>
                    <label>
                        <input type="radio" ng-model="runParams.sorting" name="sortingSelect" value="subjects"> Subjects
                    </label>
                </fieldset>
            </div>

            %{--Type of sorting to apply--}%
            <div class="heim-input-field  sr-input-area">
                <sorting-criteria criteria="runParams.ranking"></sorting-criteria>
            </div>

            <hr class="sr-divider">

            <run-button button-name="Create Plot"
                        store-results-in="scriptResults"
                        script-to-run="run"
                        arguments-to-use="runParams"
                        serialized="true"></run-button>
            <capture-plot-button></capture-plot-button>
            <br/>
            <heatmap-plot data="scriptResults" width="1200" height="1200"></heatmap-plot>

        </workflow-tab>

    </tab-container>
</div>
</script>
