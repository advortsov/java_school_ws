package com.tsystems.javaschool.dto;

/**
 * @author Alexander Dvortsov
 * @version 1.0
 * @since 26.03.2016
 */
public class OrderLineDTO {
//    private String name;

    private BookDTO bookDTO;
    private int count;


    public BookDTO getBookDTO() {
        return bookDTO;
    }

    public void setBookDTO(BookDTO bookDTO) {
        this.bookDTO = bookDTO;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
