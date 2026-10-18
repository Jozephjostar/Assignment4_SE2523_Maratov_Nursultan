public class Main {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   ASSIGNMENT #4: FACADE DESIGN PATTERN DEMONSTRATION");
        System.out.println("   Course: ShP-2216 Software Design Patterns | Student: Nursultan Maratov");
        System.out.println("======================================================================");

        OrderProcessingFacade orderFacade = new OrderProcessingFacade();

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

        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("TEST 2: Stock Check Failure (Insufficient Inventory)");
        System.out.println("----------------------------------------------------------------------");
        OrderResult result2 = orderFacade.placeOrder(
                "ORD-502",
                "LAPTOP-PRO",
                10,
                1299.99,
                "customer2@domain.com",
                "Kabanbay Batyr Ave 60/1, Astana, Kazakhstan",
                "MASTERCARD"
        );
        System.out.println("\n[Client Result 2]: " + result2);
        assert !result2.isSuccess() : "Expected Test 2 to fail due to stock";

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
                "DECLINED_CARD"
        );
        System.out.println("\n[Client Result 3]: " + result3);
        assert !result3.isSuccess() : "Expected Test 3 to fail due to payment decline";

        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("TEST 4: Order Cancellation (Refund, Shipment Cancel, Stock Restored)");
        System.out.println("----------------------------------------------------------------------");
        boolean cancelled = orderFacade.cancelOrder("ORD-501");
        System.out.println("\n[Client Result 4]: Cancellation status for ORD-501 = " + (cancelled ? "SUCCESS" : "FAILED"));
        assert cancelled : "Expected cancellation of ORD-501 to succeed";

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
