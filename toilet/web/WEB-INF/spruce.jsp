<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set scope="request" var="title" value="Spruce"/>
<c:set scope="request" var="page.titleAfter" value="true"/><%@ include file="/WEB-INF/head.jspf" %>
<div><main class="spruceMain">
    <article>
    <header><h1><h:local key="page_spruceHeader"/></h1></header>
    <div class="spruceQuote">
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    </div></article>
<hr/>
<h:local key="page_spruceArticle" />
</main></div>
<%@ include file="/WEB-INF/foot.jspf" %>
