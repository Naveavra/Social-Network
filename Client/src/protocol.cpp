//
// Created by Nave Avraham on 27/12/2021.
//
#include "../include/protocol.h"
#include <iostream>


#include <boost/algorithm/string.hpp>

using namespace std;

bool protocol::send(vector<string> bytes) {
    string res ="";
    for(string s: bytes){
        res+=s;
    }
    return handler.sendLine(res);
}

short bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}


vector<string> protocol::encode(string commandLine) { //TODO for some reason it shows the encode func is unreachable(colored in grey)
    //std::cout<<commandLine<<std::endl;
    vector<string> msg = handler.split(commandLine,' ');
    char const0[2];
    handler.shortToBytes(00,const0);
    char opBytes[2];
    handler.getOpCodeBytes(msg[0],opBytes);
    short opCode= bytesToShort(opBytes);
    if(opCode == 1){
        msg = registerEncode(msg,const0);
    }
    else if(opCode==2){
        msg = loginEncode(msg,const0);
    }
    else if(opCode==3){
        //TODO DONT NEED TO ENCODE LOGOUT, SHOULD BE FINE;
    }
    else if(opCode==4){
        msg = followEncode(msg,const0);
    }
    else if(opCode==5){
        msg = postEncode(msg, const0); //TODO nave
     //   msg.push_back(bytesToString(const0)); //TODO POST ENCODE
    }
    else if(opCode==6){
        msg = pmEncode(msg,const0);
    }
    else if(opCode==7){
        //TODO DONT NEED TO ENCODE LOGSTAT
    }
    else if(opCode==8){
        msg.push_back(bytesToString(const0));
    }
    msg[0] = bytesToString(opBytes);
    msg.push_back(";");
    return msg;
}

vector<string> protocol::registerEncode(vector<string> msg,char* const0){
    vector<string> res; //TODO MAYBE CREATE NEW VECTOR AND MAKE SURE TO DELETE(Same goes for all decode funcs)
   // res.push_back(msg[0]);
    for(int i = -1; i<3; i++){ // start condition i=-1 so that it adds 0 only after the second message
        if(i<=2){               // the message has 4 strings to add and 0 is added only after the first iteration
            res.push_back(msg[i+1]);
        }
        if(i>=0){
            res.push_back(bytesToString(const0));//TODO check if it adds more than 1 zero, if so theres another solution/
            //res.push_back("0") //TODO this is the another solution, make sure to change it in all relevant funcs below.
        }
    }
    return res;
}
vector<string> protocol::loginEncode(vector<string> msg,char* const0){
    vector<string> res;
    for(int i = 0; i<msg.size(); i++){
        res.push_back(msg[i]);
        //std::cout<<msg[i]<<std::endl;
        if(i==1 || i==2){
            res.push_back(bytesToString(const0));
        }
    }
    return res;
}

vector<string> protocol::postEncode(vector<string> msg,char* const0){
    vector<string> res;
    for (int i=0; i<msg.size(); i++){
        res.push_back(msg[i]);
        res.push_back(" ");

    }
    res.push_back(bytesToString(const0));
    return  res;
}

vector<string> protocol::followEncode(vector<string> msg,char* const0){
    vector<string> res;
    for(int i = 0; i<3; i++){
        res.push_back(msg[i]);
    }
    res.push_back(bytesToString(const0));
    return res;
}
vector<string> protocol::pmEncode(vector<string> msg,char* const0){
    vector<string> res;
    for(int i = 0; i<msg.size(); i++){
        res.push_back(msg[i]);
        if(i==1 || i==msg.size()-1){
            res.push_back(bytesToString(const0));
        }else if(i>1){
            res.push_back(" ");
        }
    }
    return res;
}


string protocol::bytesToString(char* arr){
    string res ="";
    for(int i = 0; i<2;i++){
        res+= arr[i];
    }
    return res;
}
void protocol::readFromSocket() {
    while (!this->terminate) {
        string answer;
        if(!handler.getLine(answer)){
            this->terminate = true;
        }
        if(!answer.empty()){
            answer = decode(answer);
            std::cout << answer << std::endl;
            if (answer == "ACK 3") {
                this->terminate = true;
                this->handler.close();
            }

        }
    }
}

void protocol::keyboard() {
    while (!this->terminate) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        //int len = line.length();
        if (!send(encode(line))) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
    }
}
protocol::protocol(connectionHandler &handler):terminate(false),handler(handler) {}

string protocol::decode(string &answer) {
    //std::cout<<"Entered decode in client"<<std::endl;
    vector<string> res = handler.split(answer,' ');
    char h[2];
    handler.getOpCodeBytes(res[0],h);
    short opCode = bytesToShort(h);
    if(opCode == 9){
        if(res[1] == "0"){
            res[1] = "PM";
        }else if(res[1] == "1"){
            res[1] = "Public";
        }
    }
    else if(opCode == 4){
        if(res[1] == "0"){
            res[1] = "FOLLOW";
        }else if(res[1] == "1"){
            res[1] = "UNFOLLOW";
        }
    }
    return answer.substr(0,answer.length()-1);
}
