#include <iostream>
#include "functions.h"

int main() {

    string directory, type;
    HANDLE hPipe = CreateNamedPipeA("\\\\.\\pipe\\mynamedpipe",
        PIPE_ACCESS_DUPLEX,
        PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
        1,
        1024 * 16,
        1024 * 16,
        NMPWAIT_USE_DEFAULT_WAIT, NULL);
	
	int clientId = 0;
	std::cout << "Server started" << std::endl;
	std::cout << "Waiting for clients..." << std::endl;

    while (true) {
        if (!ConnectNamedPipe(hPipe, NULL)) {
            CloseHandle(hPipe);
            exit(1);
        }
        ++clientId;
		std::cout << "Client with ID: "<< clientId << " connected" << std::endl;
		
        GetRequest(directory, type, hPipe, clientId);
        std::cout << "Requested directory: " << directory << std::endl;
        std::cout << "Requested type: " << type << std::endl;
        SendInfo(directory, type, hPipe);
        DisconnectNamedPipe(hPipe);
    }
    return 0;
}