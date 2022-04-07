package co.tala.example.http.client.core

enum class ContentType(val value: String) {
    APPLICATION_JSON("application/json"),
    TEXT_PLAIN("text/plain"),
    APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_OFFSET_OCTET_STREAM("application/offset+octet-stream"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    MULTIPART_FORM_DATA("multipart/form-data")
}
