function getSummaryImageData() {

    resultsTabPanel.body.mask("Loading ..", 'x-mask-loading');

    if (!(GLOBAL.CurrentSubsetIDs[0]) && !(GLOBAL.CurrentSubsetIDs[1])) {
        Ext.Msg.alert('Subsets are unavailable.',
            'Please select one or two Comparison subsets and run Summary Statistics.');
        resultsTabPanel.body.unmask();
        return;
    }

    gridstore = new Ext.data.JsonStore(
        {
           // url : pageInfo.basePath+'/chart/analysisGrid',
            url : pageInfo.basePath+'/table/analysisGrid',
            root : 'rows',
            fields : ['name', 'url']
        }
    );

    gridstore.on('load', imageStoreLoaded);

    var myparams = Ext.urlEncode(
        {
            charttype : "basicgrid",
            concept_key : "",
            result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
        }
    );

    gridstore.load({
        params: myparams,
        callback: function () {
            resultsTabPanel.body.unmask();
        }
    });

}

function imageStoreLoaded() {

    var exportButton = new Ext.Button ({
        text:'Export to Excel',
        listeners: {
            click: function () {
                window.location = 'data:application/vnd.ms-excel;base64,' +
                    Base64.encode(grid.getExcelXml());
            }
        }
    });

    var cm = buildColumnModel(gridstore.reader.meta.fields);

    grid = analysisImagePanel.getComponent('imageView');
    if (grid) {
        analysisImagePanel.remove(grid);
    }

    grid = new GridViewPanel({
        id: 'gridView',
        title: 'Image View',
        viewConfig: {
            forceFit : true
        },
        bbar: new Ext.Toolbar({
            buttons: [exportButton]
        }),
        frame:true,
        layout: 'fit',
        cm : cm,
        store: gridstore
    });

    analysisImagePanel.add(grid);
    analysisImagePanel.doLayout();
}


function buildImageAnalysis(nodein) {
    var node = nodein // getNodeForAnalysis(nodein);
    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
        alert('Empty subsets found, need a valid subset to analyze!');
        return;
    }


    if ((GLOBAL.CurrentSubsetIDs[1] == null && !isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && !isSubsetEmpty(2))) {
        runAllQueries(function () {
                buildAnalysis(node);
            }
        );
        return;
    }

    resultsTabPanel.body.mask("Running analysis...", 'x-mask-loading');

    Ext.Ajax.request(
        {
            url : pageInfo.basePath+"/chart/analysis",
            method : 'POST',
            timeout: '600000',
            params :  Ext.urlEncode(
                {
                    charttype : "analysis",
                    concept_key : node.attributes.id,
                    result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
                    result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
                }
            ), // or a URL encoded string
            success: function (result, request) {
                buildAnalysisComplete(result);
                resultsTabPanel.body.unmask();
            },
            failure: function (result, request) {
                buildAnalysisComplete(result);
            }
        }
    );
    getAnalysisImageData(node.attributes.id);
}


function getAnalysisImageData(concept_key) {
    gridstore = new Ext.data.JsonStore(
        {
            url : pageInfo.basePath+'/chart/analysisGrid',
            root : 'rows',
            fields : ['name', 'url']
        }
    );
    gridstore.on('load', imageStoreLoaded);
    var myparams = Ext.urlEncode(
        {
            charttype : "analysisgrid",
            concept_key : concept_key,
            result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
        }
    );
    // or a URL encoded string */

    gridstore.load({
        params : myparams
    });
}