@param app.HelloPage page

<html lang="en">
<meta charset="UTF-8">
<head>
    <p> Aaa </p>
    <script>
        ws = new WebSocket("ws://" + location.hostname + ":" + location.port + "/websockets");
    </script>
</head>
<body>
<p>Hallöle</p>
<p>Hier kannst du was hochladen! </p>
<p>Aktuelle Uhrzeit: <b>${page.getTime()}</b></p>

<h1>Upload example</h1>
<form method="post" action="/upload-example" enctype="multipart/form-data">
    <input type="file" name="files" multiple>
    <button>Submit</button>
</form>
</body>
</html>




