import java.util.HashMap;
import java.util.Map;

/**
 * Subsystem component responsible for order dispatch, courier coordination,
 * tracking generation, and shipping cancellation.
 *
 * Note: Subsystem is unaware of the Facade and can be directly used by logistics
 * or courier services.
 */
public class ShippingService {

    private final Map<String, String> orderTrackingMap = new HashMap<>(); // orderId -> trackingNumber
    private final Map<String, String> trackingAddressMap = new HashMap<>(); // trackingNumber -> address
    private int trackingCounter = 1000;

    public String dispatchOrder(String orderId, String destinationAddress) {
        if (destinationAddress == null || destinationAddress.trim().isEmpty()) {
            System.out.printf("[ShippingService] Dispatch FAILED: invalid address for order %s%n", orderId);
            return null;
        }

        String trackingNumber = "TRK-KZ-" + (++trackingCounter);
        orderTrackingMap.put(orderId, trackingNumber);
        trackingAddressMap.put(trackingNumber, destinationAddress);

        System.out.printf("[ShippingService] Dispatched order %s -> %s (Tracking: %s)%n",
                orderId, destinationAddress, trackingNumber);
        return trackingNumber;
    }

    public boolean cancelShipment(String trackingNumber) {
        if (trackingNumber == null || !trackingAddressMap.containsKey(trackingNumber)) {
            System.out.printf("[ShippingService] Cancellation FAILED: unknown tracking %s%n", trackingNumber);
            return false;
        }

        trackingAddressMap.remove(trackingNumber);
        // Remove from order tracking map
        orderTrackingMap.values().removeIf(t -> t.equals(trackingNumber));
        System.out.printf("[ShippingService] ROLLBACK: Cancelled courier dispatch for tracking %s%n", trackingNumber);
        return true;
    }

    public String getTrackingNumber(String orderId) {
        return orderTrackingMap.get(orderId);
    }
}
