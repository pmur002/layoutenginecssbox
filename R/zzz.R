
.onLoad <- function(libname, pkgname) {
    .jpackage(pkgname, lib.loc=libname)

    options(layoutEngine.backend=cssboxEngine)
}
