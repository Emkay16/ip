package duke;

import duke.task.*;
import duke.parser.Parser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class Duke {

    public static final String LINE = "_____________________________________________________\n";
    public static final String LOGO = " ____        _        \n"
            + "|  _ \\ _   _| | _____ \n"
            + "| | | | | | | |/ / _ \\\n"
            + "| |_| | |_| |   <  __/\n"
            + "|____/ \\__,_|_|\\_\\___|\n";

    public static final String WELCOME_MESSAGE = LINE + "Hello from\n" + LOGO
            + "What can I do for you today?\n" + LINE;
    public static final String BYE_MESSAGE = LINE + "Goodbye! Hope to see you around soon!\n" + LINE;
    public static final String HELP_MESSAGE = LINE + "Add todo:\n"
            + "Command prefix: NONE\n"
            + "Argument(s): task\n\n"
            + "Show tasks:\n"
            + "Command prefix: list\n\n"
            + "Check task off:\n"
            + "Command prefix: done\n"
            + "Argument(s): task number\n";
    public static final String EMPTY_LIST_MESSAGE = "There are not tasks in the list\n";
    public static final String ADD_MESSAGE = " added to list\n";
    public static final String NO_DESCRIPTION_MESSAGE = "The description cannot be empty!\n";
    public static final String NO_DEADLINE_MESSAGE = "Please indicate a deadline after \"/by\"\n";
    public static final String NO_TIME_MESSAGE = "Please indicate the event time after \"/at\"\n";
    public static final String TASK_CHECKED_MESSAGE = "Task checked off!\n";
    public static final String TASK_ALREADY_CHECKED_MESSAGE = " is already checked off\n";
    public static final String ALL_TASKS_CHECKED_MESSAGE = "All remaining tasks have been checked off!\n";
    public static final String TASK_UNCHECKED_MESSAGE = "Task is no longer checked off\n";
    public static final String TASK_NOT_CHECKED_MESSAGE = " is not checked\n";
    public static final String TASK_DELETED_MESSAGE = "This task has been removed: \n";
    public static final String INVALID_ARGUMENT_MESSAGE = "Invalid argument!\n";
    public static final String INVALID_COMMAND_MESSAGE = "I am sorry, I do not recognise that command\n"
            + "Please try again\n";
    public static final String GENERIC_ERROR_MESSAGE = "Something went wrong!\n";

    public static final String FILENAME = "data.txt";

    private static Parser parser = new Parser();

    public static void main(String[] args) {
        ArrayList<Task> list = new ArrayList<>();
        int taskCount = handleFile(list);
        printWelcome();
        interact(list, taskCount);
        saveData(list);
        printBye();
    }

    private static int handleFile(ArrayList<Task> list) {
        int taskCount = 0;
        try {
            File saveData = new File(FILENAME);
            if(saveData.createNewFile()) { //file is created
                System.out.println("data.txt created");
            } else { //file already exists
                Scanner reader = new Scanner(saveData);
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    loadData(list, line);
                    taskCount++;
                }
                System.out.println("data loaded");
                System.out.println("tasks: " + taskCount);
            }
        } catch (IOException e) {
            System.out.print(LINE + GENERIC_ERROR_MESSAGE + LINE);
        }
        return taskCount;
    }

    private static void loadData(ArrayList<Task> list, String line) {
        boolean isDone = true;
        int indexOfSeparator = 0;
        String desc = new String();
        String date = new String();

        if (line.charAt(0) == '0') {
            isDone = false;
        }

        switch (line.charAt(1)) {
        case 'T':
            desc = line.substring(2);
            list.add(new ToDo(desc, isDone));
            break;
        case 'D':
            indexOfSeparator = line.indexOf('|');
            desc = line.substring(2, indexOfSeparator);
            date = line.substring(indexOfSeparator + 1);
            list.add(new Deadline(desc, isDone, date));
            break;
        case 'E':
            indexOfSeparator = line.indexOf('|');
            desc = line.substring(2, indexOfSeparator);
            date = line.substring(indexOfSeparator + 1);
            list.add(new Event(desc, isDone, date));
            break;
        }
    }

    public static void interact(ArrayList<Task> list, int taskCount) {

        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();


        String[] parsedInput = parser.inputParser(input);
        String command = parsedInput[0];
        String arguments = parsedInput[1];

        while (!command.equalsIgnoreCase("bye")) {
            switch (command.toLowerCase()) {
            case "help":
                System.out.print(LINE + HELP_MESSAGE + LINE); //we should probably write an entire method for help
                break;
            case "list":
                printList(list, taskCount);
                break;
            case "todo":
                taskCount = addToDo(arguments, list, taskCount);
                break;
            case "deadline":
                taskCount = addDeadline(arguments, list, taskCount);
                break;
            case "event":
                taskCount = addEvent(arguments, list, taskCount);
                break;
            case "done":
                markAsDone(list, taskCount, arguments);
                break;
            case "undo":
                undoMarkAsDone(list, taskCount, arguments);
                break;
            case "delete":
                delete(list, taskCount, arguments);
                break;
            default:
                System.out.print(LINE + INVALID_COMMAND_MESSAGE + LINE);
            }
            input = scan.nextLine();

            parsedInput = parser.inputParser(input);
            command = parsedInput[0];
            arguments = parsedInput[1];
        }
    }

    public static int addToDo(String input, ArrayList<Task> list, int index) {
        if(input == null) {
            System.out.print(LINE + NO_DESCRIPTION_MESSAGE + LINE);
        } else {
            list.add(new ToDo(input));
            System.out.print(LINE + "\"" + input + "\"" + ADD_MESSAGE + LINE);
            index++;
        }
        return index;

    }

    public static int addDeadline(String input, ArrayList<Task> list, int index) {
        if (input == null) {
            System.out.print(LINE + NO_DESCRIPTION_MESSAGE + LINE);
        } else if(input.toLowerCase().contains("/by")) {
            try {
                String desc = input.substring(0, input.toLowerCase().indexOf("/by") - 1);
                String dueDate = input.substring(input.toLowerCase().indexOf("/by") + 4);
                list.add(new Deadline(desc, dueDate));
                System.out.print(LINE + "\"" + desc + "\"" + ADD_MESSAGE +
                        "Please complete by: " + dueDate + "\n" + LINE);
                index++;
            } catch (Exception e) {
                System.out.print(LINE + INVALID_ARGUMENT_MESSAGE + LINE);
            }
        } else {
            System.out.print(LINE + INVALID_ARGUMENT_MESSAGE + NO_DEADLINE_MESSAGE + LINE);
        }

        return index;
    }

    public static int addEvent(String input, ArrayList<Task> list, int taskCount) {
        if (input == null) {
            System.out.print(LINE + NO_DESCRIPTION_MESSAGE + LINE);
        } else if(input.toLowerCase().contains("/at")) {
            try {
                String desc = input.substring(0, input.toLowerCase().indexOf("/at")-1);
                String date = input.substring(input.toLowerCase().indexOf("/at")+4);
                list.add(new Event(desc, date));
                System.out.print(LINE + "\"" + desc + "\"" + ADD_MESSAGE +
                        "It occurs at: " + date + "\n" + LINE);
                taskCount++;
            } catch (Exception e) {
                System.out.print(LINE + INVALID_ARGUMENT_MESSAGE + LINE);
            }
        } else {
            System.out.print(LINE + INVALID_ARGUMENT_MESSAGE + NO_TIME_MESSAGE + LINE);
        }

        return taskCount;
    }

    public static void printList(ArrayList<Task> list, int taskCount) {
        if(taskCount == 0) {
            System.out.print(LINE + EMPTY_LIST_MESSAGE + LINE);
        } else {
            System.out.print(LINE);
            for (int i = 0; i < list.size(); i++) {
                if (i < 9) {
                    System.out.print(" ");
                }
                System.out.println(i + 1 + list.get(i).toString());
            }
            System.out.print("There are " + Task.getTasksRemaining() + " undone task(s) on the list\n" + LINE);
        }
    }

    public static void markAsDone(ArrayList<Task> list, int max, String input) {
        try {
            int taskNo = Integer.parseInt(input);
            if (taskNo <= max && taskNo > 0) { //no. is valid
                if (list.get(taskNo - 1).getStatus()) {
                    System.out.print(LINE + "Task \"" + list.get(taskNo - 1).getDesc() + "\""
                            + TASK_ALREADY_CHECKED_MESSAGE + LINE);
                } else {
                    list.get(taskNo - 1).check();
                    System.out.print(LINE + TASK_CHECKED_MESSAGE);
                    System.out.println("  " + list.get(taskNo - 1).getStatusSymbol() + list.get(taskNo - 1).getDesc());
                    if (Task.getTasksRemaining() == 0) {
                        System.out.print(ALL_TASKS_CHECKED_MESSAGE);
                    }
                    System.out.print(LINE);
                }
            } else {
                printInvalidArgumentMessage();
            }
        } catch (Exception e) {
            printInvalidArgumentMessage();
        }

    }

    public static void undoMarkAsDone(ArrayList<Task> list, int max, String input) {
        try {
            int taskNo = Integer.parseInt(input);
            if (taskNo <= max && taskNo > 0) { //no. is valid
                if (!list.get(taskNo - 1).getStatus()) {
                    System.out.print(LINE + "Task \"" + list.get(taskNo - 1).getDesc() + "\""
                            + TASK_NOT_CHECKED_MESSAGE + LINE);
                } else {
                    list.get(taskNo - 1).uncheck();
                    System.out.print(LINE + TASK_UNCHECKED_MESSAGE);
                    System.out.println("  " + list.get(taskNo - 1).getStatusSymbol() + list.get(taskNo - 1).getDesc());
                    System.out.print(LINE);
                }
            } else {
                printInvalidArgumentMessage();
            }
        } catch (Exception e) {
            printInvalidArgumentMessage();
        }

    }

    private static void delete(ArrayList<Task> list, int max, String input) {
        try {
            int taskNo = Integer.parseInt(input);
            if (taskNo <= max && taskNo > 0) {
                System.out.print(LINE + TASK_DELETED_MESSAGE + list.get(taskNo - 1).toString() + "\n" + LINE);
                list.get(taskNo - 1).remove();
                list.remove(taskNo - 1);
            } else {
                printInvalidArgumentMessage();
            }
        } catch (Exception e) {
            printInvalidArgumentMessage();
        }
    }

    private static void saveData(ArrayList<Task> list) {
        try {
            FileWriter fw = new FileWriter(FILENAME);
            fw.write(composeOutput(list.get(0)) + System.lineSeparator());

            for (int i = 1; i< list.size(); i++) {
                fw.append(composeOutput(list.get(i))).append(System.lineSeparator());
            }
            fw.close();

        } catch (IOException e) {
            System.out.print(LINE + GENERIC_ERROR_MESSAGE + LINE);
        } catch (IndexOutOfBoundsException e){

        }
    }

    private static String composeOutput(Task data) {
        boolean isDone = data.getStatus();
        StringBuilder output = new StringBuilder();

        switch(data.toString().charAt(2)){
        case 'T':
            if(!isDone) {
                output.append("0");
            } else {
                output.append("1");
            }
            output.append('T');
            output.append(data.getDesc());
            break;
        case 'D':
            if(!isDone) {
                output.append("0");
            } else {
                output.append("1");
            }
            output.append('D');
            output.append(data.getDesc());
            output.append('|');
            output.append(data.getDate());
            break;
        case 'E':
            if(!isDone) {
                output.append("0");
            } else {
                output.append("1");
            }
            output.append('E');
            output.append(data.getDesc());
            output.append('|');
            output.append(data.getDate());
            break;
        }
        return output.toString();
    }

    private static void printWelcome() {
        System.out.print(WELCOME_MESSAGE);
    }

    private static void printBye() {
        System.out.print(BYE_MESSAGE);
    }

    private static void printInvalidArgumentMessage() {
        System.out.print(LINE + INVALID_ARGUMENT_MESSAGE + LINE);
    }
}
