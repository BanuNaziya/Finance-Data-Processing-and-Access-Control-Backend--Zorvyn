package com.finance.backend.model;


public enum Role {
    /**
     * VIEWER: Most restricted role.
     * Can only view (GET) financial records and dashboard data.
     * Cannot create, update, or delete anything.
     */
    VIEWER,

    /**
     * ANALYST: Mid-level role.
     * Can view all records + create and update financial transactions.
     * Cannot delete records or manage user accounts.
     */
    ANALYST,

    /**
     * ADMIN: Highest privilege role.
     * Has complete access: manage users, create/update/delete transactions.
     * Default admin is seeded at startup with email: admin@finance.com
     */
    ADMIN
}
