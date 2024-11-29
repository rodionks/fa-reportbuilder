package com.github.rodionks.reportbuildergr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class ReportBuilderApiServiceTest {

    @Mock
    private FaGraphqlClientService faGraphqlClientService;

    @Mock
    private CsvService csvService;

    private ReportBuilderApiService reportBuilderApiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportBuilderApiService = new ReportBuilderApiService(faGraphqlClientService, csvService);
    }

    @Test
    void getTransactions_validInput_shouldReturnCsvResponse() throws IOException {
        Long portfolioId = 123L;
        String startDate = "2021-01-01";
        String endDate = "2021-12-31";
        List<List<String>> transactions = List.of(
                List.of("John", "Doe", "1000"),
                List.of("Jane", "Smith", "2000")
        );
        String csvContent = "Name,Last Name,Amount\nJohn,Doe,1000\nJane,Smith,2000\n";

        when(faGraphqlClientService.executeTransactionsQuery(
                List.of(portfolioId), startDate, endDate)).thenReturn(transactions);
        when(csvService.convertToCsv(transactions, TransactionsCsvHeaders.HEADERS)).thenReturn(csvContent);

        ResponseEntity<byte[]> response = reportBuilderApiService.getTransactions(portfolioId, startDate, endDate);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("attachment; filename=transactions.csv",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("text/csv", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals(csvContent, new String(response.getBody()));
    }

    @Test
    void getTransactions_csvConversionFails_shouldReturnServerError() throws IOException {
        Long portfolioId = 123L;
        String startDate = "2021-01-01";
        String endDate = "2021-12-31";
        List<List<String>> transactions = List.of(
                List.of("John", "Doe", "1000"),
                List.of("Jane", "Smith", "2000")
        );

        when(faGraphqlClientService.executeTransactionsQuery(
                List.of(portfolioId), startDate, endDate)).thenReturn(transactions);
        when(csvService.convertToCsv(Mockito.anyList(), Mockito.any())).thenThrow(new IOException("Error converting to CSV"));

        ResponseEntity<byte[]> response = reportBuilderApiService.getTransactions(portfolioId, startDate, endDate);

        assertEquals(500, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void getTransactions_noStartDateOrEndDate_shouldReturnCsvResponse() throws IOException {
        Long portfolioId = 123L;
        List<List<String>> transactions = List.of(
                List.of("John", "Doe", "1000"),
                List.of("Jane", "Smith", "2000")
        );
        String csvContent = "Name,Last Name,Amount\nJohn,Doe,1000\nJane,Smith,2000\n";

        when(faGraphqlClientService.executeTransactionsQuery(
                List.of(portfolioId), null, null)).thenReturn(transactions);
        when(csvService.convertToCsv(transactions, TransactionsCsvHeaders.HEADERS)).thenReturn(csvContent);

        ResponseEntity<byte[]> response = reportBuilderApiService.getTransactions(portfolioId, null, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(csvContent, new String(response.getBody()));
    }

    @Test
    void getTransactions_missingPortfolioId_shouldReturnBadRequest() {
        ResponseEntity<byte[]> response = reportBuilderApiService.getTransactions(null, "2021-01-01", "2021-12-31");

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}
