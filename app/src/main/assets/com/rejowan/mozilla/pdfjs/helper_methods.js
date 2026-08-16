// #region open url and extract print images
function openUrl(args) {
    PDFViewerApplication.open(args)
        .then(() => sendDocumentProperties())
        .catch((e) => {
            if (e.message !== 'Failed to fetch') // Covered by native resource loaders
                JWI.onLoadFailed(e.message, e.name);
        });

    let callback = (event) => {
        // const { pageNumber } = event;
        PDFViewerApplication.eventBus.off("pagerendered", callback);

        PDFViewerApplication.pdfDocument.annotationStorage.originalOnAnnotationEditor = PDFViewerApplication.pdfDocument.annotationStorage.onAnnotationEditor;
        PDFViewerApplication.pdfDocument.annotationStorage.onAnnotationEditor = (type) => {
            PDFViewerApplication.pdfDocument.annotationStorage.originalOnAnnotationEditor(type);
            JWI.onAnnotationEditor(type);
        };

        JWI.onLoadSuccess(PDFViewerApplication.pagesCount);
    };
    PDFViewerApplication.eventBus.on("pagerendered", callback);
}

function extractPrintImages() {
    let pages = printContainer.querySelectorAll("img");
    JWI.conveyMessage(null, "PRINT_START", null);

    pages.forEach((page, index) => {
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");

        canvas.width = page.naturalWidth;
        canvas.height = page.naturalHeight;

        ctx.drawImage(page, 0, 0);

        const base64Data = canvas.toDataURL("image/png");

        JWI.conveyMessage(base64Data, "PAGE_DATA", `${index + 1}`);
    });

    printContainer.textContent = "";
    JWI.conveyMessage(null, "PRINT_END", null);
}
// #endregion

// #region auto-scroll functionality
let autoScrollState = {
    isActive: false,
    isPaused: false,
    speed: 50, // pixels per second
    animationFrameId: null,
    lastTimestamp: null,
    accumulatedScroll: 0 // Track fractional scroll amounts
};

function startAutoScroll(pixelsPerSecond) {
    autoScrollState.speed = pixelsPerSecond;
    autoScrollState.isActive = true;
    autoScrollState.isPaused = false;
    autoScrollState.lastTimestamp = null;
    autoScrollState.accumulatedScroll = 0;

    function autoScrollStep(timestamp) {
        if (!autoScrollState.isActive) return;
        if (autoScrollState.isPaused) {
            autoScrollState.lastTimestamp = null;
            autoScrollState.animationFrameId = requestAnimationFrame(autoScrollStep);
            return;
        }

        if (autoScrollState.lastTimestamp === null) {
            autoScrollState.lastTimestamp = timestamp;
        }

        const elapsed = timestamp - autoScrollState.lastTimestamp;
        // Accumulate fractional scroll amounts for low speeds
        autoScrollState.accumulatedScroll += (autoScrollState.speed * elapsed) / 1000;

        // Only scroll when we have at least 0.5 pixel accumulated
        if (autoScrollState.accumulatedScroll >= 0.5) {
            const scrollAmount = autoScrollState.accumulatedScroll;
            autoScrollState.accumulatedScroll = 0;

            const isHorizontal = PDFViewerApplication.pdfViewer.scrollMode === ScrollMode.HORIZONTAL;

            if (isHorizontal) {
                viewerContainer.scrollLeft += scrollAmount;
                // Check if reached end
                const maxScroll = viewerContainer.scrollWidth - viewerContainer.clientWidth;
                if (viewerContainer.scrollLeft >= maxScroll) {
                    stopAutoScroll();
                    JWI.onAutoScrollEnd();
                    return;
                }
            } else {
                viewerContainer.scrollTop += scrollAmount;
                // Check if reached end
                const maxScroll = viewerContainer.scrollHeight - viewerContainer.clientHeight;
                if (viewerContainer.scrollTop >= maxScroll) {
                    stopAutoScroll();
                    JWI.onAutoScrollEnd();
                    return;
                }
            }
        }

        autoScrollState.lastTimestamp = timestamp;
        autoScrollState.animationFrameId = requestAnimationFrame(autoScrollStep);
    }

    autoScrollState.animationFrameId = requestAnimationFrame(autoScrollStep);
}

function stopAutoScroll() {
    autoScrollState.isActive = false;
    autoScrollState.isPaused = false;
    if (autoScrollState.animationFrameId) {
        cancelAnimationFrame(autoScrollState.animationFrameId);
        autoScrollState.animationFrameId = null;
    }
}

function pauseAutoScroll() {
    autoScrollState.isPaused = true;
}

function resumeAutoScroll() {
    autoScrollState.isPaused = false;
}

function setAutoScrollSpeed(pixelsPerSecond) {
    autoScrollState.speed = pixelsPerSecond;
}

function isAutoScrollActive() {
    return autoScrollState.isActive;
}

function isAutoScrollPaused() {
    return autoScrollState.isPaused;
}
// #endregion

// #region pdf.js ui elements show/hide
function setEditorModeButtonsEnabled(enabled) {
    editorModeButtons.style.display = enabled ? "inline flex" : "none";
}

function setEditorHighlightButtonEnabled(enabled) {
    editorHighlight.style.display = enabled ? "inline-block" : "none";
}

function setEditorFreeTextButtonEnabled(enabled) {
    editorFreeText.style.display = enabled ? "inline-block" : "none";
}

function setEditorStampButtonEnabled(enabled) {
    editorStamp.style.display = enabled ? "inline-block" : "none";
}

function setEditorInkButtonEnabled(enabled) {
    editorInk.style.display = enabled ? "inline-block" : "none";
}

function setToolbarViewerMiddleEnabled(enabled) {
    toolbarViewerMiddle.style.display = enabled ? "flex" : "none";
}

function setToolbarViewerLeftEnabled(enabled) {
    toolbarViewerLeft.style.display = enabled ? "flex" : "none";
}

function setToolbarViewerRightEnabled(enabled) {
    toolbarViewerRight.style.display = enabled ? "flex" : "none";
}

function setSidebarToggleButtonEnabled(enabled) {
    sidebarToggleButton.style.display = enabled ? "flex" : "none";
}

function setPageNumberContainerEnabled(enabled) {
    numPages.parentElement.style.display = enabled ? "flex" : "none";
}

function setViewFindButtonEnabled(enabled) {
    viewFindButton.style.display = enabled ? "flex" : "none";
}

function setZoomOutButtonEnabled(enabled) {
    zoomOutButton.style.display = enabled ? "flex" : "none";
}

function setZoomInButtonEnabled(enabled) {
    zoomInButton.style.display = enabled ? "flex" : "none";
}

function setZoomScaleSelectContainerEnabled(enabled) {
    scaleSelectContainer.style.display = enabled ? "flex" : "none";
}

function setSecondaryToolbarToggleButtonEnabled(enabled) {
    secondaryToolbarToggleButton.style.display = enabled ? "flex" : "none";
}

function setToolbarEnabled(enabled) {
    toolbar.style.display = enabled ? "block" : "none";
    viewerContainer.style.top = enabled ? "var(--toolbar-height)" : "0px";
    viewerContainer.style.setProperty("--visible-toolbar-height", enabled ? "var(--toolbar-height)" : "0px");
}

function setSecondaryPrintEnabled(enabled) {
    secondaryPrint.style.display = enabled ? "flex" : "none";
}

function setSecondaryDownloadEnabled(enabled) {
    secondaryDownload.style.display = enabled ? "flex" : "none";
}

function setPresentationModeEnabled(enabled) {
    presentationMode.style.display = enabled ? "flex" : "none";
}

function setGoToFirstPageEnabled(enabled) {
    firstPage.style.display = enabled ? "flex" : "none";
}

function setGoToLastPageEnabled(enabled) {
    lastPage.style.display = enabled ? "flex" : "none";
}

function setPageRotateCwEnabled(enabled) {
    pageRotateCw.style.display = enabled ? "flex" : "none";
}

