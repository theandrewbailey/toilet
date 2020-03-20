"use strict";
// bling.js, because I want the $ of jQuery without the jQuery
if(undefined===window.$){window.$=document.querySelectorAll.bind(document);
	[EventTarget.prototype,window,XMLHttpRequest.prototype].forEach(function setOn(p){
		Object.defineProperty(p,"on",{get(){return function onElement(t,f){this.addEventListener(t,f);return this;};}});});
	[NodeList.prototype,HTMLCollection.prototype].forEach(function setOnArray(p){Object.setPrototypeOf(p,Array.prototype);
		Object.defineProperty(p,"on",{get(){return function onArray(t,f){this.forEach(function onEach(e){e.addEventListener(t,f);});return this;};}});
});}
var toilet=new (function toilet(){
	function forceLoad(e){
		document.location.href=e.target.href;
		document.location.reload(true);
	};
	function checkInputValidity(e){
		e.target.checkValidity();
	};
	function displayValidityMessage(e){
		if(e.target.validity.patternMismatch&&undefined!==e.target.dataset["patternmismatch"]){
			e.target.setCustomValidity(e.target.dataset["patternmismatch"]);
		}else if(e.target.validity.valueMissing&&undefined!==e.target.dataset["valuemissing"]){
			e.target.setCustomValidity(e.target.dataset["valuemissing"]);
		}else if(!e.target.validity.patternMismatch&&!e.target.validity.valueMissing){
			e.target.setCustomValidity("");
		}
	};
	function testTextarea(ta,regex){
		if(undefined!==regex&&undefined!==ta.dataset["patternmismatch"]&&!regex.test(ta.value)){
			ta.setCustomValidity(ta.dataset["patternmismatch"]);
			return false;
		}else if(ta.required&&ta.validity.valueMissing&&undefined!==ta.dataset["valuemissing"]){
			ta.setCustomValidity(ta.dataset["valuemissing"]);
			return false;
		}
		ta.setCustomValidity("");
		return true;
	};
	function setTextareaEvents(ta){
		const pattern=new RegExp(ta.dataset["pattern"],"u");
		ta.on("input",function testOnInput(e){testTextarea(e.target,pattern);}).on("invalid",function testOnInvalid(e){testTextarea(e.target,pattern);});
	};
	function testFormTextareas(e){
		const textareas=e.target.getElementsByTagName("textarea");
		for(var i=0;i<textareas.length;i++){
			const ta=textareas.item(i);
			if(!testTextarea(ta,new RegExp(ta.dataset["pattern"],"u"))){
				e.preventDefault();
				return false;
			}
		}
		return true;
	};
	function sizeIframe(iFrame){
		iFrame.style.setProperty("height",iFrame.contentWindow.document.body.scrollHeight+'px');
		return iFrame;
	};
	function isLoaded(doc){return doc&&("complete"===doc.readyState||"interactive"===doc.readyState);}
	this.flush=function flush(ready=false){
		const commentElement=$("#comments");
		$("button.refreshLink,a.refreshLink").forEach(function refreshComments(elem){
			elem.href=document.location.protocol+"//"+document.location.host+document.location.pathname;
			if(undefined!==commentElement&&null!==commentElement){
				elem.href+="#commentSubmission";
			}
			elem.on("click",forceLoad);
		});
		$("input,select").forEach(function checkFieldValidity(elem){
			elem.on("input",checkInputValidity).on("invalid",displayValidityMessage);
		});
		$("textarea").forEach(setTextareaEvents);
		$("form").forEach(function testFormPresubmit(elem){
			elem.on("submit",testFormTextareas);
		});
		$("iframe").forEach(function sizeIframeInitially(iFrame){
			isLoaded(iFrame.contentDocument.documentElement)?sizeIframe(iFrame):iFrame.on("load",function sizeIframeOnLoad(){sizeIframe(iFrame)});
		});
		window.on("resize",function sizeIframeOnResize(){
			$("iframe").forEach(sizeIframe);
		});
	};
	isLoaded(document)?this.flush():document.on('DOMContentLoaded',this.flush);
})();
