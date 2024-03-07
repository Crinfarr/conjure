/**
 * IPos Printer Service
 * IPosPrinterService.aidl
 * AIDL Version: 1.0.0
 */

package com.iposprinter.iposprinterservice;
import com.iposprinter.iposprinterservice.IPosPrinterCallback;
import android.graphics.Bitmap;

interface IPosPrinterService {
    /**
     * Query printer status
     * @return The current printer status
     * <ul>
     * <li>0: PRINTER_NORMAL - Can start a new print job
     * <li>1: PRINTER_PAPERLESS - Stop printing, if the current print is not finished, reload paper and reprint
     * <li>2: PRINTER_THP_HIGH_TEMPERATURE - Pause printing, if current print is not finished, it will continue after cooling down, no need to reprint
     * <li>3: PRINTER_MOTOR_HIGH_TEMPERATURE - Will not print, need to reinitialize the printer after cooling down and start a new print job
     * <li>4: PRINTER_IS_BUSY - The printer is currently printing
     * <li>5: PRINTE_ERROR_UNKNOWN - Printer error
     */
    int getPrinterStatus();

    /**
     * Initialize the printer
     * Powers on the printer and initializes default settings
     * Check the printer status before use, wait if PRINTER_IS_BUSY
     * @param callback Callback for execution result
     * @return
     */
    void printerInit(in IPosPrinterCallback callback);

    /**
     * Set the print density of the printer, affects subsequent prints until reinitialization
     * @param depth: Density level, range 1-10, function not executed if out of range. Default level 6.
     * @param callback Callback for execution result
     * @return
     */
    void setPrinterPrintDepth(int depth, in IPosPrinterCallback callback);

    /**
     * Set the print font type, affects subsequent prints until reinitialization
     * (Currently only supports ST font, more fonts will be supported in the future)
     * @param typeface: Font name ST (Song Ti)
     * @param callback Callback for execution result
     * @return
     */
    void setPrinterPrintFontType(String typeface, in IPosPrinterCallback callback);

    /**
     * Set the font size, affects subsequent prints until reinitialization
     * Note: Font size is a non-standard international print mode,
     * adjusting font size will affect character width, so the number of characters per line will also change,
     * therefore layout based on fixed-width font may be disrupted and needs to be adjusted manually
     * @param fontsize: Font size, currently supports 16, 24, 32, 48, illegal size defaults to 24
     * @param callback Callback for execution result
     * @return
     */
    void setPrinterPrintFontSize(int fontsize, in IPosPrinterCallback callback);

    /**
     * Set the alignment, affects subsequent prints until reinitialization
     * @param alignment: Alignment 0 - left, 1 - center, 2 - right, default center
     * @param callback Callback for execution result
     * @return
     */
    void setPrinterPrintAlignment(int alignment, in IPosPrinterCallback callback);

    /**
     * Feed paper (forced line break, ends previous print content and feeds 'lines' lines, the motor rotates to feed paper without sending data to the printer)
     * @param lines: Number of lines to feed (each line is one pixel)
     * @param callback Callback for execution result
     * @return
     */
    void printerFeedLines(int lines, in IPosPrinterCallback callback);

    /**
     * Print blank lines (forced line break, ends previous print content and prints blank lines, sends 0x00 data to the printer)
     * @param lines: Number of blank lines to print, limited to 100 lines
     * @param height: Height of blank lines (in pixels)
     * @param callback Callback for execution result
     * @return
     */
    void printBlankLines(int lines, int height, in IPosPrinterCallback callback);

    /**
     * Print text
     * Text wraps to the next line when it reaches the end of a line
     * @param text: Text string to print
     * @param callback Callback for execution result
     * @return
     */
    void printText(String text, in IPosPrinterCallback callback);

    /**
     * Print text with specified font type and size, font setting is only valid for this print
     * Text wraps to the next line when it reaches the end of a line
     * @param text: Text string to print
     * @param typeface: Font name ST (currently only supports one font)
     * @param fontsize: Font size, currently supports 16, 24, 32, 48, illegal size defaults to 24
     * @param callback Callback for execution result
     * @return
     */
    void printSpecifiedTypeText(String text, String typeface, int fontsize, in IPosPrinterCallback callback);