function setPageRotateCcwEnabled(enabled) {
    pageRotateCcw.style.display = enabled ? "flex" : "none";
}

function setCursorSelectToolEnabled(enabled) {
    cursorSelectTool.style.display = enabled ? "flex" : "none";
}

function setCursorHandToolEnabled(enabled) {
    cursorHandTool.style.display = enabled ? "flex" : "none";
}

function setScrollPageEnabled(enabled) {
    scrollPage.style.display = enabled ? "flex" : "none";
}

function setScrollVerticalEnabled(enabled) {
    scrollVertical.style.display = enabled ? "flex" : "none";
}

function setScrollHorizontalEnabled(enabled) {
    scrollHorizontal.style.display = enabled ? "flex" : "none";
}

function setScrollWrappedEnabled(enabled) {
    scrollWrapped.style.display = enabled ? "flex" : "none";
}

function setSpreadNoneEnabled(enabled) {
    spreadNone.style.display = enabled ? "flex" : "none";
}

function setSpreadOddEnabled(enabled) {
    spreadOdd.style.display = enabled ? "flex" : "none";
}

function setSpreadEvenEnabled(enabled) {
    spreadEven.style.display = enabled ? "flex" : "none";
}

function setDocumentPropertiesEnabled(enabled) {
    documentProperties.style.display = enabled ? "flex" : "none";
}
// #endregion

// #region pdf.js ui elements click/do functionality
function downloadFile() {
    secondaryDownload.click();
}

function printFile() {
    printContainer.isCancelled = false;
    printContainer.textContent = "";
    secondaryPrint.click();
}

function cancelPrinting() {
    printContainer.isCancelled = true;
    printCancel.click();
    printContainer.textContent = "";
}

function startPresentationMode() {
    presentationMode.click();
}

function goToFirstPage() {
    firstPage.click();
}

function goToLastPage() {
    lastPage.click();
}

function selectCursorSelectTool() {
    cursorSelectTool.click();
}

function selectCursorHandTool() {
    cursorHandTool.click();
}

function selectScrollPage() {
    scrollPage.click();
}

function selectScrollVertical() {
    scrollVertical.click();
}

function selectScrollHorizontal() {
    scrollHorizontal.click();
}

function selectScrollWrapped() {
    scrollWrapped.click();
}

function selectSpreadNone() {
    spreadNone.click();
}

function selectSpreadOdd() {
    spreadOdd.click();
}

function selectSpreadEven() {
    spreadEven.click();
}

// Reading theme functions
function setReadingTheme(theme) {
    const viewer = document.getElementById('viewer');
    const viewerContainer = document.getElementById('viewerContainer');

    // Remove existing theme classes
    viewer.classList.remove('theme-light', 'theme-dark', 'theme-sepia', 'theme-black');
    viewerContainer.classList.remove('theme-light', 'theme-dark', 'theme-sepia', 'theme-black');

    // Apply theme styles
    let filter = 'none';
    let bgColor = '#f5f5f5';

    switch(theme) {
        case 'light':
            filter = 'none';
            bgColor = '#f5f5f5';
            break;
        case 'dark':
            filter = 'invert(0.85) hue-rotate(180deg)';
            bgColor = '#1a1a1a';
            break;
        case 'sepia':
            filter = 'sepia(0.3) brightness(0.95)';
            bgColor = '#f5e6d3';
            break;
        case 'black':
            filter = 'invert(1) hue-rotate(180deg)';
            bgColor = '#000000';
            break;
    }

    // Apply to viewer container background
    viewerContainer.style.backgroundColor = bgColor;
    document.body.style.backgroundColor = bgColor;

    // Apply filter to all page canvases
    const pages = viewer.querySelectorAll('.page');
    pages.forEach(page => {
        page.style.filter = filter;
    });

    // Also apply to future pages via CSS
    let styleEl = document.getElementById('reading-theme-style');
    if (!styleEl) {
        styleEl = document.createElement('style');
        styleEl.id = 'reading-theme-style';
        document.head.appendChild(styleEl);
    }
    styleEl.textContent = `
        .page { filter: ${filter} !important; }
        #viewerContainer { background-color: ${bgColor} !important; }
    `;
}

function showDocumentProperties() {
    documentProperties.click();
}

let lastFindQuery = "";
let lastFindOptions = {
    caseSensitive: false,
    entireWord: false,
    highlightAll: true,
    matchDiacritics: false,
};

function startFind(searchTerm) {
    if (findInput) {
        findInput.value = searchTerm;

        const caseSensitive = findMatchCase?.checked || false;
        const entireWord = findEntireWord?.checked || false;
        // Force highlightAll so PDF.js extracts text from every page up front
        // and builds a complete global match list. Without this, matches are
        // only computed on visited pages and arrow navigation traverses a
        // subset of the document in non-deterministic order.
        const highlightAll = true;
        const matchDiacritics = findMatchDiacritics?.checked || false;

        lastFindQuery = searchTerm;
        lastFindOptions = { caseSensitive, entireWord, highlightAll, matchDiacritics };

        PDFViewerApplication.eventBus.dispatch("find", {
            source: this,
            type: "",
            query: searchTerm,
            phraseSearch: false,
            caseSensitive: caseSensitive,
            entireWord: entireWord,
            highlightAll: highlightAll,
            matchDiacritics: matchDiacritics,
            findPrevious: false,
        });
    } else {
        console.error("Find toolbar input not found.");
    }
}

function stopFind() {
    lastFindQuery = "";
    PDFViewerApplication.eventBus.dispatch("find", {
        source: this,
        type: "",
        query: "",
        phraseSearch: false,
        caseSensitive: false,
        entireWord: false,
        highlightAll: false,
        findPrevious: false,
    });
}

function dispatchFindAgain(findPrevious) {
    if (!lastFindQuery) {
        // No active query — fall back to clicking the toolbar button (which
        // is a no-op when the toolbar isn't open, but avoids dispatching an
        // empty find).
        const button = findPrevious ? findPreviousButton : findNextButton;
        if (button) button.click();
        return;
    }
    PDFViewerApplication.eventBus.dispatch("find", {
        source: this,
        type: "again",
        query: lastFindQuery,
        phraseSearch: false,
        caseSensitive: lastFindOptions.caseSensitive,
        entireWord: lastFindOptions.entireWord,
        highlightAll: lastFindOptions.highlightAll,
        matchDiacritics: lastFindOptions.matchDiacritics,
        findPrevious: findPrevious,
    });
}

function findNext() {
    dispatchFindAgain(false);
}

function findPrevious() {
    dispatchFindAgain(true);
}

function submitPassword(inpPassword) {
    password.value = inpPassword;
    passwordSubmit.click();
}

function cancelPasswordDialog() {
    passwordCancel.click();
}
// #endregion

// #region pdf.js ui element get content
function sendDocumentProperties() {
    PDFViewerApplication.pdfDocument.getMetadata().then((info) => {
        JWI.onLoadProperties(
            info.info.Title || "-",
            info.info.Subject || "-",
            info.info.Author || "-",
            info.info.Creator || "-",
            info.info.Producer || "-",
            info.info.CreationDate || "-",
            info.info.ModDate || "-",
            info.info.Keywords || "-",
            info.info.Language || "-",
            info.info.PDFFormatVersion || "-",
            info.contentLength || 0,
            info.info.IsLinearized || "-",
            info.info.EncryptFilterName || "-",
            info.info.IsAcroFormPresent || "-",
            info.info.IsCollectionPresent || "-",
            info.info.IsSignaturesPresent || "-",
            info.info.IsXFAPresent || "-",
            JSON.stringify(info.info.Custom || "{}")
        );
    });
}

function getLabelText() {
    return passwordText.innerText;
}

const ScrollMode = {
    UNKNOWN: -1,
    VERTICAL: 0,
    HORIZONTAL: 1,
    WRAPPED: 2,
    PAGE: 3,
};

