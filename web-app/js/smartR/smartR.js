'use strict';

window.addSmartRPanel = function addSmartRPanel(parentPanel) {
    var smartRPanel = new Ext.Panel({
        id: 'smartRPanel',
        title: 'SmartR',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        collapsible: true,
        autoScroll: true,
        tbar: new Ext.Toolbar({
            id: 'smartRToolbar',
            title: 'R Scripts',
            items: []
        }),
        listeners: {
            render: function (panel) {
                panel.body.on('click', function () {
                    if (typeof updateOnView === "function") {
                        updateOnView();
                    }
                });

                /**
                 * WORKAROUND : code below is needed to reorder the javascript script load that're broken due to
                 * ExtJS panel
                 */
                // start workaround
                var updater = panel.getUpdater();
                updater.on('update', function() {
                    var panelBody = jQuery(arguments[0].dom);
                    var scripts = panelBody.children('script');
                    scripts.remove(); // remove scripts from panel body
                    panelBody.append(scripts); // re-append again
                });
                updater.update({
                    url: pageInfo.basePath + '/smartR/index',
                    method: 'POST',
                    scripts: false });
                // end workaround
            }
        }
    });
    parentPanel.add(smartRPanel);
};

