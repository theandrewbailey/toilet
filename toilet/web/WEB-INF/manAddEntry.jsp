<%@ include file="/WEB-INF/head.jspf" %>
<c:if test="${ERROR_MESSAGE != null}"><p class="error">${ERROR_MESSAGE}</p></c:if>
<form action="article" method="post" class="adminform" accept-charset="UTF-8">
    <h:localVar key="page_patternMismatch" var="patternMismatch" /><h:localVar key="page_valueMissing" var="valueMissing" />
    <h:textbox id="section" label="Category: " labelNextLine="false" patternMismatch="${patternMismatch}" datalist="${groups}" value="${art.sectionid.name}"/><br/>

    <h:textbox id="articletitle" label="Title: " maxLength="250" size="64" labelNextLine="false" value="${art.articletitle}" required="true" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" /><br/>
    <h:textbox id="description" label="Description: " maxLength="250" size="64" labelNextLine="false" value="${art.description}" required="true" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" /><br/>
    <h:textbox id="postedname" label="By: " maxLength="250" size="43" labelNextLine="false" value="${art.postedname}" patternMismatch="${patternMismatch}" /><br/>
    <fmt:formatDate value="${art.posted}" pattern="EEE, dd MMM yyyy HH:mm:ss z" var="formattedDate" />
    <h:textbox id="posted" label="Posted Date: " maxLength="50" size="32" labelNextLine="false" title="ala Fri, 21 Dec 2012 00:20:12 EDT" value="${formattedDate}" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" /><br/>
    <h:checkbox id="comments" label="Commentable" checked="${art.comments}" /><br/>
    <h:textarea id="postedmarkdown" length="100" height="20" label="Text (>64000):" styleClass="articleText" value="${art.postedmarkdown}" required="true" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" /><br/>
    <h:password id="words" label="Magic words: " labelNextLine="false" /><br/>
    <button type="submit" name="action" value="Preview">Preview</button>
    <button type="submit" name="action" value="Add Entry">Add Entry</button>
    <h:hidden id="submit-type" value="article" />
</form>
<br/><br/>
<c:if test="${param.action == 'Preview'}"><div><main id="leftContent"><%@ include file="/WEB-INF/article.jspf" %></main></div></c:if>
<%@ include file="/WEB-INF/manFoot.jspf" %>
