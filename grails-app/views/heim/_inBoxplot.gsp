<r:require modules="smartR_boxplot"/>
<r:layoutResources/>

<div class="heim-analysis-container">

    <div id="heim-tabs" style="margin-top: 25px;">

        <ul>
            <li class="heim-tab"><a href="#fragment-fetch"><span>Fetch</span></a></li>
            <li class="heim-tab"><a href="#fragment-run"><span>Run</span></a></li>
        </ul>

        %{--========================================================================================================--}%
        %{--Load Data--}%
        %{--========================================================================================================--}%
        <div id="fragment-fetch">
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
                        <input type="button" class='txt' value="Clear Window" id="sr-subset1-btn">
                    </td>
                </tr>
                <tr>
                    <td style='padding-right: 2em; padding-bottom: 1em'>
                        <mark>Step 3:</mark> Drop exactly one numerical node into this window.<br/>
                        NOTE: This window maps to the second cohort!<br/>
                        (Example: Age, Pulse, Blood Pressure)<br/>
                        <div id='concept2' class="queryGroupIncludeSmall"></div>
                        <input type="button" class='txt' value="Clear Window" id="sr-subset-2">
                    </td>
                    <td style='padding-right: 2em; padding-bottom: 1em'>
                        <mark>Step 4 (optional):</mark> Drop any number of concepts subsetting the choice made on the left into this window.<br/>
                        NOTE: This window maps to the second cohort!<br/>
                        (Example: Demographics/Male, Demographics/Female)<br/>
                        <div id='subsets2' class="queryGroupIncludeSmall"></div>
                        <input type="button" class='txt' value="Clear Window" id="sr-concept-2">
                    </td>
                </tr>
            </table>

            <button id="sr-btn-fetch-boxplot" class="sr-action-button">Fetch Data</button>

            %{--result--}%
            <div id="heim-fetch-output" class="sr-status-and-output-container"></div>
        </div>


        %{--========================================================================================================--}%
        %{--Run Analysis--}%
        %{--========================================================================================================--}%
        <div id="fragment-run">
            <button id="sr-btn-run-boxplot" class="sr-action-button">Run Boxplot</button>

            <div id="heim-run-output" class="sr-status-and-output-container"></div>
        </div>

    </div>
</div>

<r:layoutResources/>
