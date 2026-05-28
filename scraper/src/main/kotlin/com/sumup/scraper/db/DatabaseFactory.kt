package com.sumup.scraper.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Paths

object DatabaseFactory {
    fun init(dbPath: String = "data/salons.db") {
        // Resolve the absolute path to ensure we hit the root 'data' folder
        // regardless of whether we run via Gradle directly or Make.
        val projectRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString()

        // If running inside the 'scraper' directory, step back one level
        val actualRoot = if (projectRoot.endsWith("scraper")) {
            File(projectRoot).parent
        } else {
            projectRoot
        }

        val dbFile = File(actualRoot, dbPath)
        dbFile.parentFile?.mkdirs()

        println("Initializing SQLite database at: ${dbFile.absolutePath}")

        // Connect with WAL mode enabled to prevent SQLITE_BUSY locking errors
        Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}?journal_mode=WAL",
            driver = "org.sqlite.JDBC"
        )

        // Automatically create the table (safe to run multiple times)
        transaction {
            SchemaUtils.create(Salons)
        }
    }
}