function getActualScaleFor(value) {
    const SCROLLBAR_PADDING = 40;
    const VERTICAL_PADDING = 5;
    const MAX_AUTO_SCALE = 1.25;
    const SpreadMode = {
        UNKNOWN: -1,
        NONE: 0,
        ODD: 1,
        EVEN: 2,
    };
    const currentPage = PDFViewerApplication.pdfViewer._pages[PDFViewerApplication.pdfViewer._currentPageNumber - 1];
    if (!currentPage) return -1;
    let hPadding = SCROLLBAR_PADDING,
        vPadding = VERTICAL_PADDING;
    if (PDFViewerApplication.pdfViewer.isInPresentationMode) {
        hPadding = vPadding = 4;
        if (PDFViewerApplication.pdfViewer._spreadMode !== SpreadMode.NONE) {
            hPadding *= 2;
        }
    } else if (PDFViewerApplication.pdfViewer.removePageBorders) {
        hPadding = vPadding = 0;
    } else if (PDFViewerApplication.pdfViewer._scrollMode === ScrollMode.HORIZONTAL) {
        [hPadding, vPadding] = [vPadding, hPadding];
    }
    const pageWidthScale = (((PDFViewerApplication.pdfViewer.container.clientWidth - hPadding) / currentPage.width) * currentPage.scale) / PDFViewerApplication.pdfViewer.pageWidthScaleFactor();
    const pageHeightScale = ((PDFViewerApplication.pdfViewer.container.clientHeight - vPadding) / currentPage.height) * currentPage.scale;
    let scale = -3;
    function isPortraitOrientation(size) {
        return size.width <= size.height;
    }
    switch (value) {
        case "page-actual":
            scale = 1;
            break;
        case "page-width":
            scale = pageWidthScale;
            break;
        case "page-height":
            scale = pageHeightScale;
            break;
        case "page-fit":
            scale = Math.min(pageWidthScale, pageHeightScale);
            break;
        case "auto":
            const horizontalScale = isPortraitOrientation(currentPage) ? pageWidthScale : Math.min(pageHeightScale, pageWidthScale);
            scale = Math.min(MAX_AUTO_SCALE, horizontalScale);
            break;
        default:
            scale = -2;
    }
    return scale;
}
// #endregion

// #region pdf.js ui element set content
function setFindHighlightAll(enabled) {
    findHighlightAll.checked = enabled;
}

function setFindMatchCase(enabled) {
    findMatchCase.checked = enabled;
}

function setFindEntireWord(enabled) {
    findEntireWord.checked = enabled;
}

function setFindMatchDiacritics(enabled) {
    findMatchDiacritics.checked = enabled;
}

function setViewerScrollbar(enabled) {
    if (enabled) viewerContainer.classList.remove("noScrollbar");
    else viewerContainer.classList.add("noScrollbar");
}

function scrollTo(offset) {
    viewerContainer.scrollTop = offset;
}

function scrollToRatio(ratio, isHorizontalScroll) {
    if (isHorizontalScroll) {
        let totalScrollable = viewerContainer.scrollWidth - viewerContainer.clientWidth;
        viewerContainer.scrollLeft = totalScrollable * ratio;
    } else {
        let totalScrollable = viewerContainer.scrollHeight - viewerContainer.clientHeight;
        viewerContainer.scrollTop = totalScrollable * ratio;
    }
}

function enableVerticalSnapBehavior() {
    viewerContainer.classList.remove("horizontal-snap");
    viewerContainer.classList.add("vertical-snap");
    viewerContainer.style.scrollSnapType = "y mandatory";
    viewerContainer._originalScrollSnapType = "y mandatory";
}

function enableHorizontalSnapBehavior() {
    viewerContainer.classList.remove("vertical-snap");
    viewerContainer.classList.add("horizontal-snap");
    viewerContainer.style.scrollSnapType = "x mandatory";
    viewerContainer._originalScrollSnapType = "x mandatory";
}

function removeSnapBehavior() {
    viewerContainer.classList.remove("vertical-snap");
    viewerContainer.classList.remove("horizontal-snap");
    viewerContainer.style.scrollSnapType = "none";
    viewerContainer._originalScrollSnapType = "none";
}

function centerPage(vertical, horizontal, singlePageArrangemenentEnabled = false) {
    if (singlePageArrangemenentEnabled) {
        viewerContainer.classList.add("single-page-arrangement");
        viewerContainer.classList.remove("vertical-center");
        viewerContainer.classList.remove("horizontal-center");

        if (vertical) viewerContainer.classList.add("single-page-arrangement-vertical-center");
        else viewerContainer.classList.remove("single-page-arrangement-vertical-center");

        if (horizontal) viewerContainer.classList.add("single-page-arrangement-horizontal-center");
        else viewerContainer.classList.remove("single-page-arrangement-horizontal-center");
    } else {
        viewerContainer.classList.remove("single-page-arrangement");
        viewerContainer.classList.remove("single-page-arrangement-vertical-center");
        viewerContainer.classList.remove("single-page-arrangement-horizontal-center");

        if (vertical) viewerContainer.classList.add("vertical-center");
        else viewerContainer.classList.remove("vertical-center");

        if (horizontal) viewerContainer.classList.add("horizontal-center");
        else viewerContainer.classList.remove("horizontal-center");
    }
}

function applySinglePageArrangement() {
    if ($all(".full-size-container").length != 0) return "Already in view pager mode";

    let pages = $all(".page");

    pages.forEach((page) => {
        let parent = page.parentElement;
        parent.removeChild(page);

        let pageContainer = document.createElement("div");
        pageContainer.classList.add("full-size-container");

        pageContainer.appendChild(page);
        parent.appendChild(pageContainer);
    });
}

function removeSinglePageArrangement() {
    let pageContainers = $all(".full-size-container");

    pageContainers.forEach((pageContainer) => {
        let parent = pageContainer.parentElement;
        let page = pageContainer.children[0];

        parent.removeChild(pageContainer);
        parent.appendChild(page);
    });
}

