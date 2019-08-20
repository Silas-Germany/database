rm -rf 	complexOrm{,Api}/src/{iosTest,iosMain}
cp -r complexOrm/src/linuxMain complexOrm/src/iosMain
cp -r complexOrm/src/linuxTest complexOrm/src/iosTest
cp -r complexOrmApi/src/linuxMain complexOrmApi/src/iosMain
cp -r complexOrmApi/src/linuxTest complexOrmApi/src/iosTest

sed -i.bash s/uuid.uuid_generate/platform.darwin.uuid_generate/ complexOrm/src/iosMain/kotlin/com/github/silasgermany/complexorm/models/ComplexOrmDatabase.kt
sed -i.bash s/uuid.uuid_t/platform.posix.uuid_t/ complexOrm/src/iosMain/kotlin/com/github/silasgermany/complexorm/models/ComplexOrmDatabase.kt
