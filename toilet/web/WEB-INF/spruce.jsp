<%@ taglib prefix="sp" uri="http://theandrewbailey.com/spruce" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set scope="request" var="title" value="Spruce"/>
<c:set scope="request" var="page.titleAfter" value="true"/><%@ include file="/WEB-INF/head.jspf" %>
<section class="spruceMain">
    <section id="spruceQuote">
    <h1><imead:keyVal key="page_spruce_header" /></h1>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    </section>
<hr/><article>
<imead:keyVal key="entry_spruce" />
</article></section>
<%@ include file="/WEB-INF/foot.jspf" %>
