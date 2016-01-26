<r:require modules="smartR_boxplot"/>
<r:layoutResources/>


<div class="heim-analysis-container">

    <div id="heim-tabs" style="margin-top: 25px;">

        <ul>
            <li class="heim-tab"><a href="#fragment-load"><span>Fetch</span></a></li>
            <li class="heim-tab"><a href="#fragment-preprocess"><span>Preprocess</span></a></li>
            <li class="heim-tab"><a href="#fragment-run"><span>Run</span></a></li>
        </ul>

        %{--========================================================================================================--}%
        %{--Load Data--}%
        %{--========================================================================================================--}%
        <div id="fragment-load">
            <table>
                <tr>
                    <td style='padding-right: 2em; padding-bottom: 1em'>
                        <mark>Step 1:</mark> Drop exactly one numerical node into this window.<br/>
                        NOTE: This window maps to the first cohort!<br/>
                        (Example: Age, Pulse, Blood Pressure)<br/>
                        <div id='concept1' class="queryGroupIncludeSmall"></div>
                        <input type="button" class='txt'  value="Clear Window" id="sr-concept1-btn">
                    </td>
                    <td style='padding-right: 2em; padding-bottom: 1em'>
                        <mark>Step 2 (optional):</mark> Drop any number of concepts subsetting the choice made on the left into this window.<br/>
                        NOTE: This window maps to the first cohort!<br/>
                        (Example: Demographics/Male, Demographics/Female)<br/>
                        <div id='subsets1' class="queryGroupIncludeSmall"></div>
                        <input type="button" class='txt' onclick="clearVarSelection('subsets1')" value="Clear Window" id="sr-subset1-btn">
                    </td>
                </tr>
                <tr>
                    <td style='padding-right: 2em; padding-bottom: 1em'>
                        <mark>Step 3:</mark> Drop exactly one numerical node into this window.<br/>
                        NOTE: This window maps to the second cohort!<br/>
                        (Example: Age, Pulse, Blood Pressure)<br/>
                        <div id='concept2' class="queryGroupIncludeSmall"></div>
                        <input type="button" class='txt' onclick="clearVarSelection('concept2')" value="Clear Window" id="sr-subset-2">
                    </td>
                    <td style='padding-right: 2em; padding-bottom: 1em'>
                        <mark>Step 4 (optional):</mark> Drop any number of concepts subsetting the choice made on the left into this window.<br/>
                        NOTE: This window maps to the second cohort!<br/>
                        (Example: Demographics/Male, Demographics/Female)<br/>
                        <div id='subsets2' class="queryGroupIncludeSmall"></div>
                        <input type="button" class='txt' onclick="clearVarSelection('subsets2')" value="Clear Window" id="sr-concept-2">
                    </td>
                </tr>
            </table>

            <button id="sr-btn-fetch-boxplot" class="sr-action-button">Fetch Data</button>

            <hr class="sr-divider">

            %{--result--}%
            <div id="heim-run-output" class="sr-output-container"></div>


            <div id="boxplot1"></div>
            <div id="boxplot2"></div>
        </div>


        %{--========================================================================================================--}%
        %{--Preprocess--}%
        %{--========================================================================================================--}%
        <div id="fragment-preprocess">

        </div>

        %{--========================================================================================================--}%
        %{--Run Analysis--}%
        %{--========================================================================================================--}%
        <div id="fragment-run">
            <button id="sr-btn-run-boxplot" class="sr-action-button">Run Boxplot</button>
        </div>

    </div>
</div>



<script>
    activateDragAndDrop('concept1');
    activateDragAndDrop('concept2');
    activateDragAndDrop('subsets1');
    activateDragAndDrop('subsets2');

    function register() {
        registerConceptBox('concept1', [1], 'valueicon', 1, 1);
        registerConceptBox('concept2', [2], 'valueicon', 0, 1);
        registerConceptBox('subsets1', [1], 'alphaicon', 0, undefined);
        registerConceptBox('subsets2', [2], 'alphaicon', 0, undefined);
    }

    function getSettings() {
        return {};
    }

    function customSanityCheck() {
        var conceptBox2 = Ext.get('concept2').dom;
        if (conceptBox2.childNodes.length === 0 && ! isSubsetEmpty(2)) {
            alert('You have specified a second cohort but you have not defined a second concept!\nConsider placing the same concept in the second box.');
            return false;
        }
        return true;
    }
</script>

<g:javascript>
    smartR.initAnalysis('boxplot');
</g:javascript>

<r:layoutResources/>
