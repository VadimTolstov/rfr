package io.student.rangiffler.data.db;

/** Provides the currently applied migration version of a schema. */
public interface SchemaVersion {
    String value();
}
