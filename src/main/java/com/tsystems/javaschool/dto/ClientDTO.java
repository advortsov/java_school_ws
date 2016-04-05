package com.tsystems.javaschool.dto;

/**
 * @author Alexander Dvortsov
 * @version 1.0
 * @since 26.03.2016
 */
public class ClientDTO {
    private String clientName;
    private String clientSurname;
    private String clientUserName;
    private String clientEmail;
    private int clientTotal;

    public int getClientTotal() {
        return clientTotal;
    }

    public void setClientTotal(int clientTotal) {
        this.clientTotal = clientTotal;
    }

    //===================
    private int clientPercentage;

    public int getClientPercentage() {
        return clientPercentage;
    }

    public void setClientPercentage(int clientPercentage) {
        this.clientPercentage = clientPercentage;
    }
    //===================

    public ClientDTO() {
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientSurname() {
        return clientSurname;
    }

    public void setClientSurname(String clientSurname) {
        this.clientSurname = clientSurname;
    }

    public String getClientUserName() {
        return clientUserName;
    }

    public void setClientUserName(String clientUserName) {
        this.clientUserName = clientUserName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientDTO clientDTO = (ClientDTO) o;

        return clientUserName.equals(clientDTO.clientUserName);

    }

    @Override
    public int hashCode() {
        return clientUserName.hashCode();
    }
}
