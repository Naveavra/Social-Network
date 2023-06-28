//
// Created by Nave Avraham on 27/12/2021.
//
#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <thread>
#include "../include/protocol.h"


/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main(int argc, char* argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    connectionHandler connectionHandler(host, port);
    protocol p(connectionHandler);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    std::thread threadWrite(&protocol::keyboard,&p);
    std::thread threadRead(&protocol::readFromSocket,&p);
    threadRead.join();
    threadWrite.join();
    if(p.terminate){
        p.handler.close();
    }
    return 0;
}
