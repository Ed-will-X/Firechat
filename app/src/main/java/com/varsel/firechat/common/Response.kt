package com.varsel.firechat.common

sealed class Response() {
    class Success() : Response()
    class Fail() : Response()
}
