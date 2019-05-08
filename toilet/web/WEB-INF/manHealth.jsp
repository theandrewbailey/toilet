<%@ include file="/WEB-INF/head.jspf" %>
<div class="adminform">
    uptime<pre>${uptime.get()}</pre><br/>
    free -m<pre>${free.get()}</pre><br/>
    df -hx tmpfs<pre>${disk.get()}</pre><br/>

    <c:forEach items="${certPaths}" var="certPath">
        Certificate Path:<table>
        <c:forEach items="${certPath.getCertificates()}" var="cert"><tr class="secondmin"><td class="secondmin">
            <details><summary>Certificate: ${certInfo.get(cert).get("Subject")} (expires in ${fn:substringBefore(((cert.getNotAfter().getTime() - $_LIBODYSSEY_REQUEST_START_TIME.getTime()) / 86400000),".")} days)</summary>
                <table><c:forEach items="${certInfo.get(cert)}" var="info"><tr class="secondmin">
                    <td class="secondmin">${info.key}</td><td>${info.value}</td></tr>
                </c:forEach></table>
            </details>
        </c:forEach></td></tr></table>
    </c:forEach>

    <details><summary>Article count: ${articles.get().size()}</summary><table>
    <c:forEach items="${articles.get()}" var="art"><tr class="secondmin"><td><t:articleUrl article="${art}"/></td><td><c:if test="${art.sectionid.name != ' '}">${art.sectionid.name}</c:if></td><td><h:time datetime="${art.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/></td></tr>
    </c:forEach></table></details>

    <details><summary>Comment count: ${comments.get().size()}</summary><table>
    <c:forEach items="${comments.get()}" var="comm"><tr class="secondmin"><td><h:time datetime="${comm.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/></td><td>${comm.postedname}</td><td><t:articleUrl article="${comm.articleid}"/></td></tr>
    </c:forEach></table></details>

    <details><summary>File count: ${files.get().size()}</summary>
    <table><c:forEach items="${files.get()}" var="file">
    <tr class="secondmin"><td><a href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>content/${file.filename}" target="_blank" rel="noopener">${file.filename}</a></td>
    <td><h:filesize length="${file.datasize}"/></td><td>${file.mimetype}</td><td><h:time datetime="${file.atime}" pattern="yyyy-MM-dd h:mm a" /></td></tr></c:forEach></table>
    </details>
</div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
