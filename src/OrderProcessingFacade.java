import java.util.HashMap;
import java.util.Map;

public class OrderProcessingFacade {

    private final InventoryService inventoryService;
    private final PaymentProcessor paymentProcessor;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    private final Map<String, ActiveOrderRecord> activeOrders = new HashMap<>();

    public OrderProcessingFacade() {
        this(new InventoryService(), new PaymentProcessor(), new ShippingService(), new NotificationService());
    }

    public OrderProcessingFacade(InventoryService inventoryService,
                                 PaymentProcessor paymentProcessor,
                                 ShippingService shippingService,
                                 NotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentProcessor = paymentProcessor;
        this.shippingService = shippingService;
        this.notificationService = notificationService;
    }

    public OrderResult placeOrder(String orderId,
                                  String productId,
                                  int quantity,
                                  double unitPrice,
                                  String customerEmail,
                                  String shippingAddress,
                                  String paymentMethod) {

        System.out.printf("%n>>> [OrderProcessingFacade] Starting order %s for %s (%d unit(s)) <<<%n",
                orderId, productId, quantity);

        boolean stockReserved = inventoryService.reserveStock(orderId, productId, quantity);
        if (!stockReserved) {
            return OrderResult.failure(orderId, "Product " + productId + " is out of stock or requested quantity unavailable.");
        }

        double totalAmount = quantity * unitPrice;
        boolean paymentSuccess = paymentProcessor.processPayment(orderId, totalAmount, paymentMethod);
        if (!paymentSuccess) {
            inventoryService.releaseStock(orderId, productId, quantity);
            notificationService.sendPaymentFailureNotification(customerEmail, orderId, "Payment declined by provider");
            return OrderResult.failure(orderId, "Payment declined for order " + orderId + ". Reserved inventory has been released.");
        }

        String trackingNumber = shippingService.dispatchOrder(orderId, shippingAddress);
        if (trackingNumber == null) {
            paymentProcessor.refundPayment(orderId);
            inventoryService.releaseStock(orderId, productId, quantity);
            notificationService.sendPaymentFailureNotification(customerEmail, orderId, "Shipping dispatch failed");
            return OrderResult.failure(orderId, "Shipping dispatch failed. Payment refunded and stock released.");
        }

        inventoryService.commitStock(orderId, productId);
        notificationService.sendOrderConfirmation(customerEmail, orderId, trackingNumber, totalAmount);

        activeOrders.put(orderId, new ActiveOrderRecord(orderId, productId, quantity, totalAmount, customerEmail, trackingNumber));

        System.out.printf(">>> [OrderProcessingFacade] Order %s COMPLETED SUCCESSFULLY <<<%n", orderId);
        return OrderResult.success(orderId, trackingNumber, totalAmount, "Order completed and dispatched.");
    }

    public boolean cancelOrder(String orderId) {
        System.out.printf("%n>>> [OrderProcessingFacade] Processing cancellation for order %s <<<%n", orderId);

        ActiveOrderRecord record = activeOrders.get(orderId);
        if (record == null) {
            System.out.printf("[OrderProcessingFacade] Cannot cancel order %s: order not found or already cancelled.%n", orderId);
            return false;
        }

        shippingService.cancelShipment(record.trackingNumber);
        paymentProcessor.refundPayment(orderId);
        inventoryService.releaseStock(orderId, record.productId, record.quantity);
        notificationService.sendCancellationNotification(record.customerEmail, orderId, record.totalAmount);

        activeOrders.remove(orderId);
        System.out.printf(">>> [OrderProcessingFacade] Order %s CANCELLED SUCCESSFULLY <<<%n", orderId);
        return true;
    }

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
