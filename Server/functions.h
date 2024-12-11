#pragma once
#include <string>
#include <map>
#include <vector>
#include <Windows.h>
#include <time.h>
#include <ctime>

using std::string;
using std::map;
using std::vector;

typedef struct SFileInfo {
	string name;
	string type;
	FILETIME creationTime;
} FileInfo;

typedef struct SDirectoryInfo {
	int quantity;
	time_t searchTime;
	long long size; //sum of sizes of files in the folder, internal folders not included
	vector<FileInfo> files;

} DirectoryInfo;

DirectoryInfo FindInformation(const std::string& directory);
void GetRequest(string& directory, string& type, HANDLE hPipe,int clientId);
void SendInfo(string& directory, string& type, HANDLE hPipe);
