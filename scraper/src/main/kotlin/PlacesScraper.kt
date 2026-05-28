package com.sumup.scraper

import com.sumup.scraper.db.DatabaseFactory

fun main() {
    println("Starting Warsaw Beauty Salon Scraper...")

    // Initialize the database and create the table
    DatabaseFactory.init()

    println("Database initialized successfully. Schema is ready.")
}