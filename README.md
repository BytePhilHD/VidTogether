<h2>Watch your own Videos together </h2>

 **Host your own Site for watching mp4 Videos with others!**
 
 The main Page for testing is [s2.bytephil.de](https://s2.bytephil.de)
 
<h3>Features</h3>

- [x] Config
- [x] Load Videos from Files Folder and play them to all Clients
- [ ] Password protected Rooms
- [ ] Video Pause Function
- [ ] Room Chat

Newest Release:
[Releases](https://github.com/BytePhilHD/UploadServer/releases)

<h1>How to install</h1>
Download the newest Version from the Releases Page.

**Install Java 11, if you havent already**

**Windows:** Create a batch file (e.g. "start.bat") in that you need the Line 
`java -Xmx1G -jar WebTest.jar`. If you rename your .jar file, type your new Name
instead of "WebTest". 

**Linux:** Create a sh file (e.g. "start.sh") in that you need the Line
`screen -AmdS WebTest java -Xmx1G -jar WebTest.jar`. If you havent already we recommend
installing screen (apt install screen), if you dont want to use it, just write the same as on Windows.

No start the file (Windows doubleclick, Linux ./start.sh). The Programm should start up.
If you're running the Programm at home, you may need to release your Port (standard 80) in your
Routers setting.

Now you can access your Site via localhost (if you changed your Port localhost:YOURPORT) or
use the IP Address instead of localhost


