#!/system/bin/sh

PACKAGE="com.alivc.live.pusher.demo"

# Callgrind tool
#VGPARAMS='-v --error-limit=no --trace-children=yes --log-file=/sdcard/valgrind.log.%p --tool=callgrind --callgrind-out-file=/sdcard/callgrind.out.%p'

# Memcheck tool
VGPARAMS='-v --error-limit=no --trace-children=yes --log-file=/sdcard/valgrind.log.%p --tool=memcheck --leak-check=full --show-reachable=yes'

export TMPDIR=/data/data/$PACKAGE

exec /data/local/valdroid/bin/valgrind $VGPARAMS $* 

