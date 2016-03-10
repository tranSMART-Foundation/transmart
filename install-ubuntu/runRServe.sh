# INSTALL_BASE needs to be set; see InstallTransmart.sh
if [ -z "$INSTALL_BASE" ] ; then
	echo "INSTALL_BASE is not set in runRServe.sh"
	exit -1;
fi

cd $INSTALL_BASE/transmart-data
source vars
source /etc/profile.d/Rpath.sh
# make -C R start_Rserve &
# R CMD "Rserve" --no-save
R CMD Rserve