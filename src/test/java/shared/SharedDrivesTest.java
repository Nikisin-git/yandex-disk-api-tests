package shared;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты группы: «Общие диски организации в Яндекс 360 для бизнеса»
 * Покрываемые API:
 * - GET /v1/disk/resources (path к общим дискам) — Получение списка общих дисков
 * ВАЖНО: Данные тесты требуют аккаунта Яндекс 360 для бизнеса.
 * Для обычных (бесплатных) пользователей Яндекс.Диска общие диски недоступны.
 * В документации Яндекс.Диска общие диски организации доступны по пути:
 *   disk:/Общие диски/
 * Для работы с ними используются стандартные endpoint'ы /v1/disk/resources
 * с указанием соответствующего пути.
 */
@DisplayName("Общие диски организации в Яндекс 360 для бизнеса")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SharedDrivesTest extends BaseTest {

    /**
     * Путь к общим дискам в Яндекс 360.
     * Для обычных пользователей этот путь не существует.
     */
    private static final String SHARED_DRIVES_PATH = "disk:/Общие диски";

    // ============================================================================================
    // Общие диски организации GET https://cloud-api.yandex.net/v1/disk/resources
    // Используется стандартный endpoint /v1/disk/resources с путём к общим дискам.
    // Для аккаунтов Яндекс 360 для бизнеса возвращает список общих дисков организации.
    // Для обычных пользователей ожидается 404 (путь не существует).
    // ============================================================================================

    @Test
    @Order(1)
    @DisplayName("GET Общие диски организации (проверка доступности) — ожидаемый код 200 или 404")
    void getSharedDrives_checkAvailability_returns200or404()
    {
        Response response = givenAuth()
                .queryParam("path", SHARED_DRIVES_PATH)
                .get("/v1/disk/resources");

        int statusCode = response.getStatusCode();
        if (statusCode == 200)
        {
            // Аккаунт Яндекс 360 — общие диски доступны
            System.out.println("ИНФОРМАЦИЯ: Общие диски доступны. Аккаунт является частью организации Яндекс 360.");
            assertEquals("dir", response.jsonPath().getString("type"),
                    "Тип ресурса общих дисков должен быть 'dir'");
        }

        else if (statusCode == 404)
        {
            // Обычный аккаунт — общие диски недоступны
            System.out.println("ИНФОРМАЦИЯ: Общие диски недоступны (код 404). "
                    + "Для данного теста требуется аккаунт Яндекс 360 для бизнеса.");
        }

        else
        {
            fail(String.format(
                    "ПРЕДУПРЕЖДЕНИЕ: GET /v1/disk/resources?path=%s — "
                            + "Ожидаемый код: 200 (Яндекс 360) или 404 (обычный аккаунт), "
                            + "Полученный код: %d. Тело ответа: %s",
                    SHARED_DRIVES_PATH, statusCode, response.getBody().asString()));
        }
    }

    @Test
    @Order(2)
    @DisplayName("GET Общие диски организации (невалидный токен) — ожидаемый код 401")
    void getSharedDrives_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", SHARED_DRIVES_PATH)
                .get("/v1/disk/resources");

        assertStatusCode(response, 401,
                "GET /v1/disk/resources?path=Общие диски (невалидный токен)");
    }

    @Test
    @Order(3)
    @DisplayName("GET Общие диски организации (без авторизации) — ожидаемый код 401")
    void getSharedDrives_noAuth_returns401()
    {
        Response response = givenNoAuth()
                .queryParam("path", SHARED_DRIVES_PATH)
                .get("/v1/disk/resources");

        assertStatusCode(response, 401,
                "GET /v1/disk/resources?path=Общие диски (без авторизации)");
    }

    @Test
    @Order(4)
    @DisplayName("GET Общие диски организации (с параметрами limit и offset) — ожидаемый код 200 или 404")
    void getSharedDrives_withPagination_returns200or404()
    {
        Response response = givenAuth()
                .queryParam("path", SHARED_DRIVES_PATH)
                .queryParam("limit", 10)
                .queryParam("offset", 0)
                .get("/v1/disk/resources");

        assertStatusCodeOneOf(response,
                "GET /v1/disk/resources?path=Общие диски&limit=10&offset=0",
                200, 404);
    }

    @Test
    @Order(5)
    @DisplayName("GET Общие диски организации (несуществующий подпуть) — ожидаемый код 404")
    void getSharedDrives_nonExistentSubPath_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", SHARED_DRIVES_PATH + "/NonExistentDrive_XYZ_99999")
                .get("/v1/disk/resources");

        assertStatusCode(response, 404,
                "GET /v1/disk/resources (несуществующий подпуть в общих дисках)");
    }
}