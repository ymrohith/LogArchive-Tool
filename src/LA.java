import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

public class LA {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java LA <log-directory>");
            return;
        }

        String logDirPath = args[0];
        File logDir = new File(logDirPath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            System.out.println("Invalid log directory: " + logDirPath);
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String archiveName = "logs_archive_" + timestamp + ".tar.gz";
        File outputDir = new File("archived_logs");
        outputDir.mkdir();

        File archiveFile = new File(outputDir, archiveName);

        try {
            File tarFile = new File(outputDir, "temp_" + timestamp + ".tar");
            createTarFile(logDir, tarFile);
            compressGzip(tarFile, archiveFile);
            tarFile.delete();

            File logFile = new File(outputDir, "archive_log.txt");
            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write("[" + timestamp + "] Archived " + logDir.getName() + " to " + archiveName + "\n");
            }

            System.out.println("✅ Logs archived successfully: " + archiveFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("❌ Error while archiving logs: " + e.getMessage());
        }
    }

    private static void createTarFile(File dir, File tarFile) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("tar", "-cf", tarFile.getAbsolutePath(), "-C", dir.getParent(), dir.getName());
        Process process = pb.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Tar command failed.");
            }
        } catch (InterruptedException e) {
            throw new IOException("Tar process interrupted.");
        }
    }

    private static void compressGzip(File inputFile, File gzipFile) throws IOException {
        try (
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gos = new GZIPOutputStream(fos)
        ) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gos.write(buffer, 0, len);
            }
        }
    }
}

