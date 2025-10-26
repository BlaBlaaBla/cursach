1. Клонировать проект
git clone <ссылка_на_репозиторий>
cd cursach

2. Дать права на Maven Wrapper
chmod +x mvnw

3. Очистить локальный Maven-кэш (опционально, для чистой проверки)
rm -rf ~/.m2/repository

4. Собрать проект и скачать все зависимости
./mvnw clean package -U

5. Запустить приложение Через Maven

./mvnw javafx:run

Приложение тестировалось на Kali Linux с JDK-21.
