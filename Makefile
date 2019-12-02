JCC = javac

JFLAGS = -d bin

all: Main.class

Main.class:
	mkdir -p bin/server_file/splits
	mkdir -p bin/client_file
	$(JCC) $(JFLAGS) bittorrent/Main.java

run:
	cd bin/
	java bittorrent/Main

clean:
	rm -rf bin/bittorrent/Main.class bin/bittorrent/client/*.class bin/bittorrent/server/*.class bin/bittorrent/util/*.class bin/client_file/* bin/server_file/splits/*
