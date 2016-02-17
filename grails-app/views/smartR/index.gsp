%{--load all js in one go--}%
<r:require modules="smartR_all"/>

<div data-ng-app="smartRApp" style="padding: 10px;">
    <h1>SmartR - Dynamic Data Visualization and Interaction</h1>
    <br>

    <select ng-model="template">
        <option value="heatmap">heatmap</option>
        <option value="correlation">correlation</option>
    </select>

    <hr style="margin: 20px;">

    <ng-include src="template"></ng-include>

    %{-- Render all templates --}%

    <g:render template="/layouts/heatmap"/>
    <g:render template="/layouts/correlation"/>
</div>
