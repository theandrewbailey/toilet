<%@ include file="/WEB-INF/head.jspf" %>
<form action="article" method="post" class="adminform">
    <label for="Groupings">Category:</label>
    <select name="Groupings">
        <c:forEach items="${groups}" var="group"><option value="${group}" ${group == art.sectionid.name ? "selected=\"true\" " : ""}>${group}</option></c:forEach>
    <option value="">new group --&gt;</option></select>
    <input name="newGrouping" id="newGrouping" maxlength="250" size="32"/><br/>

    <h:textbox id="name" label="Title: " length="250" size="64" labelNextLine="false" value="${art.articletitle}" /><br/>
    <h:textbox id="desc" label="Description: " length="250" size="64" labelNextLine="false" value="${art.description}" /><br/>
    <h:textbox id="by" label="By: " length="250" size="43" labelNextLine="false" value="${art.postedname}" /><br/>
    <fmt:formatDate value="${art.posted}" pattern="EEE, dd MMM yyyy HH:mm:ss z" var="formattedDate" />
    <h:textbox id="posted" label="Posted Date: " length="50" size="32" labelNextLine="false" title="ala Fri, 21 Dec 2012 00:20:12 EDT" value="${formattedDate}" /><br/>
    <h:checkbox id="commentable" label=" Commentable" checked="${art.comments}" /><br/>
    <h:textarea id="text" length="100" height="20" label="Text (>64000):" styleClass="articleText" value="${text}" /><br/>
    <h:password id="words" label="Magic words: " labelNextLine="false" /><br/>
    <h:requestToken/>
    <input type="submit" value="Add Entry" />
    <input type="hidden" name="submit-type" id="submit-type" value="${entryType}" />
</form>
<%@ include file="/WEB-INF/manFoot.jspf" %>
