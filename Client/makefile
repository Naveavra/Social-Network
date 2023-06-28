CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread

all: echoClient

echoClient: bin/echoClient.o bin/protocol.o bin/connectionHandler.o
	@echo "Building target : EchoClient"
	g++ -o bin/BGSclient bin/connectionHandler.o bin/echoClient.o bin/protocol.o $(LDFLAGS)
	@echo "finished linking"
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/protocol.o: src/protocol.cpp
	g++ $(CFLAGS) -o bin/protocol.o src/protocol.cpp

.PHONY: clean
clean:
	rm -f bin/*