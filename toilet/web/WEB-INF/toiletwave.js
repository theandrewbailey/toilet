"use strict";
if (undefined === window.$) {
    window.$ = function $(query, node = document) {
        return Array.from(node.querySelectorAll(query));
    };
    Object.defineProperty(Array.prototype, "on", {
        get() {
            return function arrayOn(t, f, o) {
                this.forEach(function addEvent(e) {
                    e.addEventListener(t, f, o);
                });
                return this;
            };
        }
    });
    [EventTarget.prototype, window, XMLHttpRequest.prototype].forEach(function setOn(p) {
        Object.defineProperty(p, "on", {
            get() {
                return function onElement(t, f, o) {
                    this.addEventListener(t, f, o);
                    return this;
                };
            }
        });
    });
}
function preloader(query) {
    function rmPlaceholder(e) {
        try {
            const par = e.currentTarget.closest("picture").parentNode;
            function rmChild() {
                par.removeChild(par.querySelector(".placeholder"));
            }
            setTimeout(rmChild, 100); //prevent flicker on Firefox
        } catch (x) {}
    }
    function enhancePicture(a, res) {
        if (a.matches(".indexPage a.withFigure")) {
            try {
                const aPic = $("picture:not(.swapped):not(.placeholder)", a)[0];
                const imgsrc = $("img", a)[0].src;
                const inDoc = Document.parseHTMLUnsafe(res);
                const inPic = $("img[src=\"" + imgsrc + "\"]", inDoc)[0].closest("picture");
                aPic.classList.add("placeholder");
                const newPic = document.adoptNode(inPic);
                aPic.insertAdjacentElement('afterend', newPic);
                const img = $("img", newPic)[0];
                img.on('load', rmPlaceholder).classList.add("swapped");
                a.on('click', function rmBehind() {
                    try {
                        aPic.parentNode.removeChild(aPic);
                    } catch (x) {}
                }, {
                    once: true
                });
            } catch (x) {}
        }
    }
    function preloadLink(e) {
        const a = e.currentTarget;
        a.removeEventListener("mouseenter", preloadLink);
        a.removeEventListener("touchstart", preloadLink);
        a.removeEventListener("focus", preloadLink);
        const u = new URL(a.href, document.location.origin);
        if (a.classList.contains("nocache") || document.location.origin !== u.origin || a.getAttribute("download") || "_blank" === a.getAttribute("target")) {
            a.classList.add("nocache");
        } else if (undefined === a.dataset.body || undefined === a.dataset.expires) {
            const req = new XMLHttpRequest();
            req.open("GET", u.href);
            req.timeout = 1000;
            req.on("load", function cachePage(r) {
                try {
                    if (4 === req.readyState && 200 === req.status) {
                        enhancePicture(a, req.response);
                        if (!a.classList.contains("nocache")) {
                            const inDoc = Document.parseHTMLUnsafe(req.response);
                            const cacheHeaders = req.getResponseHeader("cache-control").split(", ");
                            const revalidate = cacheHeaders.some(function testReval(h) {
                                return 0 < h.search("revalidate");
                            });
                            const publicCache = cacheHeaders.some(function testPublic(h) {
                                return h === "public";
                            });
                            const validFor = Number(cacheHeaders.find(function findAge(h) {
                                        return h.startsWith("max-age=");
                                    }).replace("max-age=", ""));
                            if ($("base")[0].href === $("base", inDoc)[0].href && !revalidate && publicCache && validFor) {
                                a.dataset.body = inDoc.documentElement.innerHTML;
                                a.dataset.expires = Date.now() + (validFor * 1000);
                                a.on('click', useCache);
                                return;
                            }
                        }
                    }
                } catch (x) {}
                a.classList.add("nocache");
            }).on("timeout", function timedOut(r) {
                $("a").forEach(function disableCache(l) {
                    l.classList.add("nocache");
                });
            }).send();
        } else {
            a.on('click', useCache);
        }
    }
    function useCache(e) {
        const a = e.currentTarget;
        if (a.dataset.body && a.dataset.expires) {
            if (Date.now() > Number(a.dataset.expires)) {
                delete a.dataset.body;
                delete a.dataset.expires;
                return;
            }
            const u = new URL(a.href, document.location.origin);
            history.replaceState({
                url: location.href,
                body: document.documentElement.innerHTML
            }, document.title, location.href);
            swapDocument(a.dataset.body);
            history.pushState({
                url: u.href,
                body: document.documentElement.innerHTML
            }, document.title, u.href);
            window.scrollTo(0, 0);
            e.preventDefault();
        }
    }
    function swapDocument(body) {
        const inDoc = Document.parseHTMLUnsafe(body);
        $("title")[0].innerHTML = inDoc.querySelector("title").innerHTML;
        document.body.innerHTML = inDoc.body.innerHTML;
        toilet();
    }

    $(query).on('mouseenter', preloadLink, {
        once: true,
        passive: true
    }).on('touchstart', preloadLink, {
        once: true,
        passive: true
    }).on('focus', preloadLink, {
        once: true,
        passive: true
    });
    if (null === window.onpopstate) {
        window.onpopstate = function swap(e) {
            swapDocument(e.state.body);
        };
    }
}
function enhanceSearch(query) {
    var unfocusTimer = null;
    function getSuggestions(e) {
        const box = e.currentTarget;
        if (null === box) {
            return;
        }
        const list = box.nextElementSibling;
        box.removeAttribute('list');
        if (undefined === box.dataset.pending && 2 < box.value.length && box.size >= box.value.length) {
            const req = new XMLHttpRequest();
            const act = box.closest("form").action;
            req.open("GET", act + "?suggestion=" + box.value);
            req.on("load", function fillSuggestions(r) {
                if (200 === req.status) {
                    const res = JSON.parse(req.response);
                    if (0 !== res.length) {
                        while (list.firstChild) {
                            list.removeChild(list.firstChild);
                        }
                        res.forEach(function add(s) {
                            const a = document.createElement("a");
                            a.href = act + "?" + encodeURI(box.name) + "=" + encodeURI(s);
                            a.textContent = s;
                            const li = document.createElement("li");
                            li.appendChild(a);
                            list.appendChild(li);
                        });
                        list.classList.add("show");
                        preloader("#" + list.getAttribute("id") + " a");
                    }
                }
                var pending = box.dataset.pending;
                delete box.dataset.pending;
                if (pending !== box.value) {
                    getSuggestions(e);
                }
            }).on("timeout", function timedOut(r) {
                box.removeEventListener("input", getSuggestions);
            }).timeout = 1000;
            box.dataset.pending = box.value;
            req.send();
        }
    }
    function searchFocus(e) {
        if (unfocusTimer) {
            clearTimeout(unfocusTimer);
            unfocusTimer = null;
        }
        const list = e.currentTarget.nextElementSibling;
        list.classList.add("show");
        const selected = list.querySelector(".selected");
        if (selected) {
            selected.classList.remove("selected");
        }
    }
    function searchBlur(e) {
        if (unfocusTimer) {
            clearTimeout(unfocusTimer);
        }
        unfocusTimer = setTimeout(function hideList() {
            e.target.nextElementSibling.classList.remove("show");
        }, 10000);
    }
    function searchDown(e) {
        const box = e.currentTarget;
        const list = box.nextElementSibling;
        var selected = list.querySelector(".selected");
        switch (e.keyCode) {
        case 40: // down
            if (selected) {
                selected.classList.remove("selected");
                const next = selected.nextElementSibling;
                if (null !== next) {
                    next.classList.add("selected");
                    selected = next;
                }
            } else {
                list.firstElementChild.classList.add("selected");
                selected = list.firstElementChild;
            }
            e.preventDefault();
            break;
        case 38: // up
            if (selected) {
                selected.classList.remove("selected");
                const next = selected.previousElementSibling;
                if (null !== next) {
                    next.classList.add("selected");
                    selected = next;
                }
            } else {
                list.lastElementChild.classList.add("selected");
                selected = list.lastElementChild;
            }
            e.preventDefault();
            break;
        case 39: // right
            if (selected) {
                box.value = selected.innerText;
                selected.classList.remove("selected");
                selected = null;
                e.preventDefault();
            }
            break;
        case 13: // enter
            if (selected) {
                box.value = selected.innerText;
                e.preventDefault();
                selected.querySelector("a").dispatchEvent(new MouseEvent("click", {
                        cancelable: true
                    }));
                return;
            }
            break;
        default:
            if (selected) {
                selected.classList.remove("selected");
                selected = null;
            }
        }
        if (selected) {
            selected.querySelector("a").dispatchEvent(new FocusEvent("focus"));
        }
    }

    $(query).forEach(function initSearch(form) {
        const box = $('input[type="search"]', form).on("input", getSuggestions).on("focus", searchFocus).on("blur", searchBlur).on("keydown", searchDown)[0];
        const lists = $(".suggestionList", form);
        if (box && lists.length === 0) {
            const suggestionList = document.createElement("ol");
            suggestionList.setAttribute("id", "list" + Math.floor(Math.random() * 999999999999999));
            suggestionList.classList.add("suggestionList");
            const searchbutton = $("button.search")[0];
            searchbutton.parentNode.insertBefore(suggestionList, searchbutton);
        } else {
            lists.forEach(function hideLists(o) {
                o.classList.remove("show");
            });
        }
    });
}
function enhanceLazyload(query) {
    const imgs = $(query).map(function getLazyImgs(node) {
        return $("img[loading='lazy']", node);
    }).flat().reverse();
    var eagerLoadTime = 5000;
    var eagerLoadTimer = null;
    function eagerLoad() {
        const img = imgs[imgs.length - 1];
        eagerLoadTimer = null;
        if (img) {
            if ($("img:not([loading='lazy'])").every(function f(i) {
                    return i.complete;
                })) {
                img.loading = "eager";
                imgs.pop();
            } else {
                eagerLoadTime *= 2;
            }
            if (imgs.length) {
                eagerLoadTimer = setTimeout(eagerLoad, getNextEagerLoadTime());
            }
        }
    }
    function getNextEagerLoadTime() {
        const wpt = window.performance.timing;
        if (wpt.loadEventEnd && wpt.navigationStart) {
            return eagerLoadTime + wpt.loadEventEnd - wpt.navigationStart;
        }
        return eagerLoadTime * 2;
    }

    if (eagerLoadTimer) {
        clearTimeout(eagerLoadTimer);
    }
    eagerLoadTimer = setTimeout(eagerLoad, getNextEagerLoadTime());
}
function enhanceLayout() {
    function sizeIframe(iFrame) {
        iFrame.style.setProperty("width", 'auto');
        const p = iFrame.parentNode.querySelector("article:not(:last-child)>p:last-of-type");
        if (undefined !== p) {
            iFrame.style.setProperty("width", (p.clientWidth - 1) + 'px');
        }
        iFrame.style.setProperty("height", iFrame.contentWindow.document.body.scrollHeight + 50 + 'px');
        return iFrame;
    }
    function onResize() {
        $("iframe").forEach(sizeIframe);
    }

    if (null === window.onresize) {
        window.onresize = onResize;
    }
}
function toilet() {
    function isLoaded(doc) {
        return doc && ("complete" === doc.readyState);
    }
    function checkBoxes(e) {
        e.preventDefault();
        $("." + e.currentTarget.dataset.check, e.currentTarget.closest("fieldset,form")).forEach(function check(c) {
            c.checked = true;
        });
    }
    function flush() {
        $("button[data-check]").on("click", checkBoxes);
        preloader("a:not(.nocache)");
        enhanceSearch("form.search");
        enhanceLazyload("html");
        enhanceLayout();
    }
    isLoaded(document) ? flush() : window.on('load', flush);
}
toilet();
