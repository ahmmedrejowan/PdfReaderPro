// #region dom elements
const viewerContainer = lazy("#viewerContainer"),
    viewer = lazy("#viewer"),
    printContainer = lazy("#printContainer"),
    printCancel = lazy("#printCancel"),
    editorModeButtons = lazy("#editorModeButtons"),
    editorHighlight = lazy("#editorHighlight"),
    editorFreeText = lazy("#editorFreeText"),
    editorStamp = lazy("#editorStamp"),
    editorInk = lazy('#editorInk'),
    toolbarViewerMiddle = lazy('#toolbarViewerMiddle'),
    toolbarViewerLeft = lazy('#toolbarViewerLeft'),
    toolbarViewerRight = lazy('#toolbarViewerRight'),
    sidebarToggleButton = lazy('#sidebarToggleButton'),
    numPages = lazy('#numPages'),
    viewFindButton = lazy('#viewFindButton'),
    zoomOutButton = lazy('#zoomOutButton'),
    zoomInButton = lazy('#zoomInButton'),
    scaleSelectContainer = lazy('#scaleSelectContainer'),
    secondaryToolbarToggleButton = lazy('#secondaryToolbarToggleButton'),
    toolbar = lazy('.toolbar'),
    secondaryPrint = lazy('#secondaryPrint'),
    secondaryDownload = lazy('#secondaryDownload'),
    presentationMode = lazy('#presentationMode'),
    firstPage = lazy('#firstPage'),
    lastPage = lazy('#lastPage'),
    pageRotateCw = lazy('#pageRotateCw'),
    pageRotateCcw = lazy('#pageRotateCcw'),
    cursorSelectTool = lazy('#cursorSelectTool'),
    cursorHandTool = lazy('#cursorHandTool'),
    scrollPage = lazy('#scrollPage'),
    scrollVertical = lazy('#scrollVertical'),
    scrollHorizontal = lazy('#scrollHorizontal'),
    scrollWrapped = lazy('#scrollWrapped'),
    spreadNone = lazy('#spreadNone'),
    spreadOdd = lazy('#spreadOdd'),
    spreadEven = lazy('#spreadEven'),
    documentProperties = lazy('#documentProperties'),
    findInput = lazy('#findInput'),
    findMatchCase = lazy('#findMatchCase'),
    findEntireWord = lazy('#findEntireWord'),
    findHighlightAll = lazy('#findHighlightAll'),
    findMatchDiacritics = lazy('#findMatchDiacritics'),
    findNextButton = lazy('#findNextButton'),
    findPreviousButton = lazy('#findPreviousButton'),
    passwordText = lazy('#passwordText'),
    password = lazy('#password'),
    passwordSubmit = lazy('#passwordSubmit'),
    passwordCancel = lazy('#passwordCancel'),
    editorHighlightButton = lazy('#editorHighlightButton'),
    editorFreeTextButton = lazy('#editorFreeTextButton'),
    editorInkButton = lazy('#editorInkButton'),
    editorStampButton = lazy('#editorStampButton'),
    editorSignatureButton = lazy('#editorSignatureButton'),
    editorSignatureAddSignature = lazy('#editorSignatureAddSignature'),
    editorFreeHighlightThickness = lazy('#editorFreeHighlightThickness'),
    editorHighlightShowAll = lazy('#editorHighlightShowAll'),
    editorFreeTextFontSize = lazy('#editorFreeTextFontSize'),
    editorFreeTextColor = lazy('#editorFreeTextColor'),
    editorInkColor = lazy('#editorInkColor'),
    editorInkThickness = lazy('#editorInkThickness'),
    editorInkOpacity = lazy('#editorInkOpacity'),
    editorStampAddImage = lazy('#editorStampAddImage');
// #endregion

let DOUBLE_CLICK_THRESHOLD = 300;
let LONG_CLICK_THRESHOLD = 500;
let isContextMenuActive = false;

window.originalPrint = window.print;
window.print = () => {
    JWI.createPrintJob();
};

