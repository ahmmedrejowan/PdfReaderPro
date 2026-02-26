package com.rejowan.pdfreaderpro.presentation.components.pdf;

import com.bhuvaneshw.pdf.PdfViewer;

import org.jetbrains.annotations.Nullable;

import kotlin.Unit;

/**
 * A utility class for interacting with a {@link PdfViewer}.
 */
public class PdfUtil {

    /**
     * Registers a listener to be invoked when the PDF is ready to be displayed.
     *
     * @param viewer   The {@link PdfViewer} instance.
     * @param listener The listener to be invoked.
     */
    public static void onReady(PdfViewer viewer, PdfOnReadyListener listener) {
        viewer.onReady(viewer1 -> {
            listener.onReady();
            return Unit.INSTANCE;
        });
    }

    /**
     * Gets the actual scale for the PDF viewer.
     *
     * @param viewer   The {@link PdfViewer} instance.
     * @param listener The listener to be invoked with the scale value.
     */
    public static void getActualScaleFor(PdfViewer viewer, PdfOnGetScaleListener listener) {
        viewer.getActualScaleFor(PdfViewer.Zoom.ACTUAL_SIZE, scale -> {
            listener.onValue(scale);
            return Unit.INSTANCE;
        });

    }

    /**
     * A listener to be invoked when the PDF is ready to be displayed.
     */
    public interface PdfOnReadyListener {
        /**
         * Called when the PDF is ready.
         */
        void onReady();
    }

    /**
     * A listener to be invoked with a scale value.
     */
    public interface PdfOnGetScaleListener {
        /**
         * Called with the scale value.
         *
         * @param scale The scale value, or {@code null} if it could not be determined.
         */
        void onValue(@Nullable Float scale);
    }
}
