
<script type="text/ng-template" id="timeline">

	<div ng-controller="TimelineController">

		<tab-container>

			<workflow-tab tab-name="Fetch Data">
				<concept-box concept-group="conceptBoxes.datapoints"></concept-box>
				<br/>
				<br/>
				<fetch-button concept-map="conceptBoxes"></fetch-button>
			</workflow-tab>

			<workflow-tab tab-name="Run Analysis">
				<run-button button-name="Create Plot" results-storage="scriptResults" script-to-run="run" parameter-map="params"></run-button>
				<br/>
				<br/>
				<timeline-plot data="scriptResults" width="1200" height="1200"></timeline-plot>
			</workflow-tab>

		</tab-container>

	</div>

</script>