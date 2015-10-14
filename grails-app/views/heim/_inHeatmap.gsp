%{--include js lib for heatmap dynamically--}%
<r:require modules="heatmap"/>
<r:layoutResources disposition="defer"/>

<div style="width: 98%">

    <p>
        Start Heatmap analysis with load, preprocess options and then run analysis.
    </p>

    <div id="tabs" style="margin-top: 25px;">

        <ul>
            <li><a href="#fragment-load"><span>Load</span></a></li>
            <li><a href="#fragment-preprocess"><span>Preprocess</span></a></li>
            <li><a href="#fragment-run"><span>Run</span></a></li>
        </ul>

        <div id="fragment-load">
            <form>
                %{--High dimension dropzone--}%
                <div class="heim-input-field heim-dropzone">
                    %{--High dimensional input--}%
                    <label>Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into
                    the box. The nodes needs to be from the same platform.</label>
                    <div id='divIndependentVariable' class="heim-dropzone" style="border:1px solid #666; height: 100px"></div>
                    <div style="margin-top: 10px;">
                        <button type="button" onclick="alert('Clear');">Clear</button>
                    </div>
                </div>

                %{--Display independent variable--}%
                <div id="displaydivIndependentVariable" class="independentVars"></div>

                %{--Select identifier--}%
                <div class="heim-input-field heim-autocomplete">
                    <label>Select a Gene/Pathway/mirID/UniProtID:</label> 
                    <input id="tags">
                </div>

                %{--tool buttons--}%
                <div style="margin-top: 10px; text-align: right;">
                    <button>Load Data</button>
                </div>

            </form>
        </div>

        <div id="fragment-preprocess">
            <form>
                %{--Calculate z-score--}%
                <div>
                    <input type="checkbox" id="chkCalculateZscore" name="chkCalculateZscore">
                    <span>Calculate z-score on the fly</span>
                </div>

                %{--Calculate log fold--}%
                <div>
                    <input type="checkbox" id="chkLogFoldChange" name="chkLogFoldChange">
                    <span>Calculate log-fold change based on two data nodes</span>
                </div>
                <div>
                    <select id="logFoldInput1">
                        <option>dummy 1</option>
                        <option>dummy 2</option>
                        <option>dummy 3</option>
                        <option>dummy 4</option>
                    </select>
                    relative to
                    <select id="logFoldInput2">
                        <option>dummy 1</option>
                        <option>dummy 2</option>
                        <option>dummy 3</option>
                        <option>dummy 4</option>
                    </select>
                </div>

                %{--tool buttons--}%
                <div style="margin-top: 10px; text-align: right;">
                    <button>Preprocess</button>
                </div>
            </form>
        </div>

        <div id="fragment-run">

        </div>
    </div>


</div>
