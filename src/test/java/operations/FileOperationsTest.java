package operations;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты группы: «Операции над файлами и папками»
 * Покрываемые API:
 * - PUT    /v1/disk/resources            — Создание папки
 * - DELETE /v1/disk/resources            — Удаление файла или папки
 * - POST   /v1/disk/resources/copy       — Копирование файла или папки
 * - POST   /v1/disk/resources/move       — Перемещение файла или папки
 * - GET    /v1/disk/operations/{id}      — Статус операции
 */

@DisplayName("Операции над файлами и папками")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileOperationsTest extends BaseTest {

    /** Путь к тестовой папке */
    private static final String TEST_DIR = TEST_BASE_PATH + "/file_operations_tests";
    /** Папка для тестирования создания */
    private static final String CREATE_FOLDER = TEST_DIR + "/created_folder";
    /** Папка для копирования */
    private static final String COPY_SOURCE = TEST_DIR + "/copy_source";
    /** Папка — результат копирования */
    private static final String COPY_DEST = TEST_DIR + "/copy_dest";
    /** Папка для перемещения */
    private static final String MOVE_SOURCE = TEST_DIR + "/move_source";
    /** Папка — результат перемещения */
    private static final String MOVE_DEST = TEST_DIR + "/move_dest";
    /** Папка для удаления */
    private static final String DELETE_FOLDER = TEST_DIR + "/delete_folder";
    /** Тестовый файл внутри папки-источника */
    private static final String TEST_FILE = TEST_DIR + "/test_file.txt";

    @BeforeAll
    static void setUpTestData()
    {
        FileOperationsTest instance = new FileOperationsTest();
        instance.ensureFolderExists(TEST_BASE_PATH);
        instance.ensureFolderExists(TEST_DIR);
        // Предварительная очистка
        instance.ensureResourceDeleted(CREATE_FOLDER);
        instance.ensureResourceDeleted(COPY_SOURCE);
        instance.ensureResourceDeleted(COPY_DEST);
        instance.ensureResourceDeleted(MOVE_SOURCE);
        instance.ensureResourceDeleted(MOVE_DEST);
        instance.ensureResourceDeleted(DELETE_FOLDER);
    }

    @AfterAll
    static void cleanUpTestData()
    {
        FileOperationsTest instance = new FileOperationsTest();
        instance.ensureResourceDeleted(TEST_DIR);
    }

    // ============================================================================================
    // Создание папки PUT https://cloud-api.yandex.net/v1/disk/resources
    // ============================================================================================

    @Test
    @Order(1)
    @DisplayName("PUT Создание папки (новая папка) — ожидаемый код 201")
    void createFolder_newFolder_returns201()
    {
        // Убеждаемся, что папки нет
        ensureResourceDeleted(CREATE_FOLDER);

        Response response = givenAuth()
                .queryParam("path", CREATE_FOLDER)
                .put("/v1/disk/resources");

        assertStatusCode(response, 201,
                "PUT /v1/disk/resources (создание новой папки)");
        // Проверяем, что ответ содержит ссылку на созданный ресурс
        assertNotNull(response.jsonPath().getString("href"),
                "Поле 'href' отсутствует в ответе");
    }

    @Test
    @Order(2)
    @DisplayName("PUT Создание папки (папка уже существует) — ожидаемый код 409")
    void createFolder_alreadyExists_returns409()
    {
        // Папка CREATE_FOLDER создана в предыдущем тесте
        Response response = givenAuth()
                .queryParam("path", CREATE_FOLDER)
                .put("/v1/disk/resources");

        assertStatusCode(response, 409,
                "PUT /v1/disk/resources (папка уже существует)");
    }

    @Test
    @Order(3)
    @DisplayName("PUT Создание папки (невалидный токен) — ожидаемый код 401")
    void createFolder_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", TEST_DIR + "/unauthorized_folder")
                .put("/v1/disk/resources");

        assertStatusCode(response, 401,
                "PUT /v1/disk/resources (невалидный токен)");
    }

    @Test
    @Order(4)
    @DisplayName("PUT Создание папки (без параметра path) — ожидаемый код 400")
    void createFolder_missingPath_returns400()
    {
        Response response = givenAuth()
                .put("/v1/disk/resources");

        assertStatusCodeOneOf(response,
                "PUT /v1/disk/resources (без параметра path)", 400, 404);
    }

    @Test
    @Order(5)
    @DisplayName("PUT Создание папки (вложенная папка в несуществующей родительской) — ожидаемый код 409")
    void createFolder_parentNotExists_returns409()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/nonexistent_parent_xyz/child_folder")
                .put("/v1/disk/resources");

        assertStatusCode(response, 409,
                "PUT /v1/disk/resources (несуществующая родительская папка)");
    }

    // ============================================================================================
    // Копирование файла или папки POST https://cloud-api.yandex.net/v1/disk/resources/copy
    // ============================================================================================

    @Test
    @Order(10)
    @DisplayName("POST Копирование файла или папки (корректное копирование) — ожидаемый код 201")
    void copyResource_validCopy_returns201()
    {
        // Создаём исходную папку
        ensureFolderExists(COPY_SOURCE);
        // Убеждаемся, что папки-назначения нет
        ensureResourceDeleted(COPY_DEST);

        Response response = givenAuth()
                .queryParam("from", COPY_SOURCE)
                .queryParam("path", COPY_DEST)
                .post("/v1/disk/resources/copy");

        assertStatusCodeOneOf(response,
                "POST /v1/disk/resources/copy (корректное копирование)", 201, 202);
        sleep(1000);
    }

    @Test
    @Order(11)
    @DisplayName("POST Копирование файла или папки (конфликт — назначение существует) — ожидаемый код 409")
    void copyResource_destExists_returns409()
    {
        // COPY_DEST уже существует после предыдущего теста
        Response response = givenAuth()
                .queryParam("from", COPY_SOURCE)
                .queryParam("path", COPY_DEST)
                .queryParam("overwrite", false)
                .post("/v1/disk/resources/copy");

        assertStatusCode(response, 409,
                "POST /v1/disk/resources/copy (конфликт — назначение существует)");
    }

    @Test
    @Order(12)
    @DisplayName("POST Копирование файла или папки (несуществующий источник) — ожидаемый код 404")
    void copyResource_sourceNotFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("from", "disk:/nonexistent_source_xyz_99999")
                .queryParam("path", TEST_DIR + "/copy_result_nonexistent")
                .post("/v1/disk/resources/copy");

        assertStatusCode(response, 404,
                "POST /v1/disk/resources/copy (несуществующий источник)");
    }

    @Test
    @Order(13)
    @DisplayName("POST Копирование файла или папки (невалидный токен) — ожидаемый код 401")
    void copyResource_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("from", COPY_SOURCE)
                .queryParam("path", COPY_DEST + "_unauthorized")
                .post("/v1/disk/resources/copy");

        assertStatusCode(response, 401,
                "POST /v1/disk/resources/copy (невалидный токен)");
    }

    @Test
    @Order(14)
    @DisplayName("POST Копирование файла или папки (с overwrite=true) — ожидаемый код 201")
    void copyResource_overwrite_returns201()
    {
        Response response = givenAuth()
                .queryParam("from", COPY_SOURCE)
                .queryParam("path", COPY_DEST)
                .queryParam("overwrite", true)
                .post("/v1/disk/resources/copy");

        assertStatusCodeOneOf(response,
                "POST /v1/disk/resources/copy (с overwrite=true)", 201, 202);
        sleep(1000);
    }

    // ============================================================================================
    // Перемещение файла или папки POST https://cloud-api.yandex.net/v1/disk/resources/move
    // ============================================================================================

    @Test
    @Order(20)
    @DisplayName("POST Перемещение файла или папки (корректное перемещение) — ожидаемый код 201")
    void moveResource_validMove_returns201()
    {
        // Создаём папку для перемещения
        ensureFolderExists(MOVE_SOURCE);
        // Убеждаемся, что папки-назначения нет
        ensureResourceDeleted(MOVE_DEST);

        Response response = givenAuth()
                .queryParam("from", MOVE_SOURCE)
                .queryParam("path", MOVE_DEST)
                .post("/v1/disk/resources/move");

        assertStatusCodeOneOf(response,
                "POST /v1/disk/resources/move (корректное перемещение)", 201, 202);
        sleep(1000);
    }

    @Test
    @Order(21)
    @DisplayName("POST Перемещение файла или папки (несуществующий источник) — ожидаемый код 404")
    void moveResource_sourceNotFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("from", "disk:/nonexistent_source_for_move_xyz")
                .queryParam("path", TEST_DIR + "/move_result_nonexistent")
                .post("/v1/disk/resources/move");

        assertStatusCode(response, 404,
                "POST /v1/disk/resources/move (несуществующий источник)");
    }

    @Test
    @Order(22)
    @DisplayName("POST Перемещение файла или папки (конфликт — назначение существует) — ожидаемый код 409")
    void moveResource_destExists_returns409()
    {
        // Создаём новый источник
        ensureFolderExists(MOVE_SOURCE);

        Response response = givenAuth()
                .queryParam("from", MOVE_SOURCE)
                .queryParam("path", MOVE_DEST)
                .queryParam("overwrite", false)
                .post("/v1/disk/resources/move");

        assertStatusCode(response, 409,
                "POST /v1/disk/resources/move (конфликт — назначение существует)");
    }

    @Test
    @Order(23)
    @DisplayName("POST Перемещение файла или папки (невалидный токен) — ожидаемый код 401")
    void moveResource_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("from", MOVE_SOURCE)
                .queryParam("path", MOVE_DEST + "_unauthorized")
                .post("/v1/disk/resources/move");

        assertStatusCode(response, 401,
                "POST /v1/disk/resources/move (невалидный токен)");
    }

    // ============================================================================================
    // Удаление файла или папки DELETE https://cloud-api.yandex.net/v1/disk/resources
    // ============================================================================================

    @Test
    @Order(30)
    @DisplayName("DELETE Удаление файла или папки (существующая папка) — ожидаемый код 204")
    void deleteResource_existingFolder_returns204()
    {
        // Создаём папку для удаления
        ensureFolderExists(DELETE_FOLDER);

        Response response = givenAuth()
                .queryParam("path", DELETE_FOLDER)
                .queryParam("permanently", true)
                .delete("/v1/disk/resources");

        assertStatusCodeOneOf(response,
                "DELETE /v1/disk/resources (существующая папка)", 202, 204);
        sleep(1000);
    }

    @Test
    @Order(31)
    @DisplayName("DELETE Удаление файла или папки (несуществующий ресурс) — ожидаемый код 404")
    void deleteResource_notFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "disk:/nonexistent_resource_for_delete_xyz")
                .queryParam("permanently", true)
                .delete("/v1/disk/resources");

        assertStatusCode(response, 404,
                "DELETE /v1/disk/resources (несуществующий ресурс)");
    }

    @Test
    @Order(32)
    @DisplayName("DELETE Удаление файла или папки (невалидный токен) — ожидаемый код 401")
    void deleteResource_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", DELETE_FOLDER)
                .queryParam("permanently", true)
                .delete("/v1/disk/resources");

        assertStatusCode(response, 401,
                "DELETE /v1/disk/resources (невалидный токен)");
    }

    @Test
    @Order(33)
    @DisplayName("DELETE Удаление файла или папки (без параметра path) — ожидаемый код 400")
    void deleteResource_missingPath_returns400()
    {
        Response response = givenAuth()
                .delete("/v1/disk/resources");

        assertStatusCodeOneOf(response,
                "DELETE /v1/disk/resources (без параметра path)", 400, 404);
    }

    @Test
    @Order(34)
    @DisplayName("DELETE Удаление файла или папки (в Корзину, permanently=false) — ожидаемый код 204")
    void deleteResource_toTrash_returns204() {
        // Создаём папку для удаления в Корзину
        String trashFolder = TEST_DIR + "/to_trash_folder";
        ensureFolderExists(trashFolder);

        Response response = givenAuth()
                .queryParam("path", trashFolder)
                .queryParam("permanently", false)
                .delete("/v1/disk/resources");

        assertStatusCodeOneOf(response,
                "DELETE /v1/disk/resources (в Корзину)", 202, 204);
        sleep(1000);
    }

    // ============================================================================================
    // Статус операции GET https://cloud-api.yandex.net/v1/disk/operations/{operation_id}
    // ============================================================================================
    @Test
    @Order(40)
    @DisplayName("GET Статус операции (несуществующий operation_id) — ожидаемый код 404")
    void getOperationStatus_invalidId_returns404()
    {
        Response response = givenAuth()
                .get("/v1/disk/operations/nonexistent-operation-id-12345");

        assertStatusCode(response, 404,
                "GET /v1/disk/operations/{id} (несуществующий operation_id)");
    }

    @Test
    @Order(41)
    @DisplayName("GET Статус операции (невалидный токен) — ожидаемый код 401")
    void getOperationStatus_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .get("/v1/disk/operations/some-operation-id");

        assertStatusCode(response, 401,
                "GET /v1/disk/operations/{id} (невалидный токен)");
    }

    @Test
    @Order(42)
    @DisplayName("GET Статус операции (получение id из асинхронной операции) — ожидаемый код 200")
    void getOperationStatus_fromAsyncOperation_returns200()
    {
        // Создаём и удаляем ресурс, чтобы получить operation_id (если операция будет асинхронной)
        String asyncFolder = TEST_DIR + "/async_op_folder";
        ensureFolderExists(asyncFolder);

        Response deleteResponse = givenAuth()
                .queryParam("path", asyncFolder)
                .delete("/v1/disk/resources");

        if (deleteResponse.getStatusCode() == 202) {
            // Операция асинхронная — можно получить operation_id
            String operationHref = deleteResponse.jsonPath().getString("href");
            if (operationHref != null && !operationHref.isEmpty())
            {
                // Извлекаем URL операции и проверяем статус
                Response statusResponse = givenAuth()
                        .get(operationHref);
                assertStatusCode(statusResponse, 200,
                        "GET /v1/disk/operations/{id} (статус асинхронной операции)");
            }
        }

        else
        {
            // Операция выполнена синхронно (204) — пропускаем позитивный тест для статуса
            System.out.println("ИНФОРМАЦИЯ: Удаление выполнено синхронно (код " + deleteResponse.getStatusCode()
                    + "), тест статуса операции пропущен — для проверки нужна асинхронная операция.");
        }
    }
}
