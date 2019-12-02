JCC = javac

JFLAGS = -d bin

default: Main.class

Main.class:
    $(JCC) $(JFLAGS) bittorrent/Main.java

clean:
    rm  bin/bittorrent/Main.class bin/bittorrent/client/*.class bin/bittorrent/server/*.class bin/bittorrent/util/*.class bin/client_file/* bin/server_file/splits/*