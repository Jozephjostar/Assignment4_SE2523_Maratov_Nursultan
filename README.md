# Assignment 4: Facade Design Pattern

- **Course:** ShP-2216 – Software Design Patterns (OP 6B06102)
- **Student:** Nursultan Maratov
- **Group:** SE-2523
- **Topic:** Option B — Online Order Processing Facade
- **Language:** Java 17 (Pure Java SE, IntelliJ IDEA compatible)
- **Repository URL:** https://github.com/nursultanmaratov/Assignment4-FacadePattern

---

## 1. Project Overview & Architectural Role Map

The purpose of this assignment is to implement the **Facade Structural Design Pattern** in Java. The Facade provides a simplified, high-level interface to a complex subsystem of four collaborating e-commerce services, handling multi-step orchestration and automatic compensating transactions (rollback) upon subsystem failures.

| Role | Class / Component | Source Path | Description |
|---|---|---|---|
| **Facade** | `OrderProcessingFacade` | `src/OrderProcessingFacade.java` | Central coordinator exposing a simplified API (`placeOrder`, `cancelOrder`). Orchestrates subsystem operations and executes rollback on errors. |
| **Subsystem 1** | `InventoryService` | `src/InventoryService.java` | Manages product stock, checks availability, reserves inventory, and releases stock upon rollback. Unaware of Facade. |
| **Subsystem 2** | `PaymentProcessor` | `src/PaymentProcessor.java` | Simulates payment gateway authorization and processes refunds. Unaware of Facade. |
| **Subsystem 3** | `ShippingService` | `src/ShippingService.java` | Coordinates order dispatch, generates carrier tracking codes (`TRK-KZ-xxxx`), and cancels courier shipments. Unaware of Facade. |
| **Subsystem 4** | `NotificationService` | `src/NotificationService.java` | Sends confirmation receipts, payment failure alerts, and cancellation notices. Unaware of Facade. |
| **DTO / Return Object** | `OrderResult` | `src/OrderResult.java` | Immutable result object containing operation status, tracking ID, amount, and user-facing messages. Prevents subsystem leaking. |
| **Client** | `Main` | `src/Main.java` | Standard client that executes exclusively against `OrderProcessingFacade` to run test scenarios 1–4, and demonstrates direct subsystem access in test 5. |

---

## 2. Technical & Clean Code Highlights

1. **Subsystem Independence (Decoupling):**
   Subsystem classes maintain **zero back-references** to `OrderProcessingFacade`. Advanced clients (e.g., warehouse inventory managers or logistics coordinators) can bypass the Facade and use subsystem classes directly (demonstrated in Scenario 5).
2. **Single Responsibility Principle (SRP):**
   `OrderProcessingFacade` only *orchestrates* the workflow sequence; it does not reimplement inventory counts, credit card algorithms, or courier routing.
3. **Information Hiding (No Subsystem Leakage):**
   The client interacts strictly with high-level arguments and receives an `OrderResult` DTO. It never handles warehouse reservation keys, carrier tracking internal states, or raw billing tokens.
4. **Resilient Error Handling & Compensating Transactions (Rollback):**
   If payment fails, reserved warehouse stock is immediately released (`inventoryService.releaseStock`) and a failure notification is triggered. If cancellation occurs, courier dispatch is aborted, funds refunded, and inventory replenished.

---

## 3. How to Open and Run in IntelliJ IDEA

### In IntelliJ IDEA:
1. Open IntelliJ IDEA $\rightarrow$ **File** $\rightarrow$ **Open...**
2. Select the `Assignment4_SE2523_Maratov_Nursultan` folder.
3. IntelliJ will automatically detect the project structure via `.idea` and `Assignment4_SE2523_Maratov_Nursultan.iml`.
4. Ensure Project SDK is set to **Java 17** (or 21) in **File $\rightarrow$ Project Structure $\rightarrow$ Project**.
5. Open `src/Main.java` and click the green **Run** arrow next to `public static void main(String[] args)`.

### From Command Line:
```bash
# Navigate to project directory
cd Assignment4_SE2523_Maratov_Nursultan

# Compile using Java 17
javac --release 17 -encoding UTF-8 -d out @sources.txt

# Run with assertions enabled
java -ea -cp out Main
```

---

## 4. Test Scenarios & Verification Outcomes

| Test | Scenario | Orchestration Sequence | Result |
|---|---|---|---|
| **Test 1** | **Happy Path** (Valid Order) | Check stock $\rightarrow$ Reserve $\rightarrow$ Charge card $\rightarrow$ Dispatch shipping $\rightarrow$ Commit stock $\rightarrow$ Send email | `PASS` (Success, Tracking `TRK-KZ-1001`) |
| **Test 2** | **Out of Stock** | Check stock $\rightarrow$ Insufficient $\rightarrow$ Fast-fail before charging | `PASS` (Clean failure, 0 side-effects) |
| **Test 3** | **Payment Declined & Rollback** | Reserve stock $\rightarrow$ Payment declined $\rightarrow$ **Rollback:** Release stock $\rightarrow$ Notify customer | `PASS` (Stock restored from 0 to 2) |
| **Test 4** | **Order Cancellation** | Retrieve order $\rightarrow$ Cancel shipment $\rightarrow$ Refund money $\rightarrow$ Return stock $\rightarrow$ Cancellation email | `PASS` (Full rollback executed) |
| **Test 5** | **Advanced Direct Subsystem Access** | Direct call to `InventoryService` bypassing Facade | `PASS` (Subsystem completely usable independently) |

---

## 5. Defense Quick Reference

1. **What is the Facade pattern?**
   A structural design pattern that provides a simplified, higher-level interface to a complex subsystem of classes, making the subsystem easier to use without hiding it from advanced clients.
2. **What does `OrderProcessingFacade` hide from the client?**
   - The ordering sequence (order of operations).
   - Temporary reservation states and rollback logic.
   - Internal dependencies between inventory, billing, dispatch, and emails.
3. **What remains accessible to advanced clients?**
   All subsystem classes (`InventoryService`, `PaymentProcessor`, `ShippingService`, `NotificationService`) have `public` interfaces and remain directly accessible without going through the Facade.
4. **Facade vs Adapter vs Mediator:**
   - **Facade:** Simplifies an existing complex subsystem with a new, unified interface.
   - **Adapter:** Converts one existing incompatible interface into another expected interface.
   - **Mediator:** Centralizes multidirectional communication between peer components that know about the mediator.
