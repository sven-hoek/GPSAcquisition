mkdir amidar-sim2Remote
tar -pxzf amidar-sim2.tar.gz -C amidar-sim2Remote
cd amidar-sim2Remote/amidar-sim2/Amidar
java -cp ../Amidar/bin:../AmidarTools/bin:../AmidarTools/lib/axtConverter.jar:../AmidarTools/lib/bcel-5.2.jar:../AmidarTools/lib/commons-lang-2.6.jar:../AmidarTools/lib/j-text-utils-0.3.3.jar:../AmidarTools/lib/json-simple-1.1.1.jar:../AmidarTools/lib/lombok.jar:../AXTLoader/bin:../Synthesis/bin:../cgra/CGRA/bin amidar.sweep.AmidarRemoteManager createRegistry
