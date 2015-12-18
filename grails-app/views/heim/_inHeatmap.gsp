<r:require modules="smartR_heatmap"/>
<r:layoutResources disposition="defer"/>

 %{--Template for summary statistic--}%
<script id="summary-row-tmp" type="text/x-jsrender">
    <table class="sr-summary-table">
        <tbody>
        <tr>
            <th>Loaded</th>
            <th>Subset 1</th>
            <th>Subset 2</th>
        </tr>
        {{for summaryStat}}
        <tr>
            <td>{{:key}}</td>
            <td>{{:val1}}</td>
            <td>{{:val2}}</td>
        </tr>
        {{/for}}
        </tbody>
    </table>
</script>

%{--Template for --}%
<script id="marker-selection-table-tmp" type="text/x-jsrender">
    <table id="markerSelectionTable" class="tablesorterAnalysisResults">
        <thead>
            <tr>
                <th class="header">Row label</th>
                <th class="header">Entity</th>
                <th class="header">Log2(fold change) S2 vs S1</th>
                <th class="header">t</th>
                <th class="header">p-value</th>
                <th class="header">Adjusted p-value</th>
                <th class="header">B</th>
            </tr>
        </thead>
        <tbody>
            {{for entries}}
            <tr>
                <td>{{:rowLabel}}</td>
                <td>{{:biomarker}}</td>
                <td>{{:log2FoldChange}}</td>
                <td>{{:t}}</td>
                <td>{{:pValue}}</td>
                <td>{{:adjustedPValue}}</td>
                <td>{{:B}}</td>
            </tr>
            {{/for}}
        </tbody>
    </table>
</script>

%{--Empty Result -Dialog--}%
<div id="sr-fetch-dialog" title="Fetch Data" style="display: none">
    <p>This action will replace all results from previous processes. Are you sure?</p>
