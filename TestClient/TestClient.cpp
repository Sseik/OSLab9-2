#include <iostream>
#include <Windows.h>
#include <fileapi.h>

using namespace std;

int main() {
    HANDLE hPipe = CreateFileA("\\\\.\\pipe\\mynamedpipe", GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
    char buffer[1000] = "D:/!P0l1T3cHn1kx/crashtest|.txt";
    WriteFile(hPipe, buffer, sizeof(buffer), NULL, NULL);
    while (ReadFile(hPipe, buffer, 1000, NULL, NULL))
        cout << buffer;
	while (1);
    CloseHandle(hPipe);
}
