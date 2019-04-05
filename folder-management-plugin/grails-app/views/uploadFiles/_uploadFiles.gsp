<asset:javascript src="fine-uploader/iframe.xss.response-3.7.1.js"/>
<asset:javascript src="fine-uploader/jquery.fineuploader-3.7.1.js"/>
<asset:javascript src="fine-uploader/jquery.fineuploader-3.7.1.min.js"/>
<asset:stylesheet href="fineuploader-3.5.0.css"/>

<div id="uploadtitle"><p>Upload files in folder ${parentFolder?.folderName}</p></div>

<div id="fine-uploader-basic" class="btn btn-success">
    <i class="icon-upload icon-white"/>
    <p>To upload files, click or drag files in this zone.</p>
</div>

<table style="width: 100%;" class="uploadtable" id="uploadtable"></table>

 <form name="form">
    <input type="hidden" name="parent" id="parentFolderId" value="${parentFolder?.id}" />
    <input type="hidden" name="parentName" id="parentFolderName" value="${parentFolder?.folderName}" />
    <input type="hidden" name="existingfiles" id="existingfiles" value="yes" />
</form>

<g:logMsg>_uploadFiles.gsp done</g:logMsg>
