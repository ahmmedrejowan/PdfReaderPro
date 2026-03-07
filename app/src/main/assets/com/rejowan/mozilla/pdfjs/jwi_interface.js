try {
    // Checking for Android JWI
    JWI;
} catch (e) {
    // No Android JWI is provided.
    console.log("Using dummy interface");

    window.JWI = {
        getHighlightEditorColorsString() {
            return "yellow=#FFFF98,green=#53FFBC,blue=#80EBFF,pink=#FFCBE6,red=#FF4F5F";
        },
        onLoadFailed() { },
        onLoadSuccess() { },
        onProgressChange() { },
        onPageRendered() { },
        onPasswordDialogChange() { },
        onLinkClick() { },
        onSingleClick() { },
        getValidCustomProtocols() { return ""; },
        onDoubleClick() { },
        onLongClick() { },
        onScaleChange() { },
        onPageChange() { },
        onSpreadModeChange() { },
        onScrollModeChange() { },
        onScroll() { },
        onFindMatchChange() { },
        onFindMatchStart() { },
        onFindMatchComplete() { },
        onLoadProperties() { },
        createPrintJob() { },
        conveyMessage() { },
        onPrintProcessStart() { },
        onPrintProcessEnd() { },
        onPrintProcessProgress() { },
        onShowEditorMessage() { },
        onAnnotationEditor() { },
        onEditorStateChange() { },
        onOutlineLoaded() { },
        onAttachmentsLoaded() { },
    };
}
