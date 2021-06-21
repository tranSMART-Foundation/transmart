<?php require __DIR__ . '/../../lib/php/env_helper.inc.php' ?>
driver_class=oracle.jdbc.driver.OracleDriver
url=jdbc:oracle:thin:@<?= $_ENV['ORAHOST'] ?>:<?= $_ENV['ORAPORT'] ?><?= isset($_ENV['ORASVC']) ? "/{$_ENV['ORASVC']}" : ":{$_ENV['ORASID']}", "\n" ?>