    /**
     * Print text with specified font type, size and alignment, font settings are only valid for this print
     * Text wraps to the next line when it reaches the end of a line
     * @param text: Text string to print
     * @param typeface: Font name ST (currently only supports one font)
     * @param fontsize: Font size, currently supports 16, 24, 32, 48, illegal size defaults to 24
     * @param alignment: Alignment (0 - left, 1 - center, 2 - right)
     * @param callback Callback for execution result
     * @return
     */
    void PrintSpecFormatText(String text, String typeface, int fontsize, int alignment, IPosPrinterCallback callback);

    /**
     * Print a row of a table, can specify column widths and alignments
     * @param colsTextArr Array of text strings for each column
     * @param colsWidthArr Array of column widths. Total width cannot exceed ((384 / fontsize) << 1) - (number of columns + 1)
     *                     (calculated using English characters, each Chinese character counts as two English characters, each width must be greater than 0)
     * @param colsAlign Array of column alignments (0 - left, 1 - center, 2 - right)
     * @param isContinuousPrint Whether to continue printing the table, 1 - continue, 0 - do not continue
     * Note: The lengths of the three parameter arrays should be consistent. If the width of colsTextArr[i] exceeds colsWidthArr[i], the text will wrap to the next line.
     * @param callback Callback for execution result
     * @return
     */
    void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, int isContinuousPrint, in IPosPrinterCallback callback);

    /**
     * Print an image
     * @param alignment: Alignment 0 - left, 1 - center, 2 - right, default center
     * @param bitmapSize: Bitmap size, range 1-16, out of range defaults to 10, unit: 24bit
     * @param mBitmap: Bitmap object (max width 384 pixels, larger images cannot be printed and will trigger an exception callback)
     * @param callback Callback for execution result
     * @return
     */
    void printBitmap(int alignment, int bitmapSize, in Bitmap mBitmap, in IPosPrinterCallback callback);

    /**
     * Print a 1D barcode
     * @param data: Barcode data
     * @param symbology: Barcode type
     *    0 - UPC-A,
     *    1 - UPC-E,
     *    2 - JAN13(EAN13),
     *    3 - JAN8(EAN8),
     *    4 - CODE39,
     *    5 - ITF,
     *    6 - CODABAR,
     *    7 - CODE93,
     *    8 - CODE128
     * @param height: Barcode height, range 1-16, out of range defaults to 6, each unit is 24 pixels high
     * @param width: Barcode width, range 1-16, out of range defaults to 12, each unit is 24 pixels wide
     * @param textposition: Text position 0 - no text, 1 - text above barcode, 2 - text below barcode, 3 - text above and below
     * @param callback Callback for execution result
     * @return
     */
    void printBarCode(String data, int symbology, int height, int width, int textposition, in IPosPrinterCallback callback);

    /**
     * Print a QR code
     * @param data: QR code data
     * @param modulesize: QR code module size (in dots, range 1-16), out of range defaults to 10
     * @param mErrorCorrectionLevel: QR code error correction level (0:L 1:M 2:Q 3:H)
     * @param callback Callback for execution result
     * @return
     */
    void printQRCode(String data, int modulesize, int mErrorCorrectionLevel, in IPosPrinterCallback callback);

    /**
     * Print raw byte data
     * @param rawPrintData Raw byte data
     * @param callback Result callback
     */
    void printRawData(in byte[] rawPrintData, in IPosPrinterCallback callback);

    /**
     * Print using ESC/POS commands
     * @param data Command data
     * @param callback Result callback
     */
    void sendUserCMDData(in byte[] data, in IPosPrinterCallback callback);

    /**
     * Execute print
     * After completing all print function methods, this method needs to be called for the printer to actually print;
     * Before calling this method, the printer status needs to be checked, it will only execute when the printer is PRINTER_NORMAL, otherwise it will not execute.
     * @param feedlines: Print and feed 'feedlines' lines
     * @param callback Result callback
     */
    void printerPerformPrint(int feedlines, in IPosPrinterCallback callback);
}