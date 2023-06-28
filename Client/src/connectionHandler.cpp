#include "../include/connectionHandler.h"

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

connectionHandler::connectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}

connectionHandler::~connectionHandler() {
    close();
}

bool connectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool connectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool connectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool connectionHandler::getLine(std::string& line) {
    //std::cout<<"entered get line"<<std::endl;
    return getFrameAscii(line, '\n');
}

bool connectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\n');
}

bool connectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    //std::cout<<"entered getFrameAscii"<<std::endl;
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        do{
            getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool connectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    bool result=sendBytes(frame.c_str(),frame.length());
    if(!result) return false;
    return sendBytes(&delimiter,1);
}

// Close down the connection properly.
void connectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}
std::vector<string> connectionHandler::split(const string &str,char delimiter){
    std::vector<string> result;
    std::stringstream stringS(str);
    string word;
    while(getline(stringS,word,delimiter)){
        result.push_back(word);
    }
    return result;
}
void connectionHandler::shortToBytes(short num, char* bytesArr){
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

void connectionHandler::getOpCodeBytes(std::string opCodeMsg,char* bytes){
    if(opCodeMsg == "REGISTER"){
        shortToBytes(1,bytes);
    }
    else if(opCodeMsg == "LOGIN"){
        shortToBytes(2,bytes);
    }
    else if(opCodeMsg == "LOGOUT"){
        shortToBytes(3,bytes);
    }
    else if(opCodeMsg == "FOLLOW"){
        shortToBytes(4,bytes);
    }
    else if(opCodeMsg == "POST"){
        shortToBytes(5,bytes);
    }
    else if(opCodeMsg == "PM"){
        shortToBytes(6,bytes);
    }
    else if(opCodeMsg == "LOGSTAT"){
        shortToBytes(7,bytes);
    }
    else if(opCodeMsg == "STAT"){
        shortToBytes(8,bytes);
    }
    else if(opCodeMsg == "NOTIFICATION"){
        shortToBytes(9,bytes);
    }
    else if(opCodeMsg == "ACK"){
        shortToBytes(10,bytes);
    }
    else if(opCodeMsg == "ERROR"){
        shortToBytes(11,bytes);
    }
    else if(opCodeMsg == "BLOCK"){
        shortToBytes(12,bytes);
    }
}

