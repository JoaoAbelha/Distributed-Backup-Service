#!/bin/bash


function runTestApp {

	xterm -e "java bin/TestApp.class "$1" "$2" "$3" "$4"" & $SHELL &

}
function usage {

	case $1 in
		BACKUP )
			echo "Usage: <Access Point> <Protocol> <File> <Replication Degree>"
            sleep 1
			exit ;;
		RESTORE )
			echo "Usage: <Access Point> <Protocol> <File>"
            sleep 1
			exit ;;
		SPACERECLAIM )
			echo "Usage: <Access Point> <Protocol> <Number of Bytes>"
            sleep 1
			exit ;;
		STATE )
			echo "Usage: <Access Point> <Protocol>"
            sleep 1            
			exit ;;
		DELETE )
			echo "Usage: <Access Point> <Protocol> <File>"
            sleep 1
			exit ;;
		EMPTY )
			echo "Usage: <Access Point> <Protocol> [ <Number of Bytes> | <File> | <File>, <Replication Degree>]"
            sleep 1
			exit ;;
	esac

}

cd bin

if (( $# == 0 )); 
then	
 	usage EMPTY
	exit	
fi

case $2 in
	BACKUP )
		if (( $# != 4 )); then
    			usage BACKUP
		fi
		runTestApp $1 $2 $3 $4 ;;
	RESTORE )
		if (( $# != 3 )); then
    			usage RESTORE
		fi
		runTestApp $1 $2 $3 ;;
	SPACERECLAIM )
		if (( $# != 3 )); then
    			usage SPACERECLAIM
		fi
		runTestApp $1 $2 $3 ;;
	STATE )
		if (( $# != 2 )); then
    			usage STATE
		fi
		runTestApp $1 $2 ;;
	DELETE )
		if (( $# != 3 )); then
    			usage DELETE
		fi
		runTestApp $1 $2 $3 ;;
esac

cd ..

