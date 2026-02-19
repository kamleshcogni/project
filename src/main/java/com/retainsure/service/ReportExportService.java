package com.retainsure.service;

import com.retainsure.model.RetentionReport;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReportExportService {

    private final ReportService reportService;

    public ReportExportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public byte[] exportCsv() {
        List<RetentionReport> reports = reportService.listAll();
        StringBuilder sb = new StringBuilder();
        sb.append("reportId,renewalRate,churnRate,campaignEffectiveness,generatedDate\n");
        for (RetentionReport r : reports) {
            sb.append(r.getReportId()).append(",")
                    .append(r.getRenewalRate()).append(",")
                    .append(r.getChurnRate()).append(",")
                    .append(r.getCampaignEffectiveness()).append(",")
                    .append(r.getGeneratedDate()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportExcel() {
        List<RetentionReport> reports = reportService.listAll();

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Retention Reports");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Report ID");
            header.createCell(1).setCellValue("Renewal Rate");
            header.createCell(2).setCellValue("Churn Rate");
            header.createCell(3).setCellValue("Campaign Effectiveness");
            header.createCell(4).setCellValue("Generated Date");

            int rowIdx = 1;
            for (RetentionReport r : reports) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getReportId());
                row.createCell(1).setCellValue(r.getRenewalRate());
                row.createCell(2).setCellValue(r.getChurnRate());
                row.createCell(3).setCellValue(r.getCampaignEffectiveness());
                row.createCell(4).setCellValue(r.getGeneratedDate());
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }

    public byte[] exportPdf() {
        List<RetentionReport> reports = reportService.listAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();
            document.add(new Paragraph("Retention Reports"));
            document.add(new Paragraph(" "));

            for (RetentionReport r : reports) {
                document.add(new Paragraph(
                        "Report ID: " + r.getReportId()
                                + " | Renewal: " + r.getRenewalRate()
                                + " | Churn: " + r.getChurnRate()
                                + " | Campaign: " + r.getCampaignEffectiveness()
                                + " | Date: " + r.getGeneratedDate()
                ));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF export failed", e);
        }
    }
}