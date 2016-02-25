<r:require modules="smartR_all"/>

<div id="sr-index" data-ng-app="smartRApp" style="padding: 10px;">
    <h1 style="font-size: 14px">SmartR - Dynamic Data Visualization and Interaction</h1>
    <br>

    <div align="left">
        <div class="sr-landing-dropdown" align="center">
            <button class="sr-landing-dropBtn">SmartR Workflows</button>
            <div class="sr-landing-dropdown-content" style="position: relative">
                <g:each in="${scriptList}">
                    <span ng-click="template='${it}'"> ${it.capitalize() + ' Workflow'} </span>
                </g:each>
            </div>
        </div>
    </div>

    <div style="width: 50%; margin: 0 auto; text-align: center">
        <cohort-summary-info></cohort-summary-info>
    </div>

    <hr class="sr-divider">

    <ng-include src="template"></ng-include>

    <g:each in="${scriptList}">
        <g:render template="/layouts/${it}"/>
    </g:each>
</div>
