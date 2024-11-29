package com.github.rodionks.reportbuildergr;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ReportBuilderApiService {

    private final FaGraphqlClientService faGraphqlClientService;
    private final CsvService csvService;

    ReportBuilderApiService(
            FaGraphqlClientService faGraphqlClientService,
            CsvService csvService
    ) {
        this.faGraphqlClientService = faGraphqlClientService;
        this.csvService = csvService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<byte[]> getTransactions(
            @RequestParam Long portfolioId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {

        if (portfolioId == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<List<String>> transactions = faGraphqlClientService.executeTransactionsQuery(
                List.of(portfolioId), startDate, endDate);

        try {
            String csvContent = this.csvService.convertToCsv(transactions, TransactionsCsvHeaders.HEADERS);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent.getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

}
