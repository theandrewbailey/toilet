"use strict";
function $(query, node = document) {
    return Array.from(node.querySelectorAll(query));
}
function $0(query, node = document) {
    return node.querySelector(query);
}
function ajax(method, url, timeout, onLoad, onTimeout, body) {
    const req = new XMLHttpRequest();
    req.open(method, url);
    req.timeout = timeout;
    req.addEventListener("load", onLoad);
    req.addEventListener("timeout", onTimeout);
    req.send(body);
}
if (undefined === window.on) {
    Object.defineProperty(Node.prototype, "rm", {get() {
            return () => {
                this?.parentNode?.removeChild(this);
            };
        }});
    Object.defineProperty(Array.prototype, "on", {get() {
            return function arrayOn(t, f, o) {
                this.forEach(function addEvent(e) {
                    e.addEventListener(t, f, o);
                });
                return this;
            };
        }});
    [EventTarget.prototype, window].forEach(function setOn(p) {
        Object.defineProperty(p, "on", {get() {
                return function on(t, f, o) {
                    this.addEventListener(t, f, o);
                    return this;
                };
            }});
    });
}
function toilet() {
    const cachedPages = new Map();
    function enhanceLinks(query) {
        function rmPlaceholder(e) {//prevent flicker on Firefox
            const img = e.currentTarget;
            setTimeout(function cleanup() {
                const fig = img.closest("figure");
                $0(".placeholder", fig).rm();
                fig.removeAttribute("style");
            }, 100);
        }
        function enhanceFigure(a, doc) {
            if (a.matches(".indexPage a.withFigure")) {
                swapPicture(a, doc);
            }
        }
        function swapPicture(a, doc) {
            if (a.matches(".indexPage a.withFigure")) {
                try {
                    const fig = $0("figure", a);
                    fig.style.setProperty("height", fig.clientHeight + "px");
                    const aPic = $0("picture:not(.swapped):not(.placeholder)", a);
                    aPic.classList.add("placeholder");
                    const newPic = $0("img[src=\"" + $0("img", a).src + "\"]", doc).closest("picture").cloneNode(true);
                    aPic.insertAdjacentElement('afterend', newPic);
                    $0("img", newPic).on('load', rmPlaceholder).classList.add("swapped");
                    a.on('click', aPic.rm, {once: true});
                } catch (x) {
                }
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
            } else if (!isCached(u)) {
                ajax("GET", u.href, 1000, function cachePage(r) {
                    if (4 === r.target.readyState && 200 === r.target.status) {
                        const doc = Document.parseHTMLUnsafe(r.target.response);
                        enhanceFigure(a, doc);
                        const cacheHeaders = r.target.getResponseHeader("cache-control")?.split(", ");
                        const isHtml = r.target.getResponseHeader("content-type")?.startsWith("text/html");
                        if (!a.classList.contains("nocache") && cacheHeaders && isHtml) {
                            const revalidate = cacheHeaders.some(function testReval(h) {
                                return 0 <= h.search("revalidate") || 0 <= h.search("no-cache") || 0 <= h.search("no-store");
                            });
                            const validFor = Number(cacheHeaders.find(function findAge(h) {
                                return h.startsWith("max-age=");
                            })?.replace("max-age=", ""));
                            if ($0("base").href === $0("base", doc).href && !revalidate && validFor) {
                                const entry = {html: r.target.response, expires: Date.now() + (validFor * 1000)};
                                try {
                                    const duration = performance.getEntriesByName(r.target.responseURL)[0].duration | 0;
                                    entry.preload = " and preloaded in " + duration + " milliseconds";
                                } catch (ex) {
                                }
                                cachedPages.set(u.href, entry);
                                a.on('click', useCache);
                                return;
                            }
                        }
                    }
                    a.classList.add("nocache");
                }, function timedOut(r) {
                    $("a").forEach(function disableCache(l) {
                        l.classList.add("nocache");
                    });
                });
            } else {
                a.on('click', useCache);
                enhanceFigure(a, Document.parseHTMLUnsafe(cachedPages.get(u.href).html));
            }
        }
        function useCache(e) {
            const a = e.currentTarget;
            const u = new URL(a.href, document.location.origin);
            if (isCached(u)) {
                $("[style]").forEach(function rmStyle(e) {
                    e.removeAttribute("style");
                });
                history.replaceState({url: location.href, html: document.documentElement.outerHTML}, document.title, location.href);
                const cached = cachedPages.get(u.href);
                showDocument(cached.html);
                if (cached.preload) {
                    $0("body>footer.downContent>p>.elapsed").insertAdjacentText('afterend', cached.preload);
                }
                history.pushState({url: u.href, html: cached.html}, document.title, u.href);
                window.scrollTo(0, 0);
                e.preventDefault();
            }
        }
        function isCached(url) {
            return cachedPages.has(url.href) && Date.now() <= Number(cachedPages.get(url.href).expires);
        }
        function showDocument(html) {
            const doc = Document.parseHTMLUnsafe(html);
            document.body = doc.body;
            $0("title").innerHTML = $0("title", doc).innerHTML;
            try {
                $0('link[rel="canonical"]').href = $0('link[rel="canonical"]', doc).href;
            } catch (ex) {
            }
            setTimeout(flush);
        }

        $(query).on('mouseenter', preloadLink, {once: true})
                .on('touchstart', preloadLink, {once: true, passive: true})
                .on('focus', preloadLink, {once: true});
        if (null === window.onpopstate) {
            window.onpopstate = function swap(e) {
                showDocument(e.state.html);
            };
        }
    }
    function enhancePicture(query) {
        function showZoom(e) {
            e.preventDefault();
            const pic = document.createElement("picture");
            $("source", e.currentTarget).forEach(function source(s) {
                const src = document.createElement("source");
                src.srcset = s.srcset.split(", ").pop().split(" ").shift();
                pic.appendChild(src).type = s.type;
            });
            const origImg = $0("img", e.currentTarget);
            const img = document.createElement("img");
            pic.appendChild(img).src = origImg.src;
            pic.classList.add("zoomed");
            document.body.classList.add("locked");
            document.body.appendChild(pic).on('click', closeZoom);
        }
        function closeZoom(e) {
            e.preventDefault();
            if ($0("picture.zoomed")) {
                $0("picture.zoomed").rm();
            }
            document.body.classList.remove("locked");
        }

        $(query).forEach(function setZoom(e) {
            e.on('click', showZoom).classList.add("zoom");
        });
    }
    function enhanceSearch(query) {
        var unfocusTimer = null;
        function getSuggestions(e) {
            const box = e.currentTarget || e.target;
            const list = box.nextElementSibling;
            box.removeAttribute('list');
            if (undefined === box.dataset.pending && 2 < box.value.length && box.size >= box.value.length) {
                ajax("GET", box.closest("form").action + "?suggestion=" + box.value, 1000, function fillSuggestions(r) {
                    if (200 === r.target.status) {
                        const res = JSON.parse(r.target.response).slice(0, 6);
                        if (0 !== res.length) {
                            while (list.firstChild) {
                                list.firstChild.rm();
                            }
                            res.forEach(function add(s) {
                                const a = document.createElement("a");
                                a.href = box.closest("form").action + "?" + encodeURI(box.name) + "=" + encodeURI(s);
                                a.textContent = s;
                                list.appendChild(document.createElement("li")).appendChild(a);
                            });
                            list.classList.add("show");
                            enhanceLinks("#" + list.getAttribute("id") + " a");
                        }
                    }
                    var pending = box.dataset.pending;
                    delete box.dataset.pending;
                    if (pending !== box.value) {
                        getSuggestions(e);
                    }
                }, function timedOut(r) {
                    box.removeEventListener("input", getSuggestions);
                });
                box.dataset.pending = box.value;
            }
        }
        function searchFocus(e) {
            if (unfocusTimer) {
                clearTimeout(unfocusTimer);
                unfocusTimer = null;
            }
            const list = e.currentTarget.nextElementSibling;
            list.classList.add("show");
            $0(".selected", list)?.classList.remove("selected");
        }
        function searchBlur(e) {
            if (unfocusTimer) {
                clearTimeout(unfocusTimer);
            }
            unfocusTimer = setTimeout(function hideList() {
                (e.currentTarget || e.target).nextElementSibling.classList.remove("show");
            }, 10000);
        }
        function searchDown(e) {
            const box = e.currentTarget;
            const list = box.nextElementSibling;
            const selected = $0(".selected", list);
            if (selected) {
                switch (e.keyCode) {
                    case 40:// down
                        selected.classList.remove("selected");
                        selected.nextElementSibling?.classList.add("selected");
                        e.preventDefault();
                        break;
                    case 38:// up
                        selected.classList.remove("selected");
                        selected.previousElementSibling?.classList.add("selected");
                        e.preventDefault();
                        break;
                    case 39:// right
                        box.value = selected.innerText;
                        selected.classList.remove("selected");
                        e.preventDefault();
                        break;
                    case 13:// enter
                        box.value = selected.innerText;
                        e.preventDefault();
                        $0("a", selected).dispatchEvent(new MouseEvent("click", {cancelable: true}));
                        return;
                    default:
                        selected.classList.remove("selected");
                    }
            } else {
                switch (e.keyCode) {
                    case 40:// down
                        list.firstElementChild.classList.add("selected");
                        e.preventDefault();
                        break;
                    case 38:// up
                        list.lastElementChild.classList.add("selected");
                        e.preventDefault();
                        break;
                        }
            }
            $0(".selected a", list)?.dispatchEvent(new FocusEvent("focus"));
        }

        $(query).forEach(function initSearch(form) {
            const lists = $(".suggestionList", form);
            if ($0('input[type="search"]', form).on("input", getSuggestions)
                    .on("focus", searchFocus).on("blur", searchBlur)
                    .on("keydown", searchDown) && lists.length === 0) {
                const list = document.createElement("ol");
                list.setAttribute("id", "list" + Math.floor(Math.random() * 999999999999));
                list.classList.add("suggestionList");
                const button = $0("button.search");
                button.parentNode.insertBefore(list, button);
            } else {
                lists.forEach(function hideLists(o) {
                    o.classList.remove("show");
                });
            }
        });
    }
    function enhanceLazyload(query) {
        function getWaitTime() {
            const wpt = window?.performance?.timing;
            if (wpt?.loadEventEnd && wpt?.navigationStart) {
                return (wpt?.loadEventEnd - wpt?.navigationStart) * 10;
            } else if (wpt?.loadEventStart && wpt?.navigationStart) {
                return (wpt?.loadEventStart - wpt?.navigationStart) * 10;
            }
            return 5000;
        }
        ;
        function eagerLoad(imgs) {//check that all not-lazy images are loaded before switching a lazy one to eager
            if ($("img:not([loading='lazy'])").every(function isComplete(i) {
                return i.complete;
            })) {
                while (imgs.length) {
                    const img = imgs.pop();
                    if (document.body.contains(img)) {//showDocument() might have taken img off page
                        img.loading = "eager";
                        if (img.complete) {
                            continue;
                        }
                        setTimeout(eagerLoad, getWaitTime(), imgs);
                        break;
                    }
                }
            } else {
                setTimeout(eagerLoad, getWaitTime() * 2, imgs);
            }
        }

        setTimeout(eagerLoad, getWaitTime(), $(query).map(function getLazyImgs(node) {
            return $("img[loading='lazy']", node);
        }).flat().reverse());
    }
    function enhanceLayout() {
        function sizeIframe(iFrame) {
            iFrame.style.setProperty("width", 'auto');
            const p = $0("article:not(:last-child)>p:last-of-type", iFrame.parentNode);
            if (undefined !== p) {
                iFrame.style.setProperty("width", (p.clientWidth - 1) + 'px');
            }
            iFrame.style.setProperty("height", iFrame.contentWindow.document.body.scrollHeight + 50 + 'px');
            return iFrame;
        }
        function onResize() {
            $("iframe.comments").forEach(sizeIframe);
        }

        if (null === window.onresize) {
            window.onresize = onResize;
        }
    }
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
        enhanceLinks("a:not(.nocache)");
        enhancePicture("picture:not(a picture)");
        enhanceSearch("form.search");
        enhanceLazyload("html");
        enhanceLayout();
    }
    isLoaded(document) ? setTimeout(flush) : window.on('load', flush);
}
toilet();