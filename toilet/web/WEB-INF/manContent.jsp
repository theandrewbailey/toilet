<%@ include file="/WEB-INF/head.jspf" %>
<form action="<h:local key="security_baseURL" locale=""/>content" method="POST" enctype="multipart/form-data" class="adminform" accept-charset="UTF-8">
<c:if test="${ERROR_MESSAGE != null}"><p class="error">${ERROR_MESSAGE}</p></c:if><h:localVar key="page_valueMissing" var="valueMissing" />
<h:textbox id="directory" label="Directory:" datalist="${directories}" maxLength="250" labelNextLine="false" value="${prop.key}" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" />
<h:file id="filedata" label="File Upload: " labelNextLine="false" required="true" valueMissing="${valueMissing}" multiple="true" />
<input type="submit" value="Upload"/>
<c:if test="${null != uploadedfiles}"><p class="adminform">Upload successful:<c:forEach items="${uploadedfiles}" var="uploadedfile"><br/><a href="<h:local key="security_baseURL" locale=""/>content/${uploadedfile.filename}" target="_blank" rel="noopener" >${uploadedfile.filename}</a></c:forEach></p></c:if>
</form>
<br/>
<form action="<h:local key="security_baseURL" locale=""/>adminContent" method="POST" class="adminform" accept-charset="UTF-8"><p>Current Content:</p>
    <c:forEach items="${content}" var="dir">
    <details ${opened_dir == dir.key ? "open='true'" : ""} ><summary>${dir.key}</summary><table>
    <c:forEach items="${dir.value}" var="con">
    <tr class="secondmin"><td><a href="${con.url}" target="_blank" rel="noopener">${con.filename}</a></td>
    <td><h:filesize length="${con.datasize}"/></td>
    <td><a href="<h:local key="security_baseURL" locale=""/>content/${dir.key}${con.filename}" target="_blank" rel="noopener"><h:time datetime="${con.atime}" pattern="yyyy-MM-dd h:mm a" /></a></td>
    <td>${con.mimetype}&nbsp;<h:button type="submit" id="action" value="delete|${dir.key}${con.filename}">Delete</h:button></td></tr>
    </c:forEach></table></details></c:forEach>
</form>
<%@ include file="/WEB-INF/manFoot.jspf" %>
