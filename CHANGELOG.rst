Change Log
**********

Add all changes to RInterface here in a human readable format.

This file is loosely based on keepachangelog.com. Each entry should 
start with 'Added', 'Changed', 'Deprecated', 'Removed', 'Fix(ed)', or 
'Security'. If needed we will start using subsections for each of those.


Unreleased
==========

- Added this CHANGELOG.rst file
- Added support for patient_id_list constraint in getHighdimData
- Fix bugs in error handling, e.g. when no Content-Type is provided by the server

[0.3.1] - 2016-04-06
====================

- Fix assay_id_list (erroneously was assay_ids)
- Change to getObservations output: change the class of the first column of the conceptInfo table to character

