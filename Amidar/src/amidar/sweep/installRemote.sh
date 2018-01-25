cd ../../../../..
tar -pczf amidar-sim2.tar.gz amidar-sim2 --exclude "amidar-sim2/axt" --exclude "amidar-sim2/.git" --exclude "amidar-sim2/.metadata" --exclude "amidar-sim2/.recommenders"
scp amidar-sim2.tar.gz $1@$2:/home/$1/amidar-sim2.tar.gz
