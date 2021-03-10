fun String.prependIfMissing(s: String) =
    if (startsWith(s)) this else s + this
