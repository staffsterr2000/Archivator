package program.classes;

import program.utils.NotValidArchiveNameException;
import program.utils.StackForDirectories;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZIPWorker {
    private Path pathToZIP;                 // абсолютный путь к экспорт-архиву (c:/temp/test/a1.zip)
    private Path pathToDir;                 // абсолютный путь к папке, в которой находится архив (c:/temp/test)
    private String fullFileNameWithoutDigitsAndExpression;      // имя файла без цифер (если есть) и расширения (c:/temp/test/a)
    private Integer digits;                         // те самые цифры в конце файла (1) или (null) если их нет в названии
    private String expression;                      // расширение (.zip)
    private ZipOutputStream zos;     // поток работы с архивом, в который будем записывать данные
    private String validArchiveCreatePathString =
//            "^(([Cc]:/[Tt]emp/[/\\p{javaLetterOrDigit}.,^%$#@!~`\\[\\]{}()+=_&'\\-—; ]*?" +
//            "|[Cc]:\\\\[Tt]emp\\\\[\\\\\\p{javaLetterOrDigit}.,^%$#@!~`\\[\\]{}()+=_&'\\-—; ]*?" +                      // проверка на валидность
            "^(((\\p{javaLetter}:)?[/\\p{javaLetterOrDigit}.,^%$#@!~`\\[\\]{}()+=_&'\\-—; ]*?" +
                    "|(\\p{javaLetter}:)?[\\\\\\p{javaLetterOrDigit}.,^%$#@!~`\\[\\]{}()+=_&'\\-—; ]*?" +                      // проверка на валидность
                    "[\\p{javaLetter}.,^%$#@!~`\\[\\]{}()+=_&'\\-—; &&[^/]])(\\d*))(\\.rar|\\.zip)$";                          // имени файла (Windows)
    private Pattern validArchiveCreatePathPattern = Pattern.compile(validArchiveCreatePathString);

    private String validArchiveNameString = "^([\\p{javaLetterOrDigit}.,^%$#@!~`\\[\\]{}()+=_&'\\-—; &&[^/]]*?)(?:\\.rar|\\.zip)$";
    private Pattern validArchiveNamePattern = Pattern.compile(validArchiveNameString);



    private static ZIPWorker instance;
    private ZIPWorker() {}
    public static ZIPWorker getInstance() {
        if (instance == null) instance = new ZIPWorker();
        return instance;
    }



//    private static void printData(Path file) throws IOException {
//        String name = file.getFileName().toString();
//        long size = Files.size(file);
//        System.out.println(String.format("File name: %s,%nFile size: %d", name, size));
//        System.out.println();
//    }
    private static void printData(ZipEntry zipEntry) {
        String name = zipEntry.getName();
        long size = zipEntry.getSize();
        System.out.println(String.format("File name: %s,%nFile size: %d%n", name, size));
    }

    private static void setSizeAndCRCInZipEntry(ZipEntry zipEntry, byte[] buffer) {
        zipEntry.setSize(buffer.length);
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(buffer);
        zipEntry.setCrc(crc.getValue());
    }



    /**
     * создаёт поток ввода, путь (+архив), если
     * путь в целом вероятный, но не существующий
     * @throws NotValidArchiveNameException если название архива
     * не правильно написано, то кидаем исключение
     */
    public void create() throws NotValidArchiveNameException {            //первый ввод, инициализируем архив, в который будем записывать данные
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter path to the archive where the program will store files:");
//            System.out.println("Введите полный путь к файлу, в который будем записывать данные:");
            String zipPathString = console.readLine();
            Matcher fileNameMatcher = validArchiveCreatePathPattern.matcher(zipPathString);

            if (!fileNameMatcher.matches()) {
                throw new NotValidArchiveNameException();
            }

            pathToZIP = Paths.get(zipPathString);
            pathToDir = pathToZIP.toAbsolutePath().getParent();
            fullFileNameWithoutDigitsAndExpression = fileNameMatcher.group(2);
            try {
                digits = Integer.parseInt(fileNameMatcher.group(3));
            } catch (NumberFormatException e) {
                digits = null;
            }
            expression = fileNameMatcher.group(4);

            if (Files.exists(pathToZIP)) {                                                          // если такой архив есть, то
                System.out.println("\nThis archive exists, create new one? (true/false):");
//                System.out.println("\nТакой архив уже существует, создать новый? (true/false):");
                boolean create = Boolean.parseBoolean(console.readLine().toLowerCase());            // создаём новый или перезаписываем
                if (create) {
                    pathToZIP = createZIP();
                    pathToDir = pathToZIP.getParent();
                }
            }
            else {
                createDirs();
            }

            System.out.println("Creating output stream.\n");
//            System.out.println("Создаём поток записи в архив.\n");
            zos = new ZipOutputStream(new FileOutputStream(pathToZIP.toFile()));                    // создаём поток для записи в
                                                                                                    // архив
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * закрывает поток вывода в архив
     */
    public void close() {
        try {
            System.out.println("Closing output stream.");
//            System.out.println("Закрываем поток записи в архив.");
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * метод, который создаёт папки, которых не хватает
     * для пути
     */
    private void createDirs() {
        if (Files.notExists(pathToDir)) {
            try {
                Files.createDirectories(pathToDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * метод, который создает архив, если у нас есть архив
     * с таким именем
     * @return путь к созданному архиву
     */
    private Path createZIP() {
        Path zip = null;
        try {
            int i = (digits != null) ? digits + 1 : 0;
            for ( ; ; i++) {
                zip = Paths.get(fullFileNameWithoutDigitsAndExpression + ((i != 0) ? i : "") + expression);
                if (Files.notExists(zip)) {
                    Files.createFile(zip);
                    digits = i;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zip;
    }



    /**
     * метод, который добавляет файл в архив
     * @param pathToImportFile путь к файлу, который добавляем в архив
     */
    public void copyFileToZIP(Path pathToImportFile) throws IOException {
        copyFileToZIP(pathToImportFile, pathToImportFile.getFileName().toString());
    }
    private void copyFileToZIP(Path pathToImportFile, String defaultZipEntryName) throws IOException {
        ZipEntry entry = new ZipEntry(defaultZipEntryName);
        byte[] buffer = Files.readAllBytes(pathToImportFile);
        setSizeAndCRCInZipEntry(entry, buffer);
        printData(entry);
        zos.putNextEntry(entry);
        zos.write(buffer);
        zos.closeEntry();
    }



    /**
     * метод, который добавляет указанную папку в архив
     * (+файлы, что находятся в ней (но это уже по желанию
     * пользователя))
     * @param pathToImportDir путь к папке, которую копируем
     */
    public void copyDirectoryToZIP(Path pathToImportDir) throws IOException {
        copyDirectoryToZIP(pathToImportDir, true);
    }
    private void copyDirectoryToZIP(Path pathToImportDir, boolean copyMainDir) throws IOException {
        class Visitor extends SimpleFileVisitor<Path> {
            private StackForDirectories stack = new StackForDirectories();
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String dirName = dir.getFileName().toString();
                if (!stack.contains(dirName)) {
                    if (copyMainDir || !pathToImportDir.getFileName().equals(Paths.get(dirName))) {
                        dirName += "/";
                        zos.putNextEntry(new ZipEntry(stack.peek() + dirName));
                        stack.push(dirName);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                stack.poll();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = stack.peek() + file.getFileName();
                instance.copyFileToZIP(file, fileName);
                return FileVisitResult.CONTINUE;
            }
        }

        Files.walkFileTree(pathToImportDir, new Visitor());
    }


    /**
     * разархивация во временную папку из архива,
     * который импортируем (1) в другой архив (2),
     * а после - непосредственное копирование
     * из временной папки в архив (2).
     * @param importZIPPath архив, из которого достаём данные (1)
     * @param createDirectory создаём ли мы папку в архиве (2)
     *                        под файлы архива (1)
     * @throws NotValidArchiveNameException если название архива
     * не правильно написано, то кидаем исключение
     */
    public void copyFilesIntoZIPFromAnotherZIP(Path importZIPPath, boolean createDirectory) throws IOException, NotValidArchiveNameException {
        Matcher fileNameMatcher = validArchiveNamePattern.matcher(importZIPPath.getFileName().toString());
        if (!fileNameMatcher.matches()) {
            throw new NotValidArchiveNameException();
        }
        ZipInputStream zis = new ZipInputStream(new FileInputStream(importZIPPath.toFile()), Charset.forName("CP866"));
        Path tempDirectory = Files.createTempDirectory(importZIPPath.getParent(), importZIPPath.getFileName().toString());
        tempDirectory.toFile().deleteOnExit();
        byte[] bufferForExport = new byte[1024];

        String directoryForArchive = tempDirectory.toString() + "/";
        if (createDirectory) {
            directoryForArchive += fileNameMatcher.group(1) + "/";
            File directoryForArchiveFile = new File(directoryForArchive);
            directoryForArchiveFile.mkdir();
            directoryForArchiveFile.deleteOnExit();
        }

        ZipEntry exportEntry;
        while ((exportEntry = zis.getNextEntry()) != null) {
            String name = exportEntry.getName();
            File file = new File(directoryForArchive + name);
            file.deleteOnExit();
            if (exportEntry.isDirectory()) {
                file.mkdirs();
                continue;
            }
            FileOutputStream fos = new FileOutputStream(file);
            int len;
            while ((len = zis.read(bufferForExport)) > 0) {
                fos.write(bufferForExport, 0, len);
            }
            zis.closeEntry();
            fos.close();
        }
        instance.copyDirectoryToZIP(tempDirectory, false);
    }
}

