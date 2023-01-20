package com.varsel.firechat.data.local.BugReport

import com.varsel.firechat.common._utils.MessageUtils

class BugReport {
    lateinit var title: String
    lateinit var message: String
    lateinit var reportId: String
    var image: String? = null

    constructor(title: String, message: String, image: String?){
        this.title = title
        this.message = message
        this.image = image
        this.reportId = MessageUtils.generateUID()
    }

    constructor()
}