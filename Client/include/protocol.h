//
// Created by Nave Avraham on 27/12/2021.
//

#ifndef CLIENT_PROTOCOL_H
#define CLIENT_PROTOCOL_H
#pragma once
#include <string>
#include <vector>
#include "connectionHandler.h"

using namespace std;
class protocol {
public:
    bool terminate;
    protocol(connectionHandler &handler);
    //read from the keyboard
    void keyboard();
    //encode includes the splitting
    vector<string> encode(string commandLine);
    vector<string> registerEncode(vector<string> msg,char* const0);
    vector<string> loginEncode(vector<string> msg,char* const0);
    vector<string> followEncode(vector<string> msg,char* const0);
    vector<string> pmEncode(vector<string> msg,char* const0);
    vector<string> postEncode(vector<string> msg,char* const0);

    //send
    bool send(vector<string> bytes);
    ~protocol() = default;
    void readFromSocket();
    connectionHandler &handler;

private:
    string bytesToString(char *arr);

    string decode(string &basicString);
};





#endif //CLIENT_CLIENT_H
