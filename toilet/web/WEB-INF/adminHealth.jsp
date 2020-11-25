<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/head.jspf" %>
<main class="adminform adminHealth">
    <c:forEach items="${processes.get()}" var="process"><details class="process" open="true"><summary>${process.key}</summary><pre>${process.value.get()}</pre></details>
    </c:forEach>
    <details class="cached"><summary>Cache page count: ${cached.get().size()}</summary>
        <ul><c:forEach items="${cached.get()}" var="pageEnt"><li>${pageEnt}</li></c:forEach></ul>
    </details>
    <c:forEach items="${certPaths}" var="certPath"><details class="certpath">
        <summary>Certificate Path to ${certInfo.get(certPath.getCertificates().get(certPath.getCertificates().size()-1)).get("Subject")} (expires in ${fn:substringBefore(((certPath.getExpiration().getTime() - $_LIBWEBSITETOOLS_REQUEST_START_TIME.getTime()) / 86400000),".")} days)</summary><c:forEach items="${certPath.getCertificates()}" var="cert">
        <details class="secondmin"><summary>Certificate: ${certInfo.get(cert).get("Subject")} (expires in ${fn:substringBefore(((cert.getNotAfter().getTime() - $_LIBWEBSITETOOLS_REQUEST_START_TIME.getTime()) / 86400000),".")} days)</summary>
            <table><c:forEach items="${certInfo.get(cert)}" var="info">
                <tr class="secondmin"><td class="secondmin">${info.key}</td><td>${info.value}</td></tr></c:forEach>
        </table></details>
    </c:forEach></details></c:forEach>
    <details class="articles"><summary>Article count: ${articles.get().size()}</summary><table>
    <c:forEach items="${articles.get()}" var="art"><tr class="secondmin"><td><t:articleUrl article="${art}"/></td><td><c:if test="${art.sectionid.name != ' '}">${art.sectionid.name}</c:if></td><td><h:time datetime="${art.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/></td></tr>
    </c:forEach></table></details>
    <details class="comments"><summary>Comment count: ${comments.get().size()}</summary><table>
    <c:forEach items="${comments.get()}" var="comm"><tr class="secondmin"><td><h:time datetime="${comm.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/></td><td>${comm.postedname}</td><td><t:articleUrl article="${comm.articleid}"/></td></tr>
    </c:forEach></table></details>
    <details class="files"><summary>File count: ${files.get().size()}</summary>
    <table><c:forEach items="${files.get()}" var="file">
    <tr class="secondmin"><td><a href="${file.url}" target="_blank" rel="noopener">${file.filename}</a></td>
    <td><h:filesize length="${file.datasize}"/></td><td>${file.mimetype}</td><td><h:time datetime="${file.atime}" pattern="yyyy-MM-dd h:mm a" /></td></tr></c:forEach></table>
    </details>
</main>
<%@ include file="/WEB-INF/adminFoot.jspf" %>
