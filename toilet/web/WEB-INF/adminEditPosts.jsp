<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/head.jspf" %>

<form action="<h:local key="security_baseURL"/>import" method="POST" enctype="multipart/form-data" accept-charset="UTF-8" class="uploadBackup">
    <a href="<h:local key="security_baseURL"/>import"><h:local key="page_download_legend"/></a>
    <fieldset><legend><h:local key="page_upload_legend"/></legend>
        <h:localVar key="page_upload_field"/><h:file id="zip" label="${page_upload_field} " labelNextLine="false" /><br/>
        <h:localVar key="page_magic_words"/><h:password id="words" label="${page_magic_words} " labelNextLine="false" size="40" />
        <input type="submit" value="<h:local key="page_upload"/>"/>
    </fieldset>
</form><br/>

<form action="<h:local key="security_baseURL" locale=""/>adminArticle" method="POST" class="adminform adminArticle" accept-charset="UTF-8"><c:forEach items="${articles}" var="art">
<article class="adminform">
    <c:choose><c:when test="${null==art.imageurl}"><h:checkbox id="select" label="" value="${art.articleid}" styleClass="articleCheckbox"/></c:when>
    <c:otherwise><h:checkbox id="selectedArticle" label="" value="${art.articleid}" styleClass="articleCheckbox articleCheckboxWithImage"/></c:otherwise></c:choose>
    <button type="submit" name="editarticle" value="${art.articleid}">Edit</button>&nbsp;
    <t:articleUrl article="${art}"/>&nbsp;<c:if test="${art.sectionid.name != ' '}">under ${art.sectionid.name},&nbsp;</c:if>
    <h:time datetime="${art.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/>&nbsp;

    <c:if test="${0!=fn:length(art.commentCollection)}"><p class="secondmin" ><c:forEach items="${art.commentCollection}" var="comm"><h:time datetime="${comm.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/> by ${comm.postedname} <button type="submit" name="deletecomment" value="${comm.commentid}">Delete</button><br/>
    </c:forEach></p></c:if>
</article></c:forEach>
<button name="selectAll" data-check="articleCheckbox">Select All</button>
<button name="selectImages" data-check="articleCheckboxWithImage">Select All with images</button>
<button type="submit" name="rewrite" value="rewrite">Rewrite</button>
<button type="submit" name="disablecomments" value="disablecomments">Disable Comments</button>
</form>
<%@ include file="/WEB-INF/adminFoot.jspf" %>
