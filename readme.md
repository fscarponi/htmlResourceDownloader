<h2>HtmlResource downloader</h2>

HtmlResource downloader is used for efficiently download massive amount of resources (files) from websites

<h2>warning:</h2>
<h3> App strategy is very aggressive, he recursively go deep into web structure, be sure to set parameter with cognition</h3>
<h5>
Al files are downloaded by a different coroutines whit the same http ktor client, it will cause a bandwidth saturation
</h5>
Usage:<br>
<ul>
<li>Clone Repo -> Distributable</li>
Actually you can build a distributable though gradle task!
<br> Application needs in the same dir a parameters.json file with parameters for start the task
<br> see DataStructure.kt/Parameters for json structure
<br><br>
<li>Clone Repo -> Run From IDE</li>
You can set parameters in main, and if there is not a parameters.json in the mail folder, it will be created!<br>
Remember to delete parameter.json if you want to reset them

</ul>
Output:<br>
<ul>
<li>all files will be downloaded in "outputFolder", without lose path structure 
</li>
<li>all skip preference (see parameteres) will be attended</li>
<li>if some file throw some kind of error will be skipped and signed in skippedFile.csv, with relative error description</li>
<li>detailed and annoying infos are printed on console </li>

</ul>


