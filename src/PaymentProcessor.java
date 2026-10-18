import java.util.HashMap;
import java.util.Map;

/**
 * Subsystem component responsible for transaction billing, payment verification,
 * and processing refunds.
 *
 * Note: Subsystem is unaware of the Facade and can be directly used by financial
 * or accounting modules.
 */
public class PaymentProcessor {

    private final Map<String, Double> settledPayments = new HashMap<>(); // orderId -> amount

    public boolean processPayment(String orderId, double amount, String paymentMethod) {
        System.out.printf("[PaymentProcessor] Processing payment: order=%s, amount=$%.2f, method=%s%n",
                orderId, amount, paymentMethod);

        if (amount <= 0) {
            System.out.printf("[PaymentProcessor] Payment FAILED: invalid amount $%.2f%n", amount);
            return false;
        }

        // Realistic failure simulation when card is declined / invalid
        if (paymentMethod != null && paymentMethod.equalsIgnoreCase("DECLINED_CARD")) {
            System.out.printf("[PaymentProcessor] Payment DECLINED: insufficient funds or blocked card for order %s%n", orderId);
            return false;
        }

        settledPayments.put(orderId, amount);
        System.out.printf("[PaymentProcessor] Payment of $%.2f SUCCESSFUL for order %s%n", amount, orderId);
        return true;
    }

    public boolean refundPayment(String orderId) {
        if (!settledPayments.containsKey(orderId)) {
            System.out.printf("[PaymentProcessor] Refund FAILED: No record found for order %s%n", orderId);
            return false;
        }

        double refundedAmount = settledPayments.remove(orderId);
        System.out.printf("[PaymentProcessor] ROLLBACK: Refunded $%.2f for order %s%n", refundedAmount, orderId);
        return true;
    }

    public boolean isPaymentCompleted(String orderId) {
        return settledPayments.containsKey(orderId);
    }
}
