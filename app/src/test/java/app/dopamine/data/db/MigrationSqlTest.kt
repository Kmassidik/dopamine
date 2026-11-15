package app.dopamine.data.db

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM check of the 1 -> 2 migration (instrumented tests cannot run here): the SQL we execute must create exactly
 * the tables Room exports in schemas/2.json, and every table of version 1 must still exist in version 2.
 */
class MigrationSqlTest {
    private fun schema(version: Int): JsonObject {
        val f = File("schemas/app.dopamine.data.db.AppDatabase/$version.json")
        return Json.parseToJsonElement(f.readText()).jsonObject["database"]!!.jsonObject
    }

    private fun tables(version: Int): Map<String, String> =
        schema(version)["entities"]!!.jsonArray.associate {
            val o = it.jsonObject
            val name = o["tableName"]!!.jsonPrimitive.content
            name to o["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", name)
        }

    @Test fun migrationCreatesTheVersion2Tables() {
        val v1 = tables(1)
        val v2 = tables(2)
        val added = v2.keys - v1.keys
        assertEquals(setOf("lesson_progress", "daily_stat"), added)
        val executed = Migrations.STATEMENTS_1_2.toSet()
        added.forEach { assertTrue("Migration SQL missing or different for $it", v2.getValue(it) in executed) }
        assertEquals(added.size, executed.size)
    }

    @Test fun existingTablesAreUntouched() {
        val v1 = tables(1)
        val v2 = tables(2)
        v1.forEach { (name, sql) -> assertEquals("Table $name changed without a migration", sql, v2[name]) }
    }

    @Test fun versionIsTwo() {
        assertEquals(2, schema(2)["version"]!!.jsonPrimitive.content.toInt())
    }

    @Test fun migration2to3CreatesTheQuizTable() {
        val v2 = tables(2)
        val v3 = tables(3)
        val added = v3.keys - v2.keys
        assertEquals(setOf("unit_quiz"), added)
        val executed = Migrations3.STATEMENTS_2_3.toSet()
        added.forEach { assertTrue("Migration SQL missing or different for $it", v3.getValue(it) in executed) }
        assertEquals(added.size, executed.size)
    }

    @Test fun version2TablesAreUntouchedIn3() {
        tables(2).forEach { (name, sql) -> assertEquals("Table $name changed without a migration", sql, tables(3)[name]) }
    }

    @Test fun databaseVersionIsThree() {
        assertEquals(3, schema(3)["version"]!!.jsonPrimitive.content.toInt())
    }
}
