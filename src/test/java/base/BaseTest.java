package base;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Базовый класс для всех автотестов Яндекс.Диска.
 * Содержит:
 * - Загрузку OAuth-токена из config.properties / системного свойства / переменной окружения
 * - Общие методы для формирования HTTP-запросов (с авторизацией, без, с невалидным токеном)
 * - Вспомогательные методы для проверки статус-кодов
 * - Утилиты для создания/удаления тестовых ресурсов
 */
public abstract class BaseTest
{

    // OAuth-токен для авторизации запросов
    protected static String OAUTH_TOKEN;

    // Базовый URL API Яндекс.Диска
    protected static final String BASE_URL = "https://cloud-api.yandex.net";

    // Заведомо невалидный токен для негативных сценариев
    protected static final String INVALID_TOKEN = "invalid_oauth_token_for_negative_testing_12345";

    // Базовый путь для всех тестовых ресурсов (папка на Диске) */
    protected static final String TEST_BASE_PATH = "disk:/autotest_temp";

    /**
     * Начальная настройка: загрузка токена и конфигурация RestAssured.
     * Выполняется один раз перед всеми тестами в каждом дочернем классе.
     */

    @BeforeAll
    static void baseSetUp()
    {
        // Загрузка настроек из config.properties
        Properties props = new Properties();
        try (InputStream is = BaseTest.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null)
            {
                props.load(is);
            }
        }
        catch (IOException e)
        {
            System.err.println("Не удалось загрузить config.properties: " + e.getMessage());
        }

        // Приоритет: системное свойство > переменная окружения > config.properties
        OAUTH_TOKEN = System.getProperty("oauth.token",
                System.getenv().getOrDefault("OAUTH_TOKEN",
                        props.getProperty("oauth.token", "")));

        if (OAUTH_TOKEN == null || OAUTH_TOKEN.isBlank() || OAUTH_TOKEN.equals("YOUR_OAUTH_TOKEN_HERE"))
        {
            fail("OAuth-токен не задан! Укажите токен в файле src/test/resources/config.properties, "
                    + "либо передайте через -Doauth.token=... или переменную окружения OAUTH_TOKEN. "
                    + "Подробности — в README.md.");
        }

