package com.nucu.dynamicpages.test.model.response

import com.nucu.dynamicpages.processor.annotations.visitor.Paginate
import com.nucu.dynamicpages.processor.annotations.visitor.VisitableProperty
import com.nucu.dynamicpages.processor.annotations.visitor.Visitor

data class CarouselResponse(
    val id: String,

    @Paginate(key = "id")
    @VisitableProperty(visitor = CarouselVisitor::class)
    val items: List<CarouselItem>
)

data class CarouselItem(
    val id: String,
    val name: String
)

class CarouselVisitor : Visitor<CarouselItem, CarouselItem> {
    override suspend fun visitValue(updated: Any, old: CarouselItem): CarouselItem {
        return if ((updated as CarouselItem).id == old.id) {
            old.copy(name = updated.name)
        } else {
            old
        }
    }
}
