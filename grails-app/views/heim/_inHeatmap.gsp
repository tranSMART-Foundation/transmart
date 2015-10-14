%{--include js lib for heatmap dynamically--}%
<r:require modules="heatmap"/>
<r:layoutResources disposition="defer"/>

<div style="width: 98%">

    <h2>
        Variable Selection
        <a href='JavaScript:D2H_ShowHelp(1505,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
            <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
        </a>
    </h2>

    <div id="tabs">

        <ul>
            <li><a href="#fragment-load"><span>Load</span></a></li>
            <li><a href="#fragment-preprocess"><span>Preprocess</span></a></li>
            <li><a href="#fragment-run"><span>Run</span></a></li>
        </ul>

        <div id="fragment-load">
            <form>
                <div class="heim-input-field heim-dropzone">
                    %{--High dimensional input--}%
                    <label>Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into
                    the box. The nodes needs to be from the same platform.</label> 
                    <div id='divIndependentVariable' class="heim-dropzone" style="border:1px solid #666; height: 100px"></div>
                    <div style="margin-top: 10px;">
                        <button type="button" onclick="alert('Clear');">Clear</button>
                    </div>
                </div>

                %{--Select projections--}%
                <div class="heim-input-field heim-radio-group">
                    <label>Select values to use:</label> 
                    <div >
                        <input type="radio" name="valueToUse" value=""> Untransformed (intensity values, counts, etc.)<br>
                        <input type="radio" name="valueToUse" value=""> Log2 Transformed<br>
                        <input type="radio" name="valueToUse" value="" checked> Calculate z-score on the fly<br>
                        <input type="radio" name="valueToUse" value=""> Global z-score<br>
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
                    <button>Apply Changes</button>
                    <button>Status</button>
                </div>

            </form>
        </div>

        <div id="fragment-preprocess">

            <fieldset class="toolFields">
                <div>
                    <input type="checkbox" id="chkGroupBySubject" name="doGroupBySubject">
                    <span>Group by subject (instead of node) for multiple nodes</span>
                </div>
                <div>
                    <input type="checkbox" id="chkCalculateZscore" name="calculateZscore">
                    <span>Calculate z-score on the fly</span>
                </div>
            </fieldset>

        </div>

        <div id="fragment-run">
            %{--TODO--}%
            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
        </div>
    </div>


</div>
