package com.burgosfacundo.inventory.config;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public void clean() {
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (var statement = connection.createStatement()) {
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");

                List<String> tables = new ArrayList<>();

                try (var resultSet = statement.executeQuery("""
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = DATABASE()
                          AND table_name <> 'flyway_schema_history'
                        """)) {

                    while (resultSet.next()) {
                        tables.add(resultSet.getString("table_name"));
                    }
                }

                for (String table : tables) {
                    statement.execute("TRUNCATE TABLE `" + table + "`");
                }

                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            return null;
        });
    }
}