import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

class Main {
    public static void main(String[] args) {
        VotingSystem system = new VotingSystem();
        system.loadData();
        
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nДобро пожаловать в систему электронного голосования!");
            System.out.println("1. Вход");
            System.out.println("2. Регистрация");
            System.out.println("3. Выход");
            System.out.print("Выберите действие: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            if (choice == 1) {
                system.login(scanner);
            } else if (choice == 2) {
                system.registerUser(scanner);
            } else if (choice == 3) {
                system.saveData();
                System.out.println("До свидания!");
                break;
            }
        }
        scanner.close();
    }
}

class VotingSystem {
    private Map<String, User> users = new HashMap<>();
    private Map<String, CEC> cecs = new HashMap<>();
    private Map<String, Candidate> candidates = new HashMap<>();
    private List<Voting> votings = new ArrayList<>();
    private SystemUser currentUser = null;
    
    public void loadData() {
        try {
            if (Files.exists(Paths.get("users.dat"))) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"));
                users = (Map<String, User>) ois.readObject();
                ois.close();
            }
            
            if (Files.exists(Paths.get("cecs.dat"))) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream("cecs.dat"));
                cecs = (Map<String, CEC>) ois.readObject();
                ois.close();
            }
            
            if (Files.exists(Paths.get("candidates.dat"))) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream("candidates.dat"));
                candidates = (Map<String, Candidate>) ois.readObject();
                ois.close();
            }
            
            if (Files.exists(Paths.get("votings.dat"))) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream("votings.dat"));
                votings = (List<Voting>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }
    
    public void saveData() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"));
            oos.writeObject(users);
            oos.close();
            
            oos = new ObjectOutputStream(new FileOutputStream("cecs.dat"));
            oos.writeObject(cecs);
            oos.close();
            
            oos = new ObjectOutputStream(new FileOutputStream("candidates.dat"));
            oos.writeObject(candidates);
            oos.close();
            
            oos = new ObjectOutputStream(new FileOutputStream("votings.dat"));
            oos.writeObject(votings);
            oos.close();
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }
    
    public void login(Scanner scanner) {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();
        
        if ("admin".equals(login)) {
            if ("admin123".equals(password)) {
                showAdminMenu(scanner);
                return;
            }
        }
        
        if (users.containsKey(login) && users.get(login).getPassword().equals(password)) {
            currentUser = users.get(login);
            showUserMenu(scanner);
        } else if (cecs.containsKey(login) && cecs.get(login).getPassword().equals(password)) {
            currentUser = cecs.get(login);
            showCECMenu(scanner);
        } else if (candidates.containsKey(login) && candidates.get(login).getPassword().equals(password)) {
            currentUser = candidates.get(login);
            showCandidateMenu(scanner);
        } else {
            System.out.println("Неверный логин или пароль!");
        }
    }
    
    public void registerUser(Scanner scanner) {
        System.out.println("\n=== Регистрация нового пользователя ===");
        System.out.print("Введите ФИО: ");
        String fullName = scanner.nextLine();
        System.out.print("Введите дату рождения (дд.мм.гггг): ");
        String birthDate = scanner.nextLine();
        System.out.print("Введите СНИЛС (при наличии): ");
        String snils = scanner.nextLine();
        System.out.print("Придумайте логин: ");
        String login = scanner.nextLine();
        System.out.print("Придумайте пароль: ");
        String password = scanner.nextLine();
        
        if (users.containsKey(login) || cecs.containsKey(login) || candidates.containsKey(login)) {
            System.out.println("Пользователь с таким логином уже существует!");
            return;
        }
        
        String uniqueId = generateUniqueId(fullName, birthDate, snils);
        User newUser = new User(login, password, fullName, birthDate, snils, uniqueId);
        users.put(login, newUser);
        System.out.println("Регистрация прошла успешно!");
    }
    
    private String generateUniqueId(String fullName, String birthDate, String snils) {
        if (snils != null && !snils.isEmpty()) {
            return "SNILS_" + snils;
        }
        return "NAME_" + fullName.hashCode() + "_" + birthDate.hashCode();
    }
    
    private void showAdminMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== Меню администратора ===");
            System.out.println("1. Просмотр списка пользователей");
            System.out.println("2. Удаление пользователя");
            System.out.println("3. Просмотр списка ЦИК");
            System.out.println("4. Удаление ЦИК");
            System.out.println("5. Создание ЦИК");
            System.out.println("6. Просмотр списка кандидатов");
            System.out.println("7. Удаление кандидата");
            System.out.println("8. Выход");
            System.out.print("Выберите действие: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    viewUsers();
                    break;
                case 2:
                    deleteUser(scanner);
                    break;
                case 3:
                    viewCECs();
                    break;
                case 4:
                    deleteCEC(scanner);
                    break;
                case 5:
                    createCEC(scanner);
                    break;
                case 6:
                    viewCandidates();
                    break;
                case 7:
                    deleteCandidate(scanner);
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Неверный выбор!");
            }
        }
    }
    
    private void showCECMenu(Scanner scanner) {
        CEC cec = (CEC) currentUser;
        while (true) {
            System.out.println("\n=== Меню ЦИК ===");
            System.out.println("1. Создать голосование");
            System.out.println("2. Добавить кандидата");
            System.out.println("3. Просмотр результатов с группировкой");
            System.out.println("4. Просмотр результатов с сортировкой");
            System.out.println("5. Выход");
            System.out.print("Выберите действие: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    createVoting(scanner);
                    break;
                case 2:
                    addCandidate(scanner);
                    break;
                case 3:
                    viewGroupedResults(scanner);
                    break;
                case 4:
                    viewSortedResults(scanner);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Неверный выбор!");
            }
        }
    }
    
    private void showCandidateMenu(Scanner scanner) {
        Candidate candidate = (Candidate) currentUser;
        while (true) {
            System.out.println("\n=== Меню кандидата ===");
            System.out.println("1. Заполнить данные о себе");
            System.out.println("2. Просмотр результатов предыдущего голосования");
            System.out.println("3. Просмотр всех голосований с участием");
            System.out.println("4. Выход");
            System.out.print("Выберите действие: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    fillCandidateData(scanner, candidate);
                    break;
                case 2:
                    viewPreviousVotingResults(candidate);
                    break;
                case 3:
                    viewAllParticipatedVotings(candidate);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Неверный выбор!");
            }
        }
    }
    
    private void showUserMenu(Scanner scanner) {
        User user = (User) currentUser;
        while (true) {
            System.out.println("\n=== Меню пользователя ===");
            System.out.println("1. Проголосовать");
            System.out.println("2. Просмотр списка кандидатов");
            System.out.println("3. Просмотр всех голосований");
            System.out.println("4. Выход");
            System.out.print("Выберите действие: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    vote(scanner, user);
                    break;
                case 2:
                    viewCandidatesList();
                    break;
                case 3:
                    viewAllVotings(user);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Неверный выбор!");
            }
        }
    }
    
    // Реализация недостающих методов
    private void viewUsers() {
        System.out.println("\n=== Список пользователей ===");
        users.values().forEach(user -> System.out.println(user.getLogin() + ": " + user.getFullName()));
    }
    
    private void deleteUser(Scanner scanner) {
        System.out.print("Введите логин пользователя для удаления: ");
        String login = scanner.nextLine();
        if (users.containsKey(login)) {
            users.remove(login);
            System.out.println("Пользователь удален.");
        } else {
            System.out.println("Пользователь не найден!");
        }
    }
    
    private void viewCECs() {
        System.out.println("\n=== Список ЦИК ===");
        cecs.values().forEach(cec -> System.out.println(cec.getLogin()));
    }
    
    private void deleteCEC(Scanner scanner) {
        System.out.print("Введите логин ЦИК для удаления: ");
        String login = scanner.nextLine();
        if (cecs.containsKey(login)) {
            cecs.remove(login);
            System.out.println("ЦИК удален.");
        } else {
            System.out.println("ЦИК не найден!");
        }
    }
    
    private void createCEC(Scanner scanner) {
        System.out.print("Введите логин для нового ЦИК: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();
        
        if (cecs.containsKey(login)) {
            System.out.println("ЦИК с таким логином уже существует!");
            return;
        }
        
        cecs.put(login, new CEC(login, password));
        System.out.println("ЦИК создан.");
    }
    
    private void viewCandidates() {
        System.out.println("\n=== Список кандидатов ===");
        candidates.values().forEach(candidate -> 
            System.out.println(candidate.getLogin() + ": " + candidate.getFullName()));
    }
    
    private void deleteCandidate(Scanner scanner) {
        System.out.print("Введите логин кандидата для удаления: ");
        String login = scanner.nextLine();
        if (candidates.containsKey(login)) {
            candidates.remove(login);
            System.out.println("Кандидат удален.");
        } else {
            System.out.println("Кандидат не найден!");
        }
    }
    
    private void createVoting(Scanner scanner) {
        System.out.print("Введите название голосования: ");
        String title = scanner.nextLine();
        System.out.print("Введите дату окончания (дд.мм.гггг): ");
        String endDateStr = scanner.nextLine();
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date endDate = sdf.parse(endDateStr);
            Voting voting = new Voting(title, endDate);
            votings.add(voting);
            System.out.println("Голосование создано.");
        } catch (Exception e) {
            System.out.println("Неверный формат даты!");
        }
    }
    
    private void addCandidate(Scanner scanner) {
        System.out.print("Введите логин кандидата: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();
        System.out.print("Введите ФИО кандидата: ");
        String fullName = scanner.nextLine();
        
        if (candidates.containsKey(login)) {
            System.out.println("Кандидат с таким логином уже существует!");
            return;
        }
        
        candidates.put(login, new Candidate(login, password, fullName));
        System.out.println("Кандидат добавлен.");
    }
    
    private void viewGroupedResults(Scanner scanner) {
        System.out.println("\n=== Результаты с группировкой ===");
        // Здесь должна быть логика группировки результатов
    }
    
    private void viewSortedResults(Scanner scanner) {
        System.out.println("\n=== Результаты с сортировкой ===");
        // Здесь должна быть логика сортировки результатов
    }
    
    private void fillCandidateData(Scanner scanner, Candidate candidate) {
        System.out.print("Введите партию: ");
        candidate.setParty(scanner.nextLine());
        System.out.print("Введите биографию: ");
        candidate.setBio(scanner.nextLine());
        System.out.println("Данные обновлены.");
    }
    
    private void viewPreviousVotingResults(Candidate candidate) {
        System.out.println("\n=== Результаты предыдущего голосования ===");
        // Здесь должна быть логика вывода результатов
    }
    
    private void viewAllParticipatedVotings(Candidate candidate) {
        System.out.println("\n=== Все голосования с участием ===");
        // Здесь должна быть логика вывода голосований
    }
    
    private void vote(Scanner scanner, User user) {
        System.out.println("\n=== Голосование ===");
        // Здесь должна быть логика голосования
    }
    
    private void viewCandidatesList() {
        System.out.println("\n=== Список кандидатов ===");
        candidates.values().forEach(candidate -> 
            System.out.println(candidate.getFullName() + " (" + candidate.getParty() + ")"));
    }
    
    private void viewAllVotings(User user) {
        System.out.println("\n=== Все голосования ===");
        votings.forEach(voting -> System.out.println(voting.getTitle() + 
            " (до " + voting.getEndDate() + ")"));
    }
    
    abstract class SystemUser implements Serializable {
        protected String login;
        protected String password;
        
        public SystemUser(String login, String password) {
            this.login = login;
            this.password = password;
        }
        
        public String getLogin() { return login; }
        public String getPassword() { return password; }
    }
    
    class User extends SystemUser {
        private String fullName;
        private String birthDate;
        private String snils;
        private String uniqueId;
        private List<String> votedIn = new ArrayList<>();
        
        public User(String login, String password, String fullName, String birthDate, String snils, String uniqueId) {
            super(login, password);
            this.fullName = fullName;
            this.birthDate = birthDate;
            this.snils = snils;
            this.uniqueId = uniqueId;
        }
        
        public String getFullName() { return fullName; }
        public String getBirthDate() { return birthDate; }
        public String getSnils() { return snils; }
        public String getUniqueId() { return uniqueId; }
        public List<String> getVotedIn() { return votedIn; }
    }
    
    class CEC extends SystemUser {
        public CEC(String login, String password) {
            super(login, password);
        }
    }
    
    class Candidate extends SystemUser {
        private String fullName;
        private String party;
        private String bio;
        private List<String> participatedIn = new ArrayList<>();
        
        public Candidate(String login, String password, String fullName) {
            super(login, password);
            this.fullName = fullName;
        }
        
        public String getFullName() { return fullName; }
        public String getParty() { return party; }
        public String getBio() { return bio; }
        public List<String> getParticipatedIn() { return participatedIn; }
        
        public void setParty(String party) { this.party = party; }
        public void setBio(String bio) { this.bio = bio; }
    }
    
    class Voting implements Serializable {
        private String id;
        private String title;
        private Date startDate;
        private Date endDate;
        private List<String> candidateIds = new ArrayList<>();
        private Map<String, Integer> votes = new HashMap<>();
        private List<String> voterIds = new ArrayList<>();
        
        public Voting(String title, Date endDate) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.startDate = new Date();
            this.endDate = endDate;
        }
        
        public String getId() { return id; }
        public String getTitle() { return title; }
        public Date getStartDate() { return startDate; }
        public Date getEndDate() { return endDate; }
        public List<String> getCandidateIds() { return candidateIds; }
        public Map<String, Integer> getVotes() { return votes; }
        public List<String> getVoterIds() { return voterIds; }
    }
}