function doOnLast() {
    hideAllControls();

    const loadingBar = $("#loadingBar");
    observe(loadingBar, { attributes: true, attributeFilter: ["style"], }, () => {
        const progress = parseInt(getComputedStyle(loadingBar).getPropertyValue("--progressBar-percent"));
        JWI.onProgressChange(progress);
    });

    const passwordDialog = $("#passwordDialog");
    passwordDialog.style.margin = "24px auto";
    observe(passwordDialog, { attributes: true }, (mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "open") {
                JWI.onPasswordDialogChange(passwordDialog.getAttribute("open") !== null);
            }
        });
    });

    const printDialog = $("#printServiceDialog");
    observe(printDialog, { attributes: true }, (mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "open") {
                printDialog.style.display = "none";
                if (printDialog.open) {
                    JWI.onPrintProcessStart();
                } else {
                    JWI.onPrintProcessEnd(printContainer.isCancelled ?? false);
                }
            }
        });
    });

    const printProgress = printDialog.querySelector("progress");
    observe(printProgress, { attributes: true }, (mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "value") {
                JWI.onPrintProcessProgress(parseFloat(printProgress.value) / 100);
            }
        });
    });

    const editorUndoBarMessage = $("#editorUndoBarMessage");
    observe(editorUndoBarMessage, { childList: true }, (mutations) => {
        mutations.forEach((mutation) => {
            JWI.onShowEditorMessage(mutation.target.textContent);
        });
    });

    const editorModeButtons = $('#editorModeButtons');
    let prevEditorState = {};
    observe(editorModeButtons, { attributes: true, subtree: true }, (mutations) => {
        const state = {};
        mutations.forEach(mutation => {
            if (mutation.type === 'attributes') {
                switch (mutation.target.id) {
                    case 'editorHighlightButton':
                        state['editorHighlightButton'] = mutation.target.classList.contains('toggled');
                        break;
                    case 'editorFreeTextButton':
                        state['editorFreeTextButton'] = mutation.target.classList.contains('toggled');
                        break;
                    case 'editorInkButton':
                        state['editorInkButton'] = mutation.target.classList.contains('toggled');
                        break;
                    case 'editorStampButton':
                        state['editorStampButton'] = mutation.target.classList.contains('toggled');
                        break;
                    default: break;
                }
            }
        })
        if (Object.keys(state).length == 4 && !isSame(prevEditorState, state)) {
            prevEditorState = state;
            JWI.onEditorStateChange(JSON.stringify(state));
        }
    })

    let singleClickTimer;
    let longClickTimer;
    let isLongClick = false;

    viewerContainer.addEventListener("contextmenu", (e) => {
        isContextMenuActive = true;
    });

    viewerContainer.addEventListener("click", (e) => {
        e.preventDefault();
        if (isContextMenuActive) {
            isContextMenuActive = false;
        } else if (e.detail === 1) {
            const rect = viewerContainer.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            // Hit-test in client coordinates, which is what getBoundingClientRect
            // reports, not the container-relative x/y sent to onSingleClick.
            const highlightHit = highlightAnchorAtPoint(e.clientX, e.clientY);
            singleClickTimer = setTimeout(() => {
                if (e.target.tagName === "A") JWI.onLinkClick(e.target.href);
                // Tapping a highlight is the more specific intent, so it takes
                // priority over the toolbar toggle that onSingleClick drives.
                else if (highlightHit) JWI.onHighlightTapped(highlightHit);
                else JWI.onSingleClick(x, y, rect.width, rect.height);
            }, DOUBLE_CLICK_THRESHOLD);
        }
    });

    viewerContainer.addEventListener("dblclick", (e) => {
        clearTimeout(singleClickTimer);
        const rect = viewerContainer.getBoundingClientRect();
        JWI.onDoubleClick(e.clientX - rect.left, e.clientY - rect.top);
    });

    viewerContainer.addEventListener("touchstart", (e) => {
        isLongClick = false;
        if (e.touches.length === 1) {
            longClickTimer = setTimeout(() => {
                isLongClick = true;
                JWI.onLongClick();
            }, LONG_CLICK_THRESHOLD);
        }
    });

    viewerContainer.addEventListener("touchend", (e) => {
        clearTimeout(longClickTimer);
    });

    viewerContainer.addEventListener("touchmove", (e) => {
        clearTimeout(longClickTimer);
    });

    setAriaLabel("Pdf Viewer");
    setAriaRoleDescription("Region");
}

