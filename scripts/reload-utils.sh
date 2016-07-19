echo $1;
echo $2;
case $2 in
    --restart-jicofo)
        CMD='sudo /etc/init.d/jicofo restart';;
    *)
        CMD='sudo /etc/init.d/prosody restart';;
esac
echo "ssh-ing to remote machine with timeout 10."
ssh -o ConnectTimeout=10 -4 -T $1 << EOF
    $CMD
EOF
