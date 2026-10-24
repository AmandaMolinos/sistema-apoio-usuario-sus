package br.com.apoiosus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MigratorTests {

	@Test
	void executarDuasVezesNaoReaplicaMigrations() throws Exception {
		Path pasta = Path.of("target", "testes");
		Files.createDirectories(pasta);
		var banco = new Banco("jdbc:sqlite:" + pasta.resolve("migrator-" + UUID.randomUUID() + ".db") + "?foreign_keys=on");

		banco.criarTabelas();
		banco.criarTabelas();

		try (var c = banco.conectar(); var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM schema_migracao")) {
			rs.next();
			assertEquals(3, rs.getInt(1));
		}
	}
}
