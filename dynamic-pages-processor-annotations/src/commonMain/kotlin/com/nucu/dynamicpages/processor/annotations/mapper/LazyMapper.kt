package com.nucu.dynamicpages.processor.annotations.mapper

/**
 * This class should be used to map a data class on demand. And it must be used in a property of a data class.
 * @param TData is the parent class of the model to be mapped.
 * @param TResult is the class that results in the mapping.
 * @param T is the rule that is going to be executed
 */
class LazyMapper<TData, TResult, T : MapperRule<TData, TResult>>(private val rule: T) {

    private lateinit var inputData: Any

    /**
     * This function must be used before [execute] in order to map the data class.
     * Note: It supports Mapper annotation and does not need to call [setInputData]
     * @param data is the data type of [TData].
     */
    fun setInputData(data: Any) {
        this.inputData = data
    }

    /**
     * The map function of the specified rule is called to execute the mapping of the class.
     * @return [TResult] which is the result of the class mapping.
     */
    suspend fun execute(): TResult {
        @Suppress("UNCHECKED_CAST")
        return rule.map(inputData as TData)
    }
}