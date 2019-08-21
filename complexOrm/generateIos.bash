rm -rf 	"src/"{iosTest,iosMain}
cp -r "src/linuxMain" "src/iosMain"
cp -r "src/linuxTest" "src/iosTest"

sed -i.bash "s/uuid.uuid_generate/platform.darwin.uuid_generate/; s/uuid.uuid_t/platform.posix.uuid_t/" "src/iosMain/kotlin/com/github/silasgermany/complexorm/models/ComplexOrmDatabase.kt"
