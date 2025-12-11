package pe.edu.vallegrande.arquitectura.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class OracleWalletLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OracleWalletLoader.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String b64 = System.getenv("ORACLE_WALLET_BASE64");
        if (b64 == null || b64.isBlank()) {
            logger.info("ORACLE_WALLET_BASE64 not set; skipping wallet extraction");
            return;
        }

        Path tmp = Path.of(System.getProperty("java.io.tmpdir"));
        Path walletDir = tmp.resolve("oracle_wallet");
        try {
            Files.createDirectories(walletDir);
            byte[] decoded = Base64.getDecoder().decode(b64);

            // Unzip bytes into walletDir
            try (InputStream in = new ByteArrayInputStream(decoded);
                 ZipInputStream zis = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path out = walletDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(out);
                    } else {
                        Files.createDirectories(out.getParent());
                        Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }

            // Set system property / env for Oracle driver
            String walletPath = walletDir.toAbsolutePath().toString();
            System.setProperty("oracle.net.tns_admin", walletPath);
            // Some drivers read TNS_ADMIN env var
            try {
                // On many platforms, setting environment variables at runtime isn't portable;
                // but set a system property as best-effort.
                logger.info("Oracle wallet extracted to {}. System property oracle.net.tns_admin set.", walletPath);
            } catch (Exception e) {
                logger.warn("Failed to set TNS_ADMIN env var programmatically: {}", e.getMessage());
            }

        } catch (IOException e) {
            logger.error("Failed to extract Oracle wallet: {}", e.getMessage(), e);
            throw e;
        }
    }
}
