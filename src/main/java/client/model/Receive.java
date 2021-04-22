package client.model;


import client.controller.RootControl;
import client.model.entity.Message;
import client.model.entity.Task;
import client.model.entity.User;
import client.model.entity.UserProfile;
import client.model.utils.BufferedWriterWrapper;
import client.model.utils.Pair;
import client.model.utils.Support;
import client.UserPreferences;
import client.controller.AuthControl;
import client.controller.MainControl;
import client.controller.RegControl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс, производящий получение и обработку
 * ответов с сервера.
 */
@Slf4j
public class Receive extends Thread{
    /**
     * Массив, содержащий значения ответа с сервера
     */
    private AuthControl authControl;
    private RegControl regControl;
    private MainControl mainControl;
    private RootControl rootControl;
    private ArrayList<Task>tempTaskList = new ArrayList<>();

    private volatile boolean mainControlInit = false;
    private volatile boolean regControlInit = false;
    private boolean taskStatsInit = false;

    private String props[];
    private final ExecutorService exec = Executors.newFixedThreadPool(1);
    private Gson gson=ClientApp.getGson();

    @Override
    public void run() {
        log.info("Receive thread is starting.");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(ClientApp.getClientSocket().getInputStream(), ClientApp.getDefaultCharset()))) {
            while (true) {
                String response = in.readLine();
                if(response!=null){
                    response= BufferedWriterWrapper.decrypt(response);
                    //System.out.println(response);
                    String[] res = response.split("@", 2);
                    Instruction command = Instruction.getInstruction(res[0] + "@");
                    if (res.length>1) {
                        props = res[1].split("/");
                    }
                    final String[]finalProps = Arrays.copyOf(props,props.length);
                    exec.execute(()-> checkResponse(command,finalProps));
                }
                else {
                    log.error("Connection refused");
                    Support.stopServer();
                    break;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("Message receive is end.");
        }
    }

    /**
     * Проверка ответа
     * @param command
     * @param props
     */
    private void checkResponse(Instruction command, String[] props){
        log.debug("Checking response: "+command);
        switch (command){
            case DEPARTMENTS:{
                log.info("Getting departments.");
                while (!regControlInit){
                }
                regControl.setDepartmentBox(props);
                break;
            }
            case POSITIONS:{
                log.info("Getting positions.");
                regControl.setPositionBox(props);
                break;
            }
            case REGISTRATION_SUCCESSFUL:{
                successfulRegistration();
                break;
            }
            case REGISTRATION_FAILED:{
                failedRegistration();
                break;
            }
            case AUTHORIZATION_SUCCESSFUL:{
                successfulAuthorization(props);
                break;
            }
            case AUTHORIZATION_FAILED:{
                failedAuthorization();
                break;
            }
            case USER_LIST:{
                log.info("Receiving actual user list.");
                break;
            }
            case USER:{
                while (!mainControlInit){
                }
                addUserInList(props);
                break;
            }
            case USER_LIST_END:{
                log.info("Receiving actual user list is end.");
                break;
            }
            case DIALOG:{
                while (!mainControlInit){
                }
                setDialogMessage(props);
                break;
            }
//            case PROFILE:{
//                openUserProfile(props);
//                break;
//            }
            case NEW_TASK:{
                Support.alertNewTask();
                break;
            }
            case TASK:{
                addTask(props);
                break;
            }
            case TASK_LIST_START:{
                tempTaskList.clear();
                log.info("Receiving actual task list.");
                break;
            }
            case TASK_LIST_END:{
                mainControl.getTempTaskList().clear();
                mainControl.getTempTaskList().addAll(tempTaskList);
                log.info("Receiving actual user list is end.");
                break;
            }
            case TASK_STATS:{
                setTaskStats(props);
                break;
            }
            case APPOINTED_TASK:{
                addAppointedTask(props);
                break;
            }
            case TASK_COMPLETE:{
                editTaskForComplete(props);
                break;
            }
            case TASK_REFUNDED:{
                editTaskForRefunded(props);
                break;
            }
            case TASK_READ:{
                Support.alertAboutTaskRead(props[0]);
                break;
            }
            case VERSION_ACTUAL:{
                Support.alertIsNeedUpdate(false);
                break;
            }
            case VERSION_DEPRECATED:{
                Support.alertIsNeedUpdate(true);
                break;
            }
            case DOWNLOAD_FILE:{
                takeFile(props);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Добавляет сообщение в чат
     * @param props
     */
    private void setDialogMessage(String[] props){
        Message message = gson.fromJson(props[1], Message.class);
        if (Integer.parseInt(props[0])==1){
            mainControl.addMessageMainChat(message);
        }
    }


    /**
     * Успещная регистрация
     */
    private void successfulRegistration() {
        log.info("Registration ending....");
        Platform.runLater(() -> {
            try {
                ClientApp.showAuthScene();
                log.info("Registration successful.");
                authControl.setRegistrationMessage(0);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Проваленная регистрация
     */
    private void failedRegistration(){
        log.info("Registration failed.");
        Platform.runLater(()-> {
            try {
                ClientApp.showAuthScene();
                authControl.setRegistrationMessage(1);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Создание текущего (авторизованного) пользователя в случае успешной авторизации.
     * @param props
     *     props[0]: id пользователя
     *     props[1]: json user
     *     props[2]: json user profile
     */
    private void successfulAuthorization(String[] props){
        log.info("Authorization successful.");
        User user = gson.fromJson(props[0], User.class);
        UserProfile userProfile = gson.fromJson(props[1], UserProfile.class);
        Pair<User, UserProfile> currentUser = new Pair<>(user, userProfile);
        ClientApp.setCurrentUser(currentUser);
        UserPreferences.savePrefUserName(user.getLogin());
        Platform.runLater(()->{
            try {
                ClientApp.showMainScene();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
        ClientApp.isNeedUpdate();
    }

    /**
     * Проваленная авторизация
     */
    private void failedAuthorization(){
        log.info("Authorization failed.");
        Platform.runLater(()->{
            try {
                ClientApp.showAuthScene();
                authControl.setRegistrationMessage(2);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Добавляет пользователя в список пользователей
     * @param props
     *     props[0]: логин пользователя
     *     props[1]: фамилия пользователя
     *     props[2]: имя пользователя
     *     props[3]: отчество пользователя
     *     props[4]: статус пользователя(оффлайн, онлайн)
     */
    private void addUserInList(String[] props){
        log.debug("Adding user in user list.");
        User user = gson.fromJson(props[0], User.class);
        UserProfile profile = gson.fromJson(props[1], UserProfile.class);
        ClientApp.getClientList().put(user.getLogin(),new Pair<>(user,profile));
        mainControl.addUserInList(user);
        log.debug("Complete");
    }

    /**
     * Добавляет задачу в собственный список задач
     * или в список задач сотрудника
     * @param props
     */
    private void addTask(String[] props){
        Task task = gson.fromJson(props[0], Task.class);
        task.initProperty();
        if (task.getRecipient().equals(ClientApp.getCurrentUser().getKey())) {
            mainControl.getTaskList().add(task);
        }
        else {
            tempTaskList.add(task);
        }
    }

    /**
     * Добавляет задачу в список задач, направленных
     * авторизованных пользователем
     * @param props
     */
    private void addAppointedTask(String[] props){
        Task task = gson.fromJson(props[0], Task.class);
        task.initProperty();
        mainControl.getMyTaskList().add(task);
    }


    /**
     * Устанавливает задачу в статус решенной в случае ее нахождения в списке
     * @param props
     */
    private void editTaskForComplete(String[] props){
        if (mainControl.getMyTaskList()!=null) {
            Task task = gson.fromJson(props[0], Task.class);
            Task taskInMyTaskList = mainControl.getMyTaskList().stream()
                    .filter(t->t.getId()==task.getId())
                    .findFirst()
                    .orElse(null);
            if (taskInMyTaskList!=null) {
                taskInMyTaskList = task;
                Support.alertTaskComplete(taskInMyTaskList.getName());
            }
            else{
                Support.alertTaskComplete("");
            }
            log.info("Edit task: set complete");
        }
    }

    /**
     * Устанавливает задачу в статус нерешенной в случае ее нахождения в списке
     * @param props
     */
    private void editTaskForRefunded(String[] props){
        if (mainControl.getTaskList()!=null) {
            Task taskInTaskList = mainControl.getTaskList().stream()
                    .filter(task->task.getId()==Integer.parseInt(props[0]))
                    .findFirst()
                    .orElse(null);
            if (taskInTaskList!=null) {
                taskInTaskList.setComplete((short) 0);
                taskInTaskList.setIsRead((short) 0);
                taskInTaskList.setAnswerAnnotation(props[1]);
                Support.alertTaskRefunded(taskInTaskList.getName());
            }
            else{
                Support.alertTaskRefunded("");
            }
            log.info("Edit task: refunded");
            rootControl.incrementNotReadTaskCount();
            rootControl.incrementNotPerformTaskCount();
        }
    }

    /**
     * Устанавливает значения количества непрочитанных и невыполненных сообщений
     */
    private void setTaskStats(String[]props){
        int notPerform = Integer.parseInt(props[0]);
        int notRead = Integer.parseInt(props[1]);
        if (notRead!=0 &&!taskStatsInit){
            Support.alertWithStats(notPerform, notRead);
        }
        taskStatsInit =true;
        rootControl.setCountNotPerformTasks(notPerform);
        rootControl.setCountNotReadTasks(notRead);
        rootControl.updateTaskStats();
    }

    /**
     * Принимает файл от сервера в отдельном потоке по сокету 4006
     * @See createFile
     * @param props
     *      props[0] - путь к файлу
     */
    private void takeFile(String[]props){
        Thread takedFile = new Thread(()->{
            File file = new File(props[0]);
            log.info("Taking file: "+file);

            try (ServerSocket inputFileSocket = new ServerSocket(4006)){
                log.info("inputFileSocket is open");
                Socket clientInputFileSocket = inputFileSocket.accept();
                try (DataInputStream dIS = new DataInputStream(clientInputFileSocket.getInputStream());
                     FileOutputStream fOS = new FileOutputStream(file,false)) {

                    byte[] byteArray = new byte[2048];
                    int in;
                    while ((in = dIS.read(byteArray))!=-1) {
                        fOS.write(byteArray,0,in);
                    }
                    log.info("Taking file is complete.");
                }
                clientInputFileSocket.close();
            }
            catch (FileNotFoundException e) {
                log.error("File not found: "+file+". Taking is not possible");
            }
            catch (IOException e){
                log.error(e.getMessage(), e);
            }
        });

        takedFile.start();
    }

    public void setMainControlInitTrue() {
        mainControlInit = true;
    }

    public void setRegControlInitTrue() {
        regControlInit = true;
    }

    public void setAuthControl(AuthControl authControl) {
        this.authControl = authControl;
    }

    public void setMainControl(MainControl mainControl) {
        this.mainControl = mainControl;
    }

    public void setRegControl(RegControl regControl) {
        this.regControl = regControl;
    }

    public void setRootControl(RootControl rootControl) {
        this.rootControl = rootControl;
    }
}