function limitScroll(maxSpeed = 100, flingThreshold = 0.5, canFling = false, adaptiveFling = false) {
    if (!viewerContainer) return;

    let lastTouchX = 0;
    let lastTouchY = 0;
    let lastTouchTime = 0;
    let accumulatedDeltaX = 0;
    let accumulatedDeltaY = 0;
    let restoreTimer;
    let isDragging = false;

    viewerContainer._originalScrollSnapType = window.getComputedStyle(viewerContainer).scrollSnapType;

    const disableSnap = () => {
        viewerContainer.style.scrollSnapType = "none";
        if (restoreTimer) clearTimeout(restoreTimer);
    };

    const restoreSnap = () => {
        viewerContainer.style.scrollSnapType = viewerContainer._originalScrollSnapType;
    };

    const clamp = (value, max) => Math.max(-max, Math.min(value, max));

    const touchStartHandler = (event) => {
        if (event.touches.length > 1) return;

        lastTouchX = event.touches[0].clientX;
        lastTouchY = event.touches[0].clientY;
        lastTouchTime = event.timeStamp;
        PDFViewerApplication._touchStartCurrentPage = PDFViewerApplication.page;

        accumulatedDeltaX = 0;
        accumulatedDeltaY = 0;
        isDragging = false;

        disableSnap();
    };

    const touchMoveHandler = (event) => {
        if (event.touches.length > 1) return;

        const touch = event.touches[0];
        const currentTouchX = touch.clientX;
        const currentTouchY = touch.clientY;

        let deltaX = lastTouchX - currentTouchX;
        let deltaY = lastTouchY - currentTouchY;

        deltaX = clamp(deltaX, maxSpeed);
        deltaY = clamp(deltaY, maxSpeed);
        if (!isDragging && (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5)) {
            isDragging = true;
        }

        viewerContainer.scrollLeft += deltaX;
        viewerContainer.scrollTop += deltaY;

        accumulatedDeltaX += deltaX;
        accumulatedDeltaY += deltaY;

        lastTouchX = currentTouchX;
        lastTouchY = currentTouchY;

        event.preventDefault();
    };

    const touchEndHandler = (event) => {
        if (!isDragging) return;

        const touchEndTime = event.timeStamp;
        const timeElapsed = touchEndTime - lastTouchTime;

        const velocityX = accumulatedDeltaX / timeElapsed;
        const velocityY = accumulatedDeltaY / timeElapsed;

        const isVerticalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.VERTICAL;
        const isHorizontalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.HORIZONTAL;

        const containerHeight = viewerContainer.clientHeight;
        const containerWidth = viewerContainer.clientWidth;

        let targetPage = PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage - 1);
        const pageHeight = targetPage.div.clientHeight;
        const pageWidth = targetPage.div.clientWidth;

        const canFlingPage = adaptiveFling ? pageWidth < containerWidth || pageHeight < containerHeight : canFling;

        event.preventDefault();

        if (canFlingPage && isHorizontalScroll && Math.abs(velocityX) > flingThreshold && Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {
                setScrollToNextPage();
            } else {
                setScrollToPreviousPage();
            }
        } else if (canFlingPage && isVerticalScroll && Math.abs(velocityY) > flingThreshold && Math.abs(velocityY) > Math.abs(velocityX)) {
            if (velocityY > 0) {
                setScrollToNextPage();
            } else {
                setScrollToPreviousPage();
            }
        } else if (isVerticalScroll && viewerContainer.scrollTop > targetPage.div.offsetTop + (targetPage.div.clientWidth * 4) / 5) {
            setScrollToNextPage();
        } else if (isVerticalScroll && viewerContainer.scrollTop < targetPage.div.offsetTop - (containerHeight * 2) / 4) {
            setScrollToPreviousPage();
        } else if (isHorizontalScroll && viewerContainer.scrollLeft > targetPage.div.offsetLeft + (targetPage.div.clientWidth * 4) / 5) {
            setScrollToNextPage();
        } else if (isHorizontalScroll && viewerContainer.scrollLeft < targetPage.div.offsetLeft - (containerHeight * 2) / 4) {
            setScrollToPreviousPage();
        } else if (setScrollToCurrentPage()) {
            restoreTimer = setTimeout(() => {
                restoreSnap();
            }, 500);
        } else {
            //restoreSnap();
        }
    };

    const resizeAndScaleListener = () => {
        setScrollToCurrentPage();
    };

    viewerContainer.addEventListener("touchstart", touchStartHandler);
    viewerContainer.addEventListener("touchmove", touchMoveHandler, { passive: false });
    viewerContainer.addEventListener("touchend", touchEndHandler, { passive: false });
    window.addEventListener("resize", resizeAndScaleListener);
    PDFViewerApplication.eventBus.on("scalechanging", resizeAndScaleListener);

    viewerContainer._scrollHandlers = { touchStartHandler, touchMoveHandler, touchEndHandler, resizeAndScaleListener };
}

function removeScrollLimit() {
    if (!viewerContainer || !viewerContainer._scrollHandlers) return;

    const { touchStartHandler, touchMoveHandler, touchEndHandler, resizeAndScaleListener } = viewerContainer._scrollHandlers;

    viewerContainer.removeEventListener("touchstart", touchStartHandler);
    viewerContainer.removeEventListener("touchmove", touchMoveHandler);
    viewerContainer.removeEventListener("touchend", touchEndHandler);
    window.removeEventListener("resize", resizeAndScaleListener);
    PDFViewerApplication.eventBus.off("scalechanging", resizeAndScaleListener);

    viewerContainer.style.scrollSnapType = viewerContainer._originalScrollSnapType;

    delete viewerContainer._scrollHandlers;
}

function setScrollToPreviousPage() {
    setScrollToPage(PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage - 2), true);
}

function setScrollToNextPage() {
    setScrollToPage(PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage));
}

function setScrollToCurrentPage() {
    let targetPage = PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage - 1);

    const isVerticalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.VERTICAL;
    const isHorizontalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.HORIZONTAL;

    if (!targetPage || !viewerContainer) return;

    const containerHeight = viewerContainer.clientHeight;
    const containerWidth = viewerContainer.clientWidth;

    const pageHeight = targetPage.div.clientHeight;
    const pageWidth = targetPage.div.clientWidth;

    const currentScrollTop = viewerContainer.scrollTop;
    const currentScrollLeft = viewerContainer.scrollLeft;

    let targetOffsetTop, targetOffsetLeft;

    if (pageHeight >= containerHeight || pageWidth >= containerWidth) {
        if (isVerticalScroll) {
            let canChange = currentScrollTop < targetPage.div.offsetTop || currentScrollTop + containerHeight > PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage)?.div?.offsetTop || 0;
            if (pageHeight > containerHeight && canChange) targetOffsetTop = nearest(currentScrollTop, targetPage.div.offsetTop, targetPage.div.offsetTop + pageHeight - containerHeight);
            else if (pageWidth > containerWidth && canChange) targetOffsetTop = targetPage.div.offsetTop - Math.abs(containerHeight - pageHeight) / 2;
            else targetOffsetTop = currentScrollTop;
        } else targetOffsetTop = currentScrollTop;
        if (isHorizontalScroll) {
            let canChange = currentScrollLeft < targetPage.div.offsetLeft || currentScrollLeft + containerWidth > PDFViewerApplication.pdfViewer.getPageView(PDFViewerApplication._touchStartCurrentPage)?.div?.offsetLeft || 0;
            if (pageWidth > containerWidth && canChange) targetOffsetLeft = nearest(currentScrollLeft, targetPage.div.offsetLeft, targetPage.div.offsetLeft + pageWidth - containerWidth);
            else if (pageHeight > containerHeight && canChange) targetOffsetLeft = targetPage.div.offsetLeft - Math.abs(containerWidth - pageWidth) / 2;
            else targetOffsetLeft = currentScrollLeft;
        } else targetOffsetLeft = currentScrollLeft;
    } else {
        targetOffsetLeft = targetPage.div.offsetLeft - (targetPage.div.parentElement.clientWidth - targetPage.div.clientWidth) / 2;
        targetOffsetTop = targetPage.div.offsetTop - (targetPage.div.parentElement.clientHeight - targetPage.div.clientHeight) / 2;
    }

    smoothScrollTo(viewerContainer, targetOffsetTop, targetOffsetLeft);
}

function setScrollToPage(targetPage, goToEnd = false) {
    const containerHeight = viewerContainer.clientHeight;
    const containerWidth = viewerContainer.clientWidth;

    const pageHeight = targetPage.div.clientHeight;
    const pageWidth = targetPage.div.clientWidth;

    let targetOffsetTop, targetOffsetLeft;

    if (pageHeight >= containerHeight || pageWidth >= containerWidth) {
        const currentScrollTop = viewerContainer.scrollTop;
        const currentScrollLeft = viewerContainer.scrollLeft;
        const isVerticalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.VERTICAL;
        const isHorizontalScroll = PDFViewerApplication.pdfViewer.scrollMode == ScrollMode.HORIZONTAL;

        if (isVerticalScroll) targetOffsetLeft = currentScrollLeft;
        else {
            if (goToEnd) targetOffsetLeft = targetPage.div.offsetLeft + targetPage.div.clientWidth - containerWidth;
            else targetOffsetLeft = targetPage.div.offsetLeft;
        }
        if (isHorizontalScroll) targetOffsetTop = currentScrollTop;
        else {
            if (goToEnd) targetOffsetTop = targetPage.div.offsetTop + targetPage.div.clientHeight - containerHeight;
            else targetOffsetTop = targetPage.div.offsetTop;
        }
    } else {
        targetOffsetLeft = targetPage.div.offsetLeft - (targetPage.div.parentElement.clientWidth - targetPage.div.clientWidth) / 2;
        targetOffsetTop = targetPage.div.offsetTop - (targetPage.div.parentElement.clientHeight - targetPage.div.clientHeight) / 2;
    }

    smoothScrollTo(viewerContainer, targetOffsetTop, targetOffsetLeft);
}

