<r:require modules="smartR_core"/>
<r:layoutResources/>

<div data-ng-app="smartRApp" style="padding: 10px;">
    <h1>SmartR - Dynamic Data Visualization and Interaction</h1>
    <br>
    <g:select name="scriptSelect" id="scriptSelect" from="${scriptList}"
              noSelection="['': 'Choose an algorithm']"
              onchange="changeInputDIV()" />

    <div id="inputDIV"></div>
</div>
<r:layoutResources/>
