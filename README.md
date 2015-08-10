The TranSMART-XNAT-Importer plugin can import clinical image-derived data from XNAT and store them in TranSMART.

![alt tag](https://raw.github.com/evast/transmart-xnat-importer-plugin/master/docs/navigator.png)  
XNAT Data hierarchy shown in TranSMART ontology. 

TranSMART-XNAT Coupling
----------

TranSMART  (http://transmartfoundation.org) is a data integration and browsing platform for central statistical analysis in translational research.

XNAT (www.xnat.org) is a framework to store, share and manage medical imaging data. This imaging archive can be connected to a computing cluster for further image analyses. After analyses of the images, the analysis results (e.g. grey matter volumes) can be stored back in XNAT. 

To correlate the image-derived results with other medical data, such as demographic or genetic data, it would be useful to import the XNAT results in TranSMART. To this end, we developed the TranSMART-XNAT-Importer plugin. To configure which XNAT image-derived data is imported in TranSMART, an administrator can create a coupling configuration in the online administration panel and start the import process. The plugin uses the default ETL Importer for clinical data to import the XNAT results in TranSMART. 

Documentation and Installation
------------------------------

More information about the plugin and installation instructions, can be found in [TranSMART-XNAT-Importer-plugin-manual.pdf](https://github.com/evast/transmart-xnat-importer-plugin/blob/master/docs/TranSMART-XNAT-Importer-plugin-manual.pdf?raw=true) in the directory docs. A user-guide to import data with step-by-step instructions and screenshots can be found in the same document.
