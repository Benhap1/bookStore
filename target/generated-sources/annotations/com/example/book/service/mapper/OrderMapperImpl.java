package com.example.book.service.mapper;

import com.example.book.service.dto.BookItemDTO;
import com.example.book.service.dto.OrderDTO;
import com.example.book.service.model.Book;
import com.example.book.service.model.BookItem;
import com.example.book.service.model.Client;
import com.example.book.service.model.Order;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-09T22:14:28+0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderDTO toDTO(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderDTO.OrderDTOBuilder orderDTO = OrderDTO.builder();

        orderDTO.clientEmail( orderClientEmail( order ) );
        orderDTO.clientBalance( orderClientBalance( order ) );
        orderDTO.id( order.getId() );
        orderDTO.orderDate( order.getOrderDate() );
        orderDTO.price( order.getPrice() );
        orderDTO.bookItems( bookItemListToBookItemDTOList( order.getBookItems() ) );
        orderDTO.status( order.getStatus() );

        return orderDTO.build();
    }

    @Override
    public Order toEntity(OrderDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        return order.build();
    }

    @Override
    public BookItemDTO bookItemToBookItemDTO(BookItem bookItem) {
        if ( bookItem == null ) {
            return null;
        }

        BookItemDTO.BookItemDTOBuilder bookItemDTO = BookItemDTO.builder();

        bookItemDTO.id( bookItem.getId() );
        bookItemDTO.name( bookItemBookName( bookItem ) );
        bookItemDTO.author( bookItemBookAuthor( bookItem ) );
        bookItemDTO.price( bookItemBookPrice( bookItem ) );
        bookItemDTO.quantity( bookItem.getQuantity() );

        return bookItemDTO.build();
    }

    private String orderClientEmail(Order order) {
        Client client = order.getClient();
        if ( client == null ) {
            return null;
        }
        return client.getEmail();
    }

    private BigDecimal orderClientBalance(Order order) {
        Client client = order.getClient();
        if ( client == null ) {
            return null;
        }
        return client.getBalance();
    }

    protected List<BookItemDTO> bookItemListToBookItemDTOList(List<BookItem> list) {
        if ( list == null ) {
            return null;
        }

        List<BookItemDTO> list1 = new ArrayList<BookItemDTO>( list.size() );
        for ( BookItem bookItem : list ) {
            list1.add( bookItemToBookItemDTO( bookItem ) );
        }

        return list1;
    }

    private String bookItemBookName(BookItem bookItem) {
        Book book = bookItem.getBook();
        if ( book == null ) {
            return null;
        }
        return book.getName();
    }

    private String bookItemBookAuthor(BookItem bookItem) {
        Book book = bookItem.getBook();
        if ( book == null ) {
            return null;
        }
        return book.getAuthor();
    }

    private BigDecimal bookItemBookPrice(BookItem bookItem) {
        Book book = bookItem.getBook();
        if ( book == null ) {
            return null;
        }
        return book.getPrice();
    }
}
