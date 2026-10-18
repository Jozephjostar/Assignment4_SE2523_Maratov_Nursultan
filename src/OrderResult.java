/**
 * Encapsulates the high-level outcome of an order operation.
 * Prevents leaking low-level subsystem data structures or exception types to the client.
 */
public class OrderResult {

    private final boolean success;
    private final String orderId;
    private final String trackingNumber;
    private final double totalAmount;
    private final String message;

    private OrderResult(boolean success, String orderId, String trackingNumber, double totalAmount, String message) {
        this.success = success;
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.totalAmount = totalAmount;
        this.message = message;
    }

    public static OrderResult success(String orderId, String trackingNumber, double totalAmount, String message) {
        return new OrderResult(true, orderId, trackingNumber, totalAmount, message);
    }

    public static OrderResult failure(String orderId, String message) {
        return new OrderResult(false, orderId, null, 0.0, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("OrderResult[SUCCESS | OrderId: %s | Tracking: %s | Total: $%.2f | Info: %s]",
                    orderId, trackingNumber, totalAmount, message);
        } else {
            return String.format("OrderResult[FAILED | OrderId: %s | Reason: %s]",
                    orderId, message);
        }
    }
}
