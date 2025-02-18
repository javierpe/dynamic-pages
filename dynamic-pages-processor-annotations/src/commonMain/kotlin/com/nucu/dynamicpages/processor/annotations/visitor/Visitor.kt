package com.nucu.dynamicpages.processor.annotations.visitor

/**
 * This class implements [Visitor] and performs the process on a value of type [TResult]
 */
interface Visitor<T, TResult> {

    /**
     * Receives a new resource of type [T] and returns an updated value of type [TResult].
     * @param updated is the new resource updated of type [Any].
     * @param old is the old resource of type[T]
     */
    suspend fun visitValue(
        updated: Any,
        old: T
    ): TResult
}
