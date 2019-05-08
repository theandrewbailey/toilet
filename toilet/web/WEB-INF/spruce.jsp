<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set scope="request" var="title" value="Spruce"/>
<c:set scope="request" var="page.titleAfter" value="true"/><%@ include file="/WEB-INF/head.jspf" %>
<div><main class="spruceMain">
    <article class="entry">
    <header><h1><h:local key="page_spruce_header" /></h1></header>
    <div id="spruceQuote">
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    <p><t:sentence>${spruce_sentence}</t:sentence></p>
    </div></article>
<hr/><article class="entry">
<h:local key="page_spruce_entry" />
</article></main></div>
<%@ include file="/WEB-INF/foot.jspf" %>
