package client.model;

import client.model.entity.User;
import client.model.entity.UserProfile;
import client.model.utils.Pair;
import client.model.utils.StageShower;
import client.model.utils.Support;
import client.UserPreferences;
import client.controller.DialogControl;
import client.controller.MainControl;
import client.controller.RootControl;
import client.model.utils.BufferedWriterWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ClientApp extends Application {

    /** Чарсет, используемый по умолчанию при передаче запросов на сервер */
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /** Название окна приложения */
    private static final String TITLE = "Hermes";

    /** Актуальная версия приложения */
    private static final String VERSION = "v1.71";

    /** Авторизовавшийся пользователь */
    private static Pair<User, UserProfile> currentUser;

    /** IP- адрес сервера */
    private static String ip = "10.50.2.254";

    /** Переменная, содержащая сведения об активности соединения с сервером */
    private static Boolean isConnectActive = false;

    private static Map<String, Pair<User, UserProfile>> clientList = new HashMap<>();

    private static Socket clientSocket;
    private static Stage primaryStage;
    private static boolean hidePrimaryStage;
    private static BorderPane rootLayout;
    private static RootControl rootControl;
    private static MainControl mainControl;
    private static Receive receive;
    private static BufferedWriterWrapper out;
    private final Image ico = new Image(getClass().getResource("/Full-logo.jpg").toExternalForm());

    /**
     * Две переменные, необходимые для переноса окна приложения
     * в случае primaryStage.initStyle(StageStyle.UNDECORATED)
     */
    private double xOffset;
    private double yOffset;

    private static Gson gson;


    @Override
    public void start(Stage primaryStage) throws Exception {
        UserPreferences.loadPrefIpArdess();
        //primaryStage.initStyle(StageStyle.UNDECORATED);
        this.primaryStage = primaryStage;
        this.primaryStage.setMinWidth(600);
        this.primaryStage.setMinHeight(550);
        this.primaryStage.setTitle(TITLE);
        this.primaryStage.setOnCloseRequest(event->{
            Platform.exit();
            System.exit(0);
        });
        this.primaryStage.getIcons().add(ico);
        log.info("Application started.");

        initRootLayout();
        showAuthScene();
    }

    public static void main (String[]args){
        launch(args);
    }

    /**
     * Устанавливает соединение с сервером.
     */
    public static void connect(){
        if (clientSocket==null) {
            log.info("Connecting to server...");
            try {
                try {
                    clientSocket = new Socket(InetAddress.getByName(ip), 4004);
                }
                catch (ConnectException e){
                    log.info("Connect failed!");
                    Support.stopServer();
                }
                if (out == null) {
                    out = new BufferedWriterWrapper(new OutputStreamWriter(clientSocket.getOutputStream(), DEFAULT_CHARSET));
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), DEFAULT_CHARSET));
                out.write(Instruction.CONNECT.getCommand());
                while (true) {
                    String response = in.readLine();
                    response=BufferedWriterWrapper.decrypt(response);
                    if (response.startsWith(Instruction.CONNECT_OK.getCommand())) {
                        log.info("Connecting is established.");
                        isConnectActive=true;
                        UserPreferences.savePrefIpAdress(ip);
                        out.write(Instruction.CONNECT_OK.getCommand());
                        break;
                    }
                }
                if (receive==null){
                    receive=new Receive();
                }
                if (!receive.isAlive()){
                    receive.start();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Восстанавливает соединение с сервером
     * @throws InterruptedException
     */
    public static void tryingConnect() throws InterruptedException {
        int count = 0;
        while (count!=10){
            try {
                count++;
                log.info("Trying connect to server...");
                clientSocket = new Socket(InetAddress.getByName(ip), 4004);
                Support.alertTryingConnect();
            }
            catch (ConnectException | UnknownHostException e){
                log.error("Connect failed!");
                Thread.sleep(5000);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Инизиализирует задний слой приложения (панель меню)
     * @throws Exception
     */
    public void initRootLayout() throws Exception{
        log.info("Initialization root layout...");
        {
            FXMLLoader rootLoader = new FXMLLoader();
            rootLoader.setLocation(ClientApp.class.getResource("/RootLayout.fxml"));
            rootLayout = (BorderPane) rootLoader.load();
            rootControl = rootLoader.getController();
            StageShower.setRootControl(rootControl);
            Scene rootScene = new Scene(rootLayout);

            rootScene.setOnMousePressed(event->{
                xOffset = primaryStage.getX() - event.getScreenX();
                yOffset = primaryStage.getY() - event.getScreenY();
            });

            rootScene.setOnMouseDragged(event->{
                primaryStage.setX(event.getScreenX() + xOffset);
                primaryStage.setY(event.getScreenY() + yOffset);
            });

            primaryStage.setResizable(true);
            primaryStage.setScene(rootScene);
            primaryStage.show();
        }
        log.info("Complete");
    }

    /**
     * Инизиализирует окно авторизации приложения
     * @throws IOException
     */
    public static void showAuthScene() throws IOException {
        log.info("Initialization authorization scene...");
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(ClientApp.class.getResource("/Authorization.fxml"));
        AnchorPane authScene = (AnchorPane) authLoader.load();
        rootLayout.setCenter(authScene);
        log.info("Complete");
        UserPreferences.setAuthControl(authLoader.getController());
        UserPreferences.loadPrefUserName();
        if (receive == null) {
            receive = new Receive();
        }
        receive.setRootControl(rootControl);
        receive.setAuthControl(authLoader.getController());
    }

    /**
     * Инициализирует основное окно приложения
     * @throws IOException
     */
    public static void showMainScene() throws IOException {
        log.info("Initialization main application scene...");
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setLocation(ClientApp.class.getResource("/MainApp.fxml"));
        AnchorPane mainScene = (AnchorPane) mainLoader.load();
        rootLayout.setCenter(mainScene);
        mainControl = mainLoader.getController();
        rootControl.setMainControl(mainControl);
        StageShower.setMainControl(mainControl);
        log.info("Complete");

        if (receive == null) {
            receive = new Receive();
        }
        receive.setMainControl(mainControl);
        mainControl.setRootControl(rootControl);
        receive.setMainControlInitTrue();
    }

    /**
     * Инициализирует окно регистраци приложения
     * @throws IOException
     */
    public static void registrationScene() throws IOException {
        log.info("Initialization registration scene...");
        FXMLLoader regLoader = new FXMLLoader();
        regLoader.setLocation(ClientApp.class.getResource("/Registration.fxml"));
        AnchorPane regScene = (AnchorPane) regLoader.load();
        rootLayout.setCenter((regScene));
        log.info("Complete");

        if (receive == null) {
            receive = new Receive();
        }
        receive.setRegControl(regLoader.getController());
        receive.setRegControlInitTrue();
    }

    /**
     * НЕ ФУНКЦИОНИРУЕТ
     * Создает новый диалог в приложении
     * @param name
     * @param dialogId
     */
    public static void openDialog(String name, int dialogId){
        try {
            FXMLLoader dialogLoader = new FXMLLoader();
            dialogLoader.setLocation(ClientApp.class.getResource("/Dialog.fxml"));
            AnchorPane dialogPane = (AnchorPane) dialogLoader.load();
            Scene dialogScene = new Scene(dialogPane);
            Stage dialogStage =new Stage();
            dialogStage.setScene(dialogScene);
            DialogControl dControl = dialogLoader.getController();
            dControl.setWhoIs(name);

            dialogStage.setTitle(name);
            dialogStage.initModality(Modality.NONE);
            dialogStage.show();

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Включает функцию фиксации приложения поверх остальных окон
     */
    public static void primaryStageChangeFixTop(){
        if (primaryStage.alwaysOnTopProperty().get()){
            primaryStage.setAlwaysOnTop(false);
            rootControl.fixTopSetText(false);
        }
        else {
            primaryStage.setAlwaysOnTop(true);
            rootControl.fixTopSetText(true);
        }
    }

    /**
     * Отправляет запрос на сервер
     * @param message
     */
    public static void outWrite(String message) {
        try {
            out.write(message);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void sendFile(File file){
        log.info("Send file: "+file);
        try (Socket fileOutputSocket = new Socket(InetAddress.getByName(ip), 4005);
            BufferedInputStream bIS = new BufferedInputStream(new FileInputStream(file));
            DataOutputStream dOS = new DataOutputStream(fileOutputSocket.getOutputStream())) {
            byte[] byteArray = new byte[2048];
            int in;
            while ((in = bIS.read(byteArray))!=-1) {
                dOS.write(byteArray,0,in);
            }
            log.info("Send file is complete.");
        } catch (FileNotFoundException e) {
            log.error("File not found: "+file+". Sending is not possible");
        } catch (IOException e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Скрывает основное окно приложения
     * @param b
     */
    public static void hidePrimaryStage(boolean b){
        if (b==true){
            primaryStage.hide();
            hidePrimaryStage=true;
        }
        else{
            primaryStage.show();
            hidePrimaryStage=false;
        }
    }

    /**
     * Попытка добавить приложению работы с треем.
     * Сворачивается, но не разворачивается.
     */
//    public void addAppToTray(){
//        java.awt.Toolkit.getDefaultToolkit();
//
//        if (!java.awt.SystemTray.isSupported()) {
//            System.out.println("No system tray support, application exiting.");
//            Platform.exit();
//        }
//
//        java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
//        URL url = ClientApp.class.getResource("/tray-logo.jpg");
//        File icon = new File(url.getFile());
//        java.awt.Image image = null;
//        try {
//            image = ImageIO.read(icon);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);
//
//        trayIcon.addActionListener(event -> Platform.runLater(this::showStage));
//        java.awt.MenuItem openItem = new java.awt.MenuItem("Развернуть");
//        openItem.addActionListener(event -> Platform.runLater(this::showStage));
//
//        java.awt.MenuItem exitItem = new java.awt.MenuItem("Закрыть");
//        exitItem.addActionListener(event -> {
//            Platform.exit();
//            tray.remove(trayIcon);
//            System.exit(0);
//        });
//
//        final java.awt.PopupMenu popup = new java.awt.PopupMenu();
//        popup.add(openItem);
//        popup.addSeparator();
//        popup.add(exitItem);
//        trayIcon.setPopupMenu(popup);
//
//        try {
//            tray.add(trayIcon);
//        } catch (AWTException e) {
//            e.printStackTrace();
//        }
//        primaryStage.hide();
//    }
//
//    private void showStage() {
//        if (primaryStage != null) {
//            primaryStage.show();
//            primaryStage.toFront();
//        }
//    }

    /**
     * Отправляет запрос проверки актуальности версии приложения
     */
    public static void isNeedUpdate() {
        if (isConnectActive) {
            outWrite(Instruction.VERSION.getCommand() + VERSION);
        }
    }

    public static Gson getGson() {
        if (gson==null){
            gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                    .create();
        }
        return gson;
    }

    public static Socket getClientSocket() {
        return clientSocket;
    }

    public static Charset getDefaultCharset() {
        return DEFAULT_CHARSET;
    }

    public static Pair<User, UserProfile> getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Pair<User, UserProfile> currentUser) {
        ClientApp.currentUser = currentUser;
        log.info(String.format("Setting current user:\t\t\t%s",ClientApp.currentUser.getKey().getLogin()));
    }

    public static Receive getReceive() {
        return receive;
    }

    public static void iconifiedPrimaryStage(){
        primaryStage.setIconified(true);
    }

    public static boolean isIconifiedPrimaryStage(){
        return primaryStage.isIconified();
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        ClientApp.ip = ip;
    }

    public static String getTITLE() {
        return TITLE;
    }

    public static String getVERSION() {
        return VERSION;
    }

    public static Boolean getIsConnectActive() {
        return isConnectActive;
    }

    public static boolean isHidePrimaryStage() {
        return hidePrimaryStage;
    }

    public static void setHidePrimaryStage(boolean hidePrimaryStage) {
        ClientApp.hidePrimaryStage = hidePrimaryStage;
    }

    public static Map<String, Pair<User, UserProfile>> getClientList() {
        return clientList;
    }

    public static void setClientList(Map<String, Pair<User, UserProfile>> clientList) {
        ClientApp.clientList = clientList;
    }
}
