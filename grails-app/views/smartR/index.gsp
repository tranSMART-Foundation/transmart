<style>
    .txt {
        font-family: 'Roboto', sans-serif;
    }
</style>

<head>
    <g:javascript library='jquery' />
    <g:javascript src='smartR/smartR.js' />
    <link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
    <r:layoutResources/>
</head>

<body>
    <div id="index" style="text-align: center">
        <h1 class="txt"> SmartR - Dynamic data visualization and interaction.</h1>
        <span style='color:red' class='txt'>SmartR is (UNSUPPORTED!) BETA software. This means it is being actively developed and not considered stable enough by the developer.</span><br/>
        <span style='color:red' class='txt'>If you want to support the project please be so kind and provide some feedback to this address: <span style='color:blue' class='txt'>sascha.herzinger@uni.lu</span></span>
        <hr class="myhr"/>

        <div id="inputDIV" class='txt' style="text-align: left">Please select a script to execute.</div>

        <hr class="myhr"/>
        <g:select
            name="scriptSelect"
            class='txt'
            from="${scriptList}"
            noSelection="['':'Choose an algorithm']"
            onchange="changeInputDIV()"/>
        &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
        <input
            id="submitButton"
            class='txt'
            type="button"
            value="(Re-)Run Analysis"
            onclick="runRScript()"/>
        <hr class="myhr"/>
    </div>

    <div id="outputDIV" class='txt'></div>
</body>
