#!/bin/sh

USER=`whoami`

if [ $USER != "root" ]; then
    echo nvmtrace must be run as root
    exit 0
fi;

NVMT_MTD=`mount | grep /mnt/nvmtrace | wc -l`

if [ $NVMT_MTD -eq "0" ]; then
    mount /mnt/nvmtrace
fi;

DISK_SRC=/opt/gtisc/lib/nvmtrace.img
DISK_DST=/mnt/ramfs/nvmtrace.img

if [ ! -f $DISK_DST ]; then
    cp $DISK_SRC $DISK_DST
fi;

LB_MTD=`losetup -a | grep $DISK_DST | wc -l`

if [ $LB_MTD -eq "0" ]; then
    losetup /dev/loop0 $DISK_DST
fi;

WSPACEPTH=/opt/gtisc/nvmtrace/workspaces
CLUSTER=`ls -1 $WSPACEPTH`

for NAME in $CLUSTER;
do 
    IPMI_HOST=`cat $WSPACEPTH/$NAME/cfg/ipmi`
    ipmitool -I lanplus -H $IPMI_HOST -U ADMIN \
	-f /opt/gtisc/etc/nvm.ipmi.passwd chassis power on > /dev/null
    
    if [ ! -d /mnt/disks/$NAME ]; then
	mkdir /mnt/disks/$NAME
    fi;
done;

sleep 5

NVMTRACE=/opt/gtisc/lib/java/nvmtrace.jar
CONFIG=/opt/gtisc/etc/nvmtrace.cfg
LOG=/var/log/nvmtrace.log

nohup java -Xmx1024m -jar $NVMTRACE $CONFIG > $LOG 2>&1 &
