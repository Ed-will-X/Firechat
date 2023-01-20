package com.varsel.firechat.domain.use_case._util.user

import java.util.*

/*
*   Sorts a hashmap by the order of insertion,
*   The key refers to the ID,
*   And the value refers to the insertion timestamp.
* */
class SortByTimestamp_UseCase {
    operator fun invoke(positioned: SortedMap<String, Long>): Map<String, Long> {
        val sorted = positioned.toList()
            .sortedBy { (key, value) -> value }
            .toMap()

        return sorted
    }
}