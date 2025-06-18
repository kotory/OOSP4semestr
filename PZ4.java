import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorageAPI {
    // Перечисление для статуса данных
    enum DataStatus {
        READ_ONLY,
        MUTABLE
    }

    // Класс для хранения записи данных
    static class DataRecord {
        private String id;
        private Map<String, Object> data;
        private DataStatus status;
        private long createdAt;
        private long updatedAt;

        public DataRecord(String id, Map<String, Object> data, DataStatus status) {
            this.id = id;
            this.data = new HashMap<>(data);
            this.status = status;
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = this.createdAt;
        }

        // Геттеры и сеттеры
        public String getId() { return id; }
        public Map<String, Object> getData() { return new HashMap<>(data); }
        public DataStatus getStatus() { return status; }
        public long getCreatedAt() { return createdAt; }
        public long getUpdatedAt() { return updatedAt; }
        
        public void updateData(Map<String, Object> newData) {
            this.data = new HashMap<>(newData);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    // Класс для кэширования данных
    static class DataCache {
        private final Map<String, DataRecord> cache;
        private final int maxSize;

        public DataCache(int maxSize) {
            this.maxSize = maxSize;
            this.cache = new ConcurrentHashMap<>();
        }

        public Optional<DataRecord> get(String dataId) {
            return Optional.ofNullable(cache.get(dataId));
        }

        public void put(DataRecord record) {
            if (record.getStatus() == DataStatus.READ_ONLY) {
                if (cache.size() >= maxSize) {
                    // Удаляем самый старый элемент (простая реализация)
                    cache.keySet().stream()
                        .min(Comparator.comparingLong(id -> cache.get(id).getUpdatedAt()))
                        .ifPresent(cache::remove);
                }
                cache.put(record.getId(), record);
            }
        }

        public void clear() {
            cache.clear();
        }
    }

    // Основной класс API
    static class DataAPI {
        private final Map<String, DataRecord> database;
        private final DataCache cache;

        public DataAPI(int cacheSize) {
            this.database = new ConcurrentHashMap<>();
            this.cache = new DataCache(cacheSize);
        }

        public String uploadData(Map<String, Object> data, boolean isReadOnly) {
            String dataId = UUID.randomUUID().toString();
            DataStatus status = isReadOnly ? DataStatus.READ_ONLY : DataStatus.MUTABLE;
            DataRecord record = new DataRecord(dataId, data, status);
            
            database.put(dataId, record);
            
            if (isReadOnly) {
                cache.put(record);
            }
            
            return dataId;
        }

        public Optional<Map<String, Object>> getData(String dataId) {
            // Сначала проверяем кэш
            Optional<DataRecord> cached = cache.get(dataId);
            if (cached.isPresent()) {
                return Optional.of(cached.get().getData());
            }
            
            // Если нет в кэше, ищем в БД
            DataRecord record = database.get(dataId);
            if (record != null) {
                // Если данные read-only, добавляем в кэш
                if (record.getStatus() == DataStatus.READ_ONLY) {
                    cache.put(record);
                }
                return Optional.of(record.getData());
            }
            
            return Optional.empty();
        }

        public boolean updateData(String dataId, Map<String, Object> newData) {
            DataRecord record = database.get(dataId);
            if (record == null) {
                return false;
            }
            
            if (record.getStatus() == DataStatus.READ_ONLY) {
                return false;
            }
            
            record.updateData(newData);
            database.put(dataId, record);
            return true;
        }

        public Map<String, Map<String, Object>> generateReport(List<String> dataIds) {
            Map<String, Map<String, Object>> report = new HashMap<>();
            
            for (String dataId : dataIds) {
                Optional<Map<String, Object>> data = getData(dataId);
                data.ifPresent(d -> report.put(dataId, d));
            }
            
            return report;
        }

        public String exportResults(List<String> dataIds) {
            Map<String, Map<String, Object>> report = generateReport(dataIds);
            return report.toString(); // Простое преобразование в строку
        }

        public boolean refreshData(String dataId) {
            // Для read-only данных проверяем кэш
            if (cache.get(dataId).isPresent()) {
                return true;
            }
            
            // Для mutable данных проверяем наличие в БД
            return database.containsKey(dataId);
        }
    }

    public static void main(String[] args) {
        // Инициализация API
        DataAPI api = new DataAPI(128);

        // 1. Загрузка данных
        Map<String, Object> readOnlyData = new HashMap<>();
        readOnlyData.put("type", "configuration");
        readOnlyData.put("value", 100);
        String roId = api.uploadData(readOnlyData, true); // Read-only данные

        Map<String, Object> mutableData = new HashMap<>();
        mutableData.put("type", "userData");
        mutableData.put("name", "John Doe");
        mutableData.put("score", 0);
        String mutableId = api.uploadData(mutableData, false); // Mutable данные

        // 2. Получение данных
        System.out.println("Read-only data:");
        api.getData(roId).ifPresent(System.out::println);

        System.out.println("\nMutable data:");
        api.getData(mutableId).ifPresent(System.out::println);

        // 3. Обновление данных
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("type", "userData");
        updatedData.put("name", "John Doe");
        updatedData.put("score", 150);
        
        boolean updateResult1 = api.updateData(roId, updatedData);
        System.out.println("\nUpdate read-only data: " + (updateResult1 ? "Success" : "Failed (expected)"));
        
        boolean updateResult2 = api.updateData(mutableId, updatedData);
        System.out.println("Update mutable data: " + (updateResult2 ? "Success" : "Failed"));

        // 4. Генерация отчета
        System.out.println("\nGenerated report:");
        Map<String, Map<String, Object>> report = api.generateReport(Arrays.asList(roId, mutableId));
        report.forEach((id, data) -> System.out.println(id + ": " + data));

        // 5. Экспорт результатов
        System.out.println("\nExported results:");
        String export = api.exportResults(Arrays.asList(roId, mutableId));
        System.out.println(export);

        // 6. Обновление данных (имитация таймера)
        System.out.println("\nRefresh status:");
        System.out.println("Read-only data exists: " + api.refreshData(roId));
        System.out.println("Mutable data exists: " + api.refreshData(mutableId));
        System.out.println("Non-existent data: " + api.refreshData("invalid_id"));
    }
}
