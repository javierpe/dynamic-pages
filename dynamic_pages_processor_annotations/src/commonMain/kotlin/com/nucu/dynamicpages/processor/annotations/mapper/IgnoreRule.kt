package com.nucu.dynamicpages.processor.annotations.mapper

/**
 * If the rule is met, the mapper will not be executed.
 * Note: Only applies if match with render is not UNKNOWN.
 *
 * @param T It is the parent class that has to be mapped.
 */
interface IgnoreRule<T> {

    /**
     * Validates that it meets the requirements to be mapped.
     * @return Boolean True if it should be ignored.
     */
    suspend fun shouldBeIgnored(data: T): Boolean
}

/**
 * Default ignore rule, it has no effect on the mapper.
 */
interface DefaultIgnoreRule : IgnoreRule<Any>