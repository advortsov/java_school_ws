package com.tsystems.javaschool.dto;

/**
 * @author Alexander Dvortsov
 * @version 1.0
 * @since 26.03.2016
 */
public class BookDTO {

    private String name;
    private String isbn;
    private int actualPrice;
    private int storeCount;
    private int soldCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(int actualPrice) {
        this.actualPrice = actualPrice;
    }

    public int getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(int storeCount) {
        this.storeCount = storeCount;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookDTO bookDTO = (BookDTO) o;

        return isbn.equals(bookDTO.isbn);

    }

    @Override
    public int hashCode() {
        return isbn.hashCode();
    }
}
