package com.tsystems.javaschool.beans;

import com.tsystems.javaschool.service.PdfService;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@ManagedBean
@ViewScoped
public class ReportingBean {

    private Date startDate;
    private Date endDate;
    private String createProceedReport;

    @EJB
    private PdfService pdfService;

    public String getCreateProceedReport() {
        return createProceedReport;
    }

    public void setCreateProceedReport(String createProceedReport) {
        this.createProceedReport = createProceedReport;
    }

    public void createProceedReport() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext ec = context.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) ec.getResponse();
        pdfService.createPdf(response, startDate, endDate);
        context.responseComplete();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public PdfService getPdfService() {
        return pdfService;
    }

    public void setPdfService(PdfService pdfService) {
        this.pdfService = pdfService;
    }
}
