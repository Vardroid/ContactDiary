package com.example.contactdiary.models

//Time format is ???
class Entry (val uid: String, val time: String, val place: String, val moreInfo: String) {
    constructor(): this("", "", "", "")
}