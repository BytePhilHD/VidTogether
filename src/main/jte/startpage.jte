@param app.HelloPage page

<html lang="en">
<body>
<p>Hello visitor!</p>
<p>The <b>user of the day</b> is ${page.userName} </p>

<h1>Upload example</h1>
<form method="post" action="/upload-example" enctype="multipart/form-data">
    <input type="file" name="files" multiple>
    <button>Submit</button>
</form>
</body>
</html>