        // Базовый URL для всех запросов RestAssured
        RestAssured.baseURI = BASE_URL;
    }

    // ========================================================================================
    // Методы для построения HTTP-запросов
    // ========================================================================================

    /**
     * Создаёт запрос с корректной авторизацией (OAuth-токен).
     */
    protected RequestSpecification givenAuth()
    {
        return RestAssured.given()
                .header("Authorization", "OAuth " + OAUTH_TOKEN)
                .header("Accept", "application/json");
    }

    /**
     * Создаёт запрос с невалидным OAuth-токеном для проверки ответа 401.
     */
    protected RequestSpecification givenInvalidAuth()
    {
        return RestAssured.given()
                .header("Authorization", "OAuth " + INVALID_TOKEN)
                .header("Accept", "application/json");
    }

    /**
     * Создаёт запрос без заголовка авторизации для проверки ответа 401.
     */
    protected RequestSpecification givenNoAuth()
    {
        return RestAssured.given()
                .header("Accept", "application/json");
    }

    // ========================================================================================
    // Методы для проверки статус-кодов
    // ========================================================================================

    /**
     * Проверяет, что статус-код ответа совпадает с ожидаемым.
     * При несовпадении — тест падает с указанием полученного кода и тела ответа.
     * @param response       HTTP-ответ
     * @param expectedCode   ожидаемый HTTP-код
     * @param apiDescription описание API-вызова (для отчёта)
     */

    protected void assertStatusCode(Response response, int expectedCode, String apiDescription)
    {
        int actualCode = response.getStatusCode();
        if (actualCode != expectedCode)
        {
            fail(String.format(
                    "ПРЕДУПРЕЖДЕНИЕ: %s — Ожидаемый код: %d, Полученный код: %d. Тело ответа: %s",
                    apiDescription, expectedCode, actualCode, response.getBody().asString()
            ));
        }
    }

    /**
     * Проверяет, что статус-код ответа входит в список допустимых значений.
     * Используется для операций, которые могут вернуть как синхронный (201),
     * так и асинхронный (202) ответ.
     * @param response       HTTP-ответ
     * @param apiDescription описание API-вызова (для отчёта)
     * @param expectedCodes  допустимые HTTP-коды
     */

    protected void assertStatusCodeOneOf(Response response, String apiDescription, int... expectedCodes)
    {
        int actualCode = response.getStatusCode();
        for (int code : expectedCodes)
        {
            if (actualCode == code) return;
        }

        fail(String.format(
                "ПРЕДУПРЕЖДЕНИЕ: %s — Ожидаемые коды: %s, Полученный код: %d. Тело ответа: %s",
                apiDescription, Arrays.toString(expectedCodes), actualCode, response.getBody().asString()
        ));
    }

    // ========================================================================================
    // Вспомогательные методы для управления тестовыми ресурсами
    // ========================================================================================

    /**
     * Создаёт папку на Яндекс.Диске.
     *
     * @param path путь к папке (например, "disk:/test_folder")
     * @return HTTP-ответ
     */
    protected Response createFolder(String path)
    {
        return givenAuth()
                .queryParam("path", path)
                .put("/v1/disk/resources");
    }

    /**
     * Удаляет ресурс с Яндекс.Диска.
     *
     * @param path        путь к ресурсу
     * @param permanently true — удалить безвозвратно, false — в Корзину
     * @return HTTP-ответ
     */
    protected Response deleteResource(String path, boolean permanently)
    {
        return givenAuth()
                .queryParam("path", path)
                .queryParam("permanently", permanently)
                .delete("/v1/disk/resources");
    }

    /**
     * Удаляет ресурс в Корзину.
     */
    protected Response deleteResource(String path)
    {
        return deleteResource(path, false);
    }

    /**
     * Проверяет, существует ли ресурс по указанному пути.
     */
    protected boolean resourceExists(String path)
    {
        Response response = givenAuth()
                .queryParam("path", path)
                .get("/v1/disk/resources");
        return response.getStatusCode() == 200;
    }

    /**
     * Гарантирует существование папки. Если папки нет — создаёт.
     */
    protected void ensureFolderExists(String path)
    {
        if (!resourceExists(path))
        {
            createFolder(path);
            sleep(500);
        }
    }

    /**
     * Гарантирует удаление ресурса. Если ресурс есть — удаляет безвозвратно.
     */
    protected void ensureResourceDeleted(String path)
    {
        if (resourceExists(path)) {
            deleteResource(path, true);
            sleep(1500);
        }
    }

    /**
     * Загружает небольшой текстовый файл на Яндекс.Диск.
     * Используется для подготовки тестовых данных (файл для скачивания, копирования и т.д.).
     * @param path    путь к файлу на Диске (например, "disk:/test/file.txt")
     * @param content содержимое файла
     */
    protected void uploadTestFile(String path, String content)
    {
        // Шаг 1: Получить URL для загрузки
        Response uploadUrlResponse = givenAuth()
                .queryParam("path", path)
                .queryParam("overwrite", true)
                .get("/v1/disk/resources/upload");

        if (uploadUrlResponse.getStatusCode() != 200)
        {
            fail("Не удалось получить URL для загрузки тестового файла: " + uploadUrlResponse.getBody().asString());
        }

        String uploadHref = uploadUrlResponse.jsonPath().getString("href");

        // Шаг 2: Загрузить файл по полученному URL
        Response uploadResponse = RestAssured.given()
                .header("Authorization", "OAuth " + OAUTH_TOKEN)
                .contentType("text/plain")
                .body(content)
                .put(uploadHref);

        if (uploadResponse.getStatusCode() != 201 && uploadResponse.getStatusCode() != 202)
        {
            fail("Не удалось загрузить тестовый файл: " + uploadResponse.getBody().asString());
        }
        sleep(1000);
    }

    /**
     * Публикует ресурс и возвращает public_key.
     */
    protected String publishResource(String path)
    {
        Response response = givenAuth()
                .queryParam("path", path)
                .put("/v1/disk/resources/publish");

        if (response.getStatusCode() != 200)
        {
            fail("Не удалось опубликовать ресурс " + path + ": " + response.getBody().asString());
        }

        sleep(500);

        // Получаем public_key из метаинформации о ресурсе
        Response resourceInfo = givenAuth()
                .queryParam("path", path)
                .get("/v1/disk/resources");
        return resourceInfo.jsonPath().getString("public_key");
    }

    /**
     * Вспомогательная пауза между запросами (для стабилизации тестов).
     */
    protected void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }
    }
}
