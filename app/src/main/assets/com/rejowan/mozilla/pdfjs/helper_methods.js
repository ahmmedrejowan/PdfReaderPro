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

function showDocumentProperties() {
    documentProperties.click();
}

function startFind(searchTerm) {
    if (findInput) {
        findInput.value = searchTerm;

        const caseSensitive = findMatchCase?.checked || false;
        const entireWord = findEntireWord?.checked || false;
        const highlightAll = findHighlightAll?.checked || false;
        const matchDiacritics = findMatchDiacritics?.checked || false;

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

function findNext() {
    findNextButton.click();
}

function findPrevious() {
    findPreviousButton.click();
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
function loadOutline() {
    const outlineDiv = $("#outlineView");
    const outline = [];

    iterateTreeElements(outline, outlineDiv.children, 'outlineItem');

    console.log(outline)
    JWI.onOutlineLoaded(JSON.stringify(outline));
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
