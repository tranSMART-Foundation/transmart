<script type="text/ng-template" id="heatmap">
<div ng-controller="HeatmapController">

    <tab-container>
        %{--========================================================================================================--}%
        %{-- Fetch Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Fetch Data">

            <concept-box
                concept-group="conceptBoxes.highDimensional"
                label="'High Dimensional'"
                alt="'Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into the box. The nodes needs to be from the same platform.'">
            </concept-box>

            %{--<concept-box concept-group="conceptBoxes.numerical"></concept-box>--}%
            %{--<concept-box concept-group="conceptBoxes.categorical"></concept-box>--}%

            <biomarker-selection biomarkers="selectedBiomarkers"></biomarker-selection>

            <br/>

            <fetch-button concept-map="conceptBoxes" biomarkers="selectedBiomarkers" show-summary-stats="true"
                          summary-data="fetchSummaryData"></fetch-button>

            <hr class="sr-divider">

            <summary-stats summary-data="fetchSummaryData"></summary-stats>



        </workflow-tab>

        %{--========================================================================================================--}%
        %{-- Preprocess Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Preprocess">

        </workflow-tab>


        %{--========================================================================================================--}%
        %{--Run Analysis--}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Run Analysis">

        </workflow-tab>

    </tab-container>
</div>
</script>
