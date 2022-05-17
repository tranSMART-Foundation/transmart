# TMINSTALL_BASE needs to be set; see InstallTransmart.sh
if [ -z "$TMINSTALL_BASE" ] ; then
	echo "TMINSTALL_BASE is not set in runRServe.sh"
	exit -1;
fi

cd $TMINSTALL_BASE/transmart-data
source vars
source /etc/profile.d/Rpath.sh
# make -C R start_Rserve &
# R CMD "Rserve" --no-save
R CMD Rserve > R/Rout.log 2>&1
