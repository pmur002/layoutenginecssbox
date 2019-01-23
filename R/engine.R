
## CSS standard says 1px = 1/96in !?
dpi <- 96

cssboxLayout <- function(html, width, height, fonts, device) {
    ## Work in temp directory
    wd <- file.path(tempdir(), "CSSBox")
    if (!dir.exists(wd))
        dir.create(wd)
    assetDir <- file.path(wd, "assets")
    if (!dir.exists(assetDir))
        dir.create(assetDir)    
    ## Copy font files
    file.copy(fontFiles(fonts, device), assetDir)
    ## Copy any assets
    copyAssets(html, assetDir)
    printDevs <- c("pdf", "postscript", "cairo_pdf", "cairo_ps")
    useFractionalMetrics <- device %in% printDevs
    engine <- .jnew("cssboxEngine")
    ## Add some CSS defaults so that we get a value when we query
    ## (e.g., default color)
    head <- xml_find_first(html$doc, "head")
    xml_add_child(head, .where=0,
                  "style",
                  type="text/css",
                  '
body {
  color: black;
  background-color: transparent;
  direction: ltr;
}
ul {
  list-style-type: disc;
  list-style-position: outside;
}
                   ')
    htmlfile <- tempfile(tmpdir=wd, fileext=".html")
    HTML <- as.character(html$doc)
    writeLines(HTML, htmlfile)
    layoutCSV <- .jcall(engine, "S", "layout",
                        paste0("file://", htmlfile),
                        as.integer(width*dpi), as.integer(height*dpi),
                        useFractionalMetrics)
    layoutDF <- read.csv(textConnection(layoutCSV),
                         header=FALSE, stringsAsFactors=FALSE,
                         quote="'\"")
    names(layoutDF) <- names(layoutFields)
    do.call(makeLayout, layoutDF)
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
