package lab5.prob4;

import java.util.ArrayList;
import java.util.List;

class Customer {
    private final String name;
    private final List<Order> orders;

    public Customer(String name) {
        this.name = name;
        orders = new ArrayList<Order>();
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public String getName() {
        return name;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
