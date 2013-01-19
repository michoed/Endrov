#Prepare the app for release. Run from ./ (the util directory)

if [ `test -e makerelease.sh` ]; then exit -1; fi

#version info
timestamp=`date +%s`
version=`cat ../endrov/ev/version.txt`

#decide on a name
name="endrov-$version.$timestamp."`git rev-parse HEAD`
echo $name

#Create a copy
cd ..
cd ..
cp -r ev $name
cd $name

#private
rm -Rf evplugin2 util2 util/myKeys   

#make sure these are up to date
make starters

#remove crap
rm -Rf CVS */CVS */*/CVS */*/*/CVS */*/*/*/CVS */*/*/*/*/CVS
rm -Rf .cvsignore */.cvsignore */*/.cvsignore */*/*/.cvsignore */*/*/*/.cvsignore
rm -Rf .project .classpath .metadata .settings
rm *.log
rm -Rf libs/unused

#add timestamp file
echo $timestamp > endrov/ev/timestamp.txt

#compress
cd ..
rm $name.zip
zip -r $name.zip $name
mkdir -p release/
mv $name.zip release/

#linecount
wc -l $name/*/*.java $name/*/*/*.java $name/*/*/*/*.java  $name/*/*/*/*.glsl
du -hc --max-depth=1 $name
echo "Num classes"
ls -1 $name/*/*.class $name/*/*/*.class $name/*/*/*/*.class | wc -l
echo "Num top-level classes"
ls -1 $name/*/*.java $name/*/*/*.java $name/*/*/*/*.java | wc -l
echo "Num images"
ls -1 $name/*/*.png $name/*/*/*.png $name/*/*/*/*.png | wc -l

echo "claimed version"
cat $name/endrov/ev/version.txt
cat $name/endrov/ev/timestamp.txt
echo ""

#delete
rm -Rf $name

cd ev
java util.debian.Main ../release/$name.zip
