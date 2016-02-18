<script type="text/ng-template" id="heatmap">
<div ng-controller="HeatmapController">

    <tab-container>
        %{--========================================================================================================--}%
        %{-- Fetch Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Fetch Data">

            <concept-box concept-group="conceptBoxes.highDimensional"></concept-box>

            <concept-box concept-group="conceptBoxes.numerical"></concept-box>

            <concept-box concept-group="conceptBoxes.categorical"></concept-box>

            <biomarker-selection biomarkers="selectedBiomarkers"></biomarker-selection>

            <br/>
            <br/>

            <fetch-button concept-map="conceptBoxes" biomarkers="selectedBiomarkers" show-summary-stats="true" summary-data="fetchSummaryData"></fetch-button>

            <summary-stats summary-data="fetchSummaryData"></summary-stats>
            <hr class="sr-divider">



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
