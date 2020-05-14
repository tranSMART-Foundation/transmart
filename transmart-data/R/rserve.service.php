<?php
$u = $_ENV['RSERVE_USER'];
$r = "$_ENV[R_PREFIX]/bin/R";
$c = $_ENV['RSERVE_CONF'];
$l = $_ENV['RSERVE_LOG'];
?>
[Unit]
Description=Rserve (TCP/IP server for running R expressions)
Documentation=http://rforge.net/Rserve/

[Service]
ExecStart=<?= $r ?> CMD Rserve --quiet --vanilla --RS-conf <?= $c, "\n" ?>
User=<?= $u, "\n" ?>
TimeoutSec=15s
Restart=always
Nice=19
StandardOutput=<?= $l, "\n" ?>
# test this for production:
#ReadOnlyDirectories=/
#ReadWriteDirectories=/var/cache/jobs
#PrivateTmp=true

[Install]
WantedBy=multi-user.target
