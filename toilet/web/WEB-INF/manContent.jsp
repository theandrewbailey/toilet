<%@ include file="/WEB-INF/head.jspf" %>
<c:if test="${error != null}"><p class="error">${error}</p></c:if>
<form action="<imead:keyVal key="thisURL"/>content" method="POST" enctype="multipart/form-data"><h:requestToken/>
<h:select id="directory" label="Directory: " parameters="${directories}" labelNextLine="false"/>
<h:file id="filedata" label="File Upload: " labelNextLine="false" />
<input type="submit" value="Upload"/>
</form><br/>
<div class="adminform">
    <c:if test="${uploadedfile != null}">just uploaded: <a href="<imead:keyVal key="thisURL"/>content/${uploadedfile.filename}">${uploadedfile.filename}</a><br/><br/></c:if>
    Current Content:<br/><form action="<imead:keyVal key="thisURL"/>adminContent" method="POST"><h:requestToken/>
    <table><c:forEach items="${content}" var="dir">
    <tr><td colspan="3">${dir.key}</td></tr>
    <c:forEach items="${dir.value}" var="con">
    <tr><td class="secondmin"><a href="<imead:keyVal key="thisURL"/>content/${dir.key}${con.filename}">${con.filename}</a></td>
    <td class="secondmin">Date: <h:time datetime="${con.uploaded}" pattern="EEE MM/dd/yy h:mm a" /></td>
    <td class="secondmin">MIME: ${con.mimetype} &nbsp; <button type="submit" name="delete" value="${con.fileuploadid}">Delete</button></td></tr>
    </c:forEach></c:forEach></table>
</form><br/>
</div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
