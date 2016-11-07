


----------

![enter image description here](https://i97.servimg.com/u/f97/19/54/36/38/forum_12.png)

MP-Devourer
===========

**MP-Devourer** is the *Magma Playout Ingest Server* (view wikipedia pages below). Some of the ingest functions are

 - Analyze a directory and provide all video files to the playout
 - Transcode video files in directory
 - Generate Thumbnails 
 - Provide clip information (video length, framerate, etc)


----------

Getting Started
-------

Get the code and compile the Netbeans Java project yourself

    git clone https://github.com/cyberpunx/mp-devourer.git
**-or-**

Simply use the jar provided in **/dist** folder or download it from the *releases* section


----------


Running
-------
Execute in therminal

    java -jar ingestserver.jar

alternatively you can do

	java -jar ingestserver.jar > output.txt

in order to generate a text file with detailed output.

----------

Usage
-----
![enter image description here](http://i.imgur.com/fU6SfVS.png)

**Media Directory**: Directory where you keep your video files.
**Run and Die**: Analyzes the directory folder and close the program.
**Start Service**: Directory polling. Not implemented yet.



 [Wikipedia: Video Server](https://en.wikipedia.org/wiki/Video_server).

 [Wikipedia: Playout](https://en.wikipedia.org/wiki/Playout).
