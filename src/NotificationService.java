/**
 * Subsystem component responsible for communicating with customers via notifications
 * (email, SMS, receipts).
 *
 * Note: Subsystem is unaware of the Facade and can be directly used by customer support
 * or automated notification alerts.
 */
public class NotificationService {

    public void sendOrderConfirmation(String email, String orderId, String trackingNumber, double totalAmount) {
        System.out.printf("[NotificationService] EMAIL -> %s: Order %s CONFIRMED. Total: $%.2f. Tracking: %s%n",
                email, orderId, totalAmount, trackingNumber);
    }

    public void sendPaymentFailureNotification(String email, String orderId, String reason) {
        System.out.printf("[NotificationService] ALERT -> %s: Payment for Order %s FAILED (%s). No funds charged.%n",
                email, orderId, reason);
    }

    public void sendCancellationNotification(String email, String orderId, double refundAmount) {
        System.out.printf("[NotificationService] EMAIL -> %s: Order %s CANCELLED. Refund of $%.2f issued.%n",
                email, orderId, refundAmount);
    }
}
