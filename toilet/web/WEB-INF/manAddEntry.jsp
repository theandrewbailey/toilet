<%@ include file="/WEB-INF/head.jspf" %>
<c:if test="${mess != null}"><p class="error">${mess}</p></c:if>
<form action="article" method="post" class="adminform"><h:requestToken/>
    <label for="section">Category:</label>
    <select name="section" id="section">
        <c:forEach items="${groups}" var="group"><option value="${group}" ${group == art.sectionid.name || param.section == group ? "selected=\"selected\" " : ""}>${group}</option></c:forEach>
    <option value="">new group --&gt;</option></select>
    <input name="newsection" id="newsection" maxlength="250" size="32"/><br/>

    <h:textbox id="articletitle" label="Title: " maxLength="250" size="64" labelNextLine="false" value="${art.articletitle}" /><br/>
    <h:textbox id="description" label="Description: " maxLength="250" size="64" labelNextLine="false" value="${art.description}" /><br/>
    <h:textbox id="postedname" label="By: " maxLength="250" size="43" labelNextLine="false" value="${art.postedname}" /><br/>
    <fmt:formatDate value="${art.posted}" pattern="EEE, dd MMM yyyy HH:mm:ss z" var="formattedDate" />
    <h:textbox id="posted" label="Posted Date: " maxLength="50" size="32" labelNextLine="false" title="ala Fri, 21 Dec 2012 00:20:12 EDT" value="${formattedDate}" /><br/>
    <h:checkbox id="comments" label="Commentable" checked="${art.comments}" /><br/>
    <h:textarea id="postedmarkdown" length="100" height="20" label="Text (>64000):" styleClass="articleText" value="${art.postedmarkdown}" /><br/>
    <h:password id="words" label="Magic words: " labelNextLine="false" /><br/>
    <button type="submit" name="action" value="Preview">Preview</button>
    <button type="submit" name="action" value="Add Entry">Add Entry</button>
    <h:hidden id="submit-type" value="article" />
</form>
<br/><br/>
<c:if test="${param.action == 'Preview'}"><article>
    <header><h1>${art.articletitle}</h1></header>
    ${art.postedhtml}
    <footer><imead:keyVal key="page_articleFooter"><imead:param><h:time datetime="${art.posted}" pubdate="true"/></imead:param><imead:param object="${param.section}"/></imead:keyVal>
    <c:if test="${param.comments}">${fn:length(art.commentCollection)}&nbsp;${fn:length(art.commentCollection) == 1 ? 'comment.' : 'comments.'}</c:if></footer>
</article></c:if>
<%@ include file="/WEB-INF/manFoot.jspf" %>
