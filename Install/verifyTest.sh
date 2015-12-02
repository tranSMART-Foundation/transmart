if [ 0 = ( verify.sh ant ) ]; then
    echo "ant ok"
fi
if [ 0 = { verify.sh foo } ]; then
    echo "foo ok"
fi
