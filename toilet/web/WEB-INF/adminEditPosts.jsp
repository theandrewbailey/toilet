<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/head.jspf" %>

<form action="adminImport" method="POST" enctype="multipart/form-data" accept-charset="UTF-8" class="uploadBackup">
    <a href="adminImport"><h:local key="page_downloadLegend"/></a>
    <fieldset><legend><h:local key="page_uploadLegend"/></legend>
        <h:localVar key="page_uploadField"/><h:file name="zip" label="${page_uploadField} " labelNextLine="false" /><br/>
        <h:localVar key="page_magicWords"/><h:password name="words" label="${page_magicWords} " labelNextLine="false" size="40" />
        <input type="submit" value="<h:local key="page_upload"/>"/>
    </fieldset>
</form><br/>

<form action="adminArticle" method="POST" class="adminform adminArticle" accept-charset="UTF-8"><c:forEach items="${articles}" var="art">
<article class="adminform">
    <c:choose><c:when test="${null==art.imageurl}"><h:checkbox name="selectedArticle" label="" value="${art.articleid}" styleClass="articleCheckbox"/></c:when>
    <c:otherwise><h:checkbox name="selectedArticle" label="" value="${art.articleid}" styleClass="articleCheckbox articleCheckboxWithImage"/></c:otherwise></c:choose>
    <button type="submit" name="editarticle" value="${art.articleid}">Edit</button>&nbsp;
    <t:articleUrl article="${art}"/>&nbsp;<c:if test="${art.sectionid.name != ' '}">under ${art.sectionid.name},&nbsp;</c:if>
    <h:time datetime="${art.posted}" pattern="EEE MM/dd/yy h:mm a"/>&nbsp;
    <c:if test="${0!=fn:length(art.commentCollection)}"><p class="secondmin" >
    <c:forEach items="${art.commentCollection}" var="comm"><h:time datetime="${comm.posted}" pattern="EEE MM/dd/yy h:mm a"/> by ${comm.postedname} <button type="submit" name="deletecomment" value="${comm.commentid}">Delete</button><br/>
    </c:forEach></p></c:if>
</article></c:forEach>
<button name="selectAll" data-check="articleCheckbox">Select All</button>
<button name="selectImages" data-check="articleCheckboxWithImage">Select All with images</button>
<button type="submit" name="rewrite" value="rewrite">Rewrite</button>
<button type="submit" name="disablecomments" value="disablecomments">Disable Comments</button>
</form>
<%@ include file="/WEB-INF/adminFoot.jspf" %>
