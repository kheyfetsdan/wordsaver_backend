package com.wordsaver.features.database.base

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> insertIntoTable(table: Table, dto: T) {
    transaction {
        addLogger(StdOutSqlLogger)
        table.insert { row ->
            for (property in T::class.memberProperties) {
                val column = table.columns.find { it.name == property.name }
                if (column != null) {
                    val value = property.get(dto)
                    when (column) {
                        is Column<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            row[column as Column<Any>] = value as Any
                        }
                        else -> throw IllegalArgumentException("Unsupported column type: ${column::class}")
                    }
                }
            }
        }
    }
}

