<%@ include file="/WEB-INF/head.jspf" %>
<form action="<imead:keyVal key="thisURL"/>content" method="POST" enctype="multipart/form-data">
<select name="directory">
    <c:forEach items="${content}" var="con"><option value="${con.key}">${con.key}</option></c:forEach>
</select>
<h:file id="filedata" label="File Upload: " labelNextLine="false" />
<input type="submit" value="Upload"/>
<h:requestToken/>
</form><br/>
<div class="adminform">Current Content:<br/><form action="<imead:keyVal key="thisURL"/>adminContent" method="POST">
    <table><c:forEach items="${content}" var="dir">
    <tr><td colspan="3">${dir.key}</td></tr>
    <c:forEach items="${dir.value}" var="con">
    <tr><td class="secondmin"><a href="<imead:keyVal key="thisURL"/>content/${dir.key}${con.filename}">${con.filename}</a></td>
    <td class="secondmin">Date: <h:time datetime="${con.uploaded}" pattern="EEE MM/dd/yy h:mm a" /></td>
    <td class="secondmin">MIME: ${con.mimetype} &nbsp; <button type="submit" name="delete" value="${con.fileuploadid}">Delete</button></td></tr>
    </c:forEach></c:forEach></table>
<h:requestToken/></form><br/>
</div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
