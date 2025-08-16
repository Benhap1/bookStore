package com.example.book.service.service;

import com.example.book.service.dto.OrderDTO;

import java.util.List;

/**
 * Defines the contract for business operations related to orders and shopping carts.
 * <p>
 * This service orchestrates the entire order lifecycle, from a client adding a book
 * to their cart (a 'DRAFT' order) to submitting it, and an admin confirming or
 * cancelling it. It provides a clear separation between client-facing and
 - * admin-facing functionalities.
 */
public interface OrderService {

    /**
     * Retrieves all of a client's orders that are currently in the 'DRAFT' status.
     * <p>
     * This effectively represents the client's current shopping cart.
     *
     * @param clientEmail The email of the client whose draft orders are to be retrieved.
     * @return A list of {@link OrderDTO} objects in 'DRAFT' status. May be empty.
     */
    List<OrderDTO> getDraftOrdersByClient(String clientEmail);

    /**
     * Retrieves a history of a client's completed or processed orders.
     * <p>
     * This includes all orders that are not in the 'DRAFT' status (e.g., SUBMITTED,
     * CONFIRMED, CANCELLED).
     *
     * @param clientEmail The email of the client whose order history is to be retrieved.
     * @return A list of {@link OrderDTO} objects representing the client's order history.
     */
    List<OrderDTO> getCompletedOrdersByClient(String clientEmail);

    /**
     * Submits a client's draft order for processing.
     * <p>
     * This action transitions an order's status from 'DRAFT' to 'SUBMITTED'. It also
     * performs a critical balance check and deducts the order total from the client's
     * account balance.
     *
     * @param orderId The ID of the draft order to submit.
     * @return An {@link OrderDTO} representing the submitted order.
     */
    OrderDTO submitOrder(Long orderId);

    /**
     * Confirms a submitted order (Admin action).
     * <p>
     * This action transitions an order's status from 'SUBMITTED' to 'CONFIRMED'.
     * This is typically performed by an administrator.
     *
     * @param orderId The ID of the order to confirm.
     * @return An {@link OrderDTO} representing the confirmed order.
     */
    OrderDTO confirmOrder(Long orderId);

    /**
     * Cancels an order.
     * <p>
     * This action transitions an order's status to 'CANCELLED'. It can be performed
     * by an admin or by the client who owns the order. Note: this method does not
     * currently handle refund logic.
     *
     * @param orderId The ID of the order to cancel.
     * @return An {@link OrderDTO} representing the cancelled order.
     */
    OrderDTO cancelOrder(Long orderId);

    /**
     * Retrieves a list of all orders in the system (Admin action).
     *
     * @return A list of all {@link OrderDTO} objects.
     */
    List<OrderDTO> getAllOrders();

    /**
     * Adds a book to a client's shopping cart (draft order).
     * <p>
     * This method finds the client's active draft order. If one doesn't exist, it
     * creates a new one. If the book is already in the cart, its quantity is
     * incremented; otherwise, a new order item is created. The total price of the
     * draft order is recalculated upon every addition.
     *
     * @param bookId      The ID of the book to add to the cart.
     * @param clientEmail The email of the client performing the action.
     */
    void addBookToDraftOrder(Long bookId, String clientEmail);

    /**
     * Searches for all orders belonging to clients whose email matches a keyword (Admin action).
     *
     * @param email The search keyword to match against client emails (case-insensitive).
     * @return A list of {@link OrderDTO} objects matching the search criteria.
     */
    List<OrderDTO> searchOrdersByClientEmail(String email);
}