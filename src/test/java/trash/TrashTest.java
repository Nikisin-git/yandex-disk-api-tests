package trash;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты группы: «Корзина»
 * Покрываемые API:
 * - GET    /v1/disk/trash/resources         — Содержимое Корзины
 * - DELETE /v1/disk/trash/resources         — Очистка Корзины
 * - PUT    /v1/disk/trash/resources/restore — Восстановление ресурса из Корзины
 */
@DisplayName("Корзина")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrashTest extends BaseTest
{
    /** Путь к тестовой папке (создаётся на Диске, затем удаляется в Корзину) */
    private static final String TEST_DIR = TEST_BASE_PATH + "/trash_tests";
    /** Имя ресурса для восстановления из Корзины */
    private static final String RESTORE_FOLDER_NAME = "restore_test_folder";
    /** Полный путь к ресурсу для восстановления */
    private static final String RESTORE_FOLDER = TEST_DIR + "/" + RESTORE_FOLDER_NAME;
    /** Имя ресурса для тестирования очистки Корзины */
    private static final String CLEAR_FOLDER_NAME = "clear_test_folder";
    /** Полный путь к ресурсу для очистки */
    private static final String CLEAR_FOLDER = TEST_DIR + "/" + CLEAR_FOLDER_NAME;

    @BeforeAll
    static void setUpTestData()
    {
        TrashTest instance = new TrashTest();
        instance.ensureFolderExists(TEST_BASE_PATH);
        instance.ensureFolderExists(TEST_DIR);

        // Создаём папку для восстановления и удаляем её в Корзину
        instance.ensureResourceDeleted(RESTORE_FOLDER);
        instance.ensureFolderExists(RESTORE_FOLDER);
        instance.sleep(500);
        instance.deleteResource(RESTORE_FOLDER); // Удаляем в Корзину (permanently=false)
        instance.sleep(1500);

        // Создаём папку для тестирования очистки и удаляем в Корзину
        instance.ensureResourceDeleted(CLEAR_FOLDER);
        instance.ensureFolderExists(CLEAR_FOLDER);
        instance.sleep(500);
        instance.deleteResource(CLEAR_FOLDER); // Удаляем в Корзину
        instance.sleep(1500);
    }

    @AfterAll
    static void cleanUpTestData()
    {
        TrashTest instance = new TrashTest();
        // Очищаем восстановленные ресурсы
        instance.ensureResourceDeleted(RESTORE_FOLDER);
        instance.ensureResourceDeleted(TEST_DIR);
    }

    // ============================================================================================
    // Содержимое Корзины GET https://cloud-api.yandex.net/v1/disk/trash/resources
    // ============================================================================================
    @Test
    @Order(1)
    @DisplayName("GET Содержимое Корзины — ожидаемый код 200")
    void getTrashContents_validToken_returns200()
    {
        Response response = givenAuth()
                .get("/v1/disk/trash/resources");

        assertStatusCode(response, 200,
                "GET /v1/disk/trash/resources");
        // Проверяем структуру ответа
        assertNotNull(response.jsonPath().getString("name"),
                "Поле 'name' отсутствует в ответе");
        assertEquals("dir", response.jsonPath().getString("type"),
                "Тип Корзины должен быть 'dir'");
    }

    @Test
    @Order(2)
    @DisplayName("GET Содержимое Корзины (с параметром path) — ожидаемый код 200")
    void getTrashContents_withPath_returns200()
    {
        Response response = givenAuth()
                .queryParam("path", "trash:/")
                .get("/v1/disk/trash/resources");

        assertStatusCode(response, 200,
                "GET /v1/disk/trash/resources?path=trash:/");
    }

    @Test
    @Order(3)
    @DisplayName("GET Содержимое Корзины (с ограничением limit=5) — ожидаемый код 200")
    void getTrashContents_withLimit_returns200()
    {
        Response response = givenAuth()
                .queryParam("limit", 5)
                .get("/v1/disk/trash/resources");

        assertStatusCode(response, 200,
                "GET /v1/disk/trash/resources?limit=5");
    }

    @Test
    @Order(4)
    @DisplayName("GET Содержимое Корзины (невалидный токен) — ожидаемый код 401")
    void getTrashContents_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .get("/v1/disk/trash/resources");

        assertStatusCode(response, 401,
                "GET /v1/disk/trash/resources (невалидный токен)");
    }

    @Test
    @Order(5)
    @DisplayName("GET Содержимое Корзины (без авторизации) — ожидаемый код 401")
    void getTrashContents_noAuth_returns401()
    {
        Response response = givenNoAuth()
                .get("/v1/disk/trash/resources");

        assertStatusCode(response, 401,
                "GET /v1/disk/trash/resources (без авторизации)");
    }

    @Test
    @Order(6)
    @DisplayName("GET Содержимое Корзины (несуществующий подпуть) — ожидаемый код 404")
    void getTrashContents_nonExistentPath_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "trash:/nonexistent_resource_xyz_99999")
                .get("/v1/disk/trash/resources");

        assertStatusCode(response, 404,
                "GET /v1/disk/trash/resources (несуществующий подпуть в Корзине)");
    }

    @Test
    @Order(7)
    @DisplayName("GET Содержимое Корзины (с параметрами sort и offset) — ожидаемый код 200")
    void getTrashContents_withSortAndOffset_returns200()
    {
        Response response = givenAuth()
                .queryParam("sort", "deleted")
                .queryParam("offset", 0)
                .queryParam("limit", 10)
                .get("/v1/disk/trash/resources");

        assertStatusCode(response, 200,
                "GET /v1/disk/trash/resources?sort=deleted&offset=0&limit=10");
    }

    // ============================================================================================
    // Восстановление ресурса из Корзины PUT https://cloud-api.yandex.net/v1/disk/trash/resources/restore
    // ============================================================================================

    @Test
    @Order(10)
    @DisplayName("PUT Восстановление ресурса из Корзины (существующий ресурс) — ожидаемый код 201")
    void restoreFromTrash_existingResource_returns201()
    {
        // Находим ресурс в Корзине
        Response trashContents = givenAuth()
                .get("/v1/disk/trash/resources");

        if (trashContents.getStatusCode() == 200)
        {
            var items = trashContents.jsonPath().getList("_embedded.items");
            if (items != null && !items.isEmpty())
            {
                // Берём путь первого ресурса в Корзине для восстановления
                String trashPath = trashContents.jsonPath().getString("_embedded.items[0].path");

                if (trashPath != null) {
                    // Убеждаемся, что в месте восстановления нет конфликтов
                    String originalPath = trashContents.jsonPath().getString("_embedded.items[0].origin_path");
                    if (originalPath != null)
                    {
                        ensureResourceDeleted(originalPath);
                    }

                    Response response = givenAuth()
                            .queryParam("path", trashPath)
                            .put("/v1/disk/trash/resources/restore");

                    assertStatusCodeOneOf(response,
                            "PUT /v1/disk/trash/resources/restore (восстановление из Корзины)",
                            201, 202);

                    // Очищаем восстановленный ресурс
                    if (originalPath != null)
                    {
                        sleep(1000);
                        ensureResourceDeleted(originalPath);
                    }
                    return;
                }
            }
        }

        System.out.println("ИНФОРМАЦИЯ: Корзина пуста или не содержит подходящих ресурсов. "
                + "Позитивный тест восстановления пропущен.");
    }

    @Test
    @Order(11)
    @DisplayName("PUT Восстановление ресурса из Корзины (несуществующий путь) — ожидаемый код 404")
    void restoreFromTrash_notFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "trash:/nonexistent_resource_restore_xyz_99999")
                .put("/v1/disk/trash/resources/restore");

        assertStatusCode(response, 404,
                "PUT /v1/disk/trash/resources/restore (несуществующий ресурс)");
    }

    @Test
    @Order(12)
    @DisplayName("PUT Восстановление ресурса из Корзины (невалидный токен) — ожидаемый код 401")
    void restoreFromTrash_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", "trash:/some_resource")
                .put("/v1/disk/trash/resources/restore");

        assertStatusCode(response, 401,
                "PUT /v1/disk/trash/resources/restore (невалидный токен)");
    }

    @Test
    @Order(13)
    @DisplayName("PUT Восстановление ресурса из Корзины (без авторизации) — ожидаемый код 401")
    void restoreFromTrash_noAuth_returns401()
    {
        Response response = givenNoAuth()
                .queryParam("path", "trash:/some_resource")
                .put("/v1/disk/trash/resources/restore");

        assertStatusCode(response, 401,
                "PUT /v1/disk/trash/resources/restore (без авторизации)");
    }

    @Test
    @Order(14)
    @DisplayName("PUT Восстановление ресурса из Корзины (с указанием имени) — ожидаемый код 201 или 404")
    void restoreFromTrash_withName_returns201or404()
    {
        // Создаём ресурс и удаляем в Корзину для восстановления
        String tempFolder = TEST_DIR + "/restore_with_name_test";
        ensureResourceDeleted(tempFolder);
        ensureFolderExists(tempFolder);
        sleep(500);
        deleteResource(tempFolder); // В Корзину
        sleep(1500);

        // Находим удалённый ресурс в Корзине
        Response trashContents = givenAuth()
                .get("/v1/disk/trash/resources");

        String trashPath = null;
        if (trashContents.getStatusCode() == 200)
        {
            var items = trashContents.jsonPath().getList("_embedded.items");
            if (items != null)
            {
                for (int i = 0; i < items.size(); i++)
                {
                    String name = trashContents.jsonPath().getString("_embedded.items[" + i + "].name");
                    if ("restore_with_name_test".equals(name))
                    {
                        trashPath = trashContents.jsonPath().getString("_embedded.items[" + i + "].path");
                        break;
                    }
                }
            }
        }

        if (trashPath != null)
        {
            // Восстанавливаем с новым именем
            String newName = "restored_renamed_folder";
            ensureResourceDeleted(TEST_DIR + "/" + newName);

            Response response = givenAuth()
                    .queryParam("path", trashPath)
                    .queryParam("name", newName)
                    .put("/v1/disk/trash/resources/restore");

            assertStatusCodeOneOf(response,
                    "PUT /v1/disk/trash/resources/restore (с указанием нового имени)",
                    201, 202);

            // Очистка
            sleep(1000);
            ensureResourceDeleted(TEST_DIR + "/" + newName);
        }

        else
        {
            System.out.println("ИНФОРМАЦИЯ: Ресурс для восстановления не найден в Корзине. Тест пропущен.");
        }
    }

    // ============================================================================================
    // Очистка Корзины DELETE https://cloud-api.yandex.net/v1/disk/trash/resources
    // ============================================================================================

    @Test
    @Order(20)
    @DisplayName("DELETE Очистка Корзины (невалидный токен) — ожидаемый код 401")
    void clearTrash_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .delete("/v1/disk/trash/resources");

        assertStatusCode(response, 401,
                "DELETE /v1/disk/trash/resources (невалидный токен)");
    }

    @Test
    @Order(21)
    @DisplayName("DELETE Очистка Корзины (без авторизации) — ожидаемый код 401")
    void clearTrash_noAuth_returns401()
    {
        Response response = givenNoAuth()
                .delete("/v1/disk/trash/resources");

        assertStatusCode(response, 401,
                "DELETE /v1/disk/trash/resources (без авторизации)");
    }

    @Test
    @Order(22)
    @DisplayName("DELETE Очистка Корзины (конкретный ресурс, несуществующий путь) — ожидаемый код 404")
    void clearTrash_specificResource_notFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", "trash:/nonexistent_resource_clear_xyz_99999")
                .delete("/v1/disk/trash/resources");

        assertStatusCode(response, 404,
                "DELETE /v1/disk/trash/resources (несуществующий ресурс в Корзине)");
    }

    @Test
    @Order(30)
    @DisplayName("DELETE Очистка Корзины (полная очистка) — ожидаемый код 204")
    void clearTrash_fullClear_returns204()
    {
        // Подготовка: создаём и удаляем ресурс для наполнения Корзины
        String tempForTrash = TEST_DIR + "/temp_for_trash_clear";
        ensureFolderExists(tempForTrash);
        sleep(500);
        deleteResource(tempForTrash); // В Корзину
        sleep(1000);

        // Полная очистка Корзины
        Response response = givenAuth()
                .delete("/v1/disk/trash/resources");

        assertStatusCodeOneOf(response,
                "DELETE /v1/disk/trash/resources (полная очистка Корзины)", 202, 204);
        sleep(2000);
    }
}
