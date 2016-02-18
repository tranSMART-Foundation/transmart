
<script type="text/ng-template" id="boxplot">

<div ng-controller="BoxplotController">

    <tab-container>

        <workflow-tab tab-name="Fetch Data">
            <concept-box concept-group="conceptBoxes.concepts"></concept-box>
            <concept-box concept-group="conceptBoxes.subsets"></concept-box>
            <br/>
            <br/>
            <fetch-button concept-map="conceptBoxes"></fetch-button>
        </workflow-tab>

        <workflow-tab tab-name="Run Analysis">
            <br/>
            <br/>
            <run-button button-name="Create Plot"
                        store-results-in="scriptResults"
                        script-to-run="run"
                        arguments-to-use="params"></run-button>
            <br/>
            <br/>
            <boxplot data="scriptResults" width="1200" height="1200"></boxplot>
        </workflow-tab>

    </tab-container>

</div>

</script>
