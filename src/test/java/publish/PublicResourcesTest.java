package publish;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты группы: «Публичные файлы и папки»
 * Покрываемые API:
 * - PUT  /v1/disk/resources/publish               — Публикация ресурса
 * - PUT  /v1/disk/resources/unpublish             — Снятие публикации ресурса
 * - GET  /v1/disk/resources/public                — Список опубликованных ресурсов
 * - GET  /v1/disk/public/resources                — Метаинформация о публичном ресурсе
 * - GET  /v1/disk/public/resources/download       — Скачивание публичного ресурса
 * - POST /v1/disk/public/resources/save-to-disk   — Сохранение публичного ресурса в «Загрузки»
 */
@DisplayName("Публичные файлы и папки")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublicResourcesTest extends BaseTest {

    /** Путь к тестовой папке */
    private static final String TEST_DIR = TEST_BASE_PATH + "/public_tests";
    /** Путь к ресурсу для публикации */
    private static final String PUBLISH_FOLDER = TEST_DIR + "/publish_folder";
    /** Путь к тестовому файлу для публикации */
    private static final String PUBLISH_FILE = TEST_DIR + "/publish_test.txt";
    /** Путь для сохранения публичного ресурса */
    private static final String SAVE_TARGET = TEST_DIR + "/saved_public_resource";

    /** public_key опубликованного ресурса (заполняется в тестах) */
    private static String publicKey;
    /** public_url опубликованного ресурса */
    private static String publicUrl;

    @BeforeAll
    static void setUpTestData()
    {
        PublicResourcesTest instance = new PublicResourcesTest();
        instance.ensureFolderExists(TEST_BASE_PATH);
        instance.ensureFolderExists(TEST_DIR);
        // Создаём папку и файл для публикации
        instance.ensureFolderExists(PUBLISH_FOLDER);
        instance.uploadTestFile(PUBLISH_FILE, "Содержимое файла для публикации");
    }

    @AfterAll
    static void cleanUpTestData()
    {
        PublicResourcesTest instance = new PublicResourcesTest();
        // Снимаем публикацию, если ещё активна
        instance.givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/unpublish");
        instance.givenAuth()
                .queryParam("path", PUBLISH_FILE)
                .put("/v1/disk/resources/unpublish");
        instance.sleep(500);
        // Удаляем тестовые данные
        instance.ensureResourceDeleted(SAVE_TARGET);
        instance.ensureResourceDeleted(TEST_DIR);
    }

    // ============================================================================================
    // Публикация ресурса PUT https://cloud-api.yandex.net/v1/disk/resources/publish
    // ============================================================================================
    @Test
    @Order(1)
    @DisplayName("PUT Публикация ресурса (существующая папка) — ожидаемый код 200")
    void publishResource_existingFolder_returns200()
    {
        Response response = givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/publish");

        assertStatusCode(response, 200,
                "PUT /v1/disk/resources/publish (существующая папка)");
        assertNotNull(response.jsonPath().getString("href"),
                "Поле 'href' отсутствует в ответе");

        sleep(500);

        // Получаем public_key для последующих тестов
        Response resourceInfo = givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .get("/v1/disk/resources");
        publicKey = resourceInfo.jsonPath().getString("public_key");
        publicUrl = resourceInfo.jsonPath().getString("public_url");
        assertNotNull(publicKey, "public_key не найден у опубликованного ресурса");
    }

    @Test
    @Order(2)
    @DisplayName("PUT Публикация ресурса (файл) — ожидаемый код 200")
    void publishResource_existingFile_returns200()
    {
        Response response = givenAuth()
                .queryParam("path", PUBLISH_FILE)
                .put("/v1/disk/resources/publish");

        assertStatusCode(response, 200,
                "PUT /v1/disk/resources/publish (существующий файл)");
    }

    @Test
    @Order(3)
    @DisplayName("PUT Публикация ресурса (несуществующий путь) — ожидаемый код 404")
    void publishResource_notFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/nonexistent_resource_publish_xyz")
                .put("/v1/disk/resources/publish");

        assertStatusCode(response, 404,
                "PUT /v1/disk/resources/publish (несуществующий ресурс)");
    }

    @Test
    @Order(4)
    @DisplayName("PUT Публикация ресурса (невалидный токен) — ожидаемый код 401")
    void publishResource_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/publish");

        assertStatusCode(response, 401,
                "PUT /v1/disk/resources/publish (невалидный токен)");
    }

    // ============================================================================================
    // Список опубликованных ресурсов GET https://cloud-api.yandex.net/v1/disk/resources/public
    // ============================================================================================
    @Test
    @Order(10)
    @DisplayName("GET Список опубликованных ресурсов — ожидаемый код 200")
    void getPublishedResources_validToken_returns200()
    {
        Response response = givenAuth()
                .get("/v1/disk/resources/public");

        assertStatusCode(response, 200,
                "GET /v1/disk/resources/public");
        assertNotNull(response.jsonPath().getList("items"),
                "Поле 'items' отсутствует в ответе");
    }

    @Test
    @Order(11)
    @DisplayName("GET Список опубликованных ресурсов (с ограничением limit=5) — ожидаемый код 200")
    void getPublishedResources_withLimit_returns200()
    {
        Response response = givenAuth()
                .queryParam("limit", 5)
                .get("/v1/disk/resources/public");

        assertStatusCode(response, 200,
                "GET /v1/disk/resources/public?limit=5");
    }

    @Test
    @Order(12)
    @DisplayName("GET Список опубликованных ресурсов (невалидный токен) — ожидаемый код 401")
    void getPublishedResources_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .get("/v1/disk/resources/public");

        assertStatusCode(response, 401,
                "GET /v1/disk/resources/public (невалидный токен)");
    }

    // ============================================================================================
    // Метаинформация о публичном ресурсе GET https://cloud-api.yandex.net/v1/disk/public/resources
    // ============================================================================================
    @Test
    @Order(20)
    @DisplayName("GET Метаинформация о публичном ресурсе (по public_key) — ожидаемый код 200")
    void getPublicResourceInfo_validKey_returns200()
    {
        Assumptions.assumeTrue(publicKey != null,
                "Тест пропущен: public_key не был получен в предыдущем тесте");

        Response response = givenAuth()
                .queryParam("public_key", publicKey)
                .get("/v1/disk/public/resources");

        assertStatusCode(response, 200,
                "GET /v1/disk/public/resources (валидный public_key)");
        assertNotNull(response.jsonPath().getString("name"),
                "Поле 'name' отсутствует в ответе");
    }

    @Test
    @Order(21)
    @DisplayName("GET Метаинформация о публичном ресурсе (невалидный public_key) — ожидаемый код 404")
    void getPublicResourceInfo_invalidKey_returns404()
    {
        Response response = givenAuth()
                .queryParam("public_key", "invalid_public_key_xyz_99999")
                .get("/v1/disk/public/resources");

        assertStatusCode(response, 404,
                "GET /v1/disk/public/resources (невалидный public_key)");
    }

    @Test
    @Order(22)
    @DisplayName("GET Метаинформация о публичном ресурсе (без параметра public_key) — ожидаемый код 400")
    void getPublicResourceInfo_missingKey_returns400()
    {
        Response response = givenAuth()
                .get("/v1/disk/public/resources");

        assertStatusCodeOneOf(response,
                "GET /v1/disk/public/resources (без параметра public_key)", 400, 404);
    }

    // ============================================================================================
    // Скачивание публичного ресурса GET https://cloud-api.yandex.net/v1/disk/public/resources/download
    // ============================================================================================
    @Test
    @Order(30)
    @DisplayName("GET Скачивание публичного ресурса (валидный public_key файла) — ожидаемый код 200")
    void downloadPublicResource_validKey_returns200()
    {
        // Получаем public_key файла (не папки — скачивание доступно только для файлов)
        Response fileInfo = givenAuth()
                .queryParam("path", PUBLISH_FILE)
                .get("/v1/disk/resources");
        String filePublicKey = fileInfo.jsonPath().getString("public_key");

        Assumptions.assumeTrue(filePublicKey != null,
                "Тест пропущен: файл не был опубликован");

        Response response = givenAuth()
                .queryParam("public_key", filePublicKey)
                .get("/v1/disk/public/resources/download");

        assertStatusCode(response, 200,
                "GET /v1/disk/public/resources/download (валидный public_key файла)");
        assertNotNull(response.jsonPath().getString("href"),
                "Поле 'href' (ссылка для скачивания) отсутствует в ответе");
    }

    @Test
    @Order(31)
    @DisplayName("GET Скачивание публичного ресурса (невалидный public_key) — ожидаемый код 404")
    void downloadPublicResource_invalidKey_returns404()
    {
        Response response = givenAuth()
                .queryParam("public_key", "invalid_public_key_download_xyz")
                .get("/v1/disk/public/resources/download");

        assertStatusCode(response, 404,
                "GET /v1/disk/public/resources/download (невалидный public_key)");
    }

    @Test
    @Order(32)
    @DisplayName("GET Скачивание публичного ресурса (без параметра public_key) — ожидаемый код 400")
    void downloadPublicResource_missingKey_returns400()
    {
        Response response = givenAuth()
                .get("/v1/disk/public/resources/download");

        assertStatusCodeOneOf(response,
                "GET /v1/disk/public/resources/download (без параметра public_key)", 400, 404);
    }

    // ============================================================================================
    // Сохранение публичного ресурса в «Загрузки» POST https://cloud-api.yandex.net/v1/disk/public/resources/save-to-disk
    // ============================================================================================
    @Test
    @Order(40)
    @DisplayName("POST Сохранение публичного ресурса в «Загрузки» (валидный public_key) — ожидаемый код 201")
    void savePublicResourceToDisk_validKey_returns201()
    {
        Assumptions.assumeTrue(publicKey != null,
                "Тест пропущен: public_key не был получен");

        // Убеждаемся, что ресурса-назначения нет
        ensureResourceDeleted(SAVE_TARGET);

        Response response = givenAuth()
                .queryParam("public_key", publicKey)
                .queryParam("name", "saved_public_resource")
                .post("/v1/disk/public/resources/save-to-disk");

        assertStatusCodeOneOf(response,
                "POST /v1/disk/public/resources/save-to-disk (валидный public_key)", 201, 202);
        sleep(1000);
    }

    @Test
    @Order(41)
    @DisplayName("POST Сохранение публичного ресурса в «Загрузки» (невалидный public_key) — ожидаемый код 404")
    void savePublicResourceToDisk_invalidKey_returns404()
    {
        Response response = givenAuth()
                .queryParam("public_key", "invalid_public_key_save_xyz")
                .post("/v1/disk/public/resources/save-to-disk");

        assertStatusCode(response, 404,
                "POST /v1/disk/public/resources/save-to-disk (невалидный public_key)");
    }

    @Test
    @Order(42)
    @DisplayName("POST Сохранение публичного ресурса в «Загрузки» (невалидный токен) — ожидаемый код 401")
    void savePublicResourceToDisk_invalidToken_returns401()
    {
        Assumptions.assumeTrue(publicKey != null,
                "Тест пропущен: public_key не был получен");

        Response response = givenInvalidAuth()
                .queryParam("public_key", publicKey)
                .post("/v1/disk/public/resources/save-to-disk");

        assertStatusCode(response, 401,
                "POST /v1/disk/public/resources/save-to-disk (невалидный токен)");
    }

    @Test
    @Order(43)
    @DisplayName("POST Сохранение публичного ресурса в «Загрузки» (без параметра public_key) — ожидаемый код 400")
    void savePublicResourceToDisk_missingKey_returns400()
    {
        Response response = givenAuth()
                .post("/v1/disk/public/resources/save-to-disk");

        assertStatusCodeOneOf(response,
                "POST /v1/disk/public/resources/save-to-disk (без параметра public_key)", 400, 404);
    }

    // ============================================================================================
    // Снятие публикации ресурса PUT https://cloud-api.yandex.net/v1/disk/resources/unpublish
    // ============================================================================================
    @Test
    @Order(50)
    @DisplayName("PUT Снятие публикации ресурса (опубликованный ресурс) — ожидаемый код 200")
    void unpublishResource_publishedResource_returns200()
    {
        // Публикуем, если ещё не опубликовано
        givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/publish");
        sleep(500);

        Response response = givenAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/unpublish");

        assertStatusCode(response, 200,
                "PUT /v1/disk/resources/unpublish (опубликованный ресурс)");
        assertNotNull(response.jsonPath().getString("href"),
                "Поле 'href' отсутствует в ответе");
    }

    @Test
    @Order(51)
    @DisplayName("PUT Снятие публикации ресурса (несуществующий путь) — ожидаемый код 404")
    void unpublishResource_notFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/nonexistent_unpublish_xyz")
                .put("/v1/disk/resources/unpublish");

        assertStatusCode(response, 404,
                "PUT /v1/disk/resources/unpublish (несуществующий ресурс)");
    }

    @Test
    @Order(52)
    @DisplayName("PUT Снятие публикации ресурса (невалидный токен) — ожидаемый код 401")
    void unpublishResource_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", PUBLISH_FOLDER)
                .put("/v1/disk/resources/unpublish");

        assertStatusCode(response, 401,
                "PUT /v1/disk/resources/unpublish (невалидный токен)");
    }
}
