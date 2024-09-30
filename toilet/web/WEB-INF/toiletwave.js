"use strict";
// bling.js, because I want the $ of jQuery without jQuery
if(undefined===window.$){window.$=document.querySelectorAll.bind(document);
	[EventTarget.prototype,window,XMLHttpRequest.prototype].forEach(function setOnOff(p){
		Object.defineProperty(p,"on",{get(){return function onElement(t,f){this.addEventListener(t,f);return this;};}})
		Object.defineProperty(p,"off",{get(){return function offElement(t,f){this.removeEventListener(t,f);return this;};}});});
	[NodeList.prototype,HTMLCollection.prototype].forEach(function setOnArray(p){Object.setPrototypeOf(p,Array.prototype);
		Object.defineProperty(p,"on",{get(){return function onArray(t,f){this.forEach(function onEach(e){e.addEventListener(t,f);});return this;};}});
		Object.defineProperty(p,"off",{get(){return function offArray(t,f){this.forEach(function offEach(e){e.removeEventListener(t,f);});return this;};}});
});}
const toilet=new (function toilet(){
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
	}};
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
		}}
		return true;
	};
	function sizeIframe(iFrame){
		iFrame.style.setProperty("width",'auto');
		const p=iFrame.parentElement.querySelector("article:not(:last-child)>p:last-of-type");
		if(undefined!==p){
			iFrame.style.setProperty("width",(p.clientWidth-1)+'px');
		}
		iFrame.style.setProperty("height",iFrame.contentWindow.document.body.scrollHeight+50+'px');
		return iFrame;
	};
	function isLoaded(doc){return doc&&("complete"===doc.readyState);}
	function inIframe(){try{return window.self!==window.top;}catch(e){return true;}}
	function asideCollide(){
		if(900<$("html")[0].clientWidth){
			const aside=$("aside")[0];
			const asiderect=aside.getBoundingClientRect();
			const style=window.getComputedStyle(aside);
			$("aside+article li img").forEach(function checkImg(i){
				const rect=i.getBoundingClientRect();
				if(asiderect.bottom+Math.ceil(parseFloat(style.marginTop))+Math.ceil(parseFloat(style.marginBottom))+1>=rect.top){
					i.classList.add("asideCollide");
				}
			});
		}
	}
	this.flush=function flush(ready=false){
		if(0===$(".errorPage,.indexPage").length){
			const lc=$(".leftContent")[0];
			const aside=$("body>div>aside")[0];
			if(undefined!==lc&&undefined!==aside){
				const ref=$(".leftContent>*:not(.searchSuggestion)")[0];
				lc.insertBefore(aside,ref);
				if(0<$("aside+article li img").length){
					window.on("resize",asideCollide);
					window.requestAnimationFrame(asideCollide);
				}
		}}
		const commentElement=$("#comments");
		$("button[data-check]").on("click",function checkBoxes(e){
			e.preventDefault();
			e.target.closest("fieldset,form").querySelectorAll("."+e.target.dataset.check).forEach(function check(c){
				c.checked=true;
			});
		});
		$("input,select").forEach(function checkInputValidity(i){
			i.on("input",checkInputValidity).on("invalid",displayValidityMessage);
		});
		$("textarea").forEach(setTextareaEvents);
		$("form").forEach(function testFormPresubmit(f){
			f.on("submit",testFormTextareas);
		});
		window.on("resize",function onResize(){
			$("iframe").forEach(sizeIframe);
		});
	};
	isLoaded(document)||inIframe()?this.flush(true):window.on('load',this.flush);
})();