function smoothScrollTo(container, targetScrollTop, targetScrollLeft, duration = 250) {
    let startScrollLeft = container.scrollLeft;
    let startScrollTop = container.scrollTop;
    const distanceLeft = targetScrollLeft - startScrollLeft;
    const distanceTop = targetScrollTop - startScrollTop + 8.5;
    const startTime = performance.now();

    function step(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easeInOutQuad = progress < 0.5 ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2;

        container.scrollLeft = startScrollLeft + distanceLeft * easeInOutQuad;
        container.scrollTop = startScrollTop + distanceTop * easeInOutQuad;

        if (progress < 1) {
            requestAnimationFrame(step);
        }
    }

    requestAnimationFrame(step);
}

function nearest(currentPoint, point1, point2) {
    if (Math.abs(currentPoint - point1) < Math.abs(currentPoint - point2)) {
        return point1;
    } else return point2;
}

function setTextSelectionColor(color) {
    viewer.style.setProperty('--selection-color', color);
}

function removeTextSelectionColor() {
    viewer.style.removeProperty('--selection-color');
}
// #endregion

// #region pdf.js editor ui
function openTextHighlighter() {
    if (editorHighlightButton.classList.contains("toggled")) return;
    editorHighlightButton.click();
}

function closeTextHighlighter() {
    if (!editorHighlightButton.classList.contains("toggled")) return;
    editorHighlightButton.click();
}

function openEditorFreeText() {
    if (editorFreeTextButton.classList.contains("toggled")) return;
    editorFreeTextButton.click();
}

function closeEditorFreeText() {
    if (!editorFreeTextButton.classList.contains("toggled")) return;
    editorFreeTextButton.click();
}

function openEditorInk() {
    if (editorInkButton.classList.contains("toggled")) return;
    editorInkButton.click();
}

function closeEditorInk() {
    if (!editorInkButton.classList.contains("toggled")) return;
    editorInkButton.click();
}

function openEditorStamp() {
    if (editorStampButton.classList.contains("toggled")) return;
    editorStampButton.click();
}

function closeEditorStamp() {
    if (!editorStampButton.classList.contains("toggled")) return;
    editorStampButton.click();
}

function openEditorSignature() {
    if (editorSignatureButton.classList.contains("toggled")) return;
    editorSignatureButton.click();
}

function closeEditorSignature() {
    if (!editorSignatureButton.classList.contains("toggled")) return;
    editorSignatureButton.click();
}

// Opens the "add a signature" dialog straight away, so the first tap lands on
// something useful rather than an empty editor toolbar.
function addSignature() {
    openEditorSignature();
    editorSignatureAddSignature.click();
}

function setHighlighterThickness(thickness) {
    editorFreeHighlightThickness.value = thickness;
    editorFreeHighlightThickness.dispatchEvent(new Event("input"));
    editorFreeHighlightThickness.dispatchEvent(new Event("change"));
}

function showAllHighlights() {
    if (editorHighlightShowAll.getAttribute("aria-pressed") == "true") return;
    editorHighlightShowAll.click();
}

function hideAllHighlights() {
    if (editorHighlightShowAll.getAttribute("aria-pressed") == "false") return;
    editorHighlightShowAll.click();
}

function setFreeTextFontSize(fontSize) {
    editorFreeTextFontSize.value = fontSize;
    editorFreeTextFontSize.dispatchEvent(new Event("input"));
    editorFreeTextFontSize.dispatchEvent(new Event("change"));
}

function setFreeTextFontColor(fontColor) {
    editorFreeTextColor.value = fontColor;
    editorFreeTextColor.dispatchEvent(new Event("input"));
    editorFreeTextColor.dispatchEvent(new Event("change"));
}

function setInkColor(color) {
    editorInkColor.value = color;
    editorInkColor.dispatchEvent(new Event("input"));
    editorInkColor.dispatchEvent(new Event("change"));
}

function setInkThickness(thickness) {
    editorInkThickness.value = thickness;
    editorInkThickness.dispatchEvent(new Event("input"));
    editorInkThickness.dispatchEvent(new Event("change"));
}

function setInkOpacity(opacity) {
    editorInkOpacity.value = opacity;
    editorInkOpacity.dispatchEvent(new Event("input"));
    editorInkOpacity.dispatchEvent(new Event("change"));
}

function selectHighlightColor(color) {
    try {
        $(`[data-color="${color.toLowerCase()}"]`).click();
    } catch (e) {
        console.log("Unable to set highlight color! If this affects the behaviour, please raise an issue!");
    }

    $all(".editToolbar .colorPicker").forEach((colorPicker) => {
        if (!colorPicker.parentElement.parentElement.classList.contains("hidden")) {
            if (colorPicker.querySelectorAll(".dropdown").length == 0) {
                colorPicker.click();
                colorPicker.querySelector(`[data-color="${color.toLowerCase()}"]`).click();
                colorPicker.click();
            } else colorPicker.querySelector(`[data-color="${color.toLowerCase()}"]`).click();
        }
    });
}

function requestStampInsert(image) {
    editorStampAddImage.value = image;
    editorStampAddImage.dispatchEvent(new Event("input"));
}

function undo() {
    const undoEvent = new KeyboardEvent("keydown", {
        key: "z",
        code: "KeyZ",
        ctrlKey: true,
        bubbles: true,
        cancelable: true,
    });

    document.dispatchEvent(undoEvent);
}

function redo() {
    const undoEvent = new KeyboardEvent("keydown", {
        key: "y",
        code: "KeyY",
        ctrlKey: true,
        bubbles: true,
        cancelable: true,
    });

    document.dispatchEvent(undoEvent);
}
// #endregion

// #region aria label
function setAriaLabel(ariaLabel) {
    viewerContainer.ariaLabel = ariaLabel;
}

function setAriaRoleDescription(roleDescription) {
    viewerContainer.role = "region";
    viewerContainer.ariaRoleDescription = roleDescription;
}
// #endregion

// #region page functions
function getInnerHtmlOfPage(pageNumber) {
    return PDFViewerApplication.pdfViewer.getPageView(pageNumber - 1).textLayer.div.innerHTML;
}

function getInnerTextOfPage(pageNumber) {
    return PDFViewerApplication.pdfViewer.getPageView(pageNumber - 1).textLayer.div.innerText;
}
// #endregion

// #region sidebar functions
async function loadOutline() {
    const pdfDocument = PDFViewerApplication.pdfDocument;
    if (!pdfDocument) {
        JWI.onOutlineLoaded(JSON.stringify([]));
        return;
    }

    try {
        const rawOutline = await pdfDocument.getOutline();
        if (!rawOutline || rawOutline.length === 0) {
            JWI.onOutlineLoaded(JSON.stringify([]));
            return;
        }

        const outline = await processOutlineItems(rawOutline, pdfDocument, 'outlineItem');
        console.log(outline);
        JWI.onOutlineLoaded(JSON.stringify(outline));
    } catch (e) {
        console.error('Error loading outline:', e);
        JWI.onOutlineLoaded(JSON.stringify([]));
    }
}

async function processOutlineItems(items, pdfDocument, idPrefix) {
    const result = [];
    for (const item of items) {
        let pageNumber = 0;

        // Resolve destination to page number
        if (item.dest) {
            try {
                let dest = item.dest;
                // If dest is a string (named destination), resolve it
                if (typeof dest === 'string') {
                    dest = await pdfDocument.getDestination(dest);
                }
                // dest is now an array, first element is the page reference
                if (dest && dest[0]) {
                    const pageIndex = await pdfDocument.getPageIndex(dest[0]);
                    pageNumber = pageIndex; // 0-based index
                }
            } catch (e) {
                console.warn('Could not resolve destination for:', item.title, e);
            }
        }

        const id = `${idPrefix}-${Math.random().toString(36).substring(2, 9)}`;
        const outlineItem = {
            title: item.title || '',
            dest: typeof item.dest === 'string' ? item.dest : JSON.stringify(item.dest),
            page: pageNumber,
            children: [],
            id: id,
        };

        // Process children recursively
        if (item.items && item.items.length > 0) {
            outlineItem.children = await processOutlineItems(item.items, pdfDocument, idPrefix);
        }

        result.push(outlineItem);
    }
    return result;
}

