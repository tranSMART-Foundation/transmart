###SmartR is a grails plugin seeking to improve the visual analytics of the [tranSMART platform](https://github.com/transmart/transmartApp) by using recent web technologies such as [d3](http://d3js.org/).

![alt tag](http://i.imgur.com/8qltmqs.png)


---
*SmartR is still under development and requires testers in order to further improve.
There is no warranty of any kind that it works as intended.
Keep it mind that making use of this software is at your own risk!*

**Please report bugs and issues here: [Issues](http://usersupport.etriks.org)**

---

####What is SmartR?
Please have a look at these [Youtube videos](https://www.youtube.com/channel/UCKUbu0z3CQfi6RcFermONSw).

####Version & Changelog
https://github.com/sherzinger/SmartR/blob/master/CHANGELOG

Current Version 0.3
- **Fix: Big datasets lead no longer to an unpextect error**
- Fix: Feature ranking works correctly now
- Fix: Added log2 to Heatmap tooltip
- Fix: Cutoff in Heatmap will not work anymore with 2 rows or less
- **Fix: Heatmap can now handle datasets containing duplicated probeset ids**
- Improved Heatmap script performance
- Fix: Correlation visualization behaves correctly now when resizing
- Fix: The "Run Analysis" button will now enable again even if JS rendering fails

Version 0.2
- Fix: Session management should be more stable now
- Fix: Large datasets should not cause SmartR to crash anymore
- Fix: Heatmap - switched red and green color
- Fix: Heatmap - use log2 values instead of raw values for ranking
- Fix: Heatmap - improved contrast

####How to add SmartR to your own tranSMART installation:
1 - Add this to your BuildConfig.groovy:
```javascript
grails.plugin.location.smartR = '/path/to/smartR'
```
2 - Add this to transmartApp/web-app/js/datasetExplorer/datasetExplorer.js:
```javascript
  loadPlugin('smartR', "/SmartR/loadScripts", function () { 
  	  resultsTabPanel.insert(4, smartRPanel); 
  });
```
Attention: Make sure that the above code is within the scope of where loadPlugin() is available.
You could for example search for DALLIANCE and add it in the same area.
3 - Run "grails war" to compile a WAR file containing SmartR

####Requirements:
- tranSMART 1.2.4 & 1.2.5 (Feature "cohort updating" not available)
- tranSMART 1.3 (full support)
- tranSMART eTRIKS/research branch (full support)
- R packages:
  - [jsonlite](https://cran.r-project.org/web/packages/jsonlite/index.html)
  - [reshape2](https://cran.r-project.org/web/packages/reshape2/index.html)
  - [limma](http://bioconductor.org/packages/release/bioc/html/limma.html)
  - [zoo](https://cran.r-project.org/web/packages/zoo/index.html) (only for Timeline Analysis)
  - [TSclust](https://cran.r-project.org/web/packages/TSclust/index.html) (only for Timeline Analysis)

####How to deploy your own visualization:
1. [Add an input view to specify the input data](https://github.com/sherzinger/SmartR/blob/master/grails-app/views/smartR/_inSample.gsp)
2. [Add an analysis script to prepare/order/filter the data and to compute statistical information](https://github.com/sherzinger/SmartR/blob/master/web-app/Scripts/Sample.R)
3. [Add an output view to visualize the processed data](https://github.com/sherzinger/SmartR/blob/master/grails-app/views/visualizations/_outSample.gsp)

