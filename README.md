# Автотесты REST API Яндекс.Диска

Проект автоматизированного тестирования [REST API Яндекс.Диска](https://yandex.ru/dev/disk/rest/).

Покрываются методы **GET**, **POST**, **PUT** и **DELETE**.

---

## Структура проекта

```
src
└──test
   │ └──java
   │      ├── base
   │      │   └── BaseTest.java                  — Базовый класс с общей логикой
   │      ├── diskinfo/
   │      │   └── DiskInfoTest.java              — Получение информации о файлах и папках
   │      ├── downloadupload/
   │      │   └── DownloadUploadTest.java        — Скачивание и загрузка
   │      ├── operations/
   │      │   └── FileOperationsTest.java        — Операции над файлами и папками
   │      ├── publish/
   │      │   ├── PublicResourcesTest.java       — Публичные файлы и папки
   │      │   └── PublicResourcesAdminTest.java  — Публичные файлы и папки (для администраторов)
   │      ├── shared/
   │      │   ├── SharedDrivesTest.java          — Общие диски организации (Яндекс 360)
   │      │   └── SharedDriveFilesTest.java      — Файлы и папки на общем диске
   │      └── trash/
   │          └── TrashTest.java                 — Корзина
   │      
   │     
   └── config.properties                         — Файл конфигурации (здесь указывается токен)

```

---

## Покрытые API

| Группа | Метод | Endpoint | Описание |
|--------|-------|----------|----------|
| **Информация** | GET | `/v1/disk` | Данные о Диске пользователя |
| **Информация** | GET | `/v1/disk/resources` | Метаинформация о файле или папке |
| **Информация** | GET | `/v1/disk/resources/files` | Плоский список всех файлов |
| **Информация** | GET | `/v1/disk/resources/last-uploaded` | Последние загруженные файлы |
| **Скачивание** | GET | `/v1/disk/resources/download` | Скачивание файла с Диска |
| **Загрузка** | GET | `/v1/disk/resources/upload` | Получение ссылки для загрузки |
| **Загрузка** | POST | `/v1/disk/resources/upload` | Загрузка файла по URL |
| **Операции** | PUT | `/v1/disk/resources` | Создание папки |
| **Операции** | DELETE | `/v1/disk/resources` | Удаление файла или папки |
| **Операции** | POST | `/v1/disk/resources/copy` | Копирование |
| **Операции** | POST | `/v1/disk/resources/move` | Перемещение |
| **Операции** | GET | `/v1/disk/operations/{id}` | Статус операции |
| **Публичные** | PUT | `/v1/disk/resources/publish` | Публикация ресурса |
| **Публичные** | PUT | `/v1/disk/resources/unpublish` | Снятие публикации |
| **Публичные** | GET | `/v1/disk/resources/public` | Список опубликованных ресурсов |
| **Публичные** | GET | `/v1/disk/public/resources` | Метаинформация о публичном ресурсе |
| **Публичные** | GET | `/v1/disk/public/resources/download` | Скачивание публичного ресурса |
| **Публичные** | POST | `/v1/disk/public/resources/save-to-disk` | Сохранение в «Загрузки» |
| **Общие диски** | GET | `/v1/disk/resources` (path: Общие диски) | Список общих дисков |
| **Корзина** | GET | `/v1/disk/trash/resources` | Содержимое Корзины |
| **Корзина** | DELETE | `/v1/disk/trash/resources` | Очистка Корзины |
| **Корзина** | PUT | `/v1/disk/trash/resources/restore` | Восстановление из Корзины |

---

## Требования

- **Java 17** или выше
- **Apache Maven 3.8+**
- **IntelliJ IDEA** (рекомендуется, но не обязательно)
- Интернет-соединение (для доступа к API Яндекс.Диска)
- OAuth-токен Яндекс.Диска (см. раздел ниже)

---

## Куда вставить OAuth-токен

### Способ 1: Файл конфигурации (рекомендуется)

Откройте файл `src/test/resources/config.properties` и замените значение:

```properties
# Замените YOUR_OAUTH_TOKEN_HERE на ваш реальный токен
oauth.token=YOUR_OAUTH_TOKEN_HERE
```

Пример с реальным токеном:
```properties
oauth.token=y0_AgAAAABkx9q7AADLWwAAAAD3aCkLAAA...   
```

### Способ 2: Переменная окружения

Установите переменную окружения `OAUTH_TOKEN`:

**Linux/macOS:**
```bash
  export OAUTH_TOKEN="ваш_токен"
```

**Windows (PowerShell):**
```powershell
$env:OAUTH_TOKEN="ваш_токен"
```

### Способ 3: Параметр командной строки Maven

```bash
  mvn test -Doauth.token="ваш_токен"
```

### Как получить OAuth-токен

1. Перейдите на [Полигон Яндекс.Диска](https://yandex.ru/dev/disk/poligon/)
2. Нажмите «Получить OAuth-токен»
3. Авторизуйтесь в Яндексе (используйте **тестовый аккаунт**, не личный!)
4. Скопируйте полученный токен

**Альтернативный способ (через регистрацию приложения):**
1. Перейдите на [OAuth Яндекса](https://oauth.yandex.ru/)
2. Зарегистрируйте новое приложение
3. В разделе «Доступы» добавьте:
   - `cloud_api:disk.app_folder` — Доступ к папке приложения
   - `cloud_api:disk.read` — Чтение данных
   - `cloud_api:disk.write` — Запись данных
   - `cloud_api:disk.info` — Информация о Диске
4. Получите токен через OAuth-авторизацию

> ⚠️ **ВАЖНО:** Не используйте личный аккаунт для тестирования! Создайте отдельный тестовый аккаунт Яндекса. Тесты создают, удаляют и перемещают файлы и папки на Диске.

---

## Запуск тестов

### Из командной строки (Maven)

```### Запуск всех тестов
```bash
mvn test
```

### Запуск отдельного класса тестов
```bash
  mvn test -Dtest=DiskInfoTest
  mvn test -Dtest=DownloadUploadTest
  mvn test -Dtest=FileOperationsTest
  mvn test -Dtest=PublicResourcesTest
  mvn test -Dtest=SharedDriveFilesTest
  mvn test -Dtest=SharedDrivesTest
  mvn test -Dtest=TrashTest


```

### Запуск конкретного теста независимо
Для запуска отдельного метода теста используйте следующую команду:
`mvn test -Dtest=ИмяКласса#имяМетода`
---

## Список тестов и команды запуска

### 1. Получение информации о файлах и папках (DiskInfoTest)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 1 | GET Данные о Диске пользователя — 200 | `mvn test -Dtest=DiskInfoTest#getDiskInfo_validToken_returns200` |
| 2 | GET Данные о Диске (невалидный токен) — 401 | `mvn test -Dtest=DiskInfoTest#getDiskInfo_invalidToken_returns401` |
| 3 | GET Данные о Диске (без авторизации) — 401 | `mvn test -Dtest=DiskInfoTest#getDiskInfo_noAuth_returns401` |
| 4 | GET Метаинформация (корневая папка) — 200 | `mvn test -Dtest=DiskInfoTest#getResourceInfo_rootFolder_returns200` |
| 5 | GET Метаинформация (несуществующий путь) — 404 | `mvn test -Dtest=DiskInfoTest#getResourceInfo_nonExistentPath_returns404` |
| 6 | GET Метаинформация (невалидный токен) — 401 | `mvn test -Dtest=DiskInfoTest#getResourceInfo_invalidToken_returns401` |
| 7 | GET Метаинформация (без параметра path) — 400 | `mvn test -Dtest=DiskInfoTest#getResourceInfo_missingPathParam_returns400` |
| 8 | GET Метаинформация (limit и offset) — 200 | `mvn test -Dtest=DiskInfoTest#getResourceInfo_withLimitAndOffset_returns200` |
| 9 | GET Плоский список всех файлов — 200 | `mvn test -Dtest=DiskInfoTest#getFilesList_validToken_returns200` |
| 10 | GET Список файлов (limit=5) — 200 | `mvn test -Dtest=DiskInfoTest#getFilesList_withLimit_returns200` |
| 11 | GET Список файлов (media_type) — 200 | `mvn test -Dtest=DiskInfoTest#getFilesList_withMediaType_returns200` |
| 12 | GET Список файлов (невалидный токен) — 401 | `mvn test -Dtest=DiskInfoTest#getFilesList_invalidToken_returns401` |
| 13 | GET Последние загруженные файлы — 200 | `mvn test -Dtest=DiskInfoTest#getLastUploaded_validToken_returns200` |
| 14 | GET Последние файлы (limit=3) — 200 | `mvn test -Dtest=DiskInfoTest#getLastUploaded_withLimit_returns200` |
| 15 | GET Последние файлы (невалидный токен) — 401 | `mvn test -Dtest=DiskInfoTest#getLastUploaded_invalidToken_returns401` |
| 16 | GET Последние файлы (без авторизации) — 401 | `mvn test -Dtest=DiskInfoTest#getLastUploaded_noAuth_returns401` |

### 2. Скачивание и загрузка (DownloadUploadTest)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 17 | GET Скачивание (существующий файл) — 200 | `mvn test -Dtest=DownloadUploadTest#getDownloadLink_existingFile_returns200` |
| 18 | GET Скачивание (несуществующий файл) — 404 | `mvn test -Dtest=DownloadUploadTest#getDownloadLink_nonExistentFile_returns404` |
| 19 | GET Скачивание (невалидный токен) — 401 | `mvn test -Dtest=DownloadUploadTest#getDownloadLink_invalidToken_returns401` |
| 20 | GET Скачивание (без параметра path) — 400 | `mvn test -Dtest=DownloadUploadTest#getDownloadLink_missingPath_returns400` |
| 21 | GET Ссылка для загрузки (новый файл) — 200 | `mvn test -Dtest=DownloadUploadTest#getUploadLink_newFile_returns200` |
| 22 | GET Ссылка для загрузки (уже существует) — 409 | `mvn test -Dtest=DownloadUploadTest#getUploadLink_existingFileNoOverwrite_returns409` |
| 23 | GET Ссылка для загрузки (overwrite=true) — 200 | `mvn test -Dtest=DownloadUploadTest#getUploadLink_overwrite_returns200` |
| 24 | GET Ссылка для загрузки (невалидный токен) — 401 | `mvn test -Dtest=DownloadUploadTest#getUploadLink_invalidToken_returns401` |
| 25 | GET Ссылка для загрузки (без path) — 400 | `mvn test -Dtest=DownloadUploadTest#getUploadLink_missingPath_returns400` |
| 26 | POST Загрузка по URL (корректный URL) — 202 | `mvn test -Dtest=DownloadUploadTest#uploadByUrl_validUrl_returns202` |
| 27 | POST Загрузка по URL (невалидный токен) — 401 | `mvn test -Dtest=DownloadUploadTest#uploadByUrl_invalidToken_returns401` |
| 28 | POST Загрузка по URL (без url) — 400 | `mvn test -Dtest=DownloadUploadTest#uploadByUrl_missingUrl_returns400` |
| 29 | POST Загрузка по URL (без path) — 400 | `mvn test -Dtest=DownloadUploadTest#uploadByUrl_missingPath_returns400` |

### 3. Операции над файлами и папками (FileOperationsTest)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 30 | PUT Создание папки (новая) — 201 | `mvn test -Dtest=FileOperationsTest#createFolder_newFolder_returns201` |
| 31 | PUT Создание папки (уже существует) — 409 | `mvn test -Dtest=FileOperationsTest#createFolder_alreadyExists_returns409` |
| 32 | PUT Создание папки (невалидный токен) — 401 | `mvn test -Dtest=FileOperationsTest#createFolder_invalidToken_returns401` |
| 33 | PUT Создание папки (без path) — 400 | `mvn test -Dtest=FileOperationsTest#createFolder_missingPath_returns400` |
| 34 | PUT Создание папки (родитель не существует) — 409 | `mvn test -Dtest=FileOperationsTest#createFolder_parentNotExists_returns409` |
| 35 | POST Копирование (корректное) — 201 | `mvn test -Dtest=FileOperationsTest#copyResource_validCopy_returns201` |
| 36 | POST Копирование (назначение существует) — 409 | `mvn test -Dtest=FileOperationsTest#copyResource_destExists_returns409` |
| 37 | POST Копирование (источник не найден) — 404 | `mvn test -Dtest=FileOperationsTest#copyResource_sourceNotFound_returns404` |
| 38 | POST Копирование (невалидный токен) — 401 | `mvn test -Dtest=FileOperationsTest#copyResource_invalidToken_returns401` |
| 39 | POST Копирование (overwrite=true) — 201 | `mvn test -Dtest=FileOperationsTest#copyResource_overwrite_returns201` |
| 40 | POST Перемещение (корректное) — 201 | `mvn test -Dtest=FileOperationsTest#moveResource_validMove_returns201` |
| 41 | POST Перемещение (источник не найден) — 404 | `mvn test -Dtest=FileOperationsTest#moveResource_sourceNotFound_returns404` |
| 42 | POST Перемещение (назначение существует) — 409 | `mvn test -Dtest=FileOperationsTest#moveResource_destExists_returns409` |
| 43 | POST Перемещение (невалидный токен) — 401 | `mvn test -Dtest=FileOperationsTest#moveResource_invalidToken_returns401` |
| 44 | DELETE Удаление (существующая папка) — 204 | `mvn test -Dtest=FileOperationsTest#deleteResource_existingFolder_returns204` |
| 45 | DELETE Удаление (не существует) — 404 | `mvn test -Dtest=FileOperationsTest#deleteResource_notFound_returns404` |
| 46 | DELETE Удаление (невалидный токен) — 401 | `mvn test -Dtest=FileOperationsTest#deleteResource_invalidToken_returns401` |
| 47 | DELETE Удаление (без path) — 400 | `mvn test -Dtest=FileOperationsTest#deleteResource_missingPath_returns400` |
| 48 | DELETE Удаление (в Корзину) — 204 | `mvn test -Dtest=FileOperationsTest#deleteResource_toTrash_returns204` |
| 49 | GET Статус операции (неверный ID) — 404 | `mvn test -Dtest=FileOperationsTest#getOperationStatus_invalidId_returns404` |
| 50 | GET Статус операции (невалидный токен) — 401 | `mvn test -Dtest=FileOperationsTest#getOperationStatus_invalidToken_returns401` |
| 51 | GET Статус операции (из асинхронной) — 200 | `mvn test -Dtest=FileOperationsTest#getOperationStatus_fromAsyncOperation_returns200` |

### 4. Публичные файлы и папки (PublicResourcesTest)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 52 | PUT Публикация папки — 200 | `mvn test -Dtest=PublicResourcesTest#publishResource_existingFolder_returns200` |
| 53 | PUT Публикация файла — 200 | `mvn test -Dtest=PublicResourcesTest#publishResource_existingFile_returns200` |
| 54 | PUT Публикация (не найден) — 404 | `mvn test -Dtest=PublicResourcesTest#publishResource_notFound_returns404` |
| 55 | PUT Публикация (невалидный токен) — 401 | `mvn test -Dtest=PublicResourcesTest#publishResource_invalidToken_returns401` |
| 56 | GET Список публичных ресурсов — 200 | `mvn test -Dtest=PublicResourcesTest#getPublishedResources_validToken_returns200` |
| 57 | GET Список публ. ресурсов (limit=5) — 200 | `mvn test -Dtest=PublicResourcesTest#getPublishedResources_withLimit_returns200` |
| 58 | GET Список публ. ресурсов (невалидный токен) — 401 | `mvn test -Dtest=PublicResourcesTest#getPublishedResources_invalidToken_returns401` |
| 59 | GET Метаинформация (валидный key) — 200 | `mvn test -Dtest=PublicResourcesTest#getPublicResourceInfo_validKey_returns200` |
| 60 | GET Метаинформация (невалидный key) — 404 | `mvn test -Dtest=PublicResourcesTest#getPublicResourceInfo_invalidKey_returns404` |
| 61 | GET Метаинформация (без key) — 400 | `mvn test -Dtest=PublicResourcesTest#getPublicResourceInfo_missingKey_returns400` |
| 62 | GET Скачивание публичного (валидный key) — 200 | `mvn test -Dtest=PublicResourcesTest#downloadPublicResource_validKey_returns200` |
| 63 | GET Скачивание публичного (невалидный key) — 404 | `mvn test -Dtest=PublicResourcesTest#downloadPublicResource_invalidKey_returns404` |
| 64 | GET Скачивание публичного (без key) — 400 | `mvn test -Dtest=PublicResourcesTest#downloadPublicResource_missingKey_returns400` |
| 65 | POST Сохранение в «Загрузки» (валидный key) — 201 | `mvn test -Dtest=PublicResourcesTest#savePublicResourceToDisk_validKey_returns201` |
| 66 | POST Сохранение (невалидный key) — 404 | `mvn test -Dtest=PublicResourcesTest#savePublicResourceToDisk_invalidKey_returns404` |
| 67 | POST Сохранение (невалидный токен) — 401 | `mvn test -Dtest=PublicResourcesTest#savePublicResourceToDisk_invalidToken_returns401` |
| 68 | POST Сохранение (без key) — 400 | `mvn test -Dtest=PublicResourcesTest#savePublicResourceToDisk_missingKey_returns400` |
| 69 | PUT Снятие публикации (валидный путь) — 200 | `mvn test -Dtest=PublicResourcesTest#unpublishResource_publishedResource_returns200` |
| 70 | PUT Снятие публикации (не найден) — 404 | `mvn test -Dtest=PublicResourcesTest#unpublishResource_notFound_returns404` |
| 71 | PUT Снятие публикации (невалидный токен) — 401 | `mvn test -Dtest=PublicResourcesTest#unpublishResource_invalidToken_returns401` |

### 5. Файлы и папки на общем диске (SharedDriveFilesTest)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 72 | GET Метаинформация (не найден) — 404 | `mvn test -Dtest=SharedDriveFilesTest#getSharedDriveResource_notFound_returns404` |
| 73 | GET Метаинформация (невалидный токен) — 401 | `mvn test -Dtest=SharedDriveFilesTest#getSharedDriveResource_invalidToken_returns401` |
| 74 | PUT Создание папки (невалидный токен) — 401 | `mvn test -Dtest=SharedDriveFilesTest#createFolderOnSharedDrive_invalidToken_returns401` |
| 75 | POST Копирование (источник не найден) — 404 | `mvn test -Dtest=SharedDriveFilesTest#copyOnSharedDrive_sourceNotFound_returns404` |
| 76 | POST Копирование (невалидный токен) — 401 | `mvn test -Dtest=SharedDriveFilesTest#copyOnSharedDrive_invalidToken_returns401` |
| 77 | POST Перемещение (источник не найден) — 404 | `mvn test -Dtest=SharedDriveFilesTest#moveOnSharedDrive_sourceNotFound_returns404` |
| 78 | POST Перемещение (невалидный токен) — 401 | `mvn test -Dtest=SharedDriveFilesTest#moveOnSharedDrive_invalidToken_returns401` |
| 79 | DELETE Удаление (не найден) — 404 | `mvn test -Dtest=SharedDriveFilesTest#deleteOnSharedDrive_notFound_returns404` |
| 80 | DELETE Удаление (невалидный токен) — 401 | `mvn test -Dtest=SharedDriveFilesTest#deleteOnSharedDrive_invalidToken_returns401` |

### 6. Общие диски организации (SharedDrivesTest)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 81 | GET Общие диски (проверка доступности) — 200/404 | `mvn test -Dtest=SharedDrivesTest#getSharedDrives_checkAvailability_returns200or404` |
| 82 | GET Общие диски (невалидный токен) — 401 | `mvn test -Dtest=SharedDrivesTest#getSharedDrives_invalidToken_returns401` |
| 83 | GET Общие диски (без авторизации) — 401 | `mvn test -Dtest=SharedDrivesTest#getSharedDrives_noAuth_returns401` |
| 84 | GET Общие диски (pagination) — 200/404 | `mvn test -Dtest=SharedDrivesTest#getSharedDrives_withPagination_returns200or404` |
| 85 | GET Общие диски (неверный подпуть) — 404 | `mvn test -Dtest=SharedDrivesTest#getSharedDrives_nonExistentSubPath_returns404` |

### 7. Корзина (TrashTest)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 86 | GET Содержимое Корзины — 200 | `mvn test -Dtest=TrashTest#getTrashContents_validToken_returns200` |
| 87 | GET Содержимое Корзины (с path) — 200 | `mvn test -Dtest=TrashTest#getTrashContents_withPath_returns200` |
| 88 | GET Содержимое Корзины (limit=5) — 200 | `mvn test -Dtest=TrashTest#getTrashContents_withLimit_returns200` |
| 89 | GET Содержимое Корзины (невалидный токен) — 401 | `mvn test -Dtest=TrashTest#getTrashContents_invalidToken_returns401` |
| 90 | GET Содержимое Корзины (без авторизации) — 401 | `mvn test -Dtest=TrashTest#getTrashContents_noAuth_returns401` |
| 91 | GET Содержимое Корзины (неверный путь) — 404 | `mvn test -Dtest=TrashTest#getTrashContents_nonExistentPath_returns404` |
| 92 | GET Содержимое Корзины (sort и offset) — 200 | `mvn test -Dtest=TrashTest#getTrashContents_withSortAndOffset_returns200` |
| 93 | PUT Восстановление (существующий ресурс) — 201 | `mvn test -Dtest=TrashTest#restoreFromTrash_existingResource_returns201` |
| 94 | PUT Восстановление (не найден) — 404 | `mvn test -Dtest=TrashTest#restoreFromTrash_notFound_returns404` |
| 95 | PUT Восстановление (невалидный токен) — 401 | `mvn test -Dtest=TrashTest#restoreFromTrash_invalidToken_returns401` |
| 96 | PUT Восстановление (без авторизации) — 401 | `mvn test -Dtest=TrashTest#restoreFromTrash_noAuth_returns401` |
| 97 | PUT Восстановление (с новым именем) — 201/404 | `mvn test -Dtest=TrashTest#restoreFromTrash_withName_returns201or404` |
| 98 | DELETE Очистка (невалидный токен) — 401 | `mvn test -Dtest=TrashTest#clearTrash_invalidToken_returns401` |
| 99 | DELETE Очистка (без авторизации) — 401 | `mvn test -Dtest=TrashTest#clearTrash_noAuth_returns401` |
| 100 | DELETE Очистка (ресурс не найден) — 404 | `mvn test -Dtest=TrashTest#clearTrash_specificResource_notFound_returns404` |
| 101 | DELETE Очистка (полная) — 204 | `mvn test -Dtest=TrashTest#clearTrash_fullClear_returns204` |

### 8. SDK Тесты (yandex-disk-restapi-java)
| № | Описание теста | Команда для запуска |
|---|----------------|---------------------|
| 102 | QueryBuilderTest | `mvn test -Dtest=QueryBuilderTest` |
| 103 | RestClientTest | `mvn test -Dtest=RestClientTest` |
| 104 | ISO8601Test / ResourcePathTest | `mvn test -Dtest=ISO8601Test` или `ResourcePathTest` |

---


### Из IntelliJ IDEA

1. Откройте проект в IntelliJ IDEA (`File → Open → выберите корневую папку проекта`)
2. Дождитесь загрузки зависимостей Maven (индексация проекта)
3. Укажите токен в `src/test/resources/config.properties`
4. Правой кнопкой мыши на нужном тестовом файле → `Run`
5. Или нажмите зелёную стрелку ▶ рядом с тестовым методом/классом

---

## Что делать, если тесты не запускаются

### Проблема: «OAuth-токен не задан!»
Убедитесь, что вы указали реальный OAuth-токен одним из трёх способов (см. выше). Проверьте, что значение не осталось `YOUR_OAUTH_TOKEN_HERE`.

### Проблема: Maven не находит зависимости
**Решение:**
```bash
  mvn clean install -U
```
Если `com.yandex.android:disk-restapi-sdk:1.03` не загружается:
1. Проверьте доступность [Maven Central](https://mvnrepository.com/artifact/com.yandex.android/disk-restapi-sdk/1.03)
2. Если артефакт недоступен, скачайте его вручную и установите локально:
```bash
   mvn install:install-file \
     -Dfile=disk-restapi-sdk-1.03.jar \
     -DgroupId=com.yandex.android \
     -DartifactId=disk-restapi-sdk \
     -Dversion=1.03 \
     -Dpackaging=jar
```


### Проблема: Ошибка «java: error: release version 17 not supported»
**Решение:** Установите Java 17 или новее:
1. Скачайте JDK 17+ с [Adoptium](https://adoptium.net/) или [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. В IntelliJ IDEA: `File → Project Structure → Project → SDK` → выберите JDK 17+
3. Также проверьте: `File → Settings → Build → Compiler → Java Compiler → Target bytecode version` → 17

### Проблема: Ошибка кодировки
**Решение:**
1. В IntelliJ IDEA: `File → Settings → Editor → File Encodings` → установите **UTF-8** везде
2. При запуске через Maven добавьте: `mvn test -Dfile.encoding=UTF-8`
3. В Windows: установите кодировку консоли: `chcp 65001`

---

## Используемые технологии и библиотеки

| Технология | Версия | Назначение |
|-----------|--------|------------|
| Java | 17+ | Язык программирования |
| JUnit 5 | 5.10.2 | Фреймворк тестирования |
| REST Assured | 5.4.0 | HTTP-клиент для API-тестов |
| Yandex Disk SDK | 1.03 | Рекомендованная Яндексом библиотека |
| Maven | 3.8+ | Сборка и управление зависимостями |
| Maven Surefire | 3.2.5 | Запуск тестов через Maven |

---

## Примечания

- Тесты создают временные ресурсы в папке `disk:/autotest_temp` и удаляют их после завершения.
- Если тесты были прерваны аварийно, папка `autotest_temp` может остаться на Диске — удалите её вручную.
- Для полного покрытия документации (общие диски, администрирование) используйте аккаунт Яндекс 360 для бизнеса с правами администратора.
