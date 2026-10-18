import java.util.HashMap;
import java.util.Map;

public class InventoryService {

    private final Map<String, Integer> stockLevels = new HashMap<>();
    private final Map<String, Integer> reservations = new HashMap<>();

    public InventoryService() {
        stockLevels.put("LAPTOP-PRO", 5);
        stockLevels.put("SMARTPHONE-X", 10);
        stockLevels.put("WIRELESS-BUDS", 2);
    }

    public void addStock(String productId, int quantity) {
        int current = stockLevels.getOrDefault(productId, 0);
        stockLevels.put(productId, current + quantity);
        System.out.printf("[InventoryService] Restocked %s: +%d (Current total: %d)%n",
                productId, quantity, stockLevels.get(productId));
    }

    public int getStock(String productId) {
        return stockLevels.getOrDefault(productId, 0);
    }

    public boolean hasSufficientStock(String productId, int quantity) {
        int available = getStock(productId);
        boolean sufficient = available >= quantity;
        System.out.printf("[InventoryService] Checking stock for %s: requested=%d, available=%d -> %s%n",
                productId, quantity, available, sufficient ? "AVAILABLE" : "OUT_OF_STOCK");
        return sufficient;
    }

    public boolean reserveStock(String orderId, String productId, int quantity) {
        if (!hasSufficientStock(productId, quantity)) {
            System.out.printf("[InventoryService] Reservation FAILED for order %s (%s x %d)%n",
                    orderId, productId, quantity);
            return false;
        }
        int remaining = stockLevels.get(productId) - quantity;
        stockLevels.put(productId, remaining);
        String reservationKey = orderId + "#" + productId;
        reservations.put(reservationKey, quantity);
        System.out.printf("[InventoryService] Reserved %d unit(s) of %s for order %s (Remaining available: %d)%n",
                quantity, productId, orderId, remaining);
        return true;
    }

    public void releaseStock(String orderId, String productId, int quantity) {
        String reservationKey = orderId + "#" + productId;
        reservations.remove(reservationKey);
        int current = stockLevels.getOrDefault(productId, 0);
        stockLevels.put(productId, current + quantity);
        System.out.printf("[InventoryService] ROLLBACK: Released %d unit(s) of %s for order %s (Restored stock: %d)%n",
                quantity, productId, orderId, stockLevels.get(productId));
    }

    public void commitStock(String orderId, String productId) {
        String reservationKey = orderId + "#" + productId;
        reservations.remove(reservationKey);
        System.out.printf("[InventoryService] Committed stock deduction for order %s (%s)%n",
                orderId, productId);
    }
}
