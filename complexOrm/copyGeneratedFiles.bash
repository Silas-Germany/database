function copy {
  mkdir -p "src/$1/kotlin/com/github/silasgermany/complexorm/"
  cp "build/generated/source/kaptKotlin/releaseUnitTest/com/github/silasgermany/complexorm/"*.kt "src/$1/kotlin/com/github/silasgermany/complexorm/"
  sed -i.bak "s/ @JvmWildcard//; s/import kotlin.jvm.JvmWildcard//" "src/$1/kotlin/com/github/silasgermany/complexorm/ComplexOrmDatabaseSchema.kt"
}
copy iosTest
copy linuxTest
