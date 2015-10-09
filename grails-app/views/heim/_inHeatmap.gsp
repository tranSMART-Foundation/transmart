%{--include js lib for heatmap dynamically--}%
<r:require modules="heatmap"/>
<r:layoutResources disposition="defer"/>

<div id="analysisWidget" style="width: 98%">

    <h2>
        Variable Selection
        <a href='JavaScript:D2H_ShowHelp(1505,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
            <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
        </a>
    </h2>

    <div id="tabs" >
        <ul>
            <li><a href="#fragment-1"><span>Load</span></a></li>
            <li><a href="#fragment-2"><span>Preprocess</span></a></li>
            <li><a href="#fragment-3"><span>Run</span></a></li>
        </ul>
        <div id="fragment-1">
            <form id="analysisForm">
                <fieldset class="inputFields">

                    %{--High dimensional input--}%
                    <div class="highDimContainer">
                        <span>Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into
                        the box. The nodes needs to be from the same platform.</span>
                        <div id='divIndependentVariable' class="queryGroupIncludeSmall highDimBox"></div>
                        <div class="highDimBtns">
                            %{--<button type="button" onclick="highDimensionalData.gather_high_dimensional_data('divIndependentVariable')">High Dimensional Data</button>--}%
                            <button type="button" onclick="heatMapView.clear_high_dimensional_input('divIndependentVariable')">Clear</button>
                        </div>
                    </div>

                    %{--Display independent variable--}%
                    <div id="displaydivIndependentVariable" class="independentVars"></div>

                    <label for="txtMaxDrawNumber">Max rows to display:</label>
                    <input type="text" id="txtMaxDrawNumber"  value="50"/>

                </fieldset>

            </form>
        </div>
        <div id="fragment-2">
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

            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
        </div>
        <div id="fragment-3">
            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
            Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.
        </div>
    </div>


</div>
