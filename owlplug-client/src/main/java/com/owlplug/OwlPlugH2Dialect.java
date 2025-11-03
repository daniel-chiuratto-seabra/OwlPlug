/* OwlPlug
 * Copyright (C) 2021 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.owlplug;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * A Custom H2 Dialect with disabled check for enumerated values.
 * Since Hibernate 6, checks constraints have been added to enumerated fields.
 * These checks are not updated with ddl-auto: update. Owlplug relies on
 * this feature to update the schema to avoid using a more complex solution
 * like Liquibase or flyway.
 * Disabling the checks is a hack to allow adding new entries to the enum
 * without breaking the SQL schema.
 */
public class OwlPlugH2Dialect extends H2Dialect {

    /**
     * Constructs an {@code OwlPlugH2Dialect} with the given dialect resolution information.
     * This constructor is typically used by Hibernate to determine the specific dialect
     * based on the database metadata.
     *
     * @param info The dialect resolution information.
     */
    public OwlPlugH2Dialect(DialectResolutionInfo info) {
        // Call the superclass constructor to initialize the H2 dialect with the provided information.
        super(info);
    }

    /**
     * Constructs a default {@code OwlPlugH2Dialect}.
     * This constructor is provided for compatibility and cases where dialect resolution
     * information is not immediately available.
     */
    public OwlPlugH2Dialect() {
        // Call the default superclass constructor.
        super();
    }

    /**
     * Constructs an {@code OwlPlugH2Dialect} with a specific database version.
     * This allows for fine-grained control over dialect behavior based on the H2 database version.
     *
     * @param version The version of the H2 database.
     */
    public OwlPlugH2Dialect(DatabaseVersion version) {
        // Call the superclass constructor to initialize the H2 dialect with the specified database version.
        super(version);
    }

    /**
     * Overrides the default Hibernate behavior to disable check conditions for enumerated values.
     * This method is intentionally designed to return {@code null}, effectively
     * preventing Hibernate from generating `CHECK` constraints for enum fields.
     * This hack allows for adding new entries to enums without requiring complex
     * schema migration tools like Liquibase or Flyway, which is beneficial for
     * OwlPlug's schema update strategy.
     *
     * @param columnName The name of the column for which the check condition is being generated.
     * @param values An array of string values representing the allowed enumerated values.
     * @return Always returns {@code null} to disable the check condition.
     */
    @Override
    public String getCheckCondition(String columnName, String[] values) {
        // Explicitly return null to disable the generation of CHECK constraints for enumerated types.
        // This is a deliberate hack to simplify schema evolution for enums in OwlPlug.
        return null;
    }

    /**
     * Overrides the default Hibernate behavior to disable check conditions for numeric ranges.
     * Similar to the {@code getCheckCondition(String, String[])} method, this method
     * also returns {@code null} to prevent Hibernate from generating `CHECK` constraints
     * for numeric range validations. This ensures consistency in disabling automatic
     * check constraint generation within OwlPlug's database schema management.
     *
     * @param columnName The name of the column for which the check condition is being generated.
     * @param min The minimum allowed value for the numeric range.
     * @param max The maximum allowed value for the numeric range.
     * @return Always returns {@code null} to disable the check condition.
     */
    public String getCheckCondition(String columnName, long min, long max) {
        // Explicitly return null to disable the generation of CHECK constraints for numeric ranges.
        // This aligns with the strategy of simplifying schema evolution by avoiding automatic check constraints.
        return null;
    }
}
