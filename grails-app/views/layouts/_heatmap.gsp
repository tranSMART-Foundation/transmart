<script type="text/ng-template" id="heatmap">
<div ng-controller="HeatmapController">

    <tab-container>
        %{--========================================================================================================--}%
        %{-- Fetch Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Fetch Data">
            <concept-box
                concept-group="fetch.conceptBoxes.highDimensional"
                type="HD"
                min="1"
                max="512"
                label="High Dimensional"
                tooltip="Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into the box. The nodes needs to be from the same platform.">
            </concept-box>

            %{--TODO include low dimensions--}%
            %{--<concept-box concept-group="fetch.conceptBoxes.numerical"></concept-box>--}%
            %{--<concept-box concept-group="fetch.conceptBoxes.categorical"></concept-box>--}%

            <biomarker-selection biomarkers="fetch.selectedBiomarkers"></biomarker-selection>
            <hr class="sr-divider">
            <fetch-button
                    disabled="fetch.disabled"
                    concept-map="fetch.conceptBoxes"
                    biomarkers="fetch.selectedBiomarkers"
                    show-summary-stats="true"
                    summary-data="fetch.summaryData">
            </fetch-button>
            <br/>
            <summary-stats summary-data="fetch.summaryData"></summary-stats>
        </workflow-tab>

        %{--========================================================================================================--}%
        %{-- Preprocess Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Preprocess">
            %{--Aggregate Probes--}%
            <div class="heim-input-field">
                <input type="checkbox" ng-model="preprocess.params.aggregateProbes">
                <span>Aggregate probes</span>
            </div>

            <hr class="sr-divider">

            <preprocess-button params="preprocess.params"
                               show-summary-stats="true"
                               summary-data="preprocess.summaryData"
                               disabled="preprocess.disabled">
            </preprocess-button>

            <br/>
            <summary-stats summary-data="preprocess.summaryData"></summary-stats>
        </workflow-tab>


        %{--========================================================================================================--}%
        %{--Run Analysis--}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Run Analysis">

            %{--Number of max row to display--}%
            <div class="heim-input-field heim-input-number sr-input-area">
                <label>Number of rows to display</label>
                <input type="text" id="txtMaxRow" ng-model="runAnalysis.params.max_row"> rows (< 1000 is preferable).
            </div>

            %{--Type of sorting to apply--}%
            <div class="heim-input-field sr-input-area">
                <h2>Sort on:</h2>
                <fieldset class="heim-radiogroup">
                    <label>
                        <input type="radio" ng-model="runAnalysis.params.sorting" name="sortingSelect" value="nodes"
                               checked> Nodes
                    </label>
                    <label>
                        <input type="radio" ng-model="runAnalysis.params.sorting" name="sortingSelect" value="subjects">
                        Subjects
                    </label>
                </fieldset>
            </div>

            %{--Type of sorting to apply--}%
            <div class="heim-input-field  sr-input-area">
                <sorting-criteria criteria="runAnalysis.params.ranking"></sorting-criteria>
            </div>

            <hr class="sr-divider">

            <run-button button-name="Create Plot"
                        store-results-in="runAnalysis.scriptResults"
                        script-to-run="run"
                        arguments-to-use="runAnalysis.params"
                        serialized="true"
                        disabled="runAnalysis.disabled">
            </run-button>
            <capture-plot-button filename="heatmap.svg" disabled="runAnalysis.download.disabled"></capture-plot-button>
            <download-results-button disabled="runAnalysis.download.disabled"></download-results-button>
            <br/>
            <heatmap-plot data="runAnalysis.scriptResults" width="1200" height="1200"></heatmap-plot>

        </workflow-tab>

    </tab-container>
</div>
</script>
