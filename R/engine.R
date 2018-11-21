
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
    HTML <- as.character(html)
    writeLines(HTML, htmlfile)
    layoutCSV <- .jcall(engine, "S", "layout",
                        paste0("file://", htmlfile),
                        as.integer(width*dpi), as.integer(height*dpi),
                        useFractionalMetrics)
    layoutDF <- read.csv(textConnection(layoutCSV),
                         header=FALSE, stringsAsFactors=FALSE)
    do.call(makeLayout, unname(layoutDF[1:10]))
}

## CSSBox does not handle numeric font-weight values
## so transform for string values
## Set a normal/bold cutoff at 500
## https://www.w3.org/TR/css-fonts-3/#font-matching-algorithm
cssboxFontWeight <- function(weight) {
    if (is.character(weight)) {
        if (all(weight %in% c("normal", "bold")))
            return(weight)
        else
            stop("Invalid font-weight value")
    } else {
        ifelse(as.numeric(weight) > 500, "bold", "normal")
    }
}

cssboxEngine <- makeEngine(cssboxLayout,
                           cssTransform=list(fontWeight=cssboxFontWeight))
