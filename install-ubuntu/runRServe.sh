# INSTALL_BASE needs to be set; see InstallTransmart.sh
if [ -z "$INSTALL_BASE" ] ; then
	return;
fi

cd $INSTALL_BASE/transmart-data
source vars
source /etc/profile.d/Rpath.sh
make -C R start_Rserve &