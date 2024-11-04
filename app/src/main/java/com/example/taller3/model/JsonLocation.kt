package com.example.taller3.model

class JsonLocation {
    var latitud : Double
    var longitud : Double
    var nombre : String


    constructor(latitud: Double, longitud: Double, nombre: String) {
        this.latitud = latitud
        this.longitud = longitud
        this.nombre = nombre
    }
}