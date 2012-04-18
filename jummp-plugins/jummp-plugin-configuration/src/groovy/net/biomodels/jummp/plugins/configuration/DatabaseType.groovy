package net.biomodels.jummp.plugins.configuration

public enum DatabaseType {
    MYSQL("MySQL"), POSTGRESQL("PostgreSQL")

    final String value

    DatabaseType(String value) {
        this.value = value
    }

    String toString() {
        value
    }
    String getKey() {
        name()
    }

    static list() {
        [MYSQL, POSTGRESQL]
    }
}
