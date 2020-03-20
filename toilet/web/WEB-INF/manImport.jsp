<%@ include file="/WEB-INF/head.jspf" %>
<div class="adminform">

<p><a href="<h:local key="security_baseURL" locale=""/>import">Backup file</a></p>

<form action="<h:local key="security_baseURL" locale=""/>import" method="POST" enctype="multipart/form-data" accept-charset="UTF-8">
<h:file id="zip" label="Upload backup: " /><br/>
<h:password id="words" label="Magic words: " labelNextLine="false" /><br/>
<input type="submit" value="Upload"/>
</form>

</div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
