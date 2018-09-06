# transmart-ICE
ICE tool for transmart ETL (formerly FCL4tranSMART)

By default, FCL4tranSMART is built for linux, macOSX and win32 on x86_64,
with the Windows build in lower case

To build using Maven:

cd Postgres/code

edit pom.xml to make sure the platform you will be using is uncommented (e.g. linux gtk x86_64)

mvn package

To run, copy the fcl4transmart directory for your platform
(preferences are saved in the directory and can be wiped by a rebuild)
then launch FCL4tranSMART for your platform by launching
fcl4transmart/FCL4tranSMART (or fcl4transmart/fcl4transmart on
Windows).

Postgres/code/fr.sanofi.fcl4transmart.product/target/products/
	fr.sanofi.fcl4transmart.product/linux/gtk/x86_64/fcl4transmart/FCL4tranSMART

Postgres/code/fr.sanofi.fcl4transmart.product/target/products/
	fr.sanofi.fcl4transmart.product/macosx/cocoa/x86_64/fcl4transmart/FCL4tranSMART

Postgres/code/fr.sanofi.fcl4transmart.product/target/products/
	fr.sanofi.fcl4transmart.product/win32/win32/x86_64/fcl4transmart/fcl4transmart

and the same paths under Oracle/code
