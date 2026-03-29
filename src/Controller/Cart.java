package Controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cart{
    private static volatile Cart instance;
    private final Map<String, Map<String, Item>> userCarts = new HashMap<>();
    private User currentUser;

    private Cart() {}

    public static Cart getInstance() {
        if (instance == null) {
            synchronized (Cart.class) {
                if (instance == null) {
                    instance = new Cart();
                }
            }
        }
        return instance;
    }

    public double calculateCartTotal() {
        return Cart.getInstance().getItems().stream()
                .mapToDouble(item -> item.price * item.quantity)
                .sum();
    }

    private double discountedTotal = -1;

    public void setDiscountedTotal(double total) {
        this.discountedTotal = total;
    }

    public double getDiscountedTotal() {
        return discountedTotal >= 0 ? discountedTotal : calculateCartTotal();
    }

    private Map<String, Item> getCurrentCart() {
        if (currentUser == null) return null;
        return userCarts.computeIfAbsent(currentUser.getUsername(), k -> new HashMap<>());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userCarts.putIfAbsent(user.getUsername(), new HashMap<>());
        }
        discountedTotal = -1;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void add(String name, double price, int menuId) {
        Map<String, Item> cart = getCurrentCart();
        if (cart == null) return;
        cart.compute(name, (k, v) -> v == null
                ? new Item(name, price, 1, menuId)
                : new Item(name, price, v.quantity + 1, menuId));
    }

    public void remove(String name) {
        Map<String, Item> cart = getCurrentCart();
        if (cart == null) return;
        cart.computeIfPresent(name, (k, v) -> v.quantity <= 1
                ? null
                : new Item(name, v.price, v.quantity - 1, v.menuId));
    }

    public List<Item> getItems() {
        Map<String, Item> cart = getCurrentCart();
        if (cart == null) return new ArrayList<>();
        return new ArrayList<>(cart.values());
    }

    public int getTotalQuantity() {
        Map<String, Item> cart = getCurrentCart();
        if (cart == null) return 0;
        return cart.values().stream().mapToInt(i -> i.quantity).sum();
    }

    public void clear() {
        if (currentUser != null) {
            userCarts.get(currentUser.getUsername()).clear();
        }
        discountedTotal = -1;
    }


    public static class Item {
        public String name;
        public double price;
        public int quantity;
        public int menuId;

        public Item(String name, double price, int quantity, int menuId) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.menuId = menuId;
        }
    }
}
