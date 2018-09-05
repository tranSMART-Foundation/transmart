#!/bin/sh

#todo check database path. Surely ~transmart/transmart?

TRANSMART_RELEASE="release-16.2"
#TRANSMART_DATABASE="oracle"
TRANSMART_DATABASE="postgres"
TRANSMART_LOGDIR="~transmart/transmart/log"

mkdir -p $TRANSMART_LOGDIR
cd ~transmart/transmart/transmart-data

. ./vars

make $TRANSMART_DATABASE'_drop' > $TRANSMART_LOGDIR/make-$TRANSMART_DATABASE'_drop'.out 2> $TRANSMART_LOGDIR/make-$TRANSMART_DATABASE'_drop'.err
make $TRANSMART_DATABASE        > $TRANSMART_LOGDIR/make-$TRANSMART_DATABASE.out 2>  $TRANSMART_LOGDIR/make-$TRANSMART_DATABASE.err
