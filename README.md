Core Play
=========

This is the Play 2 project accompanying my presentation called "Core Play". It contains all the sample code in the slides, and serves as a starting point for the 'Hands On' session.

The presentation itself can be found on http://eamelink.net/2012/06/core-play-2-presentation.html

Getting started
---------------

This is just an SBT application; you don't need Play 2. You do need SBT 0.11.2 though, or update `project/build.properties` for 0.11.3 if you have that version.

Run `sbt run`

Open a browser and add url: http://localhost:9000/websockets

In the websocket address field enter: ws://localhost:9000/websockets/cpu-info

Repeat this in other tabs, connect and disconnect and see in the logs what happens. As you might have seen I do not acquire real cpu info, it's just a counter. But the idea should be clear.