function loadAttachments() {
    const attachmentsDiv = $("#attachmentsView");
    const attachments = [];

    iterateTreeElements(attachments, attachmentsDiv.children, 'attachmentItem');

    console.log(attachments)
    JWI.onAttachmentsLoaded(JSON.stringify(attachments));
}

function iterateTreeElements(outlineArray, elements, idPrefix) {
    for (let element of elements) {
        if (element.classList.contains("treeItem")) {
            const linkElement = element.querySelector("a");
            const title = linkElement?.textContent;
            const dest = linkElement?.href;

            linkElement.id = `${idPrefix}-${Math.random().toString(36).substring(2, 9)}`;

            const outlineItem = {
                title: title,
                dest: dest,
                page: 0,
                children: [],
                id: linkElement.id,
            };

            const childItemsContainer = element.querySelector(".treeItems");
            if (childItemsContainer) {
                iterateTreeElements(outlineItem.children, childItemsContainer.children, idPrefix);
            }

            outlineArray.push(outlineItem);
        }
    }
}

function performTreeItemClick(itemId) {
    const itemElement = $(`#${itemId}`);

    if (itemElement) {
        itemElement.click();
        return true;
    }

    return false;
}
// #endregion

// #region text highlights (#41)

// How long to wait after the last selectionchange before reporting. The event
// fires continuously while a selection handle is being dragged.
const SELECTION_DEBOUNCE_MS = 120;

let selectionNotifyTimer = null;

/**
 * Resolves a page's view, viewport and unrotated page box.
 *
 * Returns null until the page has actually been rendered, since pages are
 * virtualised and viewport is only populated once a page has been laid out.
 */
function getHighlightPageView(pageNumber) {
    const viewer = PDFViewerApplication.pdfViewer;
    if (!viewer) return null;

    const pageView = viewer.getPageView(pageNumber - 1);
    if (!pageView || !pageView.viewport || !pageView.pdfPage || !pageView.div) return null;

    return pageView;
}

/**
 * Converts a point in PDF user space to normalised 0..1 coordinates against the
 * unrotated page box, with a top-left origin.
 *
 * Going via PDF user space rather than screen pixels is what makes a stored
 * highlight independent of zoom and rotation. `view` is [x0, y0, x1, y1].
 */
function toNormalisedPoint(view, x, y) {
    const width = view[2] - view[0];
    const height = view[3] - view[1];

    return {
        x: width === 0 ? 0 : (x - view[0]) / width,
        // PDF user space has a bottom-left origin, flip it so rendering is top-left.
        y: height === 0 ? 0 : (view[3] - y) / height
    };
}

/** Inverse of [toNormalisedPoint]. Returns a point in PDF user space. */
function fromNormalisedPoint(view, nx, ny) {
    const width = view[2] - view[0];
    const height = view[3] - view[1];

    return [view[0] + nx * width, view[3] - ny * height];
}

/**
 * Top-left of a page's content box in client coordinates.
 *
 * getBoundingClientRect reports the border box, but `.page` carries a 9px
 * transparent border (--page-border), and the viewport transform is relative to the
 * content box. The overlay layer is absolutely positioned, so it anchors to the
 * padding box too. Measuring from the border box instead pushed every highlight
 * down and right by the border width, which read as an underline sitting below the
 * words rather than a fill over them.
 */
function contentBoxOrigin(element, bounds) {
    const style = getComputedStyle(element);

    return {
        x: bounds.left + (parseFloat(style.borderLeftWidth) || 0),
        y: bounds.top + (parseFloat(style.borderTopWidth) || 0)
    };
}

/** True when the centre of `rect` falls inside `bounds`. */
function rectCentreWithin(rect, bounds) {
    const cx = rect.left + rect.width / 2;
    const cy = rect.top + rect.height / 2;

    return cx >= bounds.left && cx <= bounds.right && cy >= bounds.top && cy <= bounds.bottom;
}

/**
 * Describes the current text selection as JSON, or "" when there is nothing
 * usable selected.
 *
 * Shape: {"page": 12, "text": "...", "quads": [{"x":..,"y":..,"w":..,"h":..}]}
 *
 * One quad per line, because a selection spanning several lines produces one
 * client rect per line and a single bounding box would cover the gaps.
 */
function getSelectionInfo() {
    const selection = document.getSelection();
    if (!selection || selection.isCollapsed || selection.rangeCount === 0) return "";

    const text = selection.toString();
    if (!text.trim()) return "";

    const range = selection.getRangeAt(0);
    let node = range.commonAncestorContainer;
    if (node && node.nodeType === Node.TEXT_NODE) node = node.parentElement;

    const pageElement = node && node.closest ? node.closest(".page") : null;
    if (!pageElement || !pageElement.dataset.pageNumber) return "";

    const pageNumber = parseInt(pageElement.dataset.pageNumber, 10);
    const pageView = getHighlightPageView(pageNumber);
    if (!pageView) return "";

    const view = pageView.pdfPage.view;
    const viewport = pageView.viewport;
    const pageBounds = pageElement.getBoundingClientRect();
    const pageOrigin = contentBoxOrigin(pageElement, pageBounds);

    const quads = [];
    for (const rect of range.getClientRects()) {
        if (rect.width <= 0 || rect.height <= 0) continue;

        // A selection can run across a page boundary. Anything outside this page is
        // dropped rather than converted with the wrong page's viewport, which would
        // place it somewhere arbitrary.
        if (!rectCentreWithin(rect, pageBounds)) continue;

        const topLeft = viewport.convertToPdfPoint(
            rect.left - pageOrigin.x,
            rect.top - pageOrigin.y
        );
        const bottomRight = viewport.convertToPdfPoint(
            rect.right - pageOrigin.x,
            rect.bottom - pageOrigin.y
        );

        const a = toNormalisedPoint(view, topLeft[0], topLeft[1]);
        const b = toNormalisedPoint(view, bottomRight[0], bottomRight[1]);

        const width = Math.abs(b.x - a.x);
        const height = Math.abs(b.y - a.y);
        if (width <= 0 || height <= 0) continue;

        quads.push({
            x: Math.min(a.x, b.x),
            y: Math.min(a.y, b.y),
            w: width,
            h: height
        });
    }

    if (quads.length === 0) return "";

    // Where the selection sits within the viewer, in CSS pixels. The app anchors its
    // own action bar to this, so it needs viewer coordinates rather than the
    // normalised page coordinates the quads use.
    const container = document.getElementById("viewerContainer");
    const containerBounds = container.getBoundingClientRect();
    let left = Infinity, top = Infinity, right = -Infinity, bottom = -Infinity;
    for (const rect of range.getClientRects()) {
        if (rect.width <= 0 || rect.height <= 0) continue;
        left = Math.min(left, rect.left);
        top = Math.min(top, rect.top);
        right = Math.max(right, rect.right);
        bottom = Math.max(bottom, rect.bottom);
    }

    const anchor = left === Infinity ? null : {
        x: left - containerBounds.left,
        y: top - containerBounds.top,
        w: right - left,
        h: bottom - top
    };

    return JSON.stringify({ page: pageNumber, text: text, quads: quads, anchor: anchor });
}

/** Clears the current selection without disturbing anything else. */
function clearSelectionInfo() {
    const selection = document.getSelection();
    if (selection) selection.removeAllRanges();
}

function setupSelectionReporting() {
    document.addEventListener("selectionchange", () => {
        clearTimeout(selectionNotifyTimer);
        selectionNotifyTimer = setTimeout(() => {
            JWI.onTextSelected(getSelectionInfo());
        }, SELECTION_DEBOUNCE_MS);
    });
}

// Stored highlights for the open document, keyed by 1-based page number.
const storedHighlights = new Map();

