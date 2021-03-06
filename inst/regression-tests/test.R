
library(layoutEngineCSSBox)
library(gyre)
library(xtable)

xtab <- print(xtable(head(mtcars[1:3])), type="html", print=FALSE)

tests <- function() {
    grid.html("<p>test</p>")
    grid.newpage()
    grid.html(xtab, 
              x=unit(1, "npc") - unit(2, "mm"),
              y=unit(1, "npc") - unit(2, "mm"),
              just=c("right", "top"))
    grid.newpage()
    grid.html('<p style="width: 100px; border-width: 1px">This paragraph should split a line</p>')
    grid.newpage()
    grid.html('<ul><li>a</li><li>simple</li><li>list</li></ul>')
    grid.newpage()
    grid.html('<ul><li>nested</li><ul><li>list</li></ul></ul>')
}

pdf("tests.pdf")
tests()
dev.off()

cairo_pdf("tests-cairo.pdf", onefile=TRUE)
tests()
dev.off()

## Check graphical output
testoutput <- function(basename) {
    PDF <- paste0(basename, ".pdf")
    savedPDF <- system.file("regression-tests", paste0(basename, ".save.pdf"),
                            package="layoutEngineCSSBox")
    system(paste0("pdfseparate ", PDF, " test-pages-%d.pdf"))
    system(paste0("pdfseparate ", savedPDF, " model-pages-%d.pdf"))
    modelFiles <- list.files(pattern="model-pages-.*[.]pdf")
    N <- length(modelFiles)
    allGood <- TRUE
    testFiles <- list.files(pattern="test-pages-.*[.]pdf")
    if (length(testFiles) != N) {
        cat(sprintf("Number of test pages (%d) and model pages (%d) differ\n",
                    length(testFiles), N))
        allGood <- FALSE
    }
    for (i in 1:N) {
        system(paste0("convert -density 96 ",
                      "model-pages-", i, ".pdf ",
                      "model-pages-", i, ".png"))
        system(paste0("convert -density 96 ",
                      "test-pages-", i, ".pdf ",
                      "test-pages-", i, ".png"))
        result <- system(paste0("compare -metric AE ",
                                "model-pages-", i, ".png ",
                                "test-pages-", i, ".png ",
                                "diff-pages-", i, ".png ",
                                "2>&1"), intern=TRUE)
        if (result != "0") {
            cat(paste0("Test and model differ (page ", i, "; ",
                       "see diff-pages-", i, ".png)\n"))
            allGood <- FALSE
        }
    }
    if (!allGood)
        stop("Regression testing detected differences")
}

testoutput("tests")
testoutput("tests-cairo")


