<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/head.jspf" %>

<p><a href="adminExport" class="nocache"><h:local key="page_downloadLegend"/></a></p>
<form action="adminImport" method="POST" enctype="multipart/form-data" accept-charset="UTF-8" class="uploadBackup">
    <fieldset><legend><h:local key="page_uploadLegend"/></legend>
        <h:localVar key="page_uploadField"/><h:file name="zip" label="${page_uploadField} " labelNextLine="false" /><br/>
        <button type="submit"><h:local key="page_upload"/></button>
    </fieldset>
</form>

<%@ include file="/WEB-INF/adminFoot.jspf" %>
