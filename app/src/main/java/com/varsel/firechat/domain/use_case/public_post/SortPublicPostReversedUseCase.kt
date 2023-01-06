package com.varsel.firechat.domain.use_case.public_post

class SortPublicPostReversedUseCase {
    operator fun invoke(posts: List<String>): List<String> {
        val sorted = posts.sortedBy {
            extractTimestamp_from_id(it)
        }.reversed().toList()

        return sorted
    }

    fun extractTimestamp_from_id(id: String): Long{
        if(id.substring(0, 1).equals(":")){
            return id.substringAfter(":").substringBefore("-").toLong()
        } else {
            return 0L
        }
    }
}