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

```bash
   
      # Запуск всех тестов
      mvn clean test
        
      # Запуск с указанием токена через параметр
      mvn clean test -Doauth.token=YOUR_OAUTH_TOKEN_HERE
        
        # Запуск конкретной группы тестов
      mvn clean test -Dtest=diskinfo.DiskInfoTest
      mvn clean test -Dtest=downloadupload.DownloadUploadTest
      mvn clean test -Dtest=operations.FileOperationsTest
      mvn clean test -Dtest=publish.PublicResourcesTest
      mvn clean test -Dtest=shared.SharedDrivesTest
      mvn clean test -Dtest=.SharedDriveFilesTest
      mvn clean test -Dtest=trash.TrashTest
        
      mvn clean test -Dtest=diskinfo.DiskInfoTest#getDiskInfo_validToken_returns200 
````

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