</div>


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

            <div class="sr-input-area">

                %{--High dimension dropzone--}%
                <div class="heim-input-field heim-dropzone sr-hd-input">
                    %{--High dimensional input--}%
                    <label>High dimensional <i class="ui-icon ui-icon-info" title="Select high dimensional data node(s) from the Data
                        Set Explorer Tree and drag it into the box. The nodes needs to be from the same platform.">
                    </i></label>

                    <br><br>

                    <div id='heim-high-dim-var' class="sr-drop-input"></div>

                    <div style="margin-top: 10px; text-align: right;">
                        <button type="button" id="heim-btn-clear">Clear</button>
                    </div>
                </div>

                %{--TODO: Support numerical nodes--}%
                %{--Low dimension dropzone (numerical)--}%
                <div class="heim-input-field heim-dropzone sr-low-input">
                    %{--Numerical node input--}%
                    <label>Numerical (optional) <i class="ui-icon ui-icon-info"
                                                   title="Select numerical data node from the Data
                            Set Explorer Tree and drag it into the box.">
                    </i></label>
                    <br><br>

                    <div id='heim-num-var' class="sr-drop-input"></div>

                    <div style="margin-top: 10px; text-align: right;">
                        <button type="button" id="heim-btn-clear-2" disabled>Clear</button>
                    </div>
                </div>

                %{--TODO: Support categorical  nodes--}%
                %{--Low dimension dropzone (categorical)--}%
                <div class="heim-input-field heim-dropzone sr-low-input">
                    %{--High dimensional input--}%
                    <label>Categorical (optional) <i class="ui-icon ui-icon-info" title="Select categorical data node from the Data
                                Set Explorer Tree and drag it into the box.">
                    </i></label>
                    <br><br>

                    <div id='heim-categ-var' class="sr-drop-input"></div>

                    <div style="margin-top: 10px; text-align: right;">
                        <button type="button" id="heim-btn-clear-3" disabled>Clear</button>
                    </div>
                </div>
            </div>

            <div class="sr-fetch-params-area">
                %{--Select identifier--}%
                <div class="heim-input-field heim-autocomplete">
                    <label for="heim-input-txt-identifier">Select a Gene/Pathway/mirID/UniProtID:</label>
                    <input id="heim-input-txt-identifier">
                    <div id="heim-input-list-identifiers"></div>
                </div>
            </div>

            <hr class="sr-divider">

            %{--tool buttons--}%
            <div>
                <button id="heim-btn-fetch-data" class="heim-action-button">Fetch Data</button>
            </div>

            <div id="heim-fetch-output" class="sr-output-container"></div>

        </div>

        %{--========================================================================================================--}%
        %{--Preprocess--}%
        %{--========================================================================================================--}%
        <div id="fragment-preprocess">

            %{--Aggregate Probes--}%
            <div class="heim-input-field">
                <input type="checkbox" id="chkAggregateProbes" name="chkAggregateProbes">
                <span>Aggregate probes</span>
            </div>

            <hr class="sr-divider">

            %{--tool buttons--}%
            <div style="margin-top: 10px;">
                <button id="heim-btn-preprocess-heatmap" class="heim-action-button">Preprocess</button>
            </div>


            %{--Preprocess Output--}%
            <div id="heim-preprocess-output" class="sr-output-container"></div>
        </div>

        %{--========================================================================================================--}%
        %{--Run Analysis--}%
        %{--========================================================================================================--}%
        <div id="fragment-run">

            %{--Number of max row to display--}%
            <div class="heim-input-field heim-input-number sr-input-area">
                <label>Max row to display</label>
                <input type="text" id="txtMaxRow" value="100"> rows (< 1000 is preferable).
            </div>

            %{--Type of sorting to apply--}%
            <div class="heim-input-field sr-input-area">
                <h2>Sort on:</h2>
                <fieldset class="heim-radiogroup">
                    <label>
                        <input type="radio" name="sortingSelect" value="nodes" checked> Nodes
                    </label>
                    <label>
                        <input type="radio" name="sortingSelect" value="subjects"> Subjects
                    </label>
                </fieldset>
            </div>

        %{--Type of sorting to apply--}%
        <div class="heim-input-field  sr-input-area">
            <h2>Ranking Criteria:</h2>
            <div class="heim-input-field-sub" id="sr-non-multi-subset">
                <fieldset class="heim-radiogroup" id="sr-variability-group">
                    <h3>Expression variability</h3>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="coef"> Coefficient of variation
                    </label></div>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="variance"> Variance
                    </label></div>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="range"> Range between max and min after excluding outliers
                    </label></div>
                </fieldset>
                <fieldset class="heim-radiogroup" id="sr-expression-level-group">
                    <h3>Expression level</h3>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="mean"> Mean
                    </label></div>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="median"> Median
                    </label></div>
                </fieldset>
            </div>
            <div class="heim-input-field-sub" id="sr-multi-subset">
                <fieldset class="heim-radiogroup" id="sr-differential-exp-group">
                    <h3>Differential expression</h3>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="bval"> B-value/log odds ratio
                    </label></div>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="pval"> p-value
                    </label></div>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="adjpval"> Adjusted p-value (method “fdr”)
                    </label></div>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="logfold"> log fold-change
                    </label></div>
                    <div><label>
                        <input type="radio" name="rankCriteria" value="ttest"> t-statistic
                    </label></div>
                </fieldset>
            </div>
        </div>


        %{--TODO :Not relevant for now. Might be in future sprint--}%

        %{--Group Subject--}%
        %{--<div class="heim-input-field">--}%
        %{--<input type="checkbox" id="chkGroupSubject" name="chkGroupSubject">--}%
        %{--<span>Group by subject instead of node (only applicable when multiple nodes are selected).</span>--}%
        %{--</div>--}%

        %{--Apply statistical methods--}%
        %{--<div class="heim-input-field heim-input-number">--}%
        %{--<label>Apply statistical methods</label>--}%
        %{--<select id="methodSelect">--}%
        %{--<option value="none">None</option>--}%
        %{--<option value="hierarchical-clustering">Hierarchical Clustering</option>--}%
        %{--<option value="k-means-clustering">K-Means Clustering</option>--}%
        %{--<option value="marker-selection">Marker Selection</option>--}%
        %{--</select>--}%
        %{--</div>--}%

        %{--Options--}%
        <div id="clusteringOptionsDiv" hidden>
            %{--Number of clusters--}%
            <div class="heim-input-field heim-input-number" id="noOfClustersDiv">
                <label>Number of clusters</label>
                <input type="text" id="txtNoOfClusters" name="txtNoOfClusters" value="2">
            </div>

            %{--Number of markers--}%
            <div class="heim-input-field heim-input-number" id="noOfMarkersDiv">
                <label>Number of markers</label>
                <input type="text" id="txtNoOfMarkers" name="txtNoOfMarkers">
            </div>

        </div>

        <hr class="sr-divider">

        %{--tool buttons--}%
        <div>
            <button id="heim-btn-run-heatmap" class="heim-action-button">Generate Heatmap</button>
            <button id="heim-btn-snapshot-image" class="heim-action-button" disabled>Capture Heatmap</button>
            <button id="heim-btn-download-file" class="heim-action-button" disabled>Download Data</button>
        </div>

        %{--result--}%
        <div id="heim-run-output" class="sr-output-container"></div>

        %{--d3 js heatmap placeholder--}%
        <div id='visualization' class='text sr-output-container'>
            <div id="heatmap" class='text'></div>
        </div>
    </div>


    </div>
</div>
