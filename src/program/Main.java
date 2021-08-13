package program;

import program.utils.NotValidArchiveNameException;
import program.classes.ZIPWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws IOException {
        long totalTime = new Date().getTime();
        ZIPWorker worker = ZIPWorker.getInstance();
        while (true) {
            try {
                worker.create();
                break;
            } catch (NotValidArchiveNameException e) {
                e.printStackTrace();
            }
        }
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Print \"EXIT\" to stop the program.");
//            System.out.println("напиши \"EXIT\" чтобы выйти из программы.");
            System.out.println("What you want to copy FILE, DIRECTORY or ARCHIVE content to the archive?:");
//            System.out.println("копируем FILE, DIRECTORY или содержимое ARCHIVE в наш архив?:");
            String answerFileOrArchive = console.readLine();
            if (answerFileOrArchive.equalsIgnoreCase("exit")) break;
            else if (answerFileOrArchive.equalsIgnoreCase("file")) {
                System.out.println("Path to the file:");
//                System.out.println("путь к файлу:");
                String answerFileName = console.readLine();
                try {
                    Method method = ZIPWorker.class.getMethod("copyFileToZIP", Path.class);
                    method.invoke(worker, Paths.get(answerFileName));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (answerFileOrArchive.equalsIgnoreCase("directory")) {
                System.out.println("Path to the directory:");
//                System.out.println("путь к папке:");
                String answerFileName = console.readLine();
                try {
                    Method method = ZIPWorker.class.getMethod("copyDirectoryToZIP", Path.class);
                    method.invoke(worker, Paths.get(answerFileName));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (answerFileOrArchive.equalsIgnoreCase("archive")) {
                System.out.println("Path to the archive:");
//                System.out.println("путь к архиву:");
                String answerFileName = console.readLine();
                System.out.println("Do you want to create a folder for the content? (true/false):");
//                System.out.println("создать папку для данных архива в архиве? (true/false):");
                Boolean answerCreateDir = Boolean.parseBoolean(console.readLine());
                try {
                    Method method = ZIPWorker.class.getMethod("copyFilesIntoZIPFromAnotherZIP", Path.class, boolean.class);
                    method.invoke(worker, Paths.get(answerFileName), answerCreateDir);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        console.close();
        worker.close();
        System.out.println("total time of the program: " + (new Date().getTime() - totalTime) + " ms.");
    }
}
