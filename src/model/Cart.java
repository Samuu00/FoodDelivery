package model;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

import java.util.*;

/**
 * Stato della sessione d'ordine: utente, carrello (di UN solo ristorante), coupon, indirizzo di consegna.
 * Un carrello per utente, conservato tra logout e login nella stessa sessione.
 * La UI fa binding su {@link #versionProperty()} e si aggiorna da sola.
 */
public final class Cart {

    public record Item(int menuId, String name, double price, int quantity) {
        public double lineTotal() { return price * quantity; }
    }

    private static final class State {
        final Map<Integer, Item> items = new LinkedHashMap<>();
        int restaurantId;
        String restaurantName;
        String restaurantImage;
        String couponCode;
        double discountPercent;
        String deliveryAddress;
    }

    private static final class Holder { private static final Cart INSTANCE = new Cart(); }
    public static Cart get() { return Holder.INSTANCE; }

    private final Map<String, State> states = new HashMap<>();
    private final ReadOnlyIntegerWrapper version = new ReadOnlyIntegerWrapper();
    private User currentUser;

    private Cart() {}

    public ReadOnlyIntegerProperty versionProperty() { return version.getReadOnlyProperty(); }

    private void changed() { version.set(version.get() + 1); }

    private State s() {
        return currentUser == null ? new State() : states.computeIfAbsent(currentUser.getUsername(), k -> new State());
    }

    public void setCurrentUser(User user) {
        currentUser = user;
        changed();
    }

    public User getCurrentUser() { return currentUser; }

    // ---------- Carrello ----------

    /** true se il carrello ha già piatti di un altro ristorante (aggiungere lo svuoterebbe). */
    public boolean conflictsWith(int restaurantId) {
        return !isEmpty() && s().restaurantId != restaurantId;
    }

    /** Aggiunge un piatto; se il carrello era di un altro ristorante lo svuota prima. */
    public void add(Restaurant r, int menuId, String name, double price) {
        State st = s();
        if (conflictsWith(r.id())) clearItems(st);
        st.restaurantId = r.id();
        st.restaurantName = r.name();
        st.restaurantImage = r.image();
        st.items.merge(menuId, new Item(menuId, name, price, 1),
                (old, one) -> new Item(menuId, name, price, old.quantity() + 1));
        changed();
    }

    public void remove(int menuId) {
        State st = s();
        st.items.computeIfPresent(menuId, (k, v) -> v.quantity() <= 1 ? null
                : new Item(v.menuId(), v.name(), v.price(), v.quantity() - 1));
        if (st.items.isEmpty()) clearItems(st);
        changed();
    }

    public void clear() {
        clearItems(s());
        changed();
    }

    private static void clearItems(State st) {
        st.items.clear();
        st.restaurantId = 0;
        st.restaurantName = null;
        st.restaurantImage = null;
        st.couponCode = null;
        st.discountPercent = 0;
    }

    public int quantityOf(int menuId) {
        Item i = s().items.get(menuId);
        return i == null ? 0 : i.quantity();
    }

    public List<Item> getItems() { return new ArrayList<>(s().items.values()); }
    public boolean isEmpty() { return s().items.isEmpty(); }
    public int getTotalQuantity() { return s().items.values().stream().mapToInt(Item::quantity).sum(); }
    public double getSubtotal() { return s().items.values().stream().mapToDouble(Item::lineTotal).sum(); }
    public double getDiscount() { return getSubtotal() * s().discountPercent / 100.0; }
    public double getTotal() { return getSubtotal() - getDiscount(); }
    public int getRestaurantId() { return s().restaurantId; }
    public String getRestaurantName() { return s().restaurantName; }
    public String getRestaurantImage() { return s().restaurantImage; }

    // ---------- Coupon ----------

    public void applyCoupon(String code, double percent) {
        s().couponCode = code;
        s().discountPercent = percent;
        changed();
    }

    public void removeCoupon() {
        s().couponCode = null;
        s().discountPercent = 0;
        changed();
    }

    public String getCouponCode() { return s().couponCode; }
    public double getDiscountPercent() { return s().discountPercent; }

    // ---------- Consegna ----------

    public String getDeliveryAddress() { return s().deliveryAddress; }

    public void setDeliveryAddress(String address) {
        s().deliveryAddress = address;
        changed();
    }
}
