var GWASPlinkAdd
(function (_this) {
    function getAdvancedWorkflowMenu() {
        var toolbarItems = Ext.getCmp('advancedWorkflowToolbar').items.items;
        for (var i = 0; i < toolbarItems.length; i++) {
            var toolbarItem = toolbarItems[i];
            if (toolbarItem.text == 'Analysis') {
                return toolbarItem.menu;
            }
        }
        return null;
    }


    var gwasPlinkPanel = new Ext.Panel(
        {
            id: "gwasPlink",
            title: "GWAS Plink",
            layout: "fit",
            autoLoad: {
                url: plinkViewUrl,
                scripts: true,
                nocache: true,
                discardUrl: true,
                method: 'POST'
            },
            listeners: {
                activate: function (p) {
                    // p.body.mask("Loading...", 'x-mask-loading');
                    // p.body.unmask();
                }
            }
        }
    );

    var analysisId = 'gwasPlink';
    var analysisName = 'GWAS Plink';

    function showGwasPlinkAnalysis() {
        Ext.get('analysis').dom.value = analysisId;
        Ext.fly('selectedAnalysis').update(analysisName, false).removeClass('warning').addClass('selected');
        var selection = $j('#variableSelection');
        selection.mask("Loading...", 'x-mask-loading');
        $j.ajax({
            url: plinkViewUrl,
            success: function (response, status) {

                // insert response into 'variableSelection' html element
                $j('#variableSelection').html(response);

                document.getElementById("analysisOutput").innerHTML = "";
            }
        });
    }

    function addGwasPlinkAnalysis(resultTabsPanel) {
        var menu = getAdvancedWorkflowMenu();
        if (!menu) {
            setTimeout(function () {
                addGwasPlinkAnalysis(resultTabsPanel)
            }, 10);
            return;
        }
        menu.add({id: analysisId, text: analysisName, group: 'Default', handler: showGwasPlinkAnalysis});
    }

    var plinkViewUrl = pageInfo.basePath + '/gwasPlink/show';
    // exports
    _this.addGwasPlinkAnalysis = addGwasPlinkAnalysis;
    _this.gwas_plink = {
        runAnalysis: function () {
            var analysisName = document.getElementById("analysisName").value;
            var phenotypeList = []
            var phenotype = Ext.get("divCategoryVariable")
            if (phenotype && phenotype.dom.childNodes.length > 0) {
                var child = phenotype.dom.childNodes;
                var arrayNode = Array.prototype.slice.call(child);
                arrayNode.forEach(function (item) {
                    phenotypeList.push(item.getAttribute('concepttooltip'))
                });
            }
            var covariateList = []
            var covariates = Ext.get("divGroupByVariable")
            if (covariates && covariates.dom.childNodes.length > 0) {
                var child = covariates.dom.childNodes;
                var arrayNode = Array.prototype.slice.call(child);
                arrayNode.forEach(function (item) {
                    covariateList.push(item.getAttribute('concepttooltip'))
                });
            }
            if (analysisName.toLowerCase() == 'plink') {
                Ext.Msg.alert('Invalid analysis name!', 'Please select analysis name other than `plink`.');
                return;
            }
            this.submitJob({
                jobType: 'gwasPlink',
                plinkAnalysisName: analysisName,
                plinkAnalysisType: document.getElementById("analysisType").value,
                pValueThreshold: document.getElementById("pValueThreshold").value,
                previewRowsCount: document.getElementById("previewRowsCount").value,
                makePheno: document.getElementById("makePheno").checked ? 1 : '',
                phenotypes: Ext.encode(phenotypeList),
                covariates: Ext.encode(covariateList),
                additionalOption: jQuery('[name=radiotype]:checked').val()
            });
        },
        submitJob: function (formParams) {
            //Make sure at least one subset is filled in.
            if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
                Ext.Msg.alert('Missing input!', 'Please select a cohort from the \'Comparison\' tab.');
                return;
            }

            var _this = this;
            if ((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null)) {
                runAllQueries(function () {
                    _this.submitJob(formParams);
                });
                return;
            }
            createWorkflowStatus($j('#dataAssociationBody'), true);

            Ext.Ajax.request({
                url: pageInfo.basePath + "/asyncJob/createnewjob",
                method: 'POST',
                success: function (result, request) {
                    //Handle data export process
                    _this.runJob(result, formParams);
                },
                failure: function (result, request) {
                    Ext.Msg.alert('Status', 'Unable to create data export job.');
                },
                timeout: '1800000',
                params: formParams
            });

        },
        runJob: function (result, formParams) {
            var jobNameInfo = Ext.util.JSON.decode(result.responseText);
            var jobName = jobNameInfo.jobName;
            setJobNameFromRun(jobName);

            formParams.result_instance_id1 = GLOBAL.CurrentSubsetIDs[1];
            formParams.result_instance_id2 = GLOBAL.CurrentSubsetIDs[2];
            formParams.analysis = document.getElementById("analysis").value;
            formParams.jobName = jobName;

            Ext.Ajax.request(
                {
                    url: pageInfo.basePath + "/gwasPlink/scheduleJob",
                    method: 'POST',
                    timeout: '1800000',
                    params: Ext.urlEncode(formParams) // or a URL encoded string
                });

            //Start the js code to check the job status so we can display results when we are done.
            checkPluginJobStatus(jobName)
        }
    };

})(this);
