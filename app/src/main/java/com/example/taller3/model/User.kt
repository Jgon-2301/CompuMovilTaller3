package com.example.taller3.model

class User {
    lateinit var  name : String
    lateinit var lastName : String
    lateinit var email: String
    lateinit var id_number: String


    constructor()
    constructor(name: String, lastName: String, email: String, id_number: String) {
        this.name = name
        this.lastName = lastName
        this.email = email
        this.id_number = id_number
    }

}