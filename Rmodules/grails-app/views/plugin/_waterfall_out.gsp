<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
    Copyright 2008-2012 Janssen Research & Development, LLC.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  -->

<html>
    <head>
	<title>subsetPanel.html</title>
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3"/>
	<meta http-equiv="description" content="this is my page"/>
	<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
	<asset:stylesheet href="datasetExplorer.css"/>
    </head>

    <body>
	<form>
	    <br/>
	    <br/>		
	    <span class='AnalysisHeader'>Waterfall Plot</span><br/><br/>
	    <g:each var='location' in="${imageLocations}">
		<img src="${request.getContextPath()}${location}" width='600' height='600'/> <br/>
	    </g:each>
	    <br/>
     <g:render template="/plugin/downloadRawDataLink" />
	</form>
    </body>
</html>
