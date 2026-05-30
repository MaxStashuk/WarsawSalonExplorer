package com.sumup.backend.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Paths

object DatabaseFactory {
    fun init(dbPath: String = "data/salons.db") {
        val projectRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString()
        val actualRoot = if (projectRoot.endsWith("backend")) {
            File(projectRoot).parent
        } else {
            projectRoot
        }
        val dbFile = File(actualRoot, dbPath)
        dbFile.parentFile?.mkdirs()

        Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}?journal_mode=WAL",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(Salons)
        }
    }
}
