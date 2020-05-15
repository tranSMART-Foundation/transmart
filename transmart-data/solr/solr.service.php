<?php
$u = $_ENV['SOLR_USER'];
$l = $_ENV['SOLR_LOG'];
$d = $_ENV['SOLR_PREFIX'];
?>
[Unit]
Description=Apache Solr server for tranSMART
Documentation=https://lucene.apache.org/solr/

[Service]
ExecStart=/usr/bin/java -Xmx1024m -DSTOP.PORT=8079 -DSTOP.KEY=stopkey -Dsolr.solr.home=solr -jar start.jar
User=<?= $u, "\n" ?>
Restart=on-failure
WorkingDirectory=<?= $d, "\n" ?>
StandardOutput=<?= $l, "\n" ?>
# test this for production:
#ReadOnlyDirectories=/
#ReadWriteDirectories=/var/cache/jobs
PrivateTmp=true

[Install]
WantedBy=multi-user.target