function setupHelper() {
    PDFViewerApplication.findBar.highlightAll.click();
    PDFViewerApplication.pdfSidebar.close();

    // Closing the sidebar here is not enough on its own: this runs before any
    // document is loaded, and PDF.js re-derives the sidebar state per document in
    // setInitialView. sidebarViewOnLoad defaults to -1 (UNKNOWN), so a catalog
    // /PageMode of UseOutlines/UseThumbs/UseAttachments/UseOC - or a previously
    // stored sidebarView - reopens it. The sidebar element itself stays hidden by
    // hideAllControls(), but #outerContainer keeps the sidebarOpen class, which
    // offsets #viewerContainer by --sidebar-width (200px) and leaves a blank strip.
    // Pinning it to SidebarView.NONE keeps both paths from overriding it. (#73)
    PDFViewerApplicationOptions.set("sidebarViewOnLoad", 0);

    setupSelectionReporting();
    setupHighlightRendering();
    setupHorizontalScrollLock();

    // Center the active match in the viewport instead of pinning it ~50px
    // below the top (PDF.js's MATCH_SCROLL_OFFSET_TOP). The original scroll
    // runs first to keep PDF.js's internal state consistent, then we
    // recenter by adjusting scrollTop by the delta between the element's
    // center and the container's center.
    const findController = PDFViewerApplication.findController;
    const originalScrollMatchIntoView = findController.scrollMatchIntoView.bind(findController);
    findController.scrollMatchIntoView = function (args) {
        const willScroll = this._scrollMatches && !!args.element &&
            args.matchIndex !== -1 && args.matchIndex === this._selected.matchIdx &&
            args.pageIndex !== -1 && args.pageIndex === this._selected.pageIdx;
        originalScrollMatchIntoView(args);
        if (!willScroll) return;
        const container = document.getElementById("viewerContainer");
        if (!container) return;
        const elementRect = args.element.getBoundingClientRect();
        const containerRect = container.getBoundingClientRect();
        const elementCenterY = (elementRect.top + elementRect.bottom) / 2;
        const containerCenterY = (containerRect.top + containerRect.bottom) / 2;
        container.scrollTop += elementCenterY - containerCenterY;
    };

    PDFViewerApplication.eventBus.on("scalechanging", (event) => {
        const { scale } = event;
        JWI.onScaleChange(scale, PDFViewerApplication.pdfViewer.currentScaleValue);
    });

    PDFViewerApplication.eventBus.on("pagechanging", (event) => {
        const { pageNumber } = event;
        JWI.onPageChange(pageNumber);
    });

    PDFViewerApplication.eventBus.on("pagerendered", (event) => {
        const { pageNumber } = event;
        const pageDiv = PDFViewerApplication.pdfViewer.getPageView(pageNumber - 1).div;
        const textLayer = pageDiv.querySelector(".textLayer");

        if (textLayer) {
            JWI.onPageRendered(pageNumber);
            return;
        }

        observe(pageDiv, { childList: true }, (mutations, observer) => {
            const textLayer = pageDiv.querySelector(".textLayer");

            if (textLayer) {
                observer.disconnect();
                JWI.onPageRendered(pageNumber);
            }
        });
    });

    PDFViewerApplication.eventBus.on("updatefindcontrolstate", (event) => {
        JWI.onFindMatchChange(event.matchesCount?.current || 0, event.matchesCount?.total || 0);
    });

    PDFViewerApplication.eventBus.on("updatefindmatchescount", (event) => {
        JWI.onFindMatchChange(event.matchesCount?.current || 0, event.matchesCount?.total || 0);
    });

    PDFViewerApplication.eventBus.on("spreadmodechanged", (event) => {
        JWI.onSpreadModeChange(event.mode);
    });

    PDFViewerApplication.eventBus.on("scrollmodechanged", (event) => {
        JWI.onScrollModeChange(event.mode);
    });

    PDFViewerApplication.eventBus.on("outlineloaded", (e) => {
        loadOutline();
    });

    PDFViewerApplication.eventBus.on("attachmentsloaded", (e) => {
        loadAttachments();
    });

    viewerContainer.addEventListener("scroll", () => {
        let currentOffset;
        let totalScrollable;
        let isHorizontalScroll = PDFViewerApplication.pdfViewer._scrollMode === ScrollMode.HORIZONTAL;

        if (isHorizontalScroll) {
            currentOffset = viewerContainer.scrollLeft;
            totalScrollable = viewerContainer.scrollWidth - viewerContainer.clientWidth;
        } else {
            currentOffset = viewerContainer.scrollTop;
            totalScrollable = viewerContainer.scrollHeight - viewerContainer.clientHeight;
        }

        JWI.onScroll(Math.round(currentOffset), totalScrollable, isHorizontalScroll);
    });

    const searchInput = document.getElementById("findInput");
    observe(searchInput, { attributes: true, attributeFilter: ["data-status"], }, (mutationsList) => {
        mutationsList.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "data-status") {
                const newStatus = searchInput.getAttribute("data-status");

                switch (newStatus) {
                    case "pending":
                        JWI.onFindMatchStart();
                        break;
                    case "notFound":
                        JWI.onFindMatchComplete(false);
                        break;
                    default:
                        JWI.onFindMatchComplete(true);
                }
            }
        });
    });
}

function hideAllControls() {
    const ids = ["#sidebarContainer", ".hiddenSmallView", "#editorModeButtons", ".hiddenMediumView", "#secondaryOpenFile", "#viewBookmark"];

    setToolbarEnabled(false);
    ids.forEach((id) => {
        $(id).style.display = "none";
    });
}

function lazy(query) {
    let cached;

    function getEl() {
        if (!cached) cached = document.querySelector(query);
        return cached;
    }

    return new Proxy({}, {
        get(_, prop) {
            const el = getEl();
            const value = el[prop];
            if (typeof value === "function") {
                return value.bind(el);
            }
            return value;
        },
        set(_, prop, val) {
            const el = getEl();
            el[prop] = val;
            return true;
        }
    });
}

function $(query) {
    return document.querySelector(query);
}

function $all(query) {
    return document.querySelectorAll(query);
}

function observe(target, options, callback) {
    const observer = new MutationObserver(callback);
    observer.observe(target, options);
}

function isSame(prev, curr) {
    const keys = Object.keys(prev);
    if (keys.length !== Object.keys(curr).length) return false;

    return keys.every(key => prev[key] === curr[key]);
}
