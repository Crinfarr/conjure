/**
 * IPos Printer Service Callback
 * IPosPrinterCallback.aidl
 * AIDL Version: 1.0.0
 */
package com.iposprinter.iposprinterservice;

/**
 * Callback for the print service execution result
 */

interface IPosPrinterCallback {

   /**
    * Returns the execution result
    * @param isSuccess: true for successful execution, false for failed execution
    */
   oneway void onRunResult(boolean isSuccess);

   /**
    * Returns the result (string data)
    * @param result: The result, the printed length since the printer was powered on (unit: mm)
    */
   oneway void onReturnString(String result);
}