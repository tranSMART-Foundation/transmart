<div id="analysisWidget" xmlns="http://www.w3.org/1999/html">
    <h2>GWAS</h2>

    <script type="text/javascript" src="${resource(dir:'js', file:'gwasPlink.js')}"></script>
    <form id="analysisForm">
        <fieldset class="inputFields">
            <p>
                <label for="analysisName">Analysis name:</label>
                <input id="analysisName" type="text" value="result">
            </p>

            <p>
                <label for="analysisType">Analysis type:</label>
                <select id="analysisType" onchange="javascript: showAdditionalPlinkAnalysisOptions()">
                    <option value="assoc">Association analysis</option>
                    <option value="assoc_fisher">Association analysis (fisher)</option>
                    <option value="assoc_fisher_midp">Association analysis (fisher-midp)</option>
                    <option value="linear">Linear regression</option>
                    <option value="logistic">Logistic regression</option>
                </select>
            </p>

            <p class="additionalPlinkAnalysisOptions" style="display: none;">
                <label for="r1">&nbsp;</label>
                <input type="radio" id="r1" name="radiotype" value="no" style="width: auto; height: auto; margin: 0; padding: 0;" checked> Default (Additive)
                <img src="${resource(dir: 'images', file: 'help/helpicon.png')}" alt="Help" border="0" width="18" height="18" style="vertical-align:middle;margin-left:5pt;"
                    title="This test measures the additive effects of the minor allele, i.e. AA versus AB versus BB, where &quot;A&quot; is the major allele, &quot;B&quot; is the minor allele." /><br />
                <label for="r2">&nbsp;</label>
                <input type="radio" id="r2" name="radiotype" value="genotypic" style="width: auto; height: auto; margin: 0; padding: 0;"> Genotypic
                <img src="${resource(dir: 'images', file: 'help/helpicon.png')}" alt="Help" border="0" width="18" height="18" style="vertical-align:middle;margin-left:5pt;"
                    title="This test measures both the additive effects (ADD) and dominance deviation from additivity (DOMDEV) in a joint model to give GENO_2DF. Your output file will have three tests per SNP: ADD, DOMDEV and GENO_2DF. An example of DOMDEV is AB versus (AA, BB), where &quot;A&quot; is the major allele, &quot;B&quot; is the minor allele. When using the --genotypic option, you would not expect the ADD p-value to be the same as that generated using the default option because the ADD p-value here reflects the entity effect under the TEST column. PLINK recommends the use of the GENO_2DF p-value but we STRONGLY recommend you to consult your resident statistician (and buy them cake) when interpreting your results." /><br />
                <label for="r3">&nbsp;</label>
                <input type="radio" id="r3" name="radiotype" value="dominant" style="width: auto; height: auto; margin: 0; padding: 0;"> Dominant
                <img src="${resource(dir: 'images', file: 'help/helpicon.png')}" alt="Help" border="0" width="18" height="18" style="vertical-align:middle;margin-left:5pt;"
                    title="This test measures the dominant effects of the minor allele, i.e. AA versus (AB, BB), where &quot;A&quot; is the major allele, &quot;B&quot; is the minor allele." /><br />
                <label for="r4">&nbsp;</label>
                <input type="radio" id="r4" name="radiotype" value="recessive" style="width: auto; height: auto; margin: 0; padding: 0;"> Recessive
                <img src="${resource(dir: 'images', file: 'help/helpicon.png')}" alt="Help" border="0" width="18" height="18" style="vertical-align:middle;margin-left:5pt;"
                    title="This test measures the recessive effects of the minor allele, i.e. (AA, AB) versus BB, where &quot;A&quot; is the major allele, &quot;B&quot; is the minor allele." /><br />
                <br/>
            </p>

            <p>
                <label for="pValueThreshold">P-value threshold:</label>
                <input id="pValueThreshold" type="number" step="0.01" value="0.05">
            </p>

            <p>
                <label for="previewRowsCount">Max # of rows to display:</label>
                <input id="previewRowsCount" type="number" value="100" min="1" max="500">
            </p>

            <div class="phenotypeOption">
            <p>
                <label for="divCategoryVariable">Phenotypes:</label>
                <input id="makePheno" type="checkbox"> make all selected subjects as case
                <img src="${resource(dir: 'images', file: 'help/helpicon.png')}" alt="Help" border="0" width="18" height="18" style="vertical-align:middle;margin-left:5pt;"
                    title="Drag only numerical nodes into the box below. If you drag more than one node and some subjects have data in multiple nodes the values for analysis for those subjects will be chosen at random. If checkbox is checked then the value for all selected subjects will be 2 (case) and for all remaining subjects 1 (control)." />
            </p>
            <div id="divCategoryVariable" class="queryGroupIncludeSmall highDimBox"></div>
            <input type="button" value="Clean" onclick="javascript: clearDiv('divCategoryVariable')" style="padding:0px;"/>
            </div>

            <div class="covariatesOption" style="display: none;">
            <p>
                <label for="divGroupByVariable">Covariates: <img src="${resource(dir: 'images', file: 'help/helpicon.png')}" alt="Help" border="0" width="18" height="18" style="vertical-align:middle;margin-left:5pt;"
                    title="You can drag either categorical or numerical nodes into the box below. If you drag more than one node and some subjects have data in multiple nodes the values for analysis for those subjects will be chosen at random. If the value is one of '-9', 'NULL', '' (empty string/no value) it will be converted to -9. The first value that is not converted to '-9' will be converted to 1 (control). All other values will be converted to 2 (case)." /></label>
            </p>
            <div id="divGroupByVariable" class="queryGroupIncludeSmall highDimBox"></div>
            <input type="button" value="Clean" onclick="javascript: clearDiv('divGroupByVariable')" style="padding:0px;"/>
            </div>
        </fieldset>
        <fieldset class="toolFields">
            <input type="button" value="Run" class="runAnalysisBtn" onclick="gwas_plink.runAnalysis()">
        </fieldset>
    </form>
</div>
<script>
    GWASPlinkInsView.register_drag_drop();
</script>
