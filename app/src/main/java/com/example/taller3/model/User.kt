package com.example.taller3.model

class User {
    lateinit var  name : String
    lateinit var lastName : String
    lateinit var email: String
    lateinit var id_number: String
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var isAvailable: Boolean = false

    constructor()
    constructor(name: String, lastName: String, email: String, id_number: String, latitude: Double, longitude: Double, isAvailable: Boolean) {
        this.name = name
        this.lastName = lastName
        this.email = email
        this.id_number = id_number
        this.latitude = latitude
        this.longitude = longitude
        this.isAvailable = isAvailable
    }

}