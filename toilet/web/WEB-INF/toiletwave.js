"use strict";
// bling.js, because I want the $ of jQuery without jQuery
if (undefined === window.$) {
    window.$ = document.querySelectorAll.bind(document);
    [EventTarget.prototype, window, XMLHttpRequest.prototype].forEach(function setOnOff(p) {
        Object.defineProperty(p, "on", {get() {
                return function onElement(t, f, o) {
                    this.addEventListener(t, f, o);
                    return this;
                };
            }});
        Object.defineProperty(p, "off", {get() {
                return function offElement(t, f) {
                    this.removeEventListener(t, f);
                    return this;
                };
            }});
    });
    [NodeList.prototype, HTMLCollection.prototype].forEach(function setOnArray(p) {
        Object.setPrototypeOf(p, Array.prototype);
        Object.defineProperty(p, "on", {get() {
                return function onArray(t, f, o) {
                    this.forEach(function onEach(e) {
                        e.addEventListener(t, f, o);
                    });
                    return this;
                };
            }});
        Object.defineProperty(p, "off", {get() {
                return function offArray(t, f) {
                    this.forEach(function offEach(e) {
                        e.removeEventListener(t, f);
                    });
                    return this;
                };
            }});
    });
}
function toilet() {
    var eagerLoadTime = 5000;
    var eagerLoadTimer = null;
    var unfocustimer = null;
    function getSuggestions(e) {
        const searchbox = e.currentTarget;
        const list = searchbox.nextElementSibling;
        searchbox.removeAttribute('list');
        if (undefined === searchbox.dataset.pending && 2 < searchbox.value.length && searchbox.size >= searchbox.value.length) {
            const req = new XMLHttpRequest();
            const act = searchbox.closest("form").action;
            req.open("GET", act + "?suggestion=" + searchbox.value);
            req.on("load", function fillSuggestions(r) {
                if (200 === req.status) {
                    const res = JSON.parse(req.response);
                    if (0 !== res.length) {
                        while (list.firstChild) {
                            list.removeChild(list.firstChild);
                        }
                        res.forEach(function add(s) {
                            const a = document.createElement("a");
                            a.href = act + "?" + encodeURI(searchbox.name) + "=" + encodeURI(s);
                            a.textContent = s;
                            const li = document.createElement("li");
                            li.appendChild(a);
                            list.appendChild(li);
                        });
                        list.classList.add("show");
                    }
                }
                var pending = searchbox.dataset.pending;
                delete searchbox.dataset.pending;
                if (pending !== searchbox.value) {
                    getSuggestions(e);
                }
            }).on("timeout", function timedOut(r) {
                searchbox.off("input", getSuggestions);
            }).timeout = 1000;
            searchbox.dataset.pending = searchbox.value;
            req.send();
        }
    }
    function searchFocus(e) {
        if (unfocustimer) {
            clearTimeout(unfocustimer);
            unfocustimer = null;
        }
        const list = e.currentTarget.nextElementSibling;
        list.classList.add("show");
        const selected = list.querySelector(".selected");
        if (selected) {
            selected.classList.remove("selected");
        }
    }
    function searchBlur(e) {
        if (unfocustimer) {
            clearTimeout(unfocustimer);
        }
        unfocustimer = setTimeout(function () {
            e.target.nextElementSibling.classList.remove("show");
        }, 10000);
    }
    function searchDown(e) {
        const searchbox = e.currentTarget;
        const list = searchbox.nextElementSibling;
        var selected = list.querySelector(".selected");
        switch (e.keyCode) {
            case 40:// down
                if (selected) {
                    selected.classList.remove("selected");
                    const next = selected.nextElementSibling;
                    if (null !== next) {
                        next.classList.add("selected");
                    }
                } else {
                    list.firstElementChild.classList.add("selected");
                }
                e.preventDefault();
                break;
            case 38:// up
                if (selected) {
                    selected.classList.remove("selected");
                    const next = selected.previousElementSibling;
                    if (null !== next) {
                        next.classList.add("selected");
                    }
                } else {
                    list.lastElementChild.classList.add("selected");
                }
                e.preventDefault();
                break;
            case 39:// right
                if (selected) {
                    searchbox.value = selected.innerText;
                    selected.classList.remove("selected");
                    e.preventDefault();
                }
                break;
            case 13:// enter
                if (selected) {
                    searchbox.value = selected.innerText;
                }
                break;
        }
    }
    function sizeIframe(iFrame) {
        iFrame.style.setProperty("width", 'auto');
        const p = iFrame.parentElement.querySelector("article:not(:last-child)>p:last-of-type");
        if (undefined !== p) {
            iFrame.style.setProperty("width", (p.clientWidth - 1) + 'px');
        }
        iFrame.style.setProperty("height", iFrame.contentWindow.document.body.scrollHeight + 50 + 'px');
        return iFrame;
    }
    function isLoaded(doc) {
        return doc && ("complete" === doc.readyState);
    }
    function eagerLoad() {
        const img = $("img[loading='lazy']")[0];
        eagerLoadTimer = null;
        if (img) {
            if ($("img:not([loading='lazy'])").every(function f(i) {
                return i.complete;
            })) {
                img.loading = "eager";
            } else {
                eagerLoadTime *= 2;
            }
            eagerLoadTimer = setTimeout(eagerLoad, getNextEagerLoadTime());
        }
    }
    function getNextEagerLoadTime() {
        const wpt = window.performance.timing;
        if (wpt.loadEventEnd && wpt.navigationStart) {
            return eagerLoadTime + wpt.loadEventEnd - wpt.navigationStart;
        }
        return eagerLoadTime * 2;
    }
    function linkPreload(e) {
        const a = e.currentTarget;
        const u = new URL(a.href, document.location.origin);
        if (a.classList.contains("nocache") || document.location.origin !== u.origin || a.getAttribute("download") || "_blank" === a.getAttribute("target")) {
            a.classList.add("nocache");
        } else if (undefined === a.dataset.cached || undefined === a.dataset.expires) {
            const req = new XMLHttpRequest();
            req.open("GET", u.href);
            req.timeout = 1000;
            req.on("load", function cachePage(r) {
                try {
                    if (!a.classList.contains("nocache") && 4 === req.readyState && 200 === req.status) {
                        const cacheHeaders = req.getResponseHeader("cache-control").split(", ");
                        const revalidate = cacheHeaders.some(function testReval(h) {
                            return 0 < h.search("revalidate");
                        });
                        const publicCache = cacheHeaders.some(function testPublic(h) {
                            return h === "public";
                        });
                        const validFor = Number(cacheHeaders.find(function (h) {
                            return h.startsWith("max-age=");
                        }).replace("max-age=", ""));
                        if (!revalidate && publicCache && validFor) {
                            a.dataset.cached = req.response;
                            a.dataset.expires = Date.now() + (validFor * 1000);
                            a.on('click', useCache);
                            return;
                        }
                    }
                } catch (ex) {
                }
                a.classList.add("nocache");
            }).on("timeout", function timedOut(r) {
                $("a:not(.nocache)").forEach(function disableCache(l) {
                    l.classList.add("nocache");
                });
            }).send();
        } else {
            a.on('click', useCache);
        }
    }
    function useCache(e) {
        const a = e.currentTarget;
        if (a.dataset.cached && a.dataset.expires) {
            if (Date.now() > Number(a.dataset.expires)) {
                delete a.dataset.cached;
                delete a.dataset.expires;
                return;
            }
            const match = a.dataset.cached.match(/<html.*?>([\s\S]*)<\/html>/);
            const u = new URL(a.href, document.location.origin);
            history.replaceState({url: location.href, html: document.documentElement.innerHTML}, document.title, location.href);
            swapDocument(match[1]);
            history.pushState({url: u.href, html: document.documentElement.innerHTML}, document.title, u.href);
            window.scrollTo(0, 0);
            e.preventDefault();
        }
    }
    function swapDocument(d) {
        if (eagerLoadTimer) {
            clearTimeout(eagerLoadTimer);
        }
        $("html")[0].innerHTML = d;
        flush(true);
    }
    function flush(ready = false) {
        const searchbox = $('.search>input[type="search"]').on("input", getSuggestions).on("focus", searchFocus).on("blur", searchBlur).on("keydown", searchDown)[0];
        const suggestionLists = $(".search>.suggestionList");
        if (searchbox && suggestionLists.length === 0) {
            const suggestionList = document.createElement("ol");
            suggestionList.classList.add("suggestionList");
            const searchbutton = searchbox.nextElementSibling;
            searchbutton.parentNode.insertBefore(suggestionList, searchbutton);
        } else {
            suggestionLists.forEach(function hideLists(o) {
                o.classList.remove("show");
            });
        }
        $("button[data-check]").on("click", function checkBoxes(e) {
            e.preventDefault();
            e.currentTarget.closest("fieldset,form").querySelectorAll("." + e.currentTarget.dataset.check).forEach(function check(c) {
                c.checked = true;
            });
        });
        $("iframe").forEach(sizeIframe);
        if (null === window.onresize) {
            window.onresize = function onResize() {
                $("iframe").forEach(sizeIframe);
            };
        }
        $("article p img,.indexPage figure img").forEach(function enlarge(i) {
            const p = i.closest("p,article");
            if (i.width / p.clientWidth > 0.8) {
                i.classList.add("stretch");
            }
        });
        if (null === window.onpopstate) {
            window.onpopstate = function (e) {
                swapDocument(e.state.html);
            };
        }
        $("a:not(.nocache)").on('mouseenter', linkPreload, {once: true, passive: true}).on('touchstart', linkPreload, {once: true, passive: true});
        eagerLoadTimer = setTimeout(eagerLoad, getNextEagerLoadTime());
    }
    isLoaded(document) ? flush(true) : window.on('load', flush);
}
toilet();
