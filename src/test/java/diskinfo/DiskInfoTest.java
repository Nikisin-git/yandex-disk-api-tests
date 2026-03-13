package diskinfo;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты группы: «Получение информации о файлах и папках»
 * Покрываемые API:
 * - GET /v1/disk                         — Данные о Диске пользователя
 * - GET /v1/disk/resources               — Метаинформация о файле или папке
 * - GET /v1/disk/resources/files         — Плоский список всех файлов
 * - GET /v1/disk/resources/last-uploaded — Последние загруженные файлы
 */

@DisplayName("Получение информации о файлах и папках")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiskInfoTest extends BaseTest
{

    // ============================================================================================
    // Данные о Диске пользователя GET https://cloud-api.yandex.net/v1/disk
    // ============================================================================================

    @Test
    @Order(1)
    @DisplayName("GET Данные о Диске пользователя — ожидаемый код 200")
    void getDiskInfo_validToken_returns200()
    {
        Response response = givenAuth()
                .get("/v1/disk");

        assertStatusCode(response, 200, "GET /v1/disk");
        // Проверяем наличие ключевых полей в ответе
        assertNotNull(response.jsonPath().getString("user"), "Поле 'user' отсутствует в ответе");
        assertNotNull(response.jsonPath().get("total_space"), "Поле 'total_space' отсутствует в ответе");
        assertNotNull(response.jsonPath().get("used_space"), "Поле 'used_space' отсутствует в ответе");
    }

    @Test
    @Order(2)
    @DisplayName("GET Данные о Диске пользователя (невалидный токен) — ожидаемый код 401")
    void getDiskInfo_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .get("/v1/disk");

        assertStatusCode(response, 401, "GET /v1/disk (невалидный токен)");
    }

    @Test
    @Order(3)
    @DisplayName("GET Данные о Диске пользователя (без авторизации) — ожидаемый код 401")
    void getDiskInfo_noAuth_returns401()
    {
        Response response = givenNoAuth()
                .get("/v1/disk");

        assertStatusCode(response, 401, "GET /v1/disk (без авторизации)");
    }

    // ============================================================================================
    // Метаинформация о файле или папке GET https://cloud-api.yandex.net/v1/disk/resources
    // ============================================================================================

    @Test
    @Order(4)
    @DisplayName("GET Метаинформация о файле или папке (корневая папка) — ожидаемый код 200")
    void getResourceInfo_rootFolder_returns200()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/")
                .get("/v1/disk/resources");

        assertStatusCode(response, 200, "GET /v1/disk/resources?path=disk:/");
        assertEquals("dir", response.jsonPath().getString("type"),
                "Тип корневого ресурса должен быть 'dir'");
    }

    @Test
    @Order(5)
    @DisplayName("GET Метаинформация о файле или папке (несуществующий путь) — ожидаемый код 404")
    void getResourceInfo_nonExistentPath_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/non_existent_resource_xyz_99999")
                .get("/v1/disk/resources");

        assertStatusCode(response, 404, "GET /v1/disk/resources (несуществующий путь)");
    }

    @Test
    @Order(6)
    @DisplayName("GET Метаинформация о файле или папке (невалидный токен) — ожидаемый код 401")
    void getResourceInfo_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", "disk:/")
                .get("/v1/disk/resources");

        assertStatusCode(response, 401, "GET /v1/disk/resources (невалидный токен)");
    }

    @Test
    @Order(7)
    @DisplayName("GET Метаинформация о файле или папке (без параметра path) — ожидаемый код 400")
    void getResourceInfo_missingPathParam_returns400()
    {
        // Параметр path обязателен — при его отсутствии ожидается 400
        Response response = givenAuth()
                .get("/v1/disk/resources");

        assertStatusCodeOneOf(response,
                "GET /v1/disk/resources (без параметра path)", 400, 404);
    }

    @Test
    @Order(8)
    @DisplayName("GET Метаинформация о файле или папке (с параметрами limit и offset) — ожидаемый код 200")
    void getResourceInfo_withLimitAndOffset_returns200()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/")
                .queryParam("limit", 5)
                .queryParam("offset", 0)
                .get("/v1/disk/resources");

        assertStatusCode(response, 200, "GET /v1/disk/resources?path=disk:/&limit=5&offset=0");
    }

    // ============================================================================================
    // Плоский список всех файлов GET https://cloud-api.yandex.net/v1/disk/resources/files
    // ============================================================================================

    @Test
    @Order(9)
    @DisplayName("GET Плоский список всех файлов — ожидаемый код 200")
    void getFilesList_validToken_returns200()
    {
        Response response = givenAuth()
                .get("/v1/disk/resources/files");

        assertStatusCode(response, 200, "GET /v1/disk/resources/files");
        assertNotNull(response.jsonPath().getList("items"), "Поле 'items' отсутствует в ответе");
    }

    @Test
    @Order(10)
    @DisplayName("GET Плоский список всех файлов (с ограничением limit=5) — ожидаемый код 200")
    void getFilesList_withLimit_returns200()
    {
        Response response = givenAuth()
                .queryParam("limit", 5)
                .get("/v1/disk/resources/files");

        assertStatusCode(response, 200, "GET /v1/disk/resources/files?limit=5");
        assertTrue(response.jsonPath().getList("items").size() <= 5,
                "Количество элементов не должно превышать limit=5");
    }

    @Test
    @Order(11)
    @DisplayName("GET Плоский список всех файлов (фильтр по media_type) — ожидаемый код 200")
    void getFilesList_withMediaType_returns200()
    {
        Response response = givenAuth()
                .queryParam("media_type", "image")
                .get("/v1/disk/resources/files");

        assertStatusCode(response, 200, "GET /v1/disk/resources/files?media_type=image");
    }

    @Test
    @Order(12)
    @DisplayName("GET Плоский список всех файлов (невалидный токен) — ожидаемый код 401")
    void getFilesList_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .get("/v1/disk/resources/files");

        assertStatusCode(response, 401, "GET /v1/disk/resources/files (невалидный токен)");
    }

    // ============================================================================================
    // Последние загруженные файлы GET https://cloud-api.yandex.net/v1/disk/resources/last-uploaded
    // ============================================================================================

    @Test
    @Order(13)
    @DisplayName("GET Последние загруженные файлы — ожидаемый код 200")
    void getLastUploaded_validToken_returns200()
    {
        Response response = givenAuth()
                .get("/v1/disk/resources/last-uploaded");

        assertStatusCode(response, 200, "GET /v1/disk/resources/last-uploaded");
        assertNotNull(response.jsonPath().getList("items"), "Поле 'items' отсутствует в ответе");
    }

    @Test
    @Order(14)
    @DisplayName("GET Последние загруженные файлы (с ограничением limit=3) — ожидаемый код 200")
    void getLastUploaded_withLimit_returns200()
    {
        Response response = givenAuth()
                .queryParam("limit", 3)
                .get("/v1/disk/resources/last-uploaded");

        assertStatusCode(response, 200, "GET /v1/disk/resources/last-uploaded?limit=3");
        assertTrue(response.jsonPath().getList("items").size() <= 3,
                "Количество элементов не должно превышать limit=3");
    }

    @Test
    @Order(15)
    @DisplayName("GET Последние загруженные файлы (невалидный токен) — ожидаемый код 401")
    void getLastUploaded_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .get("/v1/disk/resources/last-uploaded");

        assertStatusCode(response, 401, "GET /v1/disk/resources/last-uploaded (невалидный токен)");
    }

    @Test
    @Order(16)
    @DisplayName("GET Последние загруженные файлы (без авторизации) — ожидаемый код 401")
    void getLastUploaded_noAuth_returns401()
    {
        Response response = givenNoAuth()
                .get("/v1/disk/resources/last-uploaded");

        assertStatusCode(response, 401, "GET /v1/disk/resources/last-uploaded (без авторизации)");
    }
}
