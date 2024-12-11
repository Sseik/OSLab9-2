#include <iostream>
#include "functions.h"

using std::cout;
using std::endl;

int main() {
    // Testing FindInformation function
    /*string directory = "C:/Code/OS/Lab9/OSLab9-2/Server";
    DirectoryInfo di = FindInformation(directory);
    cout << di.quantity << endl;
    cout << di.size << endl;
    for (int i = di.files.size() - 1; i >= 0; i--) {
        cout << di.files[i].name << endl;
        cout << di.files[i].type << endl;
    }*/
    string directory, type;
    HANDLE hPipe = CreateNamedPipeA("\\\\.\\pipe\\mynamedpipe", PIPE_ACCESS_DUPLEX, PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT, 1, 1024 * 16, 1024 * 16, NMPWAIT_USE_DEFAULT_WAIT, NULL);
    while (true) {
        if (!ConnectNamedPipe(hPipe, NULL)) {
            CloseHandle(hPipe);
            exit(1);
        }
        GetRequest(directory, type, hPipe);
        cout << directory << endl;
        cout << type << endl;
        SendInfo(directory, type, hPipe);
        DisconnectNamedPipe(hPipe);
    }
    return 0;
}