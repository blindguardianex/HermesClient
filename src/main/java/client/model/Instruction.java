package client.model;

import java.util.stream.Stream;

/**
 * Enum класс, содержащий все команды, направляемые серверу
 */
public enum Instruction {
    VERSION("VERSION@"),
    VERSION_ACTUAL("VERSION_ACTUAL@"),
    VERSION_DEPRECATED("VERSION_DEPRECATED@"),
    REGISTRATION("REGISTRATION@"),
    REGISTRATION_SUCCESSFUL("REGISTRATION_SUCCESSFUL@"),
    REGISTRATION_FAILED("REGISTRATION_FAILED@"),
    CONNECT("CONNECT@"),
    CONNECT_OK("CONNECT_OK@"),
    AUTHORIZATION("AUTHORIZATION@"),
    AUTHORIZATION_SUCCESSFUL("AUTHORIZATION_SUCCESSFUL@"),
    AUTHORIZATION_FAILED("AUTHORIZATION_FAILED@"),
    CONNECT_CLOSE("CONNECT_CLOSE@"),
    DIALOG_NEW("DIALOG_NEW@"),
    DIALOG_OK("DIALOG_OK@"),
    DIALOG_CREATE("DIALOG_CREATE@"),
    DIALOG("DIALOG@"),
    CHAT("CHAT@"),
    USER("USER@"),
    USER_LIST("USER_LIST@"),
    USER_LIST_END("USER_LIST_END@"),
    NEW_USER("NEW_USER@"),
    DEPARTMENTS("DEPARTMENTS@"),
    POSITIONS("POSITIONS@"),
    PROFILE("PROFILE@"),
    CREATE_NEW_TASK("CREATE_NEW_TASK@"),
    NEW_TASK("NEW_TASK@"),
    GET_TASKS("GET_TASKS@"),
    GET_APPOINTED_TASKS("GET_APPOINTED_TASKS@"),
    APPOINTED_TASK("APPOINTED_LIST@"),
    TASK_LIST_START("TASK_LIST_START@"),
    TASK("TASK@"),
    TASK_LIST_END("TASK_LIST_END@"),
    TASK_COMPLETE("TASK_COMPLETE@"),
    TASK_REFUNDED("TASK_REFUNDED@"),
    TASK_READ("TASK_READ@"),
    TASK_STATS("TASK_STATS@"),
    FILE_FOR_TASK("FILE_FOR_TASK@"),
    DOWNLOAD_FILE("DOWNLOAD_FILE@"),
    UNKNOWN_COMMAND("UNKNOWN_COMMAND@");

    /**
     * Строковое представление инструкции
     */
    private final String command;

    private Instruction(String command){
        this.command=command;
    }

    public String getCommand() {
        return command;
    }

    /**
     * Ищет и возвращает инструкцию по ее строковому представлению
     * @param command
     * @return
     */
    public static Instruction getInstruction(String command){
        return Instruction.stream()
                .filter(instruction -> instruction.getCommand().equals(command))
                .findFirst()
                .orElse(UNKNOWN_COMMAND);
    }

    public static Stream<Instruction> stream(){
        return Stream.of(Instruction.values());
    }

    @Override
    public String toString() {
        return command;
    }
}