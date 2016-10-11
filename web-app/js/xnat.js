

var EWB = {

    injectToAnalyseTab: function () {

        // We need to wait for tranSMART ExtJS layout to have initialized
        var _cmp = Ext.getCmp("resultsTabPanel")

        if (!_cmp)
            return setTimeout(EWB.injectToAnalyseTab, 500)


        // This is legacy tranSMART call on ExtJS

        //jQuery('' +
        //    '<li class=" " id="resultsTabPanel__analysisImageViewPanel">' +
        //        '<a class="x-tab-strip-close" onclick="return false;"></a>' +
        //        '<a class="x-tab-right" href="#" onclick="return false;">' +
        //            '<em class="x-tab-left">' +
        //                '<span class="x-tab-strip-inner">' +
        //                    '<span class="x-tab-strip-text ">Image View</span>' +
        //                '</span>' +
        //            '</em>' +
        //        '</a>' +
        //    '</li>').insertAfter('#resultsTabPanel__analysisGridPanel')

        console.log("Image View Injection Completed")

        analysisImagePanel = new Ext.Panel(
            {
                id : 'analysisImagePanel',
                title : 'Image View',
                region : 'center',
                split : true,
                height : 90,
                layout: 'fit',
                listeners: {
                    activate: function (p) {
                        if (isSubsetQueriesChanged(p.subsetQueries) || !Ext.get('analysis_title')) {
                            runAllQueries(getSummaryImageData, p);
                            activateTab();
                            onWindowResize();
                        } else {
                            getSummaryImageData();
                        }
                    },
                    deactivate: function(){
                        resultsTabPanel.tools.help.dom.style.display = "none";
                    },
                    'afterLayout': {
                        fn: function (el) {
                            onWindowResize();
                        }
                    }
                }
            }
        );
        _cmp.add(analysisImagePanel)
        //_cmp.getTopToolbar().add({
        //    text: 'Image View',
        //    id: 'ewbexportbutton',
        //    iconCls: 'exportbutton',
        //    handler: function () {
        //
        //        if (GLOBAL.CurrentSubsetIDs.length)
        //            EWB.sendSummary(GLOBAL.CurrentSubsetIDs)
        //        else
        //            alert("You have not yet run any selection !")
        //    }
        //})
    },

    injectToWorkflowTab: function () {

        // We need to wait for tranSMART ExtJS layout to have initialized
        var _cmp = Ext.getCmp("dataAssociationPanel")

        // Here we need a second check to make sure we position the button on the right
        if (!_cmp || !_cmp.getTopToolbar().items.length)
            return setTimeout(EWB.injectToWorkflowTab, 500)

        // This is legacy tranSMART call on ExtJS
        _cmp.getTopToolbar().add('->', {
            text: 'Export Analysis to EWB',
            id: 'ewbexportbutton',
            iconCls: 'exportbutton',
            handler: function () {

                if (GLOBAL.CurrentSubsetIDs.length)
                    EWB.sendAnalysis(GLOBAL.CurrentSubsetIDs)
                else
                    alert("You have not yet run any selection !")
            }
        })
    },

    sendSummary: function (subsets) {
        EWB.withCredentialsCheck(function () {
            EWB.withSavingValues(function (values) {

                var _params = jQuery.extend({
                    1: subsets[1],
                    2: subsets[2]
                }, values)

                jQuery.post("/transmart/EWB/saveToEntity", _params, function (data) {
                    if (data.status != "OK")
                        EWB.printOutError("saving", data);
                    else {

                        var _link = jQuery("<a />").attr("href", data.link).attr("target", "_blank").html(data.entity)
                        jQuery("#ewb-result-entity").append(_link)
                        jQuery("#ewb-result-dialog").dialog({
                            autoOpen: false,
                            height: 200,
                            width: 300,
                            modal: true,
                            buttons: {
                                OK: function () {
                                    jQuery("#ewb-result-dialog").dialog("close")
                                }
                            },
                            close: function () {
                                jQuery("#ewb-result-entity").html("")
                            }
                        }).dialog("open")
                    }
                    jQuery("#ewb-progress-dialog").dialog("close")
                }, "json")
            })
        })
    },

    sendAnalysis: function(subsets) {
        EWB.withCredentialsCheck(function () {
            EWB.withSavingValues(function (values) {

                var _params = jQuery.extend({
                    1: subsets[1],
                    2: subsets[2],
                    type: jQuery('#analysis').val(),
                    parameters: JSON.stringify(EWB.extractAnalysis())
                }, values)

                jQuery.post("/transmart/EWB/saveAsEntity", _params, function (data) {
                    if (data.status != "OK")
                        EWB.printOutError("saving", data);
                    else {

                        var _link = jQuery("<a />").attr("href", data.link).attr("target", "_blank").html(data.entity)
                        jQuery("#ewb-result-entity").append(_link)
                        jQuery("#ewb-result-dialog").dialog({
                            autoOpen: false,
                            height: 200,
                            width: 300,
                            modal: true,
                            buttons: {
                                OK: function () {
                                    jQuery("#ewb-result-dialog").dialog("close")
                                }
                            },
                            close: function () {
                                jQuery("#ewb-result-entity").html("")
                            }
                        }).dialog("open")
                    }
                    jQuery("#ewb-progress-dialog").dialog("close")
                }, "json")
            })
        })
    },

    promptCredentials: function () {
        EWB.withCredentialsTemplate(function () {
            EWB.promptCredentialsDialog.dialog("open")
        })
    },

    promptSaving: function () {
        EWB.withSavingTemplate(function () {
            EWB.promptSavingDialog.dialog("open")

            var _filename = "TRANSMART_" + GLOBAL.CurrentSubsetIDs[1] + (GLOBAL.CurrentSubsetIDs[2] ? "_" + GLOBAL.CurrentSubsetIDs[2] : "") + ".pdf"
            EWB.promptSavingDialog.find("#ewb-filename").val(_filename).select()
        })
    },

    closeCredentialsDialog: function () {
        EWB.withCredentialsTemplate(function () {
            EWB.promptCredentialsDialog.dialog("close")
        })
    },

    closeSavingDialog: function () {
        EWB.withSavingTemplate(function () {
            EWB.promptSavingDialog.dialog("close")
        })
    },

    validateCredentials: function () {

        var _l = jQuery("#ewb-login", EWB.promptCredentialsDialog).val()
        var _p = jQuery("#ewb-password", EWB.promptCredentialsDialog).val()
        var _t = window.btoa(_l + ":" + _p)

        jQuery.post("/transmart/EWB/checkCredentials", {t: _t}, function (data) {
            if (data.status != "OK")
                EWB.printOutError("credentials", data);
            else {
                GLOBAL.EWB.credentials = _t
                EWB.closeCredentialsDialog()

                if (EWB.promptCredentialsValidationAction)
                    EWB.promptCredentialsValidationAction()
                EWB.promptCredentialsValidationAction = null
            }
        }, "json")
    },

    validateSaving: function () {

        var _filename = jQuery("#ewb-filename", EWB.promptSavingDialog).val().trim()
        if (!_filename.length)
            return EWB.printOutError(null, {message: 'You have not given a name to your document !'})

        var _destination = EWB.getHierarchyBrowser('ewb-hierarchy').getSelectionModel().getSelectedNode()
        if (!_destination)
            return EWB.printOutError(null, {message: 'You must select a destination first !'})

        var _fields = {
            name: _filename,
            entity: _destination.id
        }

        EWB.closeSavingDialog()
        jQuery("#ewb-progress-dialog").dialog({
            autoOpen: false,
            height: 75,
            width: 300,
            modal: true
        }).dialog("open")

        if (EWB.promptSavingValidationAction)
            EWB.promptSavingValidationAction(_fields)
        EWB.promptSavingValidationAction = null
    },

    withCredentialsCheck: function (action) {

        if (!GLOBAL.EWB || !GLOBAL.EWB.credentials) {
            EWB.promptCredentialsValidationAction = action
            EWB.promptCredentials()
        }
        else
            action()
    },

    withSavingValues: function (action) {

        EWB.promptSavingValidationAction = action
        EWB.promptSaving()
    },

    withCredentialsTemplate: function () {

        var _func = arguments.length > 0 ? arguments[0] : function () {}
        if (EWB.promptCredentialsDialog)
            return _func()

        jQuery.get("/transmart/EWB/credentialsDialog", function (data) {

            if (EWB.promptCredentialsDialog)
                return

            jQuery("body").append(data)

            EWB.promptCredentialsDialog = jQuery("#ewb-credentials-dialog").dialog({
                autoOpen: false,
                height: 280,
                width: 350,
                modal: true,
                buttons: {
                    Connect: EWB.validateCredentials,
                    Cancel: EWB.closeCredentialsDialog
                },
                close: function () {
                    EWB.promptCredentialsForm[0].reset();
                }
            })

            EWB.promptCredentialsForm = EWB.promptCredentialsDialog.find("form").on("submit", function (event) {
                event.preventDefault()
                EWB.validateCredentials()
            })

            _func()
        })
    },

    withSavingTemplate: function () {

        var _func = arguments.length > 0 ? arguments[0] : function () {}
        if (EWB.promptSavingDialog) {

            // TODO This needs to go, all nodes should be cleared, not just collapsed. For now that will do.
            EWB.getHierarchyBrowser('ewb-hierarchy').collapseAll()
            return _func()
        }

        jQuery.get("/transmart/EWB/savingDialog", function (data) {
            if (EWB.promptSavingDialog)
                return

            jQuery("body").append(data)
            EWB.promptSavingDialog = jQuery("#ewb-save-dialog").dialog({
                autoOpen: false,
                height: 400,
                width: 350,
                modal: true,
                buttons: {
                    Send: EWB.validateSaving,
                    Cancel: EWB.closeSavingDialog
                },
                close: function () {
                    EWB.promptSavingForm[0].reset();
                },
                resizeStop: function( event, ui ) {

                    var _tree = EWB.getHierarchyBrowser('ewb-hierarchy')
                    var _width = _tree.getSize().width
                    var _height = _tree.getSize().height

                    _width += ui.size.width - ui.originalSize.width
                    _height += ui.size.height - ui.originalSize.height

                    _tree.setSize(_width, _height)
                }
            })

            EWB.constructHierarchyBrowser('ewb-hierarchy')

            EWB.promptSavingForm = EWB.promptSavingDialog.find("form").on("submit", function (event) {
                event.preventDefault()
                EWB.validateSaving()
            })

            _func()
        })


    },

    extractAnalysis: function () {

        var _result = {}
        var _scope = jQuery("#variableSelection")

        _scope.find("input[type!=button],select,textarea,div[class*=panelBoxListItem]").each(function (i, e) {

            var _e = jQuery(e)
            var _n = _e.prop('name') || _e.prop('id')

            switch (e.tagName.toLowerCase()) {
                case 'div' :

                    var _k = _e.closest("*[id]:not([id^=ext])").prop('id')
                    _result[_k] = _result[_k] || []
                    _result[_k].push(function () {

                        var _v = {}
                        jQuery.map(e.attributes, function (v) {
                            _v[v.nodeName] = v.value
                        })
                        return _v

                    }())
                    break;

                case 'input' :

                    if (_e.prop('type') == 'checkbox') {
                        _result[_n] = _e.is(':checked')
                        break;
                    }
                    if (_e.prop('type') == 'radio' && _e.is(':checked')) {

                        // Following line is due to crap use of crap ExtJS
                        _result[_n] = jQuery("label[for=" + _e.prop('id') + "].x-form-cb-label").html()
                        break;
                    }

                default :
                    _result[_n] = _e.val()
            }
        })

        return _result
    },

    populateAnalysis: function() {

        var _limit = 100
        var _s = EWB.constructScenarioData.parameters
        while (Object.keys(_s).length && _limit-- > 0) {
            for (var _k in _s) {

                if (!_s.hasOwnProperty(_k))
                    continue;

                var _e = jQuery("*[name=" + _k + "],#" + _k)
                if (!_e.size())
                    continue;

                if (_s[_k] instanceof Array && _s[_k][0] instanceof Object) {

                    // Following line is due to crap use of crap ExtJS
                    _e = _e.find(".x-panel-body").size() ? _e.find(".x-panel-body") : _e
                    for (var _i in _s[_k])
                        if (_s[_k].hasOwnProperty(_i))
                            _e.append(getPanelItemFromConcept(_s[_k][_i]))

                } else {

                    switch (_e.prop('type')) {
                        case 'checkbox' :
                            _s[_k] && !_e.is(':checked') ? _e.click() : null
                            break;
                        case 'radio' :

                            jQuery.each(jQuery("input[name=" + _k + "]").map(function () {
                                return jQuery(this).prop('id')
                            }), function (i, t) {

                                // Following line is due to crap use of crap ExtJS
                                var _l = jQuery("label[for=" + t + "].x-form-cb-label")
                                if (_l.html().trim() == _s[_k])
                                    _l.click()
                            })
                            break;
                        default :
                            _e.val(_s[_k])
                    }
                }

                delete _s[_k]
            }
        }
    },

    switchingCohorts: function () {

        // We need to wait for tranSMART ExtJS layout to have initialized
        var _cmp = jQuery("#queryTable")

        if (!_cmp.size())
            return setTimeout(function() {
                EWB.switchingAnalysis()
            }, 500);

        jQuery.get(pageInfo.basePath + '/subset/getQueryForResultInstance', {

            1: EWB.constructScenarioData.subsets[1],
            2: EWB.constructScenarioData.subsets[2]
        }, function(data){

            var _obj = {}
            if (data.query1) {
                _obj[1] = data.query1
            }
            if (data.query2) {
                _obj[2] = data.query2
            }

            refillQueryPanels(_obj)

            GLOBAL.CurrentSubsetIDs[1] = EWB.constructScenarioData.subsets[1] ? EWB.constructScenarioData.subsets[1] : null
            GLOBAL.CurrentSubsetIDs[2] = EWB.constructScenarioData.subsets[2] ? EWB.constructScenarioData.subsets[2] : null

            renderCohortSummary()

            if (!--EWB.constructAnalysisSteps)
                resultsTabPanel.body.unmask()
        });
    },

    switchingAnalysis: function() {

        // We need to wait for tranSMART ExtJS layout to have initialized
        var _cmp = Ext.getCmp("dataAssociationPanel")

        if (!_cmp)
            return setTimeout(function() {
                EWB.switchingAnalysis()
            }, 500);

        if (dataAssociationPanel.hidden) {
            dataAssociationPanel.show();
            loadAnalysisPage(EWB.constructScenarioData.type);
        }

        if (!jQuery("#variableSelection").html().length) {
            return setTimeout(function() {
                EWB.switchingAnalysis()
            }, 500);
        }

        EWB.populateAnalysis()

        if (!--EWB.constructAnalysisSteps)
            resultsTabPanel.body.unmask()
    },

    constructScenario: function() {

        var _id = function() {
            var _urlvars = {}
            window.location.search.replace(new RegExp("([^?=&]+)(=([^&]*))?", "g"), function($0, $1, $2, $3) {
                _urlvars[$1] = $3;
            })
            return _urlvars
        }()['ewb-entity']

        if(!_id)
            return;

        EWB.withCredentialsCheck(function () {
            jQuery.post("/transmart/EWB/fetchEntityAttributes", {id : _id}, function(data) {

                if (data.status != "OK")
                    EWB.printOutError("retrieving", data);
                else {
                    EWB.constructScenarioData = data.attributes
                    EWB.switchingCohorts()
                    EWB.switchingAnalysis()
                }
            })

            resultsTabPanel.body.mask("Restoring Analysis ...", 'x-mask-loading');
        })
    },

    constructHierarchyBrowser: function(destination) {

        // This is legacy tranSMART call on ExtJS
        // We use tools provided by tranSMART rather than adding supplementary JS
        var _root = new Ext.tree.AsyncTreeNode()
        var _loader = new (Ext.extend(Ext.tree.TreeLoader, {
            processResponse: function (response, node, callback) {

                var _children = jQuery.parseJSON(response.responseText)

                node.beginUpdate();

                jQuery.each(_children, function (index, child) {
                    if (!(child.leaf && !child.selectable))
                        node.appendChild(new Ext.tree.AsyncTreeNode({
                            id: child.id,
                            text: child.text,
                            leaf: child.leaf,
                            selectable: child.selectable,
                            cls: !child.selectable ? 'ewb-hierarchy-no-select' : '',
                            icon: '/transmart/EWB/fetchEntityTypeIcon?t=' + child.type,
                            listeners: {
                                'beforeclick' : {
                                    fn: function () {
                                        if (!this.attributes.selectable) {
                                            if (this.isExpanded())
                                                this.collapse()
                                            else
                                                this.expand()
                                            return false
                                        }
                                    }
                                }
                            }
                        }))
                })

                node.endUpdate();

                if (typeof callback == "function") {
                    callback(this, node);
                }

                jQuery.each(node.childNodes, function (idx, e) {
                    if (e.hidden)
                        node.removeChild(e)
                })

                if (node.childNodes.length == 0)
                    node.expandable = false
                if (node.childNodes.length == 1)
                    node.childNodes[0].expand()
            }
        }))({
            dataUrl: '/transmart/EWB/fetchHierarchyChildren'
        })

        var _tree = new Ext.tree.TreePanel({
            height: 150,
            autoScroll: true,
            nodeType: 'async',
            lines: true,
            loader: _loader,
            root: _root,
            rootVisible: false,
            renderTo: Ext.get(destination)
        });

        _tree.getRootNode().expand()

        return _tree
    },

    getHierarchyBrowser: function(location) {

        // This is legacy tranSMART call on ExtJS
        // We use tools provided by tranSMART rather than adding supplementary JS
        return Ext.getCmp(Ext.query("#" + location + " .x-tree")[0].id)
    },

    printOutError: function (type, data) {

        _message = data.message
        if (data.detail)
            _message += " :\n" + data.detail

        alert(_message)
    },

    promptCredentialsDialog: null,
    promptCredentialsForm: null,
    promptCredentialsValidationAction: null,

    promptSavingDialog: null,
    promptSavingForm: null,
    promptSavingValidationAction: null,

    constructAnalysisSteps: 2
}

jQuery(document).ready(function () {

    console.log("XNAT Plugin Present")

    if (!window.btoa || window.btoa(":") != "Og==")
        alert("XNAT Plugin Fatal Error : btoa() is undefined")

    GLOBAL.EWB = {}

    EWB.injectToAnalyseTab()
   // EWB.injectToWorkflowTab()
   // EWB.constructScenario()

});