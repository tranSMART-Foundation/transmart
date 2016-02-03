<r:require modules="smartR_resources"/>
<r:layoutResources/>

<div ng-app="smartRApp">
    <h1>SmartR - Dynamic Data Visualization and Interaction</h1>
    <br>
    <g:select name="scriptSelect" id="scriptSelect" from="${scriptList}"
              noSelection="['': 'Choose an algorithm']"
              onchange="changeInputDIV()" />

    <div id="inputDIV"></div>
</div>

<r:layoutResources/>

