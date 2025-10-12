package mainapp.cursach;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PhoneBookApp extends Application {

    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final File storageFile = new File("phonebook.dat");

    @Override
    public void start(Stage stage) {
        loadFromFile(); // загрузка базы при старте

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // --- Верх: поиск ---
        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по ФИО или номеру");
        HBox top = new HBox(10, searchField);
        root.setTop(top);

        // --- Центр: таблица контактов ---
        FilteredList<Contact> filteredContacts = new FilteredList<>(contacts, c -> true);
        SortedList<Contact> sortedContacts = new SortedList<>(filteredContacts);
        TableView<Contact> table = new TableView<>(sortedContacts);
        sortedContacts.comparatorProperty().bind(table.comparatorProperty()); // связываем сортировку

        TableColumn<Contact, String> nameCol = new TableColumn<>("ФИО");
        nameCol.setCellValueFactory(data -> data.getValue().fullNameProperty());
        nameCol.setPrefWidth(200);
        table.getColumns().add(nameCol);

        // --- Правая панель: телефоны ---
        VBox rightPane = new VBox(8);
        rightPane.setPadding(new Insets(0, 0, 0, 10));
        rightPane.setPrefWidth(300);

        Label phoneLabel = new Label("Телефоны:");
        ListView<String> phoneList = new ListView<>();
        rightPane.getChildren().addAll(phoneLabel, phoneList);

        HBox mainLayout = new HBox(10, table, rightPane);
        root.setCenter(mainLayout);

        // --- Нижняя панель: кнопки управления ---
        Button addContact = new Button("Добавить контакт");
        Button delContact = new Button("Удалить контакт");
        Button addPhone = new Button("Добавить номер");
        Button saveButton = new Button("Сохранить");
        Button loadButton = new Button("Загрузить");

        HBox bottom = new HBox(10, addContact, delContact, addPhone, saveButton, loadButton);
        root.setBottom(bottom);

        // --- Отображение телефонов выбранного контакта ---
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            phoneList.getItems().clear();
            if (sel != null) {
                for (Phone p : sel.getPhones()) {
                    phoneList.getItems().add(p.getType() + " — " + p.getNumber());
                }
            }
        });

        // --- Добавление контакта ---
        addContact.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Введите ФИО нового контакта:");
            dialog.showAndWait().ifPresent(name -> contacts.add(new Contact(name)));
        });

        // --- Удаление контакта ---
        delContact.setOnAction(e -> {
            Contact sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) contacts.remove(sel);
        });

        // --- Добавление номера телефона ---
        addPhone.setOnAction(e -> {
            Contact sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            TextInputDialog numberDialog = new TextInputDialog();
            numberDialog.setHeaderText("Введите номер телефона:");
            numberDialog.showAndWait().ifPresent(number -> {
                TextInputDialog typeDialog = new TextInputDialog();
                typeDialog.setHeaderText("Введите тип номера (сотовый, домашний, факс):");
                typeDialog.showAndWait().ifPresent(type -> {
                    sel.getPhones().add(new Phone(number, type));
                    phoneList.getItems().clear();
                    for (Phone p : sel.getPhones()) {
                        phoneList.getItems().add(p.getType() + " — " + p.getNumber());
                    }
                });
            });
        });

        // --- Сохранение ---
        saveButton.setOnAction(e -> saveToFile());

        // --- Загрузка ---
        loadButton.setOnAction(e -> {
            contacts.clear();
            loadFromFile();
        });

        // --- Поиск по ФИО или номеру телефона ---
        searchField.textProperty().addListener((obs, old, nw) -> {
            String query = nw.toLowerCase().trim();
            filteredContacts.setPredicate(c -> {
                if (query.isEmpty()) return true;

                boolean matchesName = c.getFullName() != null && c.getFullName().toLowerCase().contains(query);
                boolean matchesPhone = c.getPhones().stream().anyMatch(p -> p.getNumber() != null && p.getNumber().toLowerCase().contains(query));

                return matchesName || matchesPhone;
            });
        });

        Scene scene = new Scene(root, 850, 400);
        stage.setScene(scene);
        stage.setTitle("Телефонный справочник");
        stage.show();
    }

    // ===================== Сохранение и загрузка =====================
    private void saveToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            out.writeObject(new ArrayList<>(contacts));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        if (!storageFile.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(storageFile))) {
            List<Contact> list = (List<Contact>) in.readObject();
            contacts.addAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ===================== Модели =====================
    public static class Contact implements Serializable {
        private static final long serialVersionUID = 1L;
        private String fullName;
        private List<Phone> phones = new ArrayList<>();
        private transient javafx.beans.property.StringProperty fullNameProp;

        public Contact(String name) {
            this.fullName = name;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String name) {
            fullName = name;
            if (fullNameProp != null) fullNameProp.set(name);
        }

        public javafx.beans.property.StringProperty fullNameProperty() {
            if (fullNameProp == null) fullNameProp = new javafx.beans.property.SimpleStringProperty(fullName);
            return fullNameProp;
        }

        public List<Phone> getPhones() {
            return phones;
        }
    }

    public static class Phone implements Serializable {
        private static final long serialVersionUID = 1L;
        private String number;
        private String type;

        public Phone(String number, String type) {
            this.number = number;
            this.type = type;
        }

        public String getNumber() {
            return number;
        }

        public String getType() {
            return type;
        }
    }
}