const HIGHLIGHT_LAYER_CLASS = "jwi-highlight-layer";
const HIGHLIGHT_CLASS = "jwi-highlight";

/**
 * The overlay layer for a page, created on first use.
 *
 * Inserted straight after the canvas wrapper so it sits below the text layer in
 * paint order. Combined with `pointer-events: none` in helper.css, that keeps text
 * selection working normally over the top of a highlight.
 */
function ensureHighlightLayer(pageElement) {
    let layer = pageElement.querySelector("." + HIGHLIGHT_LAYER_CLASS);
    if (layer) return layer;

    layer = document.createElement("div");
    layer.className = HIGHLIGHT_LAYER_CLASS;

    const canvasWrapper = pageElement.querySelector(".canvasWrapper");
    if (canvasWrapper && canvasWrapper.nextSibling) {
        pageElement.insertBefore(layer, canvasWrapper.nextSibling);
    } else {
        pageElement.appendChild(layer);
    }

    return layer;
}

function removeAllChildren(element) {
    while (element.firstChild) element.removeChild(element.firstChild);
}

/**
 * Draws the stored highlights for one page.
 *
 * Always clears first. Pages are virtualised and re-render on scroll, so without
 * that the same highlight stacks up every time the page comes back into view.
 */
function renderHighlightsForPage(pageNumber) {
    const pageView = getHighlightPageView(pageNumber);
    if (!pageView) return;

    const layer = ensureHighlightLayer(pageView.div);
    removeAllChildren(layer);

    const items = storedHighlights.get(pageNumber);
    if (!items || items.length === 0) return;

    const view = pageView.pdfPage.view;
    const viewport = pageView.viewport;

    for (const item of items) {
        if (!item.quads) continue;

        for (const quad of item.quads) {
            const topLeft = fromNormalisedPoint(view, quad.x, quad.y);
            const bottomRight = fromNormalisedPoint(view, quad.x + quad.w, quad.y + quad.h);

            const a = viewport.convertToViewportPoint(topLeft[0], topLeft[1]);
            const b = viewport.convertToViewportPoint(bottomRight[0], bottomRight[1]);

            const element = document.createElement("div");
            element.className = HIGHLIGHT_CLASS;
            element.dataset.highlightId = item.id;
            element.style.left = Math.min(a[0], b[0]) + "px";
            element.style.top = Math.min(a[1], b[1]) + "px";
            element.style.width = Math.abs(b[0] - a[0]) + "px";
            element.style.height = Math.abs(b[1] - a[1]) + "px";
            element.style.backgroundColor = item.color;

            layer.appendChild(element);
        }
    }
}

/** Redraws every page that currently has a highlight layer. */
function refreshRenderedHighlights() {
    for (const pageNumber of storedHighlights.keys()) {
        renderHighlightsForPage(pageNumber);
    }
}

/** Empties every layer, including pages that no longer have any highlights. */
function clearRenderedHighlights() {
    const layers = document.querySelectorAll("." + HIGHLIGHT_LAYER_CLASS);
    for (const layer of layers) removeAllChildren(layer);
}

/**
 * Replaces the highlight set for the whole document.
 *
 * @param json A JSON array of
 * `{"id": 1, "page": 12, "color": "rgba(...)", "quads": [...]}`.
 */
function applyStoredHighlights(json) {
    let items = [];
    try {
        items = JSON.parse(json) || [];
    } catch (e) {
        console.error("Error parsing highlights:", e);
        items = [];
    }

    // Clear before repopulating, so highlights deleted since the last call do not
    // linger on pages that have dropped out of the new set entirely.
    clearRenderedHighlights();
    storedHighlights.clear();

    for (const item of items) {
        if (!storedHighlights.has(item.page)) storedHighlights.set(item.page, []);
        storedHighlights.get(item.page).push(item);
    }

    refreshRenderedHighlights();
}

/**
 * Scrolls a normalised quad on a page into the middle of the view.
 *
 * Used for highlights the document itself carries. Those are painted by the viewer's
 * annotation layer rather than our overlay, so there is no element of ours to scroll
 * to, and jumping to the page alone can leave the highlight just off screen.
 */
function scrollToPageQuad(pageNumber, x, y, w, h) {
    const pageView = getHighlightPageView(pageNumber);
    if (!pageView) return false;

    const view = pageView.pdfPage.view;
    const viewport = pageView.viewport;

    const topLeft = fromNormalisedPoint(view, x, y);
    const bottomRight = fromNormalisedPoint(view, x + w, y + h);
    const a = viewport.convertToViewportPoint(topLeft[0], topLeft[1]);
    const b = viewport.convertToViewportPoint(bottomRight[0], bottomRight[1]);

    const top = Math.min(a[1], b[1]);
    const height = Math.abs(b[1] - a[1]);

    const container = document.getElementById("viewerContainer");
    if (!container) return false;

    const target = pageView.div.offsetTop + top - (container.clientHeight - height) / 2;
    container.scrollTo({ top: Math.max(0, target), behavior: "smooth" });

    return true;
}

/** Scrolls a highlight into view and pulses it so the user can spot it. */
function scrollToHighlight(highlightId) {
    const element = document.querySelector(
        `.${HIGHLIGHT_CLASS}[data-highlight-id="${highlightId}"]`
    );
    if (!element) return false;

    element.scrollIntoView({ block: "center", behavior: "smooth" });
    element.classList.remove("jwi-highlight-pulse");
    // Force a reflow so the animation restarts when the same highlight is
    // selected twice in a row.
    void element.offsetWidth;
    element.classList.add("jwi-highlight-pulse");

    return true;
}

/**
 * Finds the highlight under a viewport point, or "" when there is none.
 *
 * Hit testing by hand rather than with pointer events, because the layer has to
 * stay `pointer-events: none` for text selection to keep working through it.
 *
 * Returns JSON of `{id, x, y, w, h}`, where the rectangle is the union of every
 * piece of that highlight in viewer coordinates. The app anchors its action bar to
 * it, the same way it does for a selection, so a multi-line highlight anchors to the
 * whole thing rather than to whichever line happened to be tapped.
 */
function highlightAnchorAtPoint(x, y) {
    const elements = document.querySelectorAll("." + HIGHLIGHT_CLASS);

    let hitId = null;
    for (const element of elements) {
        const bounds = element.getBoundingClientRect();
        if (x >= bounds.left && x <= bounds.right && y >= bounds.top && y <= bounds.bottom) {
            hitId = element.dataset.highlightId || null;
            break;
        }
    }
    if (!hitId) return "";

    const container = document.getElementById("viewerContainer");
    const containerBounds = container.getBoundingClientRect();
    let left = Infinity, top = Infinity, right = -Infinity, bottom = -Infinity;

    for (const element of document.querySelectorAll(
        `.${HIGHLIGHT_CLASS}[data-highlight-id="${hitId}"]`
    )) {
        const b = element.getBoundingClientRect();
        left = Math.min(left, b.left);
        top = Math.min(top, b.top);
        right = Math.max(right, b.right);
        bottom = Math.max(bottom, b.bottom);
    }

    return JSON.stringify({
        id: hitId,
        x: left - containerBounds.left,
        y: top - containerBounds.top,
        w: right - left,
        h: bottom - top
    });
}

// #endregion

// #region horizontal scroll lock (#74)

// Freezes horizontal panning at whatever position the page is on when the lock is
// switched on, rather than at the centre. Someone who has panned to a particular
// column wants to stay there, and re-centring would move them off it.
const horizontalScrollLock = {
    enabled: false,
    position: 0
};

function isHorizontalScrollMode() {
    const viewer = PDFViewerApplication.pdfViewer;
    return !!viewer && viewer.scrollMode === ScrollMode.HORIZONTAL;
}

/**
 * Turns the lock on at the current horizontal position, or off.
 *
 * Refuses to engage in horizontal scroll mode: that is the axis the document
 * scrolls along, and locking it would strand the reader on one page.
 *
 * @return true if the lock is now on.
 */
