<%@ include file="/WEB-INF/head.jspf" %>
<div class="adminform">

<p><a href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>import">Backup file</a></p>

<form action="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>import" method="POST" enctype="multipart/form-data">
<h:requestToken/>
<h:file id="zip" label="Upload backup: " /><br/>
<h:password id="words" label="Magic words: " labelNextLine="false" /><br/>
<input type="submit" value="Upload"/>
</form>

</div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
