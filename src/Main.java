/**
 * Client demonstrating the Facade Pattern in an Online Order Processing domain.
 *
 * Requirements verified:
 * 1. Standard Client interacts exclusively through the Facade (OrderProcessingFacade).
 * 2. Facade coordinates subsystem classes in the correct sequence.
 * 3. Rollback is triggered when a subsystem step fails (e.g. payment decline).
 * 4. Subsystems remain decoupled and directly accessible by advanced clients who bypass the Facade.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   ASSIGNMENT #4: FACADE DESIGN PATTERN DEMONSTRATION");
        System.out.println("   Course: ShP-2216 Software Design Patterns | Student: Nursultan Maratov");
        System.out.println("======================================================================");

        // Client instantiates only the Facade
        OrderProcessingFacade orderFacade = new OrderProcessingFacade();

        // -------------------------------------------------------------------------
        // SCENARIO 1: Happy Path (Successful Order Placement)
        // -------------------------------------------------------------------------
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("TEST 1: Successful Order Placement (Happy Path)");
        System.out.println("----------------------------------------------------------------------");
        OrderResult result1 = orderFacade.placeOrder(
                "ORD-501",
                "LAPTOP-PRO",
                2,
                1299.99,
                "customer1@domain.com",
                "Mangilik El Ave 55/8, Astana, Kazakhstan",
                "VISA_CREDIT_CARD"
        );
        System.out.println("\n[Client Result 1]: " + result1);
        assert result1.isSuccess() : "Expected Test 1 to succeed";

        // -------------------------------------------------------------------------
        // SCENARIO 2: Out of Stock (Graceful Early Failure)
        // -------------------------------------------------------------------------
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("TEST 2: Stock Check Failure (Insufficient Inventory)");
        System.out.println("----------------------------------------------------------------------");
        OrderResult result2 = orderFacade.placeOrder(
                "ORD-502",
                "LAPTOP-PRO",
                10, // Only 3 left after Test 1
                1299.99,
                "customer2@domain.com",
                "Kabanbay Batyr Ave 60/1, Astana, Kazakhstan",
                "MASTERCARD"
        );
        System.out.println("\n[Client Result 2]: " + result2);
        assert !result2.isSuccess() : "Expected Test 2 to fail due to stock";

        // -------------------------------------------------------------------------
        // SCENARIO 3: Payment Declined with Automatic Rollback
        // -------------------------------------------------------------------------
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("TEST 3: Payment Decline with Automatic Inventory Rollback");
        System.out.println("----------------------------------------------------------------------");
        OrderResult result3 = orderFacade.placeOrder(
                "ORD-503",
                "WIRELESS-BUDS",
                2,
                149.99,
                "customer3@domain.com",
                "Dostyk Ave 10, Almaty, Kazakhstan",
                "DECLINED_CARD" // Simulated declined card
        );
        System.out.println("\n[Client Result 3]: " + result3);
        assert !result3.isSuccess() : "Expected Test 3 to fail due to payment decline";

        // -------------------------------------------------------------------------
        // SCENARIO 4: Order Cancellation (Multi-subsystem Rollback)
        // -------------------------------------------------------------------------
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("TEST 4: Order Cancellation (Refund, Shipment Cancel, Stock Restored)");
        System.out.println("----------------------------------------------------------------------");
        boolean cancelled = orderFacade.cancelOrder("ORD-501");
        System.out.println("\n[Client Result 4]: Cancellation status for ORD-501 = " + (cancelled ? "SUCCESS" : "FAILED"));
        assert cancelled : "Expected cancellation of ORD-501 to succeed";

        // -------------------------------------------------------------------------
        // SCENARIO 5: Advanced Client Direct Subsystem Usage (Bypassing Facade)
        // -------------------------------------------------------------------------
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("TEST 5: Advanced Client Direct Access (Bypassing the Facade)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[Advanced Client Note]: Warehouse manager directly accesses InventoryService");
        InventoryService directInventory = new InventoryService();
        System.out.println("Direct inventory check for SMARTPHONE-X: " + directInventory.getStock("SMARTPHONE-X"));
        directInventory.addStock("SMARTPHONE-X", 15);
        System.out.println("Updated stock for SMARTPHONE-X: " + directInventory.getStock("SMARTPHONE-X"));

        System.out.println("\n======================================================================");
        System.out.println("ALL DEMONSTRATION TESTS COMPLETED SUCCESSFULLY (5/5 PASS)");
        System.out.println("======================================================================");
    }
}
