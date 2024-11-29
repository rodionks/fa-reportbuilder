package com.github.rodionks.reportbuildergr;


import com.github.rodionks.reportbuildergr.codegen.types.Portfolio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class FaGraphqlClientService {

    private final WebClient webClient;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String tokenUrl;
    private final String clientId;
    private final String username;
    private final String password;

    public FaGraphqlClientService(
            @Value("${fa.graphql.url}") String graphQlUrl,
            @Value("${fa.graphql.token-url}") String tokenUrl,
            @Value("${fa.graphql.client-id}") String clientId,
            @Value("${fa.graphql.username}") String username,
            @Value("${fa.graphql.password}") String password
    ) {
            this.tokenUrl = tokenUrl;
            this.clientId = clientId;
            this.username = username;
            this.password = password;

            this.webClient = WebClient.builder()
                .baseUrl(graphQlUrl)
                .filter((request, next) -> next.exchange(
                        ClientRequest.from(request)
                                .header("Authorization", "Bearer " + getToken())
                                .build()
                ))
                .build();
    }


    private String getToken() {
        // todo: consider using a more secure way to store the token
        // todo: implement token refresh, or cache the token for a certain period of time

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("username", username);
        body.add("password", password);
        body.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        try {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T safeGet(Supplier<T> supplier, T defaultValue) {
        try {
            return Objects.requireNonNullElse(supplier.get(), defaultValue);
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    public List<List<String>> executeTransactionsQuery(List<Long> ids, String startDate, String endDate) {
        //todo: consider to implement caching of data to avoid multiple requests to the server
        //todo: consider building the query with the codegen and use DGS client

        //        TransactionsGraphQLQuery query = TransactionsGraphQLQuery.newRequest()
//                .ids(ids.stream().map(String::valueOf).toList())
//                .startDate(startDate)
//                .endDate(endDate)
//                .build();
//        TransactionsProjectionRoot<BaseSubProjectionNode<?, ?>, BaseSubProjectionNode<?, ?>> projection = new TransactionsProjectionRoot<>();
//        DgsGraphQlClient dgsClient = DgsGraphQlClient.create(graphQlClient);

        HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(this.webClient);

        List<Portfolio> portfolios =  graphQlClient.documentName("Transactions")
                .variable("ids", ids)
                .variable("startDate", startDate)
                .variable("endDate", endDate)
                .retrieveSync("portfoliosByIds")
                .toEntityList(Portfolio.class);

        return portfolios.stream().flatMap(
                portfolio -> {
                    return portfolio.getTransactions().stream().map(
                            transaction -> {

                                return List.of(
                                    safeGet(() -> transaction.getPortfolio().getShortName(), ""),
                                    safeGet(() -> transaction.getSecurity().getName(), ""),
                                    safeGet(() -> transaction.getSecurity().getIsinCode(), ""),
                                    safeGet(() -> transaction.getCurrency().getCode(), ""),

                                    //todo: check value of the following field
                                    safeGet(() -> transaction.getPortfolio().getPortfolioReport().getUnits().toString(), ""),
                                    safeGet(() -> transaction.getUnitPrice().toString(), ""),
                                    safeGet(() -> transaction.getTradeAmount().toString(), ""),
                                    safeGet(() -> transaction.getType().getName(), ""),

                                    //todo: check if the date format is correct
                                    safeGet(transaction::getTransactionDate, ""),
                                    safeGet(transaction::getSettlementDate, "")
                                );

                            }
                    );
                }
        ).toList();
    }

}
