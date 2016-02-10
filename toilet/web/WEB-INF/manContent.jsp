<%@ include file="/WEB-INF/head.jspf" %>
<form action="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>content" method="POST" enctype="multipart/form-data" class="adminform"><h:requestToken/>
<c:if test="${error != null}"><p class="error">${error}</p></c:if>
<h:select id="directory" label="Directory: " parameters="${directories}" labelNextLine="false"/>
<h:file id="filedata" label="File Upload: " labelNextLine="false" />
<input type="submit" value="Upload"/>
<c:if test="${uploadedfile != null}"><p class="adminform">just uploaded: <a href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>content/${uploadedfile.filename}">${uploadedfile.filename}</a></p></c:if>
</form>
<br/>
<form action="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>adminContent" method="POST" class="adminform"><h:requestToken/><p>Current Content:</p>
    <table><c:forEach items="${content}" var="dir">
    <tr><td colspan="3">${dir.key}</td></tr>
    <c:forEach items="${dir.value}" var="con">
    <tr class="secondmin"><td><a href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>content/${dir.key}${con.filename}">${con.filename}</a></td>
    <td ><h:time datetime="${con.atime}" pattern="EEE MM/dd/yy h:mm a" /></td>
    <td >${con.mimetype} &nbsp; <button type="submit" name="delete" value="${con.fileuploadid}">Delete</button></td></tr>
    </c:forEach></c:forEach></table>
</form>
<%@ include file="/WEB-INF/manFoot.jspf" %>
