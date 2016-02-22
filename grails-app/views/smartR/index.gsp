%{--load all js in one go--}%
<r:require modules="smartR_all"/>

<div data-ng-app="smartRApp" style="padding: 10px;">
    <h1>SmartR - Dynamic Data Visualization and Interaction</h1>
    <br>

    <select ng-model="template">
        <option value="heatmap">Heatmap Workflow</option>
        <option value="correlation">Correlation Workflow</option>
        <option value="boxplot">Boxplot Workflow</option>
        <option value="volcano">Volcano Workflow</option>
        <option value="timeline">Timeline Workflow</option>
    </select>

    <hr class="sr-divider">

    <ng-include src="template"></ng-include>

    %{-- Render all templates --}%

    <g:render template="/layouts/heatmap"/>
    <g:render template="/layouts/correlation"/>
    <g:render template="/layouts/boxplot"/>
    <g:render template="/layouts/volcano"/>
    <g:render template="/layouts/timeline"/>
</div>
