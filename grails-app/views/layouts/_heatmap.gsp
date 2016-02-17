<script type="text/ng-template" id="heatmap">

    <tab-container ng-controller="HeatmapController">

        %{--========================================================================================================--}%
        %{-- Fetch Data --}%
        %{--========================================================================================================--}%
        <workflow-tab tab-name="Fetch Data">

            <concept-box concept-group="conceptBoxes.highDimensional"></concept-box>

            <concept-box concept-group="conceptBoxes.numerical"></concept-box>

            <concept-box concept-group="conceptBoxes.categorical"></concept-box>

            <br/>
            <br/>

            <fetch-button concept-map="conceptBoxes" show-summary-stats="true"></fetch-button>

            <summary-stats data="fetchResult"></summary-stats>

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

</script>
