package com.netsuite.erp.challenge.utilities;

public enum Tribes {

    GL_ACCOUNTING("GL Accounting - Brno"),
    TAX_BRNO("Tax - Brno"),
    TAX_PRAGUE("Tax - Prague"),
    O2C("Order to Cash - Brno"),
    FOUNDATIONS_BRNO("Foundations - Brno"),
    FOUNDATIONS_PRAGUE("Foundations - Prague"),
    MANUFACTURING("Manufacturing - Brno"),
    ONE_WORLD("OneWorld"),
    PROJECTS("Projects - Brno"),
    OTHER("Others");

    private final String name;


    Tribes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
