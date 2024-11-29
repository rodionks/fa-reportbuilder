package com.github.rodionks.reportbuildergr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvServiceTest {

    private CsvService csvService;

    @BeforeEach
    void setUp() {
        csvService = new CsvService();
    }

    @Test
    void convertToCsv_validData_shouldReturnCsvString() throws IOException {
        // Arrange
        List<List<String>> data = List.of(
                List.of("John", "Doe", "30"),
                List.of("Jane", "Smith", "25")
        );
        String[] headers = {"First Name", "Last Name", "Age"};

        String expectedCsv = "First Name,Last Name,Age\r\nJohn,Doe,30\r\nJane,Smith,25\r\n";

        // Act
        String actualCsv = csvService.convertToCsv(data, headers);

        // Assert
        assertEquals(expectedCsv, actualCsv);
    }

    @Test
    void convertToCsv_emptyData_shouldReturnCsvWithOnlyHeaders() throws IOException {
        // Arrange
        List<List<String>> data = List.of();
        String[] headers = {"First Name", "Last Name", "Age"};

        String expectedCsv = "First Name,Last Name,Age\r\n";

        // Act
        String actualCsv = csvService.convertToCsv(data, headers);

        // Assert
        assertEquals(expectedCsv, actualCsv);
    }

    @Test
    void convertToCsv_nullData_shouldThrowIOException() {
        // Arrange
        List<List<String>> data = null;
        String[] headers = {"First Name", "Last Name", "Age"};

        // Act & Assert
        assertThrows(NullPointerException.class, () -> csvService.convertToCsv(data, headers));
    }

    @Test
    void convertToCsv_dataWithNullValues_shouldHandleGracefully() throws IOException {
        // Arrange
        List<List<String>> data = List.of(
                List.of("John", "Doe", ""),
                List.of("Jane", "", "25")
        );
        String[] headers = {"First Name", "Last Name", "Age"};

        String expectedCsv = "First Name,Last Name,Age\r\nJohn,Doe,\r\nJane,,25\r\n";

        // Act
        String actualCsv = csvService.convertToCsv(data, headers);

        // Assert
        assertEquals(expectedCsv, actualCsv);
    }
}
