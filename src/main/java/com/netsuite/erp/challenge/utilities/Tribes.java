package com.netsuite.erp.challenge.utilities;

public enum Tribes {

               MANUFACTURING("Manufacturing"),
                       PROJECTS("Projects"),
                       P2P("Procure to Pay"),
                       CASH_MANAGEMENT("Cash Management"),
                       SUBSCRIPTIONS("Subscriptions"),
                       FOUNDATIONS("Foundations"),
                       ONE_WORLD("One World"),
                       PLANNING_ALLOCATION("Planning & Allocation"),
                       TAX("Tax"),
                       O2C("Order to Cash"),
                       GLA("GL Accounting"),
                       INVENTORY("Inventory");

    private final String name;


    Tribes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
