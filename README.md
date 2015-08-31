###SmartR is a grails plugin seeking to improve the visual analytics of the [tranSMART platform](https://github.com/transmart/transmartApp) by using recent web technologies such as [d3](http://d3js.org/).

![alt tag](http://i.imgur.com/8qltmqs.png)


---
*SmartR is still under development and requires BETA testers in order to further improve.
There is no warranty of any kind that it works as intended. 
Keep it mind that making use of this software is at your own risk!*

**Please report bugs and issues here: [Issues](https://cc-usersupport.in2p3.fr/otrs/customer.pl)**

---

####How to add SmartR to your own tranSMART installation:
1 - Add this to your BuildConfig.groovy:
```javascript
grails.plugin.location.smartR = '/path/to/smartR'
```
2 - Add this to transmartApp/web-app/js/datasetExplorer/datasetExplorer.js: 
```javascript
  loadPlugin('smartR', "/SmartR/loadScripts", function () {
    resultsTabPanel.add(smartRPanel); 
  })
```
3 - Run "grails war" to compile a WAR file containing SmartR


####Requirements:
- tranSMART 1.2.4 & 1.2.5 (Feature "cohort updating" not available)
- tranSMART 1.3 (full support)
- tranSMART eTRIKS/research branch (full support)
- R packages: 
  - [jsonlite](https://cran.r-project.org/web/packages/jsonlite/index.html)
  - [data.table](https://cran.r-project.org/web/packages/data.table/index.html)
  - [reshape2](https://cran.r-project.org/web/packages/reshape2/index.html)
  - [zoo](https://cran.r-project.org/web/packages/zoo/index.html)
  - [TSclust](https://cran.r-project.org/web/packages/TSclust/index.html)

####Known Issues: 
- none

####Supported Browsers:
- Firefox (fully supported)
- Chrome (fully supported)
- Internet Explorer (mostly supported)
- Safari (unsupported at this moment)
 
####How to deploy your own visualization:
1. [Add an input view to specify the input data](https://github.com/sherzinger/SmartR/blob/master/grails-app/views/smartR/_inSample.gsp)
2. [Add an analysis script to prepare/order/filter the data and to compute statistical information](https://github.com/sherzinger/SmartR/blob/master/web-app/Scripts/Sample.R)
3. [Add an output view to visualize the processed data](https://github.com/sherzinger/SmartR/blob/master/grails-app/views/visualizations/_outSample.gsp)

