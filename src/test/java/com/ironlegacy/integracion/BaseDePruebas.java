package com.ironlegacy.integracion;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Prepara iron_legacy_test una sola vez por JVM, con los mismos scripts de la aplicación
 * (solo cambia el nombre de la base). Las pruebas limpian las tablas entre sí.
 */
final class BaseDePruebas {

    static final String BASE = "iron_legacy_test";
    static final String URL_BASE = "jdbc:mysql://localhost:3306/" + BASE
            + "?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String URL_SERVIDOR =
            "jdbc:mysql://localhost:3306/?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String BASE_ORIGINAL = "iron_legacy";

    private static boolean preparada;

    private BaseDePruebas() {
    }

    static synchronized void preparar() throws Exception {
        if (preparada) {
            return;
        }
        try (Connection conexion = DriverManager.getConnection(URL_SERVIDOR, "iron_legacy", System.getenv("DB_CLAVE"))) {
            ejecutar(conexion, "DROP DATABASE IF EXISTS " + BASE + ";");
            ejecutar(conexion, cambiarBase(Files.readString(Path.of("db/schema.sql"))));
            ejecutar(conexion, cambiarBase(Files.readString(Path.of("db/seed.sql"))));
        }
        preparada = true;
    }

    static void limpiar(JdbcTemplate jdbc) {
        jdbc.update("DELETE FROM pago");
        jdbc.update("DELETE FROM membresia");
        jdbc.update("DELETE FROM asistencia");
        jdbc.update("DELETE FROM socio");
    }

    private static String cambiarBase(String sql) {
        return sql.replace(BASE_ORIGINAL, BASE);
    }

    private static void ejecutar(Connection conexion, String sql) {
        ScriptUtils.executeSqlScript(conexion,
                new EncodedResource(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8))));
    }
}
