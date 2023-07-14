#!/bin/bash

# edit /etc/postgresql/15/main/pg_hba.conf replace scram-sha-256 with md5
# for IPv4 connections to support including local database role/password
updatePsql=0
if (sudo grep "^host.*all.*all.*127.0.0.1/32.*scram-sha-256$" /etc/postgresql/15/main/pg_hba.conf); then
    echo "/etc/postgresql/15/main/pg_hba.conf IPv4 replace scram-ha-256 with md5"
    sudo cp /etc/postgresql/15/main/pg_hba.conf /etc/postgresql/15/main/pg_hba.conf-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sudo sed "s/^host.*all.*all.*127.0.0.1\/32.*scram-sha-256/host    all             all             127.0.0.1\/32            md5/g" "/etc/postgresql/15/main/pg_hba.conf" > "$tempFileName"
    sudo rm "/etc/postgresql/15/main/pg_hba.conf"
    sudo cp "$tempFileName" "/etc/postgresql/15/main/pg_hba.conf"
    rm $tempFileName
    updatePsql=1
fi

# Repeat (just in case) for IPv6 connections

if (sudo grep "^host.*all.*all.*::1\/128.*scram-sha-256$" /etc/postgresql/15/main/pg_hba.conf); then
    echo "/etc/postgresql/15/main/pg_hba.conf IPv6 replace scram-ha-256 with md5"
    sudo cp /etc/postgresql/15/main/pg_hba.conf /etc/postgresql/15/main/pg_hba.conf-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sudo sed "s/^host.*all.*all.*::1\/128.*scram-sha-256/host    all             all             ::1\/128                 md5/g" "/etc/postgresql/15/main/pg_hba.conf" > "$tempFileName"
    sudo rm "/etc/postgresql/15/main/pg_hba.conf"
    sudo cp "$tempFileName" "/etc/postgresql/15/main/pg_hba.conf"
    rm $tempFileName
    updatePsql=1
fi

if [ "$updatePsql" -eq 1 ]; then
    echo "restarting postgresql with updated pg_hba.conf"
    sudo systemctl restart postgresql
fi
