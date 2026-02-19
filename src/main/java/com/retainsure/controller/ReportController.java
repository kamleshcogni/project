package com.retainsure.controller;

import com.retainsure.model.RetentionReport;
import com.retainsure.service.ReportExportService;
import com.retainsure.service.ReportService;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;
    private final ReportExportService exportService;

    public ReportController(ReportService service, ReportExportService exportService) {
        this.service = service;
        this.exportService = exportService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RetentionReport> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionReport get(@PathVariable("id") Long id) { return service.getById(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionReport create(@RequestBody RetentionReport r) { return service.create(r); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionReport update(@PathVariable("id") Long id, @RequestBody RetentionReport r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") Long id) { service.delete(id); }

    // ✅ Download reports
    @GetMapping("/download/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> download(@PathVariable("type") String type) {
        byte[] data;
        String fileName;
        MediaType contentType;

        switch (type.toLowerCase()) {
            case "csv":
                data = exportService.exportCsv();
                fileName = "retention-report.csv";
                contentType = MediaType.TEXT_PLAIN;
                break;
            case "excel":
                data = exportService.exportExcel();
                fileName = "retention-report.xlsx";
                contentType = MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
                break;
            case "pdf":
                data = exportService.exportPdf();
                fileName = "retention-report.pdf";
                contentType = MediaType.APPLICATION_PDF;
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(contentType)
                .body(data);
    }
}