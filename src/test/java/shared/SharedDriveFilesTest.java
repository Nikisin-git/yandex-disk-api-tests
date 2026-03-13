package shared;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты группы: «Файлы и папки на общем диске»
 * Покрываемые API (используются стандартные endpoint'ы /v1/disk/resources с путями общих дисков):
 * - GET    /v1/disk/resources      — Метаинформация о ресурсе на общем диске
 * - PUT    /v1/disk/resources      — Создание папки на общем диске
 * - POST   /v1/disk/resources/copy — Копирование ресурса на общем диске
 * - POST   /v1/disk/resources/move — Перемещение ресурса на общем диске
 * - DELETE /v1/disk/resources     — Удаление ресурса на общем диске
 */
@DisplayName("Файлы и папки на общем диске")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SharedDriveFilesTest extends BaseTest {


//Путь к общим дискам
    private static final String SHARED_DRIVES_PATH = "disk:/Общие диски";


// Флаг: доступны ли общие диски для данного аккаунта
    private static boolean sharedDrivesAvailable = false;

 //Имя первого доступного общего диска
    private static String sharedDriveName = null;

//Базовый путь на общем диске для тестирования
    private static String testBasePath = null;

    @BeforeAll
    static void setUpTestData()
    {
        SharedDriveFilesTest instance = new SharedDriveFilesTest();

        // Проверяем доступность общих дисков
        Response response = instance.givenAuth()
                .queryParam("path", SHARED_DRIVES_PATH)
                .get("/v1/disk/resources");

        if (response.getStatusCode() == 200) {
            sharedDrivesAvailable = true;
            // Пытаемся получить имя первого общего диска
            try
            {
                var items = response.jsonPath().getList("_embedded.items");
                if (items != null && !items.isEmpty())
                {
                    sharedDriveName = response.jsonPath().getString("_embedded.items[0].name");
                    testBasePath = SHARED_DRIVES_PATH + "/" + sharedDriveName + "/autotest_temp";
                    System.out.println("ИНФОРМАЦИЯ: Найден общий диск: " + sharedDriveName);
                    instance.ensureFolderExists(testBasePath);
                }

                else
                {
                    System.out.println("ИНФОРМАЦИЯ: Общие диски доступны, но список пуст.");
                    sharedDrivesAvailable = false;
                }
            }
            catch (Exception e)
            {
                System.out.println("ИНФОРМАЦИЯ: Не удалось получить список общих дисков: " + e.getMessage());
                sharedDrivesAvailable = false;
            }
        }

        else
        {
            System.out.println("ИНФОРМАЦИЯ: Общие диски недоступны (код " + response.getStatusCode()
                    + "). Тесты этой группы будут пропущены. "
                    + "Для запуска необходим аккаунт Яндекс 360 для бизнеса.");
        }
    }

    @AfterAll
    static void cleanUpTestData()
    {
        if (sharedDrivesAvailable && testBasePath != null)
        {
            SharedDriveFilesTest instance = new SharedDriveFilesTest();
            instance.ensureResourceDeleted(testBasePath);
        }
    }

    // ============================================================================================
    // Метаинформация о ресурсе на общем диске GET https://cloud-api.yandex.net/v1/disk/resources
    // ============================================================================================


    @Test
    @Order(1)
    @DisplayName("GET Метаинформация о ресурсе на общем диске (несуществующий путь) — ожидаемый код 404")
    void getSharedDriveResource_notFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", SHARED_DRIVES_PATH + "/NonExistentDrive_XYZ/nonexistent_folder")
                .get("/v1/disk/resources");

        assertStatusCode(response, 404,
                "GET /v1/disk/resources (несуществующий путь на общем диске)");
    }

    @Test
    @Order(2)
    @DisplayName("GET Метаинформация о ресурсе на общем диске (невалидный токен) — ожидаемый код 401")
    void getSharedDriveResource_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", SHARED_DRIVES_PATH)
                .get("/v1/disk/resources");

        assertStatusCode(response, 401,
                "GET /v1/disk/resources (общий диск, невалидный токен)");
    }

    // ============================================================================================
    // Создание папки на общем диске PUT https://cloud-api.yandex.net/v1/disk/resources
    // ============================================================================================
    @Test
    @Order(11)
    @DisplayName("PUT Создание папки на общем диске (невалидный токен) — ожидаемый код 401")
    void createFolderOnSharedDrive_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", SHARED_DRIVES_PATH + "/SomeDrive/new_folder")
                .put("/v1/disk/resources");

        assertStatusCode(response, 401,
                "PUT /v1/disk/resources (создание на общем диске, невалидный токен)");
    }

    // ============================================================================================
    // Копирование ресурса на общем диске POST https://cloud-api.yandex.net/v1/disk/resources/copy
    // ============================================================================================

    @Test
    @Order(20)
    @DisplayName("POST Копирование ресурса на общем диске (несуществующий источник) — ожидаемый код 404")
    void copyOnSharedDrive_sourceNotFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("from", SHARED_DRIVES_PATH + "/NonExistentDrive_XYZ/source")
                .queryParam("path", SHARED_DRIVES_PATH + "/NonExistentDrive_XYZ/dest")
                .post("/v1/disk/resources/copy");

        assertStatusCode(response, 404,
                "POST /v1/disk/resources/copy (копирование, несуществующий источник на общем диске)");
    }

    @Test
    @Order(21)
    @DisplayName("POST Копирование ресурса на общем диске (невалидный токен) — ожидаемый код 401")
    void copyOnSharedDrive_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("from", SHARED_DRIVES_PATH + "/Drive/source")
                .queryParam("path", SHARED_DRIVES_PATH + "/Drive/dest")
                .post("/v1/disk/resources/copy");

        assertStatusCode(response, 401,
                "POST /v1/disk/resources/copy (копирование на общем диске, невалидный токен)");
    }

    // ============================================================================================
    // Перемещение ресурса на общем диске POST https://cloud-api.yandex.net/v1/disk/resources/move
    // ============================================================================================

    @Test
    @Order(30)
    @DisplayName("POST Перемещение ресурса на общем диске (несуществующий источник) — ожидаемый код 404")
    void moveOnSharedDrive_sourceNotFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("from", SHARED_DRIVES_PATH + "/NonExistentDrive_XYZ/source_move")
                .queryParam("path", SHARED_DRIVES_PATH + "/NonExistentDrive_XYZ/dest_move")
                .post("/v1/disk/resources/move");

        assertStatusCode(response, 404,
                "POST /v1/disk/resources/move (перемещение, несуществующий источник на общем диске)");
    }

    @Test
    @Order(31)
    @DisplayName("POST Перемещение ресурса на общем диске (невалидный токен) — ожидаемый код 401")
    void moveOnSharedDrive_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("from", SHARED_DRIVES_PATH + "/Drive/source")
                .queryParam("path", SHARED_DRIVES_PATH + "/Drive/dest")
                .post("/v1/disk/resources/move");

        assertStatusCode(response, 401,
                "POST /v1/disk/resources/move (перемещение на общем диске, невалидный токен)");
    }

    // ============================================================================================
    // Удаление ресурса на общем диске DELETE https://cloud-api.yandex.net/v1/disk/resources
    // ============================================================================================

    @Test
    @Order(40)
    @DisplayName("DELETE Удаление ресурса на общем диске (несуществующий ресурс) — ожидаемый код 404")
    void deleteOnSharedDrive_notFound_returns404()
    {
        Response response = givenAuth()
                .queryParam("path", SHARED_DRIVES_PATH + "/NonExistentDrive_XYZ/nonexistent_file")
                .queryParam("permanently", true)
                .delete("/v1/disk/resources");

        assertStatusCode(response, 404,
                "DELETE /v1/disk/resources (удаление, несуществующий ресурс на общем диске)");
    }

    @Test
    @Order(41)
    @DisplayName("DELETE Удаление ресурса на общем диске (невалидный токен) — ожидаемый код 401")
    void deleteOnSharedDrive_invalidToken_returns401()
    {
        Response response = givenInvalidAuth()
                .queryParam("path", SHARED_DRIVES_PATH + "/Drive/file")
                .queryParam("permanently", true)
                .delete("/v1/disk/resources");

        assertStatusCode(response, 401,
                "DELETE /v1/disk/resources (удаление на общем диске, невалидный токен)");
    }

}