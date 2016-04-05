package com.tsystems.javaschool.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.tsystems.javaschool.dto.BookDTO;
import com.tsystems.javaschool.dto.ClientDTO;
import com.tsystems.javaschool.dto.OrderDTO;
import com.tsystems.javaschool.dto.OrderLineDTO;
import com.tsystems.javaschool.webservices.ReceiveRSOrders;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Stateless
public class PdfService {

    @EJB
    private ReceiveRSOrders receiveRSOrders;

    public void createPdf(HttpServletResponse response,
                          Date startDate,
                          Date endDate) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDateString = dateFormat.format(startDate);
        String endDateString = dateFormat.format(endDate);

        System.out.println("startDateString = " + startDateString);

        String fileName = "proceed_" + startDateString + "_" + endDateString + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        List<OrderDTO> orders = receiveRSOrders.getOrdersPerPeriod(startDate, endDate);

        Document document = new Document(PageSize.A4, 30, 30, 30, 30);

        populateDocument(document, orders, response, startDateString, endDateString);
    }

    private void populateDocument(Document document, List<OrderDTO> orders, HttpServletResponse response,
                                  String startDateString, String endDateString) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = null;
        try {
            PdfWriter.getInstance(document, baos);

            document.open();

            Paragraph paragraph1 = new Paragraph("This is proceed report for period from "
                    + startDateString + " to " + endDateString + "):", FontFactory.getFont(FontFactory.HELVETICA,
                    14, Font.NORMAL, new CMYKColor(0, 255, 0, 0)));
            document.add(paragraph1);

            Paragraph paragraph2 = new Paragraph("Orders per period ("
                    + startDateString + " - " + endDateString + "):", FontFactory.getFont(FontFactory.HELVETICA,
                    12, Font.BOLDITALIC, new CMYKColor(0, 255, 0, 0)));
            document.add(paragraph2);

            document.add(createOrdersTable(orders));

            Paragraph paragraph3 = new Paragraph("Top-10 clients per period ("
                    + startDateString + " - " + endDateString + "):", FontFactory.getFont(FontFactory.HELVETICA,
                    12, Font.BOLDITALIC, new CMYKColor(0, 255, 0, 0)));
            document.add(paragraph3);

            document.add(createClientsTable(orders));

            Paragraph paragraph4 = new Paragraph("Top-10 books per period ("
                    + startDateString + " - " + endDateString + "):", FontFactory.getFont(FontFactory.HELVETICA,
                    12, Font.BOLDITALIC, new CMYKColor(0, 255, 0, 0)));
            document.add(paragraph4);

            document.add(createBooksTable(orders));

            document.close();

            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            response.setContentLength(baos.size());

            os = response.getOutputStream();
            baos.writeTo(os);
            os.flush();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private PdfPTable createBooksTable(List<OrderDTO> orders) {
        // top 10 books table:
        PdfPTable table = new PdfPTable(6);
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);

        PdfPCell cell1 = new PdfPCell(new Phrase(Font.BOLD, "Name"));
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(Font.BOLD, "ISBN"));
        table.addCell(cell2);

        PdfPCell cell3 = new PdfPCell(new Phrase(Font.BOLD, "Actual price"));
        table.addCell(cell3);

        PdfPCell cell4 = new PdfPCell(new Phrase(Font.BOLD, "Count in store"));
        table.addCell(cell4);

        PdfPCell cell5 = new PdfPCell(new Phrase(Font.BOLD, "Sold count"));
        table.addCell(cell5);

        PdfPCell cell6 = new PdfPCell(new Phrase(Font.BOLD, "Percentage, %"));
        table.addCell(cell6);


        List<BookDTO> allSoldBooks = new ArrayList<>();

        for (OrderDTO orderDTO : orders) {
            for (OrderLineDTO line : orderDTO.getItems()) {
                BookDTO bookDTO = new BookDTO();
                bookDTO.setIsbn(line.getBookDTO().getIsbn());

                // fill sold books count from orderlines from ordersDTO
                if (allSoldBooks.contains(bookDTO)) {
                    BookDTO curr = allSoldBooks.get(allSoldBooks.indexOf(bookDTO));
                    int bookTotalSold = curr.getSoldCount();
                    bookTotalSold += line.getCount();
                    curr.setSoldCount(bookTotalSold);
                } else {
                    bookDTO.setStoreCount(line.getBookDTO().getStoreCount());
                    bookDTO.setName(line.getBookDTO().getName());
                    bookDTO.setActualPrice(line.getBookDTO().getActualPrice());

                    allSoldBooks.add(bookDTO);
                }
            }
        }

        // sorting by total summ descending
        allSoldBooks.sort(new Comparator<BookDTO>() {

            @Override
            public int compare(BookDTO o1, BookDTO o2) {
                if (o1.getSoldCount() < o2.getSoldCount()) return 1;
                else if (o1.getSoldCount() > o2.getSoldCount()) return -1;
                else return 0;
            }
        });

        int totalSummOfAllSoldBooks = 0;
        for (BookDTO book : allSoldBooks) {
            totalSummOfAllSoldBooks += book.getSoldCount();
        }

        // filling table
        if (allSoldBooks.size() < 10) {
            for (BookDTO book : allSoldBooks) {
                table.addCell(book.getName());
                table.addCell(book.getIsbn());
                table.addCell(String.valueOf(book.getActualPrice()));
                table.addCell(String.valueOf(book.getStoreCount()));
                table.addCell(String.valueOf(book.getSoldCount()));
                table.addCell(String.valueOf(getPercent((double) book.getSoldCount(),
                        totalSummOfAllSoldBooks)));
            }
        } else {
            for (int i = 0; i < 10; i++) {
                table.addCell(allSoldBooks.get(i).getName());
                table.addCell(allSoldBooks.get(i).getIsbn());
                table.addCell(String.valueOf(allSoldBooks.get(i).getActualPrice()));
                table.addCell(String.valueOf(allSoldBooks.get(i).getStoreCount()));
                table.addCell(String.valueOf(allSoldBooks.get(i).getSoldCount()));
                table.addCell(String.valueOf(getPercent((double) allSoldBooks.get(i).getSoldCount(),
                        totalSummOfAllSoldBooks)));
            }
        }

        PdfPCell cell = new PdfPCell(new Phrase("SUMMARY, sold count: " + totalSummOfAllSoldBooks ));
        cell.setColspan(6); // an entire row
        table.addCell(cell);

        return table;
    }

    private PdfPTable createClientsTable(List<OrderDTO> orders) {
        // top 10 clients table:
        PdfPTable table = new PdfPTable(6);
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);

        PdfPCell clientsCell1 = new PdfPCell(new Phrase(Font.BOLD, "Name"));
        table.addCell(clientsCell1);

        PdfPCell clientsCell2 = new PdfPCell(new Phrase(Font.BOLD, "Surname"));
        table.addCell(clientsCell2);

        PdfPCell clientsCell3 = new PdfPCell(new Phrase(Font.BOLD, "Username"));
        table.addCell(clientsCell3);

        PdfPCell clientsCell4 = new PdfPCell(new Phrase(Font.BOLD, "E-mail"));
        table.addCell(clientsCell4);

        PdfPCell clientsCell5 = new PdfPCell(new Phrase(Font.BOLD, "Total, rub."));
        table.addCell(clientsCell5);

        PdfPCell clientsCell6 = new PdfPCell(new Phrase(Font.BOLD, "Percentage, %"));
        table.addCell(clientsCell6);

        List<ClientDTO> allClients = new ArrayList<>();

        for (OrderDTO orderDTO : orders) {
            ClientDTO clientDTO = new ClientDTO();
            clientDTO.setClientUserName(orderDTO.getClientUserName());

            // fill clients summs from ordersDTO
            if (allClients.contains(clientDTO)) {
                ClientDTO curr = allClients.get(allClients.indexOf(clientDTO));
                int clientTotalSumm = curr.getClientTotal();
                clientTotalSumm += orderDTO.getTotalSumm();
                curr.setClientTotal(clientTotalSumm);
            } else {
                clientDTO.setClientUserName(orderDTO.getClientUserName());
                clientDTO.setClientName(orderDTO.getClientName());
                clientDTO.setClientSurname(orderDTO.getClientSurname());
                clientDTO.setClientEmail(orderDTO.getClientEmail());
                clientDTO.setClientTotal(orderDTO.getTotalSumm());
                allClients.add(clientDTO);
            }
        }

        // sorting by total summ descending
        allClients.sort(new Comparator<ClientDTO>() {
            @Override
            public int compare(ClientDTO o1, ClientDTO o2) {
                if (o1.getClientTotal() < o2.getClientTotal()) return 1;
                else if (o1.getClientTotal() > o2.getClientTotal()) return -1;
                else return 0;
            }
        });

        int totalSummOfAllClientsOrders = 0;
        for (ClientDTO client : allClients) {
            totalSummOfAllClientsOrders += client.getClientTotal();
        }

        // filling table
        if (allClients.size() < 10) {
            for (ClientDTO client : allClients) {
                table.addCell(client.getClientName());
                table.addCell(client.getClientSurname());
                table.addCell(client.getClientUserName());
                table.addCell(client.getClientEmail());
                table.addCell(String.valueOf(client.getClientTotal()));
                table.addCell(String.valueOf(getPercent((double) client.getClientTotal(),
                        totalSummOfAllClientsOrders)));
            }
        } else {
            for (int i = 0; i < 10; i++) {
                table.addCell(allClients.get(i).getClientName());
                table.addCell(allClients.get(i).getClientSurname());
                table.addCell(allClients.get(i).getClientUserName());
                table.addCell(allClients.get(i).getClientEmail());
                table.addCell(String.valueOf(allClients.get(i).getClientTotal()));
                table.addCell(String.valueOf(getPercent((double) allClients.get(i).getClientTotal(),
                        totalSummOfAllClientsOrders)));
            }
        }

        PdfPCell cell = new PdfPCell(new Phrase("SUMMARY, rub.: " + totalSummOfAllClientsOrders ));
        cell.setColspan(6); // an entire row
        table.addCell(cell);

        return table;
    }

    private PdfPTable createOrdersTable(List<OrderDTO> orders) {

        PdfPTable table = new PdfPTable(6);
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);

        PdfPCell cell1 = new PdfPCell(new Phrase(Font.BOLD, "Date"));
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(Font.BOLD, "Name"));
        table.addCell(cell2);

        PdfPCell cell3 = new PdfPCell(new Phrase(Font.BOLD, "Surname"));
        table.addCell(cell3);

        PdfPCell cell4 = new PdfPCell(new Phrase(Font.BOLD, "Username"));
        table.addCell(cell4);

        PdfPCell cell5 = new PdfPCell(new Phrase(Font.BOLD, "E-mail"));
        table.addCell(cell5);

        PdfPCell cell6 = new PdfPCell(new Phrase(Font.BOLD, "Total, rub."));
        table.addCell(cell6);

        int totalProceedPerPeriod = 0;

        for (OrderDTO dto : orders) {
            table.addCell(dto.getDate());
            table.addCell(dto.getClientName());
            table.addCell(dto.getClientSurname());
            table.addCell(dto.getClientUserName());
            table.addCell(dto.getClientEmail());
            table.addCell(String.valueOf(dto.getTotalSumm()));

            totalProceedPerPeriod += dto.getTotalSumm();
        }

        PdfPCell cell = new PdfPCell(new Phrase("SUMMARY, rub.: " + totalProceedPerPeriod ));
        cell.setColspan(6); // an entire row
        table.addCell(cell);

        return table;
    }

    /**
     * Returns formatted percent value
     *
     * @return the formatted value as string
     */
    private String getPercent(double clientTotal, int totalSummOfAllClientsOrders) {
        double num = (clientTotal / totalSummOfAllClientsOrders) * 100;
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH); // ?????????? ???? ??????, ????? ????? ??????????? ??????
        formatter.setMaximumFractionDigits(2); // ???????? ??????? 4 ????? ????? ?????
        formatter.setMinimumFractionDigits(0); // ???? ?? ???, ?? ?????? ????? ????? ?????
        return formatter.format(num);
    }
}
