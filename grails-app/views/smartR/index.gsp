%{--load all js in one go--}%
<r:require modules="smartR_all"/>

<div data-ng-app="smartRApp" style="padding: 10px;">
    <h1>SmartR - Dynamic Data Visualization and Interaction</h1>
    <br>

    <select ng-model="template">
        <option value="heatmap">Heatmap Workflow</option>
        <option value="correlation">Correlation Workflow</option>
        <option value="boxplot">Boxplot Workflow</option>
        <option value="volcanoplot">Volcanoplot Workflow</option>
        <option value="timeline">Timeline Workflow</option>
    </select>

    <div style="width: 50%; margin: 0 auto; text-align: center">
        <cohort-summary-info></cohort-summary-info>
    </div>

    <hr class="sr-divider">

    <ng-include src="template"></ng-include>

    %{-- Render all templates --}%

    <g:render template="/layouts/heatmap"/>
    <g:render template="/layouts/correlation"/>
    <g:render template="/layouts/boxplot"/>
    <g:render template="/layouts/volcanoplot"/>
    <g:render template="/layouts/timeline"/>
</div>
