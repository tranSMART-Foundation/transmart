<head>
    <g:javascript library='jquery' />
    <g:javascript src='smartR/smartR.js' />
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'smartR.css')}">
    <r:layoutResources/>

</head>

<body>
    <div id="index" style="padding: 10px;">

        <h1 class="txt"> Heim - Modular R Workflows in TranSMART.</h1>
        <br>
        <g:select name="scriptSelect" id="scriptSelect" class='txt' from="${scriptList}"
                  noSelection="['':'Choose an algorithm']"
                  onchange="changeInputDIV()" />



        %{--Input placeholder--}%
        <div id="inputDIV" style="margin-top: 20px;"></div>
    </div>

    %{--Output placeholder--}%
    <div id="outputDIV" style="margin-top: 20px;"></div>

</body>
