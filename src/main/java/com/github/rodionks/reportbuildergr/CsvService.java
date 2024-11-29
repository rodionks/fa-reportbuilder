package com.github.rodionks.reportbuildergr;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Service
public class CsvService {

    public String convertToCsv(List<List<String>> data, String[] headers) throws IOException {
        StringWriter sw = new StringWriter();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .build();

        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            data.forEach((record) -> {
                try {
                    printer.printRecord(record);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return sw.toString();
    }

}

