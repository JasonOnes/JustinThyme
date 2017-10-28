
package com.JustinThyme.justinthymer.models.forms;

public enum Roles {

    ROLE_USER ("USER"),
    ROLE_ADMIN ("ADMIN");

    private final String name;

    Roles(String name) { this.name = name;}

    public String getName() {
        return name;
    }
}
