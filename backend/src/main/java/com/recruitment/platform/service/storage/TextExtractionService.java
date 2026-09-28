package com.recruitment.platform.service.storage;

import com.recruitment.platform.exception.FileValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class TextExtractionService {

    public String extractText(InputStream inputStream, String filename) {
        String lowerFilename = filename != null ? filename.toLowerCase() : "";

        try {
            String extractedText;
            if (lowerFilename.endsWith(".pdf")) {
                extractedText = extractFromPdf(inputStream);
            } else if (lowerFilename.endsWith(".docx")) {
                extractedText = extractFromDocx(inputStream);
            } else {
                throw new FileValidationException("Unsupported file type for text extraction: " + filename);
            }

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new FileValidationException(
                        "No readable text found in document. Scanned image-only PDFs or empty documents cannot be processed."
                );
            }

            return cleanText(extractedText);
        } catch (FileValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new FileValidationException("Failed to extract text from document (corrupted or malformed file): " + e.getMessage());
        }
    }

    private String extractFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.isEncrypted()) {
                throw new FileValidationException("Password-protected or encrypted PDF documents cannot be processed. Please upload an unlocked PDF.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder sb = new StringBuilder();

            // Extract paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }

            // Extract tables (e.g. resumes formatted with tabular experience/skills)
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        if (cellText != null && !cellText.isBlank()) {
                            sb.append(cellText).append(" ");
                        }
                    }
                    sb.append("\n");
                }
            }

            return sb.toString();
        }
    }

    private String cleanText(String text) {
        // Strip null characters and normalize line breaks
        return text.replace("\u0000", "")
                   .replaceAll("\\r\\n", "\n")
                   .replaceAll("\\r", "\n")
                   .trim();
    }
}
