package com.varsel.firechat.utils

import com.varsel.firechat.model.PublicPost.PublicPost

class PostUtils {
    companion object {
        fun sortPublicPosts_reversed(posts: List<String>): List<String> {
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

        fun extractTimestamp_inList(IDs: List<String>): List<Long> {
            val extracted = mutableListOf<Long>()

            for (i in IDs){
                extracted.add(extractTimestamp_from_id(i))
            }

            return extracted
        }

        fun extractType(string: String): Int {
            if(string.substring(string.length - 2, string.length -1) == ":"){
                return string.substring(string.length - 1, string.length).toInt()
            } else {
                return 0
            }
        }
    }
}