Multiplayer-Paint
=================

Using sockets and a simple MVC paradigm, runs a server which hosts canvases and syncs them across all users working on each for a smooth, lag-free drawing experience.

The server is capable of running several canvases which are stored in the Model. Modifications by the user Controllers are stored in a blocking queue on each of the master canvases in the Model so as to eliminate concurrency issues. Views are only ever seeing the broadcasted list of moves given by the Model (if conection to the host is lost, no drawing is available).

How to use
==========

Run WhiteboardServer in eclipse, then run any number of WhiteboardClient clients and connect them to localhost (or whatever ip your server is running on). As long as the server is running, no whiteboard will be lost or removed.