function setHorizontalScrollLock(enabled) {
    const container = document.getElementById("viewerContainer");
    if (!container) return false;

    if (!enabled || isHorizontalScrollMode()) {
        horizontalScrollLock.enabled = false;
        return false;
    }

    horizontalScrollLock.enabled = true;
    horizontalScrollLock.position = container.scrollLeft;
    return true;
}

function isHorizontalScrollLocked() {
    return horizontalScrollLock.enabled;
}

/** Keeps the frozen position reachable after a zoom changes the scrollable width. */
function clampHorizontalScrollLock() {
    if (!horizontalScrollLock.enabled) return;

    const container = document.getElementById("viewerContainer");
    if (!container) return;

    const maxScroll = Math.max(0, container.scrollWidth - container.clientWidth);
    horizontalScrollLock.position = Math.min(horizontalScrollLock.position, maxScroll);
}

function setupHorizontalScrollLock() {
    const container = document.getElementById("viewerContainer");
    if (!container) return;

    // Restoring on scroll rather than setting overflow-x: hidden, because hiding
    // the overflow clamps scrollLeft to 0 and would throw the reader back to the
    // left edge instead of holding the position they picked.
    container.addEventListener("scroll", () => {
        if (!horizontalScrollLock.enabled) return;
        if (isHorizontalScrollMode()) return;
        if (container.scrollLeft !== horizontalScrollLock.position) {
            container.scrollLeft = horizontalScrollLock.position;
        }
    }, { passive: true });

    const eventBus = PDFViewerApplication.eventBus;
    if (!eventBus) return;

    // Zoom changes the scrollable width, so the frozen position can fall outside it.
    eventBus.on("scalechanging", () => setTimeout(clampHorizontalScrollLock, 0));

    // Switching to horizontal scroll mode has to release the lock, or the document
    // cannot be read at all.
    eventBus.on("scrollmodechanged", () => {
        if (isHorizontalScrollMode()) horizontalScrollLock.enabled = false;
    });
}
// #endregion

// #region highlight rendering

function setupHighlightRendering() {
    const eventBus = PDFViewerApplication.eventBus;
    if (!eventBus) return;

    // Pages are virtualised, so this fires again every time a page scrolls back in.
    eventBus.on("pagerendered", (event) => renderHighlightsForPage(event.pageNumber));

    // Zoom and rotation both change the viewport transform, so every quad has to be
    // reprojected. Deferred a tick so the new viewport is in place before we read it.
    eventBus.on("scalechanging", () => setTimeout(refreshRenderedHighlights, 0));
    eventBus.on("rotationchanging", () => setTimeout(refreshRenderedHighlights, 0));
}
// #endregion

// #region highlights already in the document (#41)

/**
 * Reads the Highlight annotations the PDF itself carries.
 *
 * These belong to the file rather than the app, so they are surfaced read only:
 * listed, navigable and searchable, but not editable. PDF.js already paints them
 * through its annotation layer, so they are deliberately not added to our overlay,
 * which would draw them a second time.
 *
 * Quads come back as a flat Float32Array, eight numbers per quad in the order
 * upper-left, upper-right, lower-left, lower-right, in PDF user space. They are
 * converted to the same normalised, top-left form the app stores its own in.
 */
async function loadDocumentHighlights() {
    const document_ = PDFViewerApplication.pdfDocument;
    if (!document_) {
        JWI.onDocumentHighlightsLoaded(JSON.stringify([]));
        return;
    }

    const results = [];

    try {
        for (let pageNumber = 1; pageNumber <= document_.numPages; pageNumber++) {
            const page = await document_.getPage(pageNumber);
            const annotations = await page.getAnnotations();
            const highlights = annotations.filter((a) => a.subtype === "Highlight");
            if (highlights.length === 0) continue;

            const view = page.view;
            const pageWidth = view[2] - view[0];
            const pageHeight = view[3] - view[1];
            if (pageWidth <= 0 || pageHeight <= 0) continue;

            const textContent = await page.getTextContent();

            highlights.forEach((annotation, index) => {
                const points = annotation.quadPoints;
                if (!points || points.length < 8) return;

                const quads = [];
                const boxes = [];
                for (let i = 0; i + 7 < points.length; i += 8) {
                    const left = points[i];
                    const top = points[i + 1];
                    const right = points[i + 2];
                    const bottom = points[i + 5];
                    if (right <= left || top <= bottom) continue;

                    boxes.push({ left, right, top, bottom });
                    quads.push({
                        x: (left - view[0]) / pageWidth,
                        y: (view[3] - top) / pageHeight,
                        w: (right - left) / pageWidth,
                        h: (top - bottom) / pageHeight
                    });
                }
                if (quads.length === 0) return;

                results.push({
                    // Stable within a document, and distinct from the app's own
                    // database ids, which are positive.
                    id: -(pageNumber * 1000 + index + 1),
                    page: pageNumber,
                    color: rgbArrayToArgb(annotation.color),
                    text: textUnderBoxes(textContent, boxes),
                    note: (annotation.contentsObj && annotation.contentsObj.str) || "",
                    label: (annotation.titleObj && annotation.titleObj.str) || "",
                    quads: quads
                });
            });
        }
    } catch (e) {
        console.error("Error reading document highlights:", e);
    }

    JWI.onDocumentHighlightsLoaded(JSON.stringify(results));
}

/** Packs pdf.js's [r, g, b] into the ARGB int the app stores colours as. */
function rgbArrayToArgb(color) {
    if (!color || color.length < 3) return -1;
    return (0xff << 24 | color[0] << 16 | color[1] << 8 | color[2]) | 0;
}

/**
 * Recovers the text sitting under a highlight.
 *
 * The annotation records where it is, not what it covers, so the words have to be
 * read back off the page. Without them the panel would list blank rows and search
 * would never match a highlight that came with the file.
 */
function textUnderBoxes(textContent, boxes) {
    const parts = [];

    for (const item of textContent.items) {
        if (!item.str || !item.transform) continue;

        const left = item.transform[4];
        const width = item.width || 0;
        const right = left + width;
        const baseline = item.transform[5];
        const middle = baseline + (item.height || 0) / 2;

        for (const box of boxes) {
            const withinY = middle <= box.top && middle >= box.bottom;
            if (!withinY) continue;

            const overlapLeft = Math.max(left, box.left);
            const overlapRight = Math.min(right, box.right);
            if (overlapRight <= overlapLeft) continue;

            parts.push(sliceByOverlap(item.str, left, width, overlapLeft, overlapRight));
            break;
        }
    }

    return parts.join(" ").replace(/\s+/g, " ").trim();
}

/**
 * Takes the part of a text run that a highlight actually covers.
 *
 * pdf.js hands back one item per text-showing operator, which is usually a whole
 * line, so a highlight over a single word would otherwise report the entire line.
 * Character positions are not available, so each word's position is estimated from
 * how far along the run it starts, and a word is kept when its midpoint falls inside
 * the highlight.
 *
 * Testing the midpoint rather than snapping the edges outwards matters: snapping
 * outwards turned a highlight on one word into that word plus its neighbours,
 * because an estimate landing slightly early swallowed the whole preceding word.
 * Still an estimate for proportional fonts, but wrong by a fraction of a word
 * rather than a whole one.
 */
function sliceByOverlap(text, left, width, overlapLeft, overlapRight) {
    if (width <= 0 || text.length === 0) return text;
    if (overlapLeft <= left && overlapRight >= left + width) return text;

    const perCharacter = width / text.length;
    const kept = [];
    const word = /\S+/g;

    let match;
    while ((match = word.exec(text)) !== null) {
        const wordLeft = left + match.index * perCharacter;
        const wordRight = left + (match.index + match[0].length) * perCharacter;
        const middle = (wordLeft + wordRight) / 2;

        if (middle >= overlapLeft && middle <= overlapRight) kept.push(match[0]);
    }

    return kept.length > 0 ? kept.join(" ") : text;
}
// #endregion
