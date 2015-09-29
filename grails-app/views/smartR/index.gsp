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
        <span style='color:red' class='txt'>Please be aware that this software is currently in a TESTING phase and all results should be handled with care.</span><br/>
        <span style='color:red' class='txt'>You can help improving SmartR by reporting bugs or providing the developer with (highly appreciated!) feedback.</span><br/>
        <input id="contactButton" class='txt' type="button" value="Contact Developer" onclick="contact()"/>
        <span style='color:red' class='txt'></span><br/>
        <hr class="myhr"/>

        <div id="inputDIV" class='txt' style="text-align: left">Please select a script to execute.</div>

        <hr class="myhr"/>
        <g:select name="scriptSelect" class='txt' from="${scriptList}" noSelection="['':'Choose an algorithm']" onchange="changeInputDIV()"/>
        &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
        <input id="submitButton" class='txt' type="button" value="(Re-)Run Analysis" onclick="runRScript()"/>
        <hr class="myhr"/>
    </div>

    <div id="outputDIV" class='txt'></div>
</body>
