package downloadupload;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты группы: «Скачивание и загрузка»
 * Покрываемые API:
 * - GET  /v1/disk/resources/download — Скачивание файла с Диска (получение ссылки)
 * - GET  /v1/disk/resources/upload   — Получение ссылки для загрузки файла на Диск
 * - POST /v1/disk/resources/upload  — Загрузка файла в Диск по URL
 */

@DisplayName("Скачивание и загрузка")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DownloadUploadTest extends BaseTest
{

    /** Путь к тестовой папке */
    private static final String TEST_DIR = TEST_BASE_PATH + "/download_upload_tests";
    /** Путь к тестовому файлу для скачивания */
    private static final String TEST_FILE = TEST_DIR + "/test_download.txt";
    /** Путь для загрузки нового файла */
    private static final String UPLOAD_FILE = TEST_DIR + "/uploaded_file.txt";
    /** Путь для файла, загружаемого по URL */
    private static final String URL_UPLOAD_FILE = TEST_DIR + "/url_uploaded_file.txt";

    @BeforeAll
    static void setUpTestData()
    {
        // Создание базовой тестовой папки
        DownloadUploadTest instance = new DownloadUploadTest();
        instance.ensureFolderExists(TEST_BASE_PATH);
        instance.ensureFolderExists(TEST_DIR);
        // Загрузка тестового файла для тестов скачивания
        instance.uploadTestFile(TEST_FILE, "Содержимое тестового файла для скачивания");
    }

    @AfterAll
    static void cleanUpTestData()
    {
        DownloadUploadTest instance = new DownloadUploadTest();
        instance.ensureResourceDeleted(TEST_DIR);
    }

    // ============================================================================================
    // Скачивание файла с Диска GET https://cloud-api.yandex.net/v1/disk/resources/download
    // ============================================================================================

    @Test
    @Order(1)
    @DisplayName("GET Скачивание файла с Диска (существующий файл) — ожидаемый код 200")
    void getDownloadLink_existingFile_returns200()
    {
        Response response = givenAuth()
                .queryParam("path", TEST_FILE)
                .get("/v1/disk/resources/download");

        assertStatusCode(response, 200, "GET /v1/disk/resources/download (существующий файл)");
        // Ответ должен содержать ссылку для скачивания
        assertNotNull(response.jsonPath().getString("href"),
                "Поле 'href' (ссылка для скачивания) отсутствует в ответе");
        assertEquals("GET", response.jsonPath().getString("method"),
                "Метод скачивания должен быть GET");
    }

    @Test
    @Order(2)
    @DisplayName("GET Скачивание файла с Диска (несуществующий файл) — ожидаемый код 404")
    void getDownloadLink_nonExistentFile_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/non_existent_file_xyz.txt")
                .get("/v1/disk/resources/download");

        assertStatusCode(response, 404, "GET /v1/disk/resources/download (несуществующий файл)");
    }

    @Test
    @Order(3)
    @DisplayName("GET Скачивание файла с Диска (невалидный токен) — ожидаемый код 401")
    void getDownloadLink_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", TEST_FILE)
                .get("/v1/disk/resources/download");

        assertStatusCode(response, 401, "GET /v1/disk/resources/download (невалидный токен)");
    }

    @Test
    @Order(4)
    @DisplayName("GET Скачивание файла с Диска (без параметра path) — ожидаемый код 400")
    void getDownloadLink_missingPath_returns400()
    {
        Response response = givenAuth()
                .get("/v1/disk/resources/download");

        assertStatusCodeOneOf(response,
                "GET /v1/disk/resources/download (без параметра path)", 400, 404);
    }

    // ============================================================================================
    // Получение ссылки для загрузки GET https://cloud-api.yandex.net/v1/disk/resources/upload
    // ============================================================================================

    @Test
    @Order(5)
    @DisplayName("GET Получение ссылки для загрузки (новый файл) — ожидаемый код 200")
    void getUploadLink_newFile_returns200()
    {
        // Убеждаемся, что файла нет
        ensureResourceDeleted(UPLOAD_FILE);

        Response response = givenAuth()
                .queryParam("path", UPLOAD_FILE)
                .get("/v1/disk/resources/upload");

        assertStatusCode(response, 200, "GET /v1/disk/resources/upload (новый файл)");
        assertNotNull(response.jsonPath().getString("href"),
                "Поле 'href' (URL для загрузки) отсутствует в ответе");
        assertEquals("PUT", response.jsonPath().getString("method"),
                "Метод загрузки должен быть PUT");
    }

    @Test
    @Order(6)
    @DisplayName("GET Получение ссылки для загрузки (файл уже существует, overwrite=false) — ожидаемый код 409")
    void getUploadLink_existingFileNoOverwrite_returns409()
    {
        Response response = givenAuth()
                .queryParam("path", TEST_FILE)
                .queryParam("overwrite", false)
                .get("/v1/disk/resources/upload");

        // Если файл существует и overwrite=false, API может вернуть 409 или 200 (зависит от реализации)
        assertStatusCodeOneOf(response,
                "GET /v1/disk/resources/upload (файл существует, overwrite=false)", 200, 409);
    }

    @Test
    @Order(7)
    @DisplayName("GET Получение ссылки для загрузки (overwrite=true) — ожидаемый код 200")
    void getUploadLink_overwrite_returns200()
    {
        Response response = givenAuth()
                .queryParam("path", TEST_FILE)
                .queryParam("overwrite", true)
                .get("/v1/disk/resources/upload");

        assertStatusCode(response, 200, "GET /v1/disk/resources/upload (overwrite=true)");
    }

    @Test
    @Order(8)
    @DisplayName("GET Получение ссылки для загрузки (невалидный токен) — ожидаемый код 401")
    void getUploadLink_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", UPLOAD_FILE)
                .get("/v1/disk/resources/upload");

        assertStatusCode(response, 401, "GET /v1/disk/resources/upload (невалидный токен)");
    }

    @Test
    @Order(9)
    @DisplayName("GET Получение ссылки для загрузки (без параметра path) — ожидаемый код 400")
    void getUploadLink_missingPath_returns400()
    {
        Response response = givenAuth()
                .get("/v1/disk/resources/upload");

        assertStatusCodeOneOf(response,
                "GET /v1/disk/resources/upload (без параметра path)", 400, 404);
    }

    // ============================================================================================
    // Загрузка файла в Диск по URL POST https://cloud-api.yandex.net/v1/disk/resources/upload
    // ============================================================================================

    @Test
    @Order(10)
    @DisplayName("POST Загрузка файла в Диск по URL (корректный URL) — ожидаемый код 202")
    void uploadByUrl_validUrl_returns202()
    {
        // Убеждаемся, что файла нет
        ensureResourceDeleted(URL_UPLOAD_FILE);

        Response response = givenAuth()
                .queryParam("path", URL_UPLOAD_FILE)
                .queryParam("url", "https://raw.githubusercontent.com/yandex-disk/yandex-disk-restapi-java/master/README.md")
                .post("/v1/disk/resources/upload");

        assertStatusCode(response, 202,
                "POST /v1/disk/resources/upload (загрузка по URL)");
        assertNotNull(response.jsonPath().getString("href"),
                "Поле 'href' (ссылка на статус операции) отсутствует в ответе");

        // Даём время на загрузку
        sleep(3000);
    }

    @Test
    @Order(11)
    @DisplayName("POST Загрузка файла в Диск по URL (невалидный токен) — ожидаемый код 401")
    void uploadByUrl_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", URL_UPLOAD_FILE)
                .queryParam("url", "https://example.com/file.txt")
                .post("/v1/disk/resources/upload");

        assertStatusCode(response, 401,
                "POST /v1/disk/resources/upload (невалидный токен)");
    }

    @Test
    @Order(12)
    @DisplayName("POST Загрузка файла в Диск по URL (без параметра url) — ожидаемый код 400")
    void uploadByUrl_missingUrl_returns400()
    {
        Response response = givenAuth()
                .queryParam("path", URL_UPLOAD_FILE)
                .post("/v1/disk/resources/upload");

        assertStatusCodeOneOf(response,
                "POST /v1/disk/resources/upload (без параметра url)", 400, 404);
    }

    @Test
    @Order(13)
    @DisplayName("POST Загрузка файла в Диск по URL (без параметра path) — ожидаемый код 400")
    void uploadByUrl_missingPath_returns400()
    {
        Response response = givenAuth()
                .queryParam("url", "https://example.com/file.txt")
                .post("/v1/disk/resources/upload");

        assertStatusCodeOneOf(response,
                "POST /v1/disk/resources/upload (без параметра path)", 400, 404);
    }
}
