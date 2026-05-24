package com.upply.job;

import com.upply.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ApplicationExcelExportService {

    @Value("${app.recruter-application-url}")
    public  String APPLICATION_URL;

    private static final String[] HEADERS = {
            "Applicant Name",
            "Email",
            "University",
            "AI Summary",
            "Job Title",
            "Cover Letter",
            "Resume View",
            "Status",
            "Matching Ratio (%)",
            "Applied At"
    };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateExcel(List<Application> applications) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Applications");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle linkStyle = workbook.createCellStyle();
            Font linkFont = workbook.createFont();
            linkFont.setUnderline(Font.U_SINGLE);
            linkFont.setColor(IndexedColors.BLUE.getIndex());
            linkStyle.setFont(linkFont);

            CreationHelper creationHelper = workbook.getCreationHelper();
            int rowIndex = 1;
            for (Application app : applications) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(app.getApplicant() != null ? app.getApplicant().getFullName() : "");
                row.createCell(1).setCellValue(app.getApplicant() != null ? app.getApplicant().getEmail() : "");
                row.createCell(2).setCellValue(app.getApplicant() != null && app.getApplicant().getUniversity() != null
                        ? app.getApplicant().getUniversity() : "");

                row.createCell(3).setCellValue(app.getSummary() != null ? app.getSummary() : "");

                row.createCell(4).setCellValue(app.getJob() != null ? app.getJob().getTitle() : "");
                row.createCell(5).setCellValue(app.getCoverLetter() != null ? app.getCoverLetter() : "");

                String resumeUrl = APPLICATION_URL + app.getId() + "/resume/view";
                Cell linkCell = row.createCell(6);
                linkCell.setCellValue(resumeUrl);
                Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
                hyperlink.setAddress(resumeUrl);
                linkCell.setHyperlink(hyperlink);
                linkCell.setCellStyle(linkStyle);

                row.createCell(7).setCellValue(app.getStatus() != null ? app.getStatus().name() : "");

                Cell ratioCell = row.createCell(8);
                Double ratio = app.getMatchingRatio();
                if(ratio==null) {
                    ratioCell.setBlank();
                }else{
                    ratioCell.setCellValue(Math.round(ratio*100.0)/100.0);
                }

                row.createCell(9).setCellValue(app.getApplyTime() != null
                        ? app.getApplyTime().format(DATE_FORMATTER) : "");
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
