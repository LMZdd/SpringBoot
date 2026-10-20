package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.model.ClientDto;
import com.ccsw.tutorial.common.pagination.PageableRequest;
import com.ccsw.tutorial.config.ResponsePage;
import com.ccsw.tutorial.game.model.GameDto;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LoanIT {

    public static final String LOCALHOST = "http://localhost:";
    public static final String SERVICE_PATH = "/loan";

    private static final int TOTAL_LOANS = 6;
    private static final int PAGE_SIZE = 5;

    private static final Long EXISTS_LOAN_ID = 1L;
    private static final Long DELETE_LOAN_ID = 6L;
    private static final Long NOT_EXISTS_LOAN_ID = 0L;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    ParameterizedTypeReference<ResponsePage<LoanDto>> responseTypePage = new ParameterizedTypeReference<ResponsePage<LoanDto>>() {
    };

    private ResponseEntity<ResponsePage<LoanDto>> findPage(int pageNumber, int pageSize, Long idGame, Long idClient, String date) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(LOCALHOST + port + SERVICE_PATH);
        if (idGame != null)
            builder.queryParam("idGame", idGame);
        if (idClient != null)
            builder.queryParam("idClient", idClient);
        if (date != null)
            builder.queryParam("date", date);

        LoanSearchDto searchDto = new LoanSearchDto();
        searchDto.setPageable(new PageableRequest(pageNumber, pageSize));

        return restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(searchDto), responseTypePage);
    }

    private LoanDto buildDto(Long gameId, Long clientId, LocalDate start, LocalDate end) {

        GameDto game = new GameDto();
        game.setId(gameId);

        ClientDto client = new ClientDto();
        client.setId(clientId);

        LoanDto dto = new LoanDto();
        dto.setGame(game);
        dto.setClient(client);
        dto.setStartDate(start);
        dto.setEndDate(end);

        return dto;
    }

    private ResponseEntity<Void> save(Long id, LoanDto dto) {

        String url = LOCALHOST + port + SERVICE_PATH + (id != null ? "/" + id : "");

        return restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(dto), Void.class);
    }

    @Test
    public void findFirstPageWithFiveSizeShouldReturnFirstFiveResults() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, PAGE_SIZE, null, null, null);

        assertNotNull(response);
        assertEquals(TOTAL_LOANS, response.getBody().getTotalElements());
        assertEquals(PAGE_SIZE, response.getBody().getContent().size());
    }

    @Test
    public void findSecondPageWithFiveSizeShouldReturnLastResult() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(1, PAGE_SIZE, null, null, null);

        assertNotNull(response);
        assertEquals(TOTAL_LOANS, response.getBody().getTotalElements());
        assertEquals(TOTAL_LOANS - PAGE_SIZE, response.getBody().getContent().size());
    }

    @Test
    public void findByGameShouldReturnLoansOfThatGame() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, PAGE_SIZE, 1L, null, null);

        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    public void findByClientShouldReturnLoansOfThatClient() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, PAGE_SIZE, null, 1L, null);

        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    public void findByIntermediateDateShouldReturnLoansThatCoverThatDay() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, PAGE_SIZE, null, null, "2026-10-09");

        assertEquals(3, response.getBody().getTotalElements());
    }

    @Test
    public void findByStartDateOfLoanShouldReturnThatLoan() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, PAGE_SIZE, null, null, "2026-10-20");

        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    public void findByDateWithoutLoansShouldReturnEmpty() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, PAGE_SIZE, null, null, "2026-10-17");

        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    public void findByGameClientAndDateShouldCombineFilters() {

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, PAGE_SIZE, 2L, 1L, "2026-10-06");
        assertEquals(1, response.getBody().getTotalElements());

        response = findPage(0, PAGE_SIZE, 2L, 2L, "2026-10-06");
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    public void saveWithoutIdShouldCreateNewLoan() {

        LoanDto dto = buildDto(1L, 3L, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 25));

        ResponseEntity<Void> saveResponse = save(null, dto);
        assertEquals(HttpStatus.OK, saveResponse.getStatusCode());

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, 10, null, null, null);
        assertEquals(TOTAL_LOANS + 1, response.getBody().getTotalElements());
    }

    @Test
    public void saveWithExactlyFourteenDaysShouldCreateNewLoan() {

        LoanDto dto = buildDto(1L, 3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15));

        assertEquals(HttpStatus.OK, save(null, dto).getStatusCode());
    }

    @Test
    public void saveWithEndDateBeforeStartDateShouldReturnBadRequest() {

        LoanDto dto = buildDto(1L, 3L, LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 5));

        assertEquals(HttpStatus.BAD_REQUEST, save(null, dto).getStatusCode());
    }

    @Test
    public void saveWithMoreThanFourteenDaysShouldReturnBadRequest() {

        LoanDto dto = buildDto(1L, 3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 16));

        assertEquals(HttpStatus.BAD_REQUEST, save(null, dto).getStatusCode());
    }

    @Test
    public void saveWithGameAlreadyLoanedShouldReturnBadRequest() {

        // El juego 1 está prestado del 01/10 al 10/10
        LoanDto dto = buildDto(1L, 3L, LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 12));

        assertEquals(HttpStatus.BAD_REQUEST, save(null, dto).getStatusCode());
    }

    @Test
    public void saveWithClientWithTwoLoansSameDayShouldReturnBadRequest() {

        // El cliente 1 ya tiene 2 préstamos entre el 05/10 y el 10/10
        LoanDto dto = buildDto(5L, 1L, LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 9));

        assertEquals(HttpStatus.BAD_REQUEST, save(null, dto).getStatusCode());
    }

    @Test
    public void modifyWithExistIdShouldModifyLoan() {

        // Mismo juego y cliente, fechas nuevas: no debe chocar consigo mismo
        LoanDto dto = buildDto(1L, 1L, LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 9));

        assertEquals(HttpStatus.OK, save(EXISTS_LOAN_ID, dto).getStatusCode());

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, 10, null, null, null);
        LoanDto loan = response.getBody().getContent().stream().filter(item -> item.getId().equals(EXISTS_LOAN_ID)).findFirst().orElse(null);

        assertNotNull(loan);
        assertEquals(LocalDate.of(2026, 10, 2), loan.getStartDate());
        assertEquals(LocalDate.of(2026, 10, 9), loan.getEndDate());
    }

    @Test
    public void modifyWithNotExistIdShouldInternalError() {

        LoanDto dto = buildDto(1L, 3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, save(NOT_EXISTS_LOAN_ID, dto).getStatusCode());
    }

    @Test
    public void deleteWithExistsIdShouldDeleteLoan() {

        restTemplate.exchange(LOCALHOST + port + SERVICE_PATH + "/" + DELETE_LOAN_ID, HttpMethod.DELETE, null, Void.class);

        ResponseEntity<ResponsePage<LoanDto>> response = findPage(0, 10, null, null, null);
        assertEquals(TOTAL_LOANS - 1, response.getBody().getTotalElements());
    }

    @Test
    public void deleteWithNotExistsIdShouldInternalError() {

        ResponseEntity<?> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH + "/" + NOT_EXISTS_LOAN_ID, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
