#include <iostream>
#include <Windows.h>
#include <fileapi.h>

using namespace std;

int main() {
    char buffer[1000];
    while (true) {
        cout << "Request or 'exit': ";
        cin >> buffer;
        if (!strcmp("exit", buffer))
            exit(0);
        HANDLE hPipe = CreateFileA("\\\\.\\pipe\\mynamedpipe", GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
        WriteFile(hPipe, buffer, sizeof(buffer), NULL, NULL);
        while (ReadFile(hPipe, buffer, 1000, NULL, NULL))
            cout << buffer;
        CloseHandle(hPipe);
    }
}
