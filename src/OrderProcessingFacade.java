import java.util.HashMap;
import java.util.Map;

/**
 * Facade class that simplifies online order processing by coordinating
 * multiple subsystem services (InventoryService, PaymentProcessor,
 * ShippingService, NotificationService).
 *
 * It provides a simple, high-level API (placeOrder, cancelOrder) and handles
 * the orchestration sequence, validation, and rollback on failure.
 */
public class OrderProcessingFacade {

    private final InventoryService inventoryService;
    private final PaymentProcessor paymentProcessor;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    // Registry of confirmed active orders to support coordinated cancellation
    private final Map<String, ActiveOrderRecord> activeOrders = new HashMap<>();

    /**
     * Default constructor instantiating the standard subsystem components.
     */
    public OrderProcessingFacade() {
        this(new InventoryService(), new PaymentProcessor(), new ShippingService(), new NotificationService());
    }

    /**
     * Constructor allowing dependency injection (e.g. for testing or custom configuration).
     */
    public OrderProcessingFacade(InventoryService inventoryService,
                                 PaymentProcessor paymentProcessor,
                                 ShippingService shippingService,
                                 NotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentProcessor = paymentProcessor;
        this.shippingService = shippingService;
        this.notificationService = notificationService;
    }

    /**
     * High-level method coordinating the entire ordering workflow:
     * 1. Stock check & reservation
     * 2. Payment processing
     * 3. Shipping dispatch
     * 4. Confirmation notification
     *
     * In case of any intermediate failure, a compensating rollback is performed.
     */
    public OrderResult placeOrder(String orderId,
                                  String productId,
                                  int quantity,
                                  double unitPrice,
                                  String customerEmail,
                                  String shippingAddress,
                                  String paymentMethod) {

        System.out.printf("%n>>> [OrderProcessingFacade] Starting order %s for %s (%d unit(s)) <<<%n",
                orderId, productId, quantity);

        // Step 1: Inventory check and reservation
        boolean stockReserved = inventoryService.reserveStock(orderId, productId, quantity);
        if (!stockReserved) {
            return OrderResult.failure(orderId, "Product " + productId + " is out of stock or requested quantity unavailable.");
        }

        // Step 2: Payment processing
        double totalAmount = quantity * unitPrice;
        boolean paymentSuccess = paymentProcessor.processPayment(orderId, totalAmount, paymentMethod);
        if (!paymentSuccess) {
            // Rollback Step 1: release reserved stock
            inventoryService.releaseStock(orderId, productId, quantity);
            notificationService.sendPaymentFailureNotification(customerEmail, orderId, "Payment declined by provider");
            return OrderResult.failure(orderId, "Payment declined for order " + orderId + ". Reserved inventory has been released.");
        }

        // Step 3: Shipping dispatch
        String trackingNumber = shippingService.dispatchOrder(orderId, shippingAddress);
        if (trackingNumber == null) {
            // Rollback Step 2 and Step 1: refund payment and release stock
            paymentProcessor.refundPayment(orderId);
            inventoryService.releaseStock(orderId, productId, quantity);
            notificationService.sendPaymentFailureNotification(customerEmail, orderId, "Shipping dispatch failed");
            return OrderResult.failure(orderId, "Shipping dispatch failed. Payment refunded and stock released.");
        }

        // Step 4: Finalize reservation and notify customer
        inventoryService.commitStock(orderId, productId);
        notificationService.sendOrderConfirmation(customerEmail, orderId, trackingNumber, totalAmount);

        // Save record for potential future cancellation
        activeOrders.put(orderId, new ActiveOrderRecord(orderId, productId, quantity, totalAmount, customerEmail, trackingNumber));

        System.out.printf(">>> [OrderProcessingFacade] Order %s COMPLETED SUCCESSFULLY <<<%n", orderId);
        return OrderResult.success(orderId, trackingNumber, totalAmount, "Order completed and dispatched.");
    }

    /**
     * High-level cancellation orchestrating rollback across all subsystems:
     * 1. Cancels courier shipment
     * 2. Refunds customer payment
     * 3. Restores inventory stock
     * 4. Sends cancellation notification
     */
    public boolean cancelOrder(String orderId) {
        System.out.printf("%n>>> [OrderProcessingFacade] Processing cancellation for order %s <<<%n", orderId);

        ActiveOrderRecord record = activeOrders.get(orderId);
        if (record == null) {
            System.out.printf("[OrderProcessingFacade] Cannot cancel order %s: order not found or already cancelled.%n", orderId);
            return false;
        }

        // Step 1: Cancel shipping
        shippingService.cancelShipment(record.trackingNumber);

        // Step 2: Refund payment
        paymentProcessor.refundPayment(orderId);

        // Step 3: Release stock back to inventory
        inventoryService.releaseStock(orderId, record.productId, record.quantity);

        // Step 4: Send cancellation email
        notificationService.sendCancellationNotification(record.customerEmail, orderId, record.totalAmount);

        activeOrders.remove(orderId);
        System.out.printf(">>> [OrderProcessingFacade] Order %s CANCELLED SUCCESSFULLY <<<%n", orderId);
        return true;
    }

    /**
     * Internal record to preserve active order metadata for cancellation rollback.
     */
    private static class ActiveOrderRecord {
        final String orderId;
        final String productId;
        final int quantity;
        final double totalAmount;
        final String customerEmail;
        final String trackingNumber;

        ActiveOrderRecord(String orderId, String productId, int quantity, double totalAmount,
                          String customerEmail, String trackingNumber) {
            this.orderId = orderId;
            this.productId = productId;
            this.quantity = quantity;
            this.totalAmount = totalAmount;
            this.customerEmail = customerEmail;
            this.trackingNumber = trackingNumber;
        }
    }
}
