DO NOT TRY TO ADD SmartR TO YOUR tranSMART INSTALLATION IN ITS CURRENT STATE!<br/>

<br/>
<br/>

How to add SmartR to my tranSMART installation:
- Add this to your BuildConfig.groovy:  
  grails.plugin.location.smartR = '/path/to/smartR' 
- Add this to transmartApp/web-app/js/datasetExplorer/datasetExplorer.js:<br/>
  loadPlugin('smartR', "/SmartR/loadScripts", function () { resultsTabPanel.add(smartRPanel); })
- Run "grails war" to compile a WAR file containing SmartR

Knows Issues:
- none

Supported Browsers:
- Firefox (fully supported)
- Chrome (fully supported)
- Internet Explorer (mostly supported)
 
How to deploy your own visualization:
- Add an input view to specify the input data
- Add an analysis script to prepare/order/filter the data and to compute statistical information
- Add an output view to visualize the processed data

A very simplistic example can be found here:
- grails-app/views/_inSample.gsp
- web-app/Scripts/Sample.R
- grails-app/views/_outSample.gsp

