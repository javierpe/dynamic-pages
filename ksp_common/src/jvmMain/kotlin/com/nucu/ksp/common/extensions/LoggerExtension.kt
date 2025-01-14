package com.nucu.ksp.common.extensions

import com.google.devtools.ksp.processing.KSPLogger

/**
 * Log when a process is finished.
 */
fun KSPLogger.logFinishProcess() {
    warn("Finished!\n==============================")
}

/**
 * Log when a process is started.
 */
fun KSPLogger.logStartProcess(processName: String) {
    warn("Start process: $processName")
}

/**
 * Log when processor is looking for symbols with specified annotation.
 */
fun KSPLogger.logLooking(processName: String) {
    warn("Looking $processName classes")
}

/**
 * Log when no symbol of annotation is found by processor.
 */
fun KSPLogger.logNotFound(processName: String) {
    warn("No $processName classes found")
}

/**
 * Log when processor is starting to create the file.
 */
fun KSPLogger.logStartProcessor(processorName: String) {
    warn("************** $processorName started! **************")
}

/**
 * Log when processor is finishing the file creation.
 */
fun KSPLogger.logEndProcessor(processorName: String, duration: Long) {
    warn("************** $processorName finished in ${duration}ms **************\n\n")
}