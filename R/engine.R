
## CSS standard says 1px = 1/96in !?
dpi <- 96

cssboxLayout <- function(html, width, height, fonts, device) {
    ## Work in temp directory
    wd <- file.path(tempdir(), "CSSBox")
    if (!dir.exists(wd))
        dir.create(wd)
    ## Copy font files
    file.copy(fontFiles(fonts, device), wd)
    printDevs <- c("pdf", "postscript", "cairo_pdf", "cairo_ps")
    useFractionalMetrics <- device %in% printDevs
    engine <- .jnew("cssboxEngine");
    htmlfile <- tempfile(tmpdir=wd, fileext=".html")
    writeLines(as.character(html), htmlfile)
    layoutCSV <- .jcall(engine, "S", "layout",
                        paste0("file://", htmlfile),
                        as.integer(width*dpi), as.integer(height*dpi),
                        useFractionalMetrics)
    layoutDF <- read.csv(textConnection(layoutCSV),
                         header=FALSE, stringsAsFactors=FALSE)
    do.call(makeLayout, unname(layoutDF[1:10]))
}

cssboxEngine <- makeEngine(cssboxLayout)
