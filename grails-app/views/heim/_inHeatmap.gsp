%{--include js lib for heatmap dynamically--}%
<r:require modules="heatmap"/>
<r:layoutResources disposition="defer"/>

<div style="width: 98%">

    <p>
        Start Heatmap analysis with load, preprocess options and then run analysis.
    </p>

    <div id="heim-tabs" style="margin-top: 25px;">

        <ul>
            <li><a href="#fragment-load"><span>Load</span></a></li>
            <li><a href="#fragment-preprocess"><span>Preprocess</span></a></li>
            <li><a href="#fragment-run"><span>Run</span></a></li>
        </ul>

        %{--========================================================================================================--}%
        %{--Load Data--}%
        %{--========================================================================================================--}%
        <div id="fragment-load">
                %{--High dimension dropzone--}%
                <div class="heim-input-field heim-dropzone">
                    %{--High dimensional input--}%
                    <label>Select high dimensional data node(s) from the Data Set Explorer Tree and drag it into
                    the box. The nodes needs to be from the same platform.</label>
                    <br><br>
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
                    <input id="heim-input-txt-identifiers">
                </div>

                %{--tool buttons--}%
                <div style="margin-top: 10px; text-align: right;">
                    <button id="heim-btn-fetch-data">Fetch Data</button>
                </div>

                <div id="heim-fetch-data-output">
                    ---- output place holder ----
                </div>
        </div>

        %{--========================================================================================================--}%
        %{--Preprocess--}%
        %{--========================================================================================================--}%
        <div id="fragment-preprocess">
            <form>
                %{--Calculate z-score--}%
                <div class="heim-input-field ">
                    <input type="checkbox" id="chkCalculateZscore" name="chkCalculateZscore">
                    <span>Calculate z-score on the fly</span>
                </div>

                %{--Calculate log fold--}%
                <div class="heim-input-field ">
                    <input type="checkbox" id="chkLogFoldChange" name="chkLogFoldChange">
                    <span>Calculate log-fold change based on two data nodes</span>
                </div>
                <div class="heim-input-field ">
                    <select id="logFoldInput1">
                        %{--TODO--}%
                        <option>dummy 1</option>
                        <option>dummy 2</option>
                        <option>dummy 3</option>
                        <option>dummy 4</option>
                    </select>
                    relative to
                    <select id="logFoldInput2">
                        %{--TODO--}%
                        <option>dummy 1</option>
                        <option>dummy 2</option>
                        <option>dummy 3</option>
                        <option>dummy 4</option>
                    </select>
                </div>

                %{--tool buttons--}%
                <div style="margin-top: 10px; text-align: right;">
                    <button id="heim-btn-preprocess-heatmap">Preprocess</button>
                </div>
            </form>
        </div>

        %{--========================================================================================================--}%
        %{--Run Analysis--}%
        %{--========================================================================================================--}%
        <div id="fragment-run">

            %{--Aggregate Probes--}%
            <div class="heim-input-field">
                <input type="checkbox" id="chkAggregateProbes" name="chkAggregateProbes">
                <span>Aggregate probes</span>
            </div>

            %{--Number of max row to display--}%
            <div class="heim-input-field heim-input-number">
                <label>Number of max row to display</label>
                <input type="text" id="txtMaxRow" value="50">
            </div>

            %{--Group Subject--}%
            <div class="heim-input-field">
                <input type="checkbox" id="chkGroupSubject" name="chkGroupSubject">
                <span>Group by subject instead of node (only applicable when multiple nodes are selected).</span>
            </div>

            %{--Apply statistical methods--}%
            <div class="heim-input-field heim-input-number">
                <label>Apply statistical methods</label>
                <select>
                    <option value="none">None</option>
                    <option value="hierarchical-clustering">Hierarchical Clustering</option>
                    <option value="k-means-clustering">K-Means Clustering</option>
                    <option value="marker-selection">Marker Selection</option>
                </select>
            </div>

            <hr style="margin-top: 20px;">

            %{--Options--}%
            <div>
                %{--Number of clusters--}%
                <div class="heim-input-field heim-input-number">
                    <label>Number of clusters</label>
                    <input type="text" id="txtNoOfClusters" name="txtNoOfClusters" value="2">
                </div>

                %{--Number of markers--}%
                <div class="heim-input-field heim-input-number">
                    <label>Number of markers</label>
                    <input type="text" id="txtNoOfMarkers" name="txtNoOfMarkers">
                </div>

                %{--apply row clustering--}%
                <div class="heim-input-field">
                    <input type="checkbox" id="chkApplyRowClustering" name="chkApplyRowClustering">
                    <span>Apply clustering for row.</span>
                </div>

                %{--apply column clustering --}%
                <div class="heim-input-field">
                    <input type="checkbox" id="chkApplyColumnClustering" name="chkApplyColumnClustering">
                    <span>Apply clustering for column.</span>
                </div>

            </div>

            %{--tool buttons--}%
            <div style="margin-top: 10px; text-align: right;">
                <button id="heim-btn-run-heatmap">Get Heatmap</button>
            </div>


        </div>
    </div>


</div>
