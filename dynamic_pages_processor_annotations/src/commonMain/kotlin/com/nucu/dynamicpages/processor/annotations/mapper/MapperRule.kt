package com.nucu.dynamicpages.processor.annotations.mapper

/**
 * Interface that allows to create a rule
 * WARNING: Be careful with the dependencies that you inject,
 * they can affect the final calculation time of the rule and affect the rendering of any UI.
 * Don't use dependencies that are not necessary and that require a lot of time to be processed.
 *
 * @property T specifies the origin data class.
 * @property R specifies the data type that rule should return.
 */
interface MapperRule<T, R> {

    /**
     * Map the origin data class and transform to specified data type.
     * @param data is the origin data class.
     * @return the result of this rule.
     */
    suspend fun map(data: T): R